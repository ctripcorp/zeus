package com.ctrip.zeus.service.model.handler.impl;

import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.GroupSlb;
import com.ctrip.zeus.model.entity.GroupVirtualServer;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.service.activate.ActiveConfService;
import com.ctrip.zeus.service.model.PathRewriteParser;
import com.ctrip.zeus.service.model.VirtualServerRepository;
import com.ctrip.zeus.service.model.handler.GroupValidator;
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
    private VirtualServerRepository virtualServerRepository;

    @Override
    public void validate(Group group) throws Exception {
        if (group == null || group.getName() == null || group.getName().isEmpty()
                || group.getAppId() == null || group.getAppId().isEmpty()) {
            throw new ValidationException("Group with null value cannot be persisted.");
        }
        if (!validateGroupVirtualServers(group.getId(), group.getGroupVirtualServers()))
            throw new ValidationException("Virtual server has invalid data.");
    }

    @Override
    public void removable(Long groupId) throws Exception {
        List<String> l = activeConfService.getConfGroupActiveContentByGroupIds(new Long[]{groupId});
        if (l.size() > 0)
            throw new ValidationException("Group must be deactivated before deletion.");
    }

    @Override
    public boolean validateGroupVirtualServers(Long groupId, List<GroupVirtualServer> groupVirtualServers) throws Exception {
        if (groupVirtualServers == null || groupVirtualServers.size() == 0)
            return false;
        if (groupId == null)
            groupId = 0L;
        Set<Long> virtualServerIds = new HashSet<>();
        Set<String> groupPaths = new HashSet<>();
        for (GroupVirtualServer groupVirtualServer : groupVirtualServers) {
            if (groupVirtualServer.getRewrite() != null && !groupVirtualServer.getRewrite().isEmpty())
                if (!PathRewriteParser.validate(groupVirtualServer.getRewrite()))
                    throw new ValidationException("Invalid rewrite value.");
            VirtualServer vs = groupVirtualServer.getVirtualServer();
            VirtualServer checkVs = virtualServerRepository.getById(vs.getId());
            if (checkVs == null) {
                checkVs = virtualServerRepository.getBySlbAndName(vs.getSlbId(), vs.getName());
                vs.setId(checkVs.getId());
            }
            if (checkVs == null)
                throw new ValidationException("Virtual Server does not exist.");
            else
                virtualServerIds.add(vs.getId());
            if (groupPaths.contains(vs.getId() + groupVirtualServer.getPath()))
                return false;
            else
                groupPaths.add(vs.getId() + groupVirtualServer.getPath());

        }
        for (Long virtualServerId : virtualServerIds) {
            Long[] groupIds = virtualServerRepository.findGroupsByVirtualServer(virtualServerId);
            for (int i = 0; i < groupIds.length; i++) {
                if (groupIds[i].equals(groupId))
                    groupIds[i] = 0L;
            }
            for (GroupVirtualServer gvs : virtualServerRepository.listGroupVsByGroups(groupIds)) {
                if (groupPaths.contains(gvs.getVirtualServer().getId() + gvs.getPath()))
                    return false;
                else
                    groupPaths.add(gvs.getVirtualServer().getId() + gvs.getPath());
            }
        }
        return true;
    }
}
