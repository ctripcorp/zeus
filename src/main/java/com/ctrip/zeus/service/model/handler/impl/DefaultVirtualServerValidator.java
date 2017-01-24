package com.ctrip.zeus.service.model.handler.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.Domain;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.service.model.handler.VirtualServerValidator;
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
    public boolean exists(Long vsId) throws Exception {
        return slbVirtualServerDao.findByPK(vsId, SlbVirtualServerEntity.READSET_FULL) != null;
    }

    @Override
    public void validateForMerge(Long[] toBeMergedItems, Long slbId, Map<Long, VirtualServer> vsRef) {

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
    public void validateForActivate(VirtualServer[] toBeActivatedItems, boolean escapedPathValidation) throws Exception {

    }

    @Override
    public void validateForDeactivate(Long[] toBeDeactivatedItems) throws Exception {

    }

    @Override
    public void checkVersionForUpdate(VirtualServer target) throws Exception {

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
