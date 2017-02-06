package com.ctrip.zeus.service.model.validation;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.Domain;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.service.model.common.ErrorType;
import com.ctrip.zeus.service.model.common.MetaType;
import com.ctrip.zeus.service.model.common.ValidationContext;
import com.ctrip.zeus.service.query.GroupCriteriaQuery;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by zhoumy on 2015/9/24.
 */
@Component("virtualServerModelValidator")
public class DefaultVirtualServerValidator implements VirtualServerValidator {
    private DynamicStringProperty portWhiteList = DynamicPropertyFactory.getInstance().getStringProperty("port.whitelist", "80,443");

    @Resource
    private GroupCriteriaQuery groupCriteriaQuery;
    @Resource
    private SlbVirtualServerDao slbVirtualServerDao;
    @Resource
    private RVsStatusDao rVsStatusDao;

    private final Pattern pattern;

    public DefaultVirtualServerValidator() {
        pattern = Pattern.compile("([\\w\\.\\-\\*]+)");
    }

    @Override
    public void validateVsFields(VirtualServer vs) throws ValidationException {
        if (vs.getDomains() == null || vs.getDomains().size() == 0) {
            throw new ValidationException("Field `domains` is not allowed empty.");
        }
        if (vs.getSlbIds() == null || vs.getSlbIds().size() == 0) {
            throw new ValidationException("Field `slb-ids` is not allowed empty.");
        }
    }

    @Override
    public void validateDomains(Long slbId, List<VirtualServer> vses, ValidationContext context) {
        Map<String, Long> domainByVs = new HashMap<>();
        for (VirtualServer vs : vses) {
            int i = vs.getSlbIds().indexOf(slbId);
            if (i < 0) continue;
            for (Domain d : vs.getDomains()) {
                Long prev = domainByVs.put(d.getName() + ":" + vs.getPort(), vs.getId());
                if (prev != null) {
                    if (prev.equals(vs.getId())) {
                        context.error(vs.getId(), MetaType.VS, ErrorType.DEPENDENCY_VALIDATION, "Duplicate domain value " + d.getName() + ":" + vs.getPort() + " is found");
                    } else {
                        context.error(vs.getId(), MetaType.SLB, ErrorType.DEPENDENCY_VALIDATION, "Domain " + d.getName() + ":" + vs.getPort() + " is found on multiple vses.");
                        context.error(prev, MetaType.SLB, ErrorType.DEPENDENCY_VALIDATION, "Domain " + d.getName() + ":" + vs.getPort() + " is found on multiple vses.");
                    }
                }
            }
        }
    }

    @Override
    public boolean isActivated(Long vsId) throws Exception {
        RelVsStatusDo e = rVsStatusDao.findByVs(vsId, RVsStatusEntity.READSET_FULL);
        return e != null && e.getOnlineVersion() != 0;
    }

    @Override
    public void unite(List<VirtualServer> virtualServers) throws Exception {
        Map<String, Long> existingHost = new HashMap<>();
        for (VirtualServer virtualServer : virtualServers) {
            for (Domain domain : virtualServer.getDomains()) {
                if (!getPortWhiteList().contains(virtualServer.getPort())) {
                    throw new ValidationException("Port " + virtualServer.getPort() + " is not allowed. Reference vs-id: " + virtualServer.getId() + ".");
                }
                if (!pattern.matcher(domain.getName()).matches()) {
                    throw new ValidationException("Invalid domain name: " + domain.getName() + ". Reference vs-id: " + virtualServer.getId() + ".");
                }
                String key = domain.getName().toLowerCase() + ":" + virtualServer.getPort();
                Long check = existingHost.get(key);
                if (check != null && !check.equals(virtualServer.getId())) {
                    throw new ValidationException(key + " already exists on current slb. Reference vs-id: " + check + ".");
                } else {
                    existingHost.put(key, virtualServer.getId());
                }
            }
        }
    }

    @Override
    public void removable(Long vsId) throws Exception {
        if (groupCriteriaQuery.queryByVsId(vsId).size() > 0)
            throw new ValidationException("Virtual server with id " + vsId + " cannot be deleted. Dependencies exist.");
        if (isActivated(vsId)) {
            throw new ValidationException("Vs need to be deactivated before delete!");
        }
    }

    @Override
    public void validate(VirtualServer virtualServer) throws ValidationException {
        if (virtualServer.getDomains() == null || virtualServer.getDomains().size() == 0)
            throw new ValidationException("Virtual server must have domain(s).");
        if (virtualServer.getSlbIds() == null || virtualServer.getSlbIds().size() == 0)
            throw new ValidationException("Missing field slbIds or empty value is found.");

        Set<Long> slbIds = new HashSet<>();
        for (Long slbId : virtualServer.getSlbIds()) {
            if (!slbIds.add(slbId)) {
                throw new ValidationException("Duplicate slbId-" + slbId + " is found.");
            }
        }
        Set<String> uniq = new HashSet<>();
        Iterator<Domain> domainIter = virtualServer.getDomains().iterator();
        while (domainIter.hasNext()) {
            Domain domain = domainIter.next();
            String name = domain.getName().toLowerCase();
            if (uniq.contains(name)) domainIter.remove();
            else {
                uniq.add(name);
                domain.setName(name);
            }
        }
    }

    @Override
    public void checkRestrictionForUpdate(VirtualServer target) throws Exception {

    }

    private Set<String> getPortWhiteList() {
        Set<String> result = new HashSet<>();
        String whiteList = portWhiteList.get();
        for (String s : whiteList.split(",")) {
            result.add(s.trim());
        }
        return result;
    }
}
