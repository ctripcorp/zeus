package com.ctrip.zeus.service.model.validation;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.SlbServer;
import com.ctrip.zeus.service.model.IdVersion;
import com.ctrip.zeus.service.model.common.ErrorType;
import com.ctrip.zeus.service.model.common.MetaType;
import com.ctrip.zeus.service.model.common.ValidationContext;
import com.ctrip.zeus.service.model.validation.SlbValidator;
import com.ctrip.zeus.service.query.SlbCriteriaQuery;
import com.ctrip.zeus.service.query.VirtualServerCriteriaQuery;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

/**
 * Created by zhoumy on 2015/6/30.
 */
@Component("slbModelValidator")
public class DefaultSlbValidator implements SlbValidator {
    @Resource
    private SlbCriteriaQuery slbCriteriaQuery;
    @Resource
    private VirtualServerCriteriaQuery virtualServerCriteriaQuery;
    @Resource
    private SlbDao slbDao;
    @Resource
    private RSlbStatusDao rSlbStatusDao;

    @Override
    public void validate(Slb slb) throws Exception {
        if (slb.getName() == null || slb.getName().isEmpty()) {
            throw new ValidationException("Slb name is required.");
        }
        if (slb.getVips() == null || slb.getVips().size() == 0) {
            throw new ValidationException("Slb vip is required.");
        }
        if (slb.getSlbServers() == null || slb.getSlbServers().size() == 0) {
            throw new ValidationException("Slb without slb servers cannot be created.");
        }
        Long nameCheck = slbCriteriaQuery.queryByName(slb.getName());
        if (!nameCheck.equals(0L) && !nameCheck.equals(slb.getId())) {
            throw new ValidationException("Duplicate name " + slb.getName() + " is found at slb " + nameCheck + ".");
        }

        Set<String> ips = new HashSet<>();
        // check if any other slb version who has the server ip is still in effect.
        for (SlbServer slbServer : slb.getSlbServers()) {
            if (!ips.add(slbServer.getIp())) {
                throw new ValidationException("Duplicate ip " + slbServer.getIp() + " is found.");
            }

            Set<IdVersion> range = slbCriteriaQuery.queryBySlbServerIp(slbServer.getIp());
            Set<Long> check = new HashSet<>();
            Iterator<IdVersion> iter = range.iterator();
            while (iter.hasNext()) {
                Long e = iter.next().getId();
                if (e.equals(slb.getId())) {
                    iter.remove();
                } else {
                    check.add(e);
                }
            }
            if (check.size() == 0)
                continue;
            check.remove(slb.getId());
            if (check.size() > 0) {
                throw new ValidationException("Slb server " + slbServer.getIp() + " exists in the system. Unique server ip is required.");
            }
        }
    }

    @Override
    public void checkRestrictionForUpdate(Slb target) throws Exception {
        SlbDo check = slbDao.findById(target.getId(), SlbEntity.READSET_FULL);
        if (check == null)
            throw new ValidationException("Slb with id " + target.getId() + " does not exist.");
        if (!target.getVersion().equals(check.getVersion()))
            throw new ValidationException("Newer offline version is detected.");
    }

    @Override
    public void removable(Long slbId) throws Exception {
        if (virtualServerCriteriaQuery.queryBySlbId(slbId).size() > 0) {
            throw new ValidationException("Slb with id " + slbId + " cannot be deleted. Dependencies exist.");
        }
        if (rSlbStatusDao.findBySlb(slbId, RSlbStatusEntity.READSET_FULL).getOnlineVersion() != 0) {
            throw new ValidationException("Slb must be deactivated before deletion.");
        }
    }

    @Override
    public void validateSlbFields(Slb slb, ValidationContext context) throws ValidationException {
        if (slb.getName() == null || slb.getName().isEmpty()) {
            throw new ValidationException("Field `name` is not allowed empty.");
        }
        if (slb.getVips() == null || slb.getVips().size() == 0) {
            throw new ValidationException("Field `vips` is not allowed empty.");
        }
        if (slb.getSlbServers() == null || slb.getSlbServers().size() == 0) {
            throw new ValidationException("Field `slb-servers` is not allowed empty.");
        }
    }

    @Override
    public void validateSlbServers(Map<Long, List<SlbServer>> serversBySlb, ValidationContext context) {
        Map<String, Long> serverUniqueCheck = new HashMap<>();
        for (Map.Entry<Long, List<SlbServer>> e : serversBySlb.entrySet()) {
            for (SlbServer s : e.getValue()) {
                if (s.getIp() == null || s.getIp().isEmpty()) {
                    context.error(e.getKey(), MetaType.SLB, ErrorType.FIELD_VALIDATION, "Field `ip` of slb server is not allowed empty.");
                    break;
                }
                Long prev = serverUniqueCheck.put(s.getIp(), e.getKey());
                if (prev != null) {
                    if (prev.equals(e.getKey())) {
                        context.error(e.getKey(), MetaType.SLB, ErrorType.FIELD_VALIDATION, "Duplicate server ip " + s.getIp() + " is found of `slb-servers` list.");
                    } else {
                        context.error(e.getKey(), MetaType.SLB, ErrorType.DEPENDENCY_VALIDATION, "Server ip " + s.getIp() + " is declared on multiple slbs.");
                        context.error(prev, MetaType.SLB, ErrorType.DEPENDENCY_VALIDATION, "Server ip " + s.getIp() + " is declared on multiple slbs.");
                    }
                }
            }
        }
    }

    @Override
    public void exists(Long[] slbId) throws Exception {

    }
}
