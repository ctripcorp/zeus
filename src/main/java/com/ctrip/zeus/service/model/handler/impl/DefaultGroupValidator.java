package com.ctrip.zeus.service.model.handler.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.GroupServer;
import com.ctrip.zeus.model.entity.GroupVirtualServer;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.service.activate.ActiveConfService;
import com.ctrip.zeus.service.model.PathRewriteParser;
import com.ctrip.zeus.service.model.handler.GroupServerValidator;
import com.ctrip.zeus.service.model.handler.GroupValidator;
import com.ctrip.zeus.service.model.handler.VirtualServerValidator;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by zhoumy on 2015/6/29.
 */
@Component("groupModelValidator")
public class DefaultGroupValidator implements GroupValidator {
    @Resource
    private ActiveConfService activeConfService;
    @Resource
    private VirtualServerValidator virtualServerModelValidator;
    @Resource
    private GroupServerValidator groupServerModelValidator;
    @Resource
    private RGroupVsDao rGroupVsDao;
    @Resource
    private GroupDao groupDao;

    @Override
    public boolean exists(Long targetId) throws Exception {
        return groupDao.findById(targetId, GroupEntity.READSET_FULL) != null;
    }

    @Override
    public void validate(Group target) throws Exception {
        if (target.getName() == null || target.getName().isEmpty()
                || target.getAppId() == null || target.getAppId().isEmpty()) {
            throw new ValidationException("Group with null value cannot be persisted.");
        }
        if (target.getHealthCheck() != null) {
            if (target.getHealthCheck().getUri() == null || target.getHealthCheck().getUri().isEmpty())
                throw new ValidationException("Health check path cannot be empty.");
        }
        validateGroupVirtualServers(target.getId(), target.getGroupVirtualServers());
        validateGroupServers(target.getGroupServers());
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
        List<String> l = activeConfService.getConfGroupActiveContentByGroupIds(new Long[]{targetId});
        if (l.size() > 0)
            throw new ValidationException("Group must be deactivated before deletion.");
    }

    @Override
    public void validateGroupVirtualServers(Long groupId, List<GroupVirtualServer> groupVirtualServers) throws Exception {
        if (groupVirtualServers == null || groupVirtualServers.size() == 0)
            throw new ValidationException("No virtual server is found bound to this group.");
        if (groupId == null)
            groupId = 0L;
        Set<Long> virtualServerIds = new HashSet<>();
        Set<String> groupPaths = new HashSet<>();
        for (GroupVirtualServer groupVirtualServer : groupVirtualServers) {
            if (groupVirtualServer.getRewrite() != null && !groupVirtualServer.getRewrite().isEmpty())
                if (!PathRewriteParser.validate(groupVirtualServer.getRewrite()))
                    throw new ValidationException("Invalid rewrite value.");
            VirtualServer vs = groupVirtualServer.getVirtualServer();
            if (!virtualServerModelValidator.exists(vs.getId()))
                throw new ValidationException("Virtual Server with id " + vs.getId() + " does not exist.");
            else {
                if (virtualServerIds.contains(vs.getId()))
                    throw new ValidationException("Group-VirtualServer is an unique combination.");
                else
                    virtualServerIds.add(vs.getId());
            }
            if (groupPaths.contains(vs.getId() + groupVirtualServer.getPath()))
                throw new ValidationException("Duplicate path \"" + groupVirtualServer.getPath() + "\" is found on virtual server " + vs.getId() + " from post entity.");
            else
                groupPaths.add(vs.getId() + groupVirtualServer.getPath());
        }
        for (RelGroupVsDo relGroupVsDo : rGroupVsDao.findAllGroupsByVses(virtualServerIds.toArray(new Long[virtualServerIds.size()]), RGroupVsEntity.READSET_FULL)) {
            if (groupId.equals(relGroupVsDo.getGroupId()))
                continue;
            if (groupPaths.contains(relGroupVsDo.getVsId() + relGroupVsDo.getPath()))
                throw new ValidationException("Duplicate path \"" + relGroupVsDo.getPath() + "\" is found on virtual server " + relGroupVsDo.getVsId() + " from existing entities.");
            else
                groupPaths.add(relGroupVsDo.getVsId() + relGroupVsDo.getPath());
        }
    }

    @Override
    public void validateGroupServers(List<GroupServer> groupServers) throws Exception {
        groupServerModelValidator.validateGroupServers(groupServers);
    }
}
