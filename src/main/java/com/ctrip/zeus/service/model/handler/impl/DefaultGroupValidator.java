package com.ctrip.zeus.service.model.handler.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.GroupServer;
import com.ctrip.zeus.model.entity.GroupVirtualServer;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.service.model.PathRewriteParser;
import com.ctrip.zeus.service.model.handler.GroupServerValidator;
import com.ctrip.zeus.service.model.handler.GroupValidator;
import com.ctrip.zeus.service.model.handler.VirtualServerValidator;
import com.ctrip.zeus.util.StringUtils;
import com.google.common.collect.Sets;
import org.springframework.stereotype.Component;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.annotation.Resource;
import java.util.*;

/**
 * Created by zhoumy on 2015/6/29.
 */
@Component("groupModelValidator")
public class DefaultGroupValidator implements GroupValidator {
    @Resource
    private VirtualServerValidator virtualServerModelValidator;
    @Resource
    private GroupServerValidator groupServerModelValidator;
    @Resource
    private RGroupVsDao rGroupVsDao;
    @Resource
    private RGroupVgDao rGroupVgDao;
    @Resource
    private RGroupStatusDao rGroupStatusDao;
    @Resource
    private GroupDao groupDao;

    private final Set<String> pathPrefixModifier = Sets.newHashSet("=", "~", "~*", "^~");
    private static final String standardSuffix = "($|/|\\?)";

    @Override
    public boolean exists(Long targetId) throws Exception {
        return groupDao.findById(targetId, GroupEntity.READSET_FULL) != null
                && rGroupVgDao.findByGroup(targetId, RGroupVgEntity.READSET_FULL) == null;
    }

    @Override
    public void validate(Group target) throws Exception {
        validate(target, false);
    }

    @Override
    public void checkVersion(Group target) throws Exception {
        GroupDo check = groupDao.findById(target.getId(), GroupEntity.READSET_FULL);
        if (check == null)
            throw new ValidationException("Group with id " + target.getId() + " does not exists.");
        if (!target.getVersion().equals(check.getVersion()))
            throw new ValidationException("Newer Group version is detected.");
    }

    @Override
    public void removable(Long targetId) throws Exception {
        RelGroupStatusDo check = rGroupStatusDao.findByGroup(targetId, RGroupStatusEntity.READSET_FULL);
        if (check.getOnlineVersion() != 0) {
            throw new ValidationException("Group must be deactivated before deletion.");
        }
    }

    @Override
    public void validate(Group target, boolean escapePathValidation) throws Exception {
        if (target.getName() == null || target.getName().isEmpty()
                || target.getAppId() == null || target.getAppId().isEmpty()) {
            throw new ValidationException("Group name and app id are required.");
        }
        if (target.getHealthCheck() != null) {
            if (target.getHealthCheck().getUri() == null || target.getHealthCheck().getUri().isEmpty())
                throw new ValidationException("Health check path cannot be empty.");
        }
        validateGroupVirtualServers(target.getId(), target.getGroupVirtualServers(), escapePathValidation);
        validateGroupServers(target.getGroupServers());
    }

    @Override
    public void validateGroupVirtualServers(Long groupId, List<GroupVirtualServer> groupVirtualServers, boolean escapePathValidation) throws Exception {
        if (groupVirtualServers == null || groupVirtualServers.size() == 0)
            throw new ValidationException("No virtual server is found bound to this group.");
        if (groupId == null)
            groupId = 0L;
        GroupVirtualServer dummy = new GroupVirtualServer();
        Map<Long, GroupVirtualServer> addingGvs = new HashMap<>();

        for (GroupVirtualServer gvs : groupVirtualServers) {
            if (gvs.getRewrite() != null && !gvs.getRewrite().isEmpty()) {
                if (!PathRewriteParser.validate(gvs.getRewrite())) {
                    throw new ValidationException("Invalid rewrite value.");
                }
            }

            VirtualServer vs = gvs.getVirtualServer();
            if (!virtualServerModelValidator.exists(vs.getId())) {
                throw new ValidationException("Virtual server with id " + vs.getId() + " does not exist.");
            }
            if (addingGvs.containsKey(vs.getId())) {
                throw new ValidationException("Group and virtual server is an unique combination.");
            } else {
                addingGvs.put(vs.getId(), dummy);
            }

            if (escapePathValidation) {
                continue;
            } else {
                doPathValidationAndMapping(addingGvs, gvs);
            }
        }

        if (escapePathValidation || addingGvs.size() == 0) return;
        List<RelGroupVsDo> retainedGvs = rGroupVsDao.findAllByVses(addingGvs.keySet().toArray(new Long[addingGvs.size()]), RGroupVsEntity.READSET_FULL);
        checkPathOverlappingAcrossVs(groupId, addingGvs, retainedGvs);

        // reset priority after auto reorder
        for (GroupVirtualServer e : groupVirtualServers) {
            e.setPriority(addingGvs.get(e.getVirtualServer().getId()).getPriority());
        }
    }

    @Override
    public void validateGroupServers(List<GroupServer> groupServers) throws Exception {
        groupServerModelValidator.validateGroupServers(groupServers);
    }

    private void doPathValidationAndMapping(Map<Long, GroupVirtualServer> mappingResult, GroupVirtualServer gvs) throws ValidationException {
        if (gvs.getPath() == null || gvs.getPath().isEmpty()) {
            throw new ValidationException("Path cannot be empty.");
        }
        List<String> pathValues = new ArrayList<>(2);
        for (String pv :  gvs.getPath().split(" ", 0)) {
            if (pv.isEmpty()) continue;
            if (pathValues.size() == 2) throw new ValidationException("Invalid path, too many whitespace modifiers is found.");

            pathValues.add(pv);
        }
        if (pathValues.size() == 2) {
            if (!pathPrefixModifier.contains(pathValues.get(0))) {
                throw new ValidationException("Invalid path, invalid prefix modifier is found.");
            }
            // format path value
            gvs.setPath(pathValues.get(0) + " " + pathValues.get(1));
        }

        String path = extractValue(gvs.getPath());
        if (path.isEmpty()) {
            mappingResult.put(gvs.getVirtualServer().getId(), new GroupVirtualServer().setPath(gvs.getPath()).setPriority(gvs.getPriority() == null ? -1000 : gvs.getPriority()));
        } else {
            mappingResult.put(gvs.getVirtualServer().getId(), new GroupVirtualServer().setPath(path).setPriority(gvs.getPriority() == null ? 1000 : gvs.getPriority()));
        }
    }

    private void checkPathOverlappingAcrossVs(Long groupId, Map<Long, GroupVirtualServer> addingGvs, List<RelGroupVsDo> retainedGvs) throws ValidationException {
        List<RelGroupVsDo> retained = new ArrayList<>();
        for (RelGroupVsDo retainedEntry : retainedGvs) {
            if (groupId.equals(retainedEntry.getGroupId()))
                continue;
            if (retainedEntry.getPriority() == 0) retainedEntry.setPriority(1000);

            String retainedPath = retainedEntry.getPath();
            try {
                retainedPath = extractValue(retainedEntry.getPath());
            } catch (ValidationException ex) {
            }

            GroupVirtualServer addingEntry = addingGvs.get(retainedEntry.getVsId());
            if (addingEntry == null) {
                throw new ValidationException("Unexpected path validation is reached. Related group and vs: " + groupId + ", " + retainedEntry.getVsId());
            }

            String addingPath = addingEntry.getPath();
            try {
                addingPath = extractValue(addingPath);
            } catch (ValidationException ex) {
            }

            // check if root path is completely equivalent, otherwise escape comparing with root path
            if (retainedPath.isEmpty() || addingPath.isEmpty()) {
                if (retainedEntry.getPath().equals(addingEntry.getPath())) {
                    retained.add(retainedEntry);
                }
                continue;
            }

            int ol = StringUtils.prefixOverlapped(addingPath, retainedPath);
            switch (ol) {
                case -1:
                    break;
                case 0:
                    retained.add(retainedEntry);
                    break;
                case 1:
                    if (addingEntry.getPriority() == null || addingEntry.getPriority() <= retainedEntry.getPriority()) {
                        addingEntry.setPriority(retainedEntry.getPriority() + 100);
                    }
                    break;
                case 2:
                    if (addingEntry.getPriority() == null || addingEntry.getPriority() >= retainedEntry.getPriority()) {
                        addingEntry.setPriority(retainedEntry.getPriority() - 100);
                    }
                    break;
                default:
                    throw new NotImplementedException();
            }
        }

        if (retained.size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (RelGroupVsDo d : retained) {
                sb.append(d.getVsId() + "(" + d.getPath() + ")");
            }
            throw new ValidationException("Path is prefix-overlapped across virtual server " + sb.toString() + ".");
        }
    }

    // expose api for testing
    public static String extractValue(String path) throws ValidationException {
        int prefixIdx = -1;
        boolean checkQuote = false;

        char[] pathArray = path.toCharArray();
        for (char c : pathArray) {
            if (Character.isAlphabetic(c)) break;
            if (c == '/') {
                prefixIdx++;
                if (prefixIdx + 1 < pathArray.length && pathArray[prefixIdx + 1] == '"') {
                    checkQuote = true;
                    prefixIdx++;
                }
                break;
            }
            if (c == '"') checkQuote = true;
            prefixIdx++;
        }
        if (checkQuote && !path.endsWith("\"")) {
            throw new ValidationException("Path should end up with quote if regex quotation is used.");
        }
        int suffixIdx = path.indexOf(standardSuffix);
        suffixIdx = suffixIdx >= 0 ? suffixIdx : path.length();
        prefixIdx = prefixIdx < suffixIdx && prefixIdx >= 0 ? prefixIdx + 1 : 0;
        path = path.substring(prefixIdx, suffixIdx);
        return path;
    }
}