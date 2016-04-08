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
        Map<Long, GroupVirtualServer> paths = new HashMap<>();

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
            if (paths.containsKey(vs.getId())) {
                throw new ValidationException("Group and virtual server is an unique combination.");
            }

            if (escapePathValidation) continue;

            String path = gvs.getPath();
            if (path == null || path.isEmpty()) {
                throw new ValidationException("Path cannot be empty.");
            }
            int idx = path.indexOf('/');
            path = idx >= 0 ? path.substring(idx + 1) : path;
            if (path.isEmpty()) {
                paths.put(vs.getId(), new GroupVirtualServer().setPath(gvs.getPath()).setPriority(gvs.getPriority() == null ? -1000 : gvs.getPriority()));
                continue;
            }
            paths.put(vs.getId(), new GroupVirtualServer().setPath(path).setPriority(gvs.getPriority() == null ? 1000 : gvs.getPriority()));
        }

        if (paths.size() == 0) return;

        List<RelGroupVsDo> retained = new ArrayList<>();
        for (RelGroupVsDo d : rGroupVsDao.findAllByVses(paths.keySet().toArray(new Long[paths.size()]), RGroupVsEntity.READSET_FULL)) {
            if (groupId.equals(d.getGroupId()))
                continue;
            if (d.getPriority() == 0) d.setPriority(1000);

            int i = 0;
            String value = d.getPath();
            while (i < value.length()) {
                char next = value.charAt(i);
                if (next == '/') {
                    value = value.substring(i + 1);
                    break;
                }
                if (Character.isAlphabetic(next)) {
                    break;
                }
                i++;
            }

            // check if root path is completely equivalent, otherwise escape comparing with root path
            GroupVirtualServer entry = paths.get(d.getVsId());
            if (value.isEmpty()) {
                if (d.getPath().equals(entry.getPath())) {
                    retained.add(d);
                }
                continue;
            }

            int ol = StringUtils.prefixOverlapped(entry.getPath(), value, '(');
            switch (ol) {
                case -1:
                    break;
                case 0:
                    if (entry.getPriority() == d.getPriority()) {
                        retained.add(d);
                    }
                    break;
                case 1:
                    if (entry.getPriority() <= d.getPriority()) {
                        retained.add(d);
                    }
                    break;
                case 2:
                    if (entry.getPriority() >= d.getPriority()) {
                        retained.add(d);
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

    @Override
    public void validateGroupServers(List<GroupServer> groupServers) throws Exception {
        groupServerModelValidator.validateGroupServers(groupServers);
    }
}
