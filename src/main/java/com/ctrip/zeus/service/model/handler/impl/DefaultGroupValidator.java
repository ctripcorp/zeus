package com.ctrip.zeus.service.model.handler.impl;

import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.GroupSlb;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.service.activate.ActiveConfService;
import com.ctrip.zeus.service.model.PathRewriteParser;
import com.ctrip.zeus.service.model.SlbRepository;
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
    private SlbRepository slbRepository;

    @Override
    public void validate(Group group) throws Exception {
        if (group == null || group.getName() == null || group.getName().isEmpty()
                || group.getAppId() == null || group.getAppId().isEmpty()) {
            throw new ValidationException("Group with null value cannot be persisted.");
        }
        if (!validateGroupSlbs(group))
            throw new ValidationException("Virtual server has invalid data.");
    }

    @Override
    public void removable(Long groupId) throws Exception {
        List<String> l = activeConfService.getConfGroupActiveContentByGroupIds(new Long[]{groupId});
        if (l.size() > 0)
            throw new ValidationException("Group must be deactivated before deletion.");
    }

    @Override
    public boolean validateGroupSlbs(Group group) throws Exception {
        if (group.getGroupSlbs().size() == 0)
           return false;
        if (group.getId() == null)
            group.setId(0L);
        Set<Long> virtualServerIds = new HashSet<>();
        Set<String> groupPaths = new HashSet<>();
        for (GroupSlb gs : group.getGroupSlbs()) {
            if (gs.getRewrite() != null && !gs.getRewrite().isEmpty() && !PathRewriteParser.validate(gs.getRewrite())) {
                throw new ValidationException("Invalid rewrite value.");
            }
            VirtualServer vs = slbRepository.getVirtualServer(gs.getVirtualServer().getId(),
                    gs.getSlbId(), gs.getVirtualServer().getName());
            if (vs == null)
                throw new ValidationException("Virtual Server does not exist.");
            virtualServerIds.add(vs.getId());
            if (groupPaths.contains(vs.getId() + gs.getPath()))
                return false;
            else
                groupPaths.add(vs.getId() + gs.getPath());
        }
        for (Long virtualServerId : virtualServerIds) {
            for (GroupSlb groupSlb : slbRepository.listGroupSlbsByVirtualServer(virtualServerId)) {
                if (groupSlb.getGroupId().equals(group.getId()))
                    continue;
                if (groupPaths.contains(virtualServerId + groupSlb.getPath()))
                    return false;
                else
                    groupPaths.add(virtualServerId + groupSlb.getPath());
            }
        }
        return true;
    }
}
