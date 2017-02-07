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
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

/**
 * Created by zhoumy on 2015/9/24.
 */
@Component("virtualServerModelValidator")
public class DefaultVirtualServerValidator implements VirtualServerValidator {
    private AtomicReference<Set<Integer>> allowedPorts = new AtomicReference<>();
    private DynamicStringProperty portWhiteList = DynamicPropertyFactory.getInstance().getStringProperty("port.whitelist", "80,443", new Runnable() {
        @Override
        public void run() {
            generateAllowedPorts();
        }
    });

    @Resource
    private GroupCriteriaQuery groupCriteriaQuery;
    @Resource
    private RVsStatusDao rVsStatusDao;

    private static final Pattern DOMAIN_PATTERN = Pattern.compile("([\\w\\.\\-\\*]+)");

    @Override
    public void validateFields(VirtualServer vs) throws ValidationException {
        if (vs.getDomains() == null || vs.getDomains().size() == 0) {
            throw new ValidationException("Field `domains` is not allowed empty.");
        }
        if (vs.getSlbIds() == null || vs.getSlbIds().size() == 0) {
            throw new ValidationException("Field `slb-ids` is not allowed empty.");
        }
        try {
            if (!getAllowedPorts().contains(Integer.parseInt(vs.getPort()))) {
                throw new ValidationException("Port " + vs.getPort() + " is not allowed.");
            }
        } catch (NumberFormatException e) {
            throw new ValidationException("Illegal port: " + vs.getPort() + ".");
        }
    }

    @Override
    public void validateDomains(Long slbId, Collection<VirtualServer> vses, ValidationContext context) {
        Map<String, Long> domainByVs = new HashMap<>();
        for (VirtualServer vs : vses) {
            int i = vs.getSlbIds().indexOf(slbId);
            if (i < 0) continue;
            for (Domain d : vs.getDomains()) {
                if (!DOMAIN_PATTERN.matcher(d.getName()).matches()) {
                    context.error(vs.getId(), MetaType.VS, ErrorType.FIELD_VALIDATION, "Illegal domain name: " + d.getName() + " is found");
                    break;
                }
                Long prev = domainByVs.put(d.getName() + ":" + vs.getPort(), vs.getId());
                if (prev != null) {
                    if (prev.equals(vs.getId())) {
                        context.error(vs.getId(), MetaType.VS, ErrorType.DEPENDENCY_VALIDATION, "Duplicate domain value " + d.getName() + ":" + vs.getPort() + " is found");
                    } else {
                        context.error(vs.getId(), MetaType.VS, ErrorType.DEPENDENCY_VALIDATION, "Domain " + d.getName() + ":" + vs.getPort() + " is found on multiple vses.");
                        context.error(prev, MetaType.VS, ErrorType.DEPENDENCY_VALIDATION, "Domain " + d.getName() + ":" + vs.getPort() + " is found on multiple vses.");
                    }
                }
            }
        }
    }

    @Override
    public void removable(Long vsId) throws Exception {
        RelVsStatusDo check = rVsStatusDao.findByVs(vsId, RVsStatusEntity.READSET_FULL);
        if (check == null) return;
        if (check.getOnlineVersion() > 0) {
            throw new ValidationException("Virtual server that you try to delete is still active.");
        }
        if (groupCriteriaQuery.queryByVsId(vsId).size() > 0)
            throw new ValidationException("Virtual server that you try to has one or more group dependencies.");
    }

    @Override
    public void checkRestrictionForUpdate(VirtualServer target) throws Exception {
        RelVsStatusDo check = rVsStatusDao.findByVs(target.getId(), RVsStatusEntity.READSET_FULL);
        if (check == null) {
            throw new ValidationException("Virtual server that you try to update does not exist.");
        }
        if (check.getOfflineVersion() > target.getVersion()) {
            throw new ValidationException("Newer version is detected.");
        }
        if (check.getOfflineVersion() != target.getVersion()) {
            throw new ValidationException("Incompatible version.");
        }
    }

    private void generateAllowedPorts() {
        Set<Integer> tmp = new HashSet<>();
        String whiteList = portWhiteList.get();
        for (String s : whiteList.split(",")) {
            try {
                tmp.add(Integer.parseInt(s));
            } catch (NumberFormatException e) {
                return;
            }
        }
        allowedPorts.set(tmp);
    }

    private Set<Integer> getAllowedPorts() {
        if (allowedPorts.get() == null) {
            generateAllowedPorts();
        }
        return allowedPorts.get();
    }
}
