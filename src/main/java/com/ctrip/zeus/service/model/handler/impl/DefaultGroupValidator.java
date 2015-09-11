package com.ctrip.zeus.service.model.handler.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.GroupVirtualServer;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.service.activate.ActiveConfService;
import com.ctrip.zeus.service.model.PathRewriteParser;
import com.ctrip.zeus.service.model.handler.GroupValidator;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
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
    private SlbVirtualServerDao slbVirtualServerDao;
    @Resource
    private GroupSlbDao groupSlbDao;
    @Resource
    private GroupDao groupDao;

    @Override
    public boolean exists(Long groupId) throws Exception {
        return groupDao.findById(groupId, GroupEntity.READSET_FULL) != null;
    }

    @Override
    public void validate(Group group) throws Exception {
        if (group == null || group.getName() == null || group.getName().isEmpty()
                || group.getAppId() == null || group.getAppId().isEmpty()) {
            throw new ValidationException("Group with null value cannot be persisted.");
        }
        validateGroupVirtualServers(group.getId(), group.getGroupVirtualServers());
    }

    @Override
    public void removable(Long groupId) throws Exception {
        List<String> l = activeConfService.getConfGroupActiveContentByGroupIds(new Long[]{groupId});
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
            SlbVirtualServerDo checkVs = slbVirtualServerDao.findByPK(vs.getId(), SlbVirtualServerEntity.READSET_FULL);
            if (checkVs == null) {
                checkVs = slbVirtualServerDao.findBySlbAndName(vs.getSlbId(), vs.getName(), SlbVirtualServerEntity.READSET_FULL);
                vs.setId(checkVs.getId());
            }
            if (checkVs == null)
                throw new ValidationException("Virtual Server does not exist.");
            else {
                if (virtualServerIds.contains(vs.getId()))
                    throw new ValidationException("Group-VirtualServer is an unique combination.");
                else
                    virtualServerIds.add(vs.getId());
            }
            if (groupPaths.contains(vs.getId() + groupVirtualServer.getPath()))
                throw new ValidationException("Duplicate path \"" + groupVirtualServer.getPath() + "\" is found on virtual server " + vs.getId() + ".");
            else
                groupPaths.add(vs.getId() + groupVirtualServer.getPath());

        }
        for (Long virtualServerId : virtualServerIds) {
            List<Long> groupIds = new ArrayList<>();
            for (GroupSlbDo groupSlb : groupSlbDao.findAllByVirtualServer(virtualServerId, GroupSlbEntity.READSET_FULL)) {
                if (!groupId.equals(groupSlb.getGroupId()))
                    groupIds.add(groupSlb.getGroupId());
            }
            for (GroupSlbDo groupSlbDo : groupSlbDao.findAllByGroups(groupIds.toArray(new Long[groupIds.size()]), GroupSlbEntity.READSET_FULL)) {
                if (groupPaths.contains(groupSlbDo.getSlbVirtualServerId() + groupSlbDo.getPath()))
                    throw new ValidationException("Duplicate path \"" + groupSlbDo.getPath() + "\" is found on virtual server " + groupSlbDo.getSlbVirtualServerId() + ".");
                else
                    groupPaths.add(groupSlbDo.getSlbVirtualServerId() + groupSlbDo.getPath());
            }
        }
    }
}
