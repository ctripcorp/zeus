package com.ctrip.zeus.service.model.handler.impl;

import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.GroupSlb;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.service.activate.ActiveConfService;
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
        if (group == null) {
            throw new ValidationException("Group with null value cannot be persisted.");
        }
        if (!validateGroupSlbs(group))
            throw new ValidationException("Virtual server cannot be found.");
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
        Set<Long> virtualServerIds = new HashSet<>();
        for (GroupSlb gs : group.getGroupSlbs()) {
            VirtualServer vs = slbRepository.getVirtualServer(gs.getVirtualServer().getId(),
                    gs.getSlbId(), gs.getVirtualServer().getName());
            if (vs == null)
                throw new ValidationException("Virtual Server does not exist.");
            virtualServerIds.add(vs.getId());
        }
        for (Long virtualServerId : virtualServerIds) {
            Set<String> paths = new HashSet<>();
            for (GroupSlb groupSlb : slbRepository.listGroupSlbsByVirtualServer(virtualServerId)) {
                if (paths.contains(groupSlb.getPath()))
                    return false;
                else
                    paths.add(groupSlb.getPath());
            }
        }
        return true;
    }
}
