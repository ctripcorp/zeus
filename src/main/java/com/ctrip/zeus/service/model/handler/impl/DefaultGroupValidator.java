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
import com.ctrip.zeus.util.PathUtils;
import com.google.common.collect.Sets;
import org.springframework.stereotype.Component;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.annotation.Resource;
import java.util.*;
import java.util.regex.Pattern;

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
    private final String standardSuffix = "($|/|\\?)";
    private final String[] standardSuffixIdentifier = new String[]{"$", "/", "\\?"};
    private final Pattern basicPathPath = Pattern.compile("^((\\w|-)+/?)(\\$|\\\\\\?)?");

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

            if (!escapePathValidation) {
                doPathValidationAndMapping(addingGvs, gvs);
            }
        }

        if (escapePathValidation || addingGvs.size() == 0) return;

        Map<Long, List<RelGroupVsDo>> retainedGvs = new HashMap<>();
        for (RelGroupVsDo e : rGroupVsDao.findAllByVses(addingGvs.keySet().toArray(new Long[addingGvs.size()]), RGroupVsEntity.READSET_FULL)) {
            List<RelGroupVsDo> relsOfVs = retainedGvs.get(e.getVsId());
            if (relsOfVs == null) {
                relsOfVs = new ArrayList<>();
                retainedGvs.put(e.getVsId(), relsOfVs);
            }
            relsOfVs.add(e);
        }
        checkPathOverlappingAcrossVs(groupId, addingGvs, retainedGvs);

        // reset priority after auto reorder enabled(priority is originally null)
        for (GroupVirtualServer e : groupVirtualServers) {
            Integer ref = addingGvs.get(e.getVirtualServer().getId()).getPriority();
            if (e.getPriority() == null) {
                e.setPriority(ref);
            } else if (e.getPriority().intValue() != ref.intValue()) {
                throw new ValidationException("Potential path overlapping problem exists at vs-" + e.getVirtualServer().getId() + ". Recommend priority is " + ref + ".");
            }
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
        for (String pv : gvs.getPath().split(" ", 0)) {
            if (pv.isEmpty()) continue;
            if (pathValues.size() == 2)
                throw new ValidationException("Invalid path, too many whitespace modifiers is found.");

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
        if ("/".equals(path)) {
            if ("/".equals(gvs.getPath())) {
                mappingResult.put(gvs.getVirtualServer().getId(), new GroupVirtualServer().setPath(gvs.getPath()).setPriority(gvs.getPriority() == null ? -2000 : gvs.getPriority()));
            } else {
                mappingResult.put(gvs.getVirtualServer().getId(), new GroupVirtualServer().setPath(gvs.getPath()).setPriority(gvs.getPriority() == null ? -1000 : gvs.getPriority()));
            }
        } else {
            mappingResult.put(gvs.getVirtualServer().getId(), new GroupVirtualServer().setPath(path).setPriority(gvs.getPriority() == null ? 1000 : gvs.getPriority()));
        }
    }

    private void checkPathOverlappingAcrossVs(Long groupId, Map<Long, GroupVirtualServer> addingGvs, Map<Long, List<RelGroupVsDo>> retainedGvs) throws ValidationException {
        Set<RelGroupVsDo> overlappedEntries = new HashSet<>();
        for (Map.Entry<Long, GroupVirtualServer> e : addingGvs.entrySet()) {
            GroupVirtualServer addingEntry = e.getValue();
            if (addingEntry == null) {
                throw new ValidationException("Unexpected path validation is reached. Related group and vs: " + groupId + ", " + e.getKey());
            }

            String addingPath = addingEntry.getPath();
            try {
                addingPath = extractValue(addingPath);
            } catch (ValidationException ex) {
            }

            boolean addingPathIsRoot = "/".equals(addingPath);
            List<String> addingPathMembers = new ArrayList<>();
            if (!addingPathIsRoot) {
                addingPathMembers = regexLevelSplit(addingPath, 1);
                if (addingPathMembers.size() == 0) addingPathMembers.add(addingPath);
//                for (String pathMember : addingPathMembers) {
//                    if (!basicPathPath.matcher(pathMember).matches()) {
//                        throw new ValidationException("Invalid characters are found in sub path " + pathMember + ".");
//                    }
//                }
            } else {
                addingPathMembers.add(addingPath);
            }

            List<RelGroupVsDo> relsOfVs = retainedGvs.get(e.getKey());
            if (relsOfVs == null) continue;

            for (RelGroupVsDo retainedEntry : relsOfVs) {
                if (groupId.equals(retainedEntry.getGroupId())) continue;
                if (retainedEntry.getPriority() == 0) retainedEntry.setPriority(1000);

                String retainedPath = retainedEntry.getPath();
                try {
                    retainedPath = extractValue(retainedEntry.getPath());
                } catch (ValidationException ex) {
                }

                boolean retainedPathIsRoot = "/".equals(retainedPath);
                List<String> retainedPathMembers = new ArrayList<>();
                if (!retainedPathIsRoot) {
                    retainedPathMembers = regexLevelSplit(retainedPath, 1);
                    if (retainedPathMembers.size() == 0) retainedPathMembers.add(retainedPath);
                } else {
                    retainedPathMembers.add(retainedPath);
                }

                // check if root path is completely equivalent, otherwise escape comparing with root path
                if (addingPathIsRoot && retainedPathIsRoot) {
                    if (retainedEntry.getPath().equals(addingEntry.getPath())) {
                        overlappedEntries.add(retainedEntry);
                    }
                    continue;
                }

                for (String ap : addingPathMembers) {
                    for (String rp : retainedPathMembers) {
                        if (addingPathIsRoot && addingEntry.getPriority() >= retainedEntry.getPriority()) {
                            addingEntry.setPriority(retainedEntry.getPriority() - 100);
                            continue;
                        }
                        if (retainedPathIsRoot && addingEntry.getPriority() <= retainedEntry.getPriority()) {
                            addingEntry.setPriority(retainedEntry.getPriority() + 100);
                            continue;
                        }

                        int ol = PathUtils.prefixOverlapped(ap, rp, standardSuffix);
                        switch (ol) {
                            case -1:
                                break;
                            case 0:
                                overlappedEntries.add(retainedEntry);
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
                }
            }
        }

        if (overlappedEntries.size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (RelGroupVsDo d : overlappedEntries) {
                sb.append(d.getVsId() + "(" + d.getPath() + ")");
            }
            throw new ValidationException("Path is prefix-overlapped across virtual server " + sb.toString() + ".");
        }
    }

    // expose api for testing
    public static String extractValue(String path) throws ValidationException {
        int idxPrefix = 0;
        int idxModifier = 0;
        boolean quote = false;

        char[] pathArray = path.toCharArray();
        for (char c : pathArray) {
            if (c == '"') {
                quote = true;
                idxPrefix++;
            } else if (c == ' ') {
                idxPrefix++;
                idxModifier = idxPrefix;
            } else if (c == '^' || c == '~' || c == '=' || c == '*') {
                idxPrefix++;
            } else if (c == '/') {
                idxPrefix++;
                if (!quote && idxPrefix < pathArray.length && pathArray[idxPrefix] == '"') {
                    quote = true;
                    idxPrefix++;
                }
                break;
            } else {
                break;
            }
        }

        if (quote && !path.endsWith("\"")) {
            throw new ValidationException("Path should end up with quote if regex quotation is used. Path=" + path + ".");
        }
        int idxSuffix = quote ? path.length() - 1 : path.length();
        if (idxPrefix == idxSuffix) {
            if (path.charAt(idxSuffix - 1) == '/') {
                return "/";
            } else {
                throw new ValidationException("Path could not be validated. Path=" + path + ".");
            }
        }
        idxPrefix = idxPrefix < idxSuffix ?
                (idxModifier > idxPrefix ? idxModifier : idxPrefix) : idxModifier;
        return path.substring(idxPrefix, idxSuffix);
    }

    private List<String> restrictAndDecorate(String path, boolean appendSuffix) throws ValidationException {
        if (path == null || path.isEmpty()) throw new ValidationException("Get empty path when trying to decorate.");
        List<String> subPaths = new ArrayList<>();
        StringBuilder pb = new StringBuilder();
        char[] pp = path.toCharArray();
        int startIdx, endIdx;
        startIdx = 0;
        endIdx = pp.length - 1;
        if (pp[startIdx] == '(' && pp[endIdx] == ')') {
            startIdx++;
            endIdx--;
        }

        for (int i = startIdx; i <= endIdx; i++) {
            switch (pp[i]) {
                case '|':
                    if (appendSuffix) {
                        String p = pb.toString();
                        pb.setLength(0);
                        for (String s : standardSuffixIdentifier) {
                            subPaths.add(p + s);
                        }
                    }
                    break;
                default:
                    pb.append(pp[i]);
                    break;
            }
        }

        if (pb.length() > 0) {
            String p = pb.toString();
            pb.setLength(0);
            if (appendSuffix) {
                for (String s : standardSuffixIdentifier) {
                    subPaths.add(p + s);
                }
            } else {
                subPaths.add(p);
            }
        }

        if (subPaths.size() == 0) {
            for (String s : standardSuffixIdentifier) {
                subPaths.add(path + s);
            }
        }

        return subPaths;
    }

    public List<String> regexLevelSplit(String path, int depth) throws ValidationException {
        List<String> pathMembers = new ArrayList<>();
        if (depth > 1) {
            throw new ValidationException("Function regexLevelSplit only support first level split.");
        }
        int fromIdx, idxSuffix;
        fromIdx = idxSuffix = 0;
        while ((idxSuffix = path.indexOf(standardSuffix, fromIdx)) != -1) {
            if (fromIdx > 0) {
                if (path.charAt(fromIdx) == '|') {
                    fromIdx++;
                    pathMembers.addAll(restrictAndDecorate(path.substring(fromIdx, idxSuffix), true));
                } else {
                    String prev = pathMembers.get(pathMembers.size() - 1);

                    List<String> subPaths = restrictAndDecorate(prev + path.substring(fromIdx, idxSuffix + 8), true);
                    pathMembers.set(pathMembers.size() - 1, subPaths.get(0));
                    for (int i = 1; i < subPaths.size(); i++) {
                        pathMembers.add(pathMembers.get(i));
                    }
                }
            } else {
                pathMembers.addAll(restrictAndDecorate(path.substring(0, idxSuffix), true));
            }
            fromIdx = idxSuffix + 8;
        }

        if (pathMembers.size() == 0) {
            pathMembers.addAll(restrictAndDecorate(path, false));
        }

        return pathMembers;
    }
}