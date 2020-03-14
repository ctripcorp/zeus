package com.ctrip.zeus.service.model.impl;

import com.ctrip.zeus.dao.entity.*;
import com.ctrip.zeus.dao.mapper.SlbTrafficPolicyGroupRMapper;
import com.ctrip.zeus.dao.mapper.SlbTrafficPolicyMapper;
import com.ctrip.zeus.dao.mapper.SlbTrafficPolicyVsRMapper;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.model.*;
import com.ctrip.zeus.service.model.IdVersion;
import com.ctrip.zeus.service.model.TrafficPolicyRepository;
import com.ctrip.zeus.service.model.ValidationFacade;
import com.ctrip.zeus.service.model.VersionUtils;
import com.ctrip.zeus.service.model.common.ValidationContext;
import com.ctrip.zeus.service.model.validation.TrafficPolicyValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.*;

/**
 * Created by zhoumy on 2017/1/11.
 */
@Repository("trafficPolicyRepository")
public class TrafficPolicyRepositoryImpl implements TrafficPolicyRepository {
    @Resource
    private SlbTrafficPolicyMapper slbTrafficPolicyMapper;

    @Resource
    private SlbTrafficPolicyGroupRMapper slbTrafficPolicyGroupRMapper;

    @Resource
    private SlbTrafficPolicyVsRMapper slbTrafficPolicyVsRMapper;

    @Resource
    private ValidationFacade validationFacade;
    @Resource
    private TrafficPolicyValidator trafficPolicyValidator;

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public List<TrafficPolicy> list() throws Exception {
        List<SlbTrafficPolicy> list = slbTrafficPolicyMapper.selectByExample(new SlbTrafficPolicyExample().createCriteria().example());
        IdVersion[] currentVersion = new IdVersion[list.size()];
        for (int i = 0; i < list.size(); i++) {
            currentVersion[i] = new IdVersion(list.get(i).getId(), list.get(i).getVersion());
        }
        return list(currentVersion);
    }

    @Override
    public List<TrafficPolicy> list(IdVersion[] key) throws Exception {
        Map<Long, TrafficPolicy> policyById = new HashMap<>();
        if (key == null || key.length == 0) return new ArrayList<>(policyById.values());

        for (IdVersion e : key) {
            policyById.put(e.getId(), new TrafficPolicy().setId(e.getId()).setVersion(e.getVersion()));
        }
        if (policyById.keySet().size() == 0) return Collections.EMPTY_LIST;

        for (SlbTrafficPolicy e : slbTrafficPolicyMapper.selectByExample(new SlbTrafficPolicyExample().
                createCriteria().
                andIdIn(new ArrayList<>(policyById.keySet())).example())) {
            TrafficPolicy tp = policyById.get(e.getId());
            if (tp != null) {
                tp.setName(e.getName());
            }
        }

        int i = 0;
        Integer[] hashes = new Integer[policyById.size()];
        for (TrafficPolicy e : policyById.values()) {
            hashes[i] = getHashCode(e.getId(), e.getVersion());
            i++;
        }

        for (SlbTrafficPolicyGroupR e : slbTrafficPolicyGroupRMapper.selectByExample(new SlbTrafficPolicyGroupRExample().createCriteria().andHashIn(Arrays.asList(hashes)).example())) {
            TrafficPolicy tp = policyById.get(e.getPolicyId());
            if (tp != null && tp.getVersion().equals(e.getPolicyVersion())) {
                tp.setCreatedTime(e.getDatachangeLasttime());
                tp.getControls().add(new TrafficControl().setGroup(new Group().setId(e.getGroupId())).setWeight(e.getWeight()));
            }
        }
        for (SlbTrafficPolicyVsR e : slbTrafficPolicyVsRMapper.selectByExample(new SlbTrafficPolicyVsRExample().createCriteria().andHashIn(Arrays.asList(hashes)).example())) {
            TrafficPolicy tp = policyById.get(e.getPolicyId());
            if (tp != null && tp.getVersion().equals(e.getPolicyVersion())) {
                tp.getPolicyVirtualServers().add(new PolicyVirtualServer().setPath(e.getPath()).setPriority(e.getPriority()).setVirtualServer(new VirtualServer().setId(e.getVsId())));
            }
        }
        return new ArrayList<>(policyById.values());
    }

    @Override
    public TrafficPolicy getById(Long id) throws Exception {
        SlbTrafficPolicy d = slbTrafficPolicyMapper.selectByPrimaryKey(id);
        return d == null ? null : getByKey(new IdVersion(d.getId(), d.getVersion()));
    }

    @Override
    public TrafficPolicy getByKey(IdVersion key) throws Exception {
        TrafficPolicy tp = new TrafficPolicy().setId(key.getId()).setVersion(key.getVersion());
        SlbTrafficPolicy d = slbTrafficPolicyMapper.selectByPrimaryKey(key.getId());
        tp.setName(d.getName());
        for (SlbTrafficPolicyGroupR e : slbTrafficPolicyGroupRMapper.selectByExample(new SlbTrafficPolicyGroupRExample().createCriteria().andPolicyIdEqualTo(key.getId()).andPolicyVersionEqualTo(key.getVersion()).example())) {
            if (tp.getVersion().equals(e.getPolicyVersion())) {
                tp.setCreatedTime(e.getDatachangeLasttime());
                tp.getControls().add(new TrafficControl().setGroup(new Group().setId(e.getGroupId())).setWeight(e.getWeight()));
            }
        }
        for (SlbTrafficPolicyVsR e : slbTrafficPolicyVsRMapper.selectByExample(new SlbTrafficPolicyVsRExample().createCriteria().andPolicyIdEqualTo(key.getId()).andPolicyVersionEqualTo(key.getVersion()).example())) {
            if (tp.getVersion().equals(e.getPolicyVersion())) {
                tp.getPolicyVirtualServers().add(new PolicyVirtualServer().setPath(e.getPath()).setPriority(e.getPriority()).setVirtualServer(new VirtualServer().setId(e.getVsId())));
            }
        }
        return tp;
    }

    @Override
    public TrafficPolicy add(TrafficPolicy trafficPolicy) throws Exception {
        return add(trafficPolicy, false);
    }

    @Override
    public TrafficPolicy add(TrafficPolicy trafficPolicy, boolean force) throws Exception {
        trafficPolicy.setId(null);
        ValidationContext context = new ValidationContext();
        validationFacade.validatePolicy(trafficPolicy, context);
        if (force) {
            if (context.getErrorPolicies().contains(trafficPolicy.getId())) {
                logger.info("Skip validate. Error:" + context.getPolicyErrorReason(trafficPolicy.getId()));
            }
        } else {
            if (context.getErrorPolicies().contains(trafficPolicy.getId())) {
                throw new ValidationException(context.getPolicyErrorReason(trafficPolicy.getId()));
            }
        }

        SlbTrafficPolicy p = SlbTrafficPolicy.builder().id(null).name(trafficPolicy.getName()).version(1).activeVersion(-1).nxActiveVersion(1).build();
        slbTrafficPolicyMapper.insert(p);
        trafficPolicy.setId(p.getId()).setVersion(1);
        maintainRelations(trafficPolicy, trafficPolicy.getId(), 1);

        return trafficPolicy;
    }

    @Override
    public TrafficPolicy update(TrafficPolicy trafficPolicy) throws Exception {
        return update(trafficPolicy, false);
    }

    @Override
    public TrafficPolicy update(TrafficPolicy trafficPolicy, boolean force) throws Exception {
        trafficPolicyValidator.checkRestrictionForUpdate(trafficPolicy);
        ValidationContext context = new ValidationContext();
        validationFacade.validatePolicy(trafficPolicy, context);
        if (force) {
            //TODO filter by error type
            if (context.getErrorPolicies().contains(trafficPolicy.getId())) {
                logger.info("Skip validate. Error:" + context.getPolicyErrorReason(trafficPolicy.getId()));
            }
        } else {
            if (context.getErrorPolicies().contains(trafficPolicy.getId())) {
                throw new ValidationException(context.getPolicyErrorReason(trafficPolicy.getId()));
            }
        }

        SlbTrafficPolicy tpd = slbTrafficPolicyMapper.selectByPrimaryKey(trafficPolicy.getId());
        if (tpd == null) {
            throw new ValidationException("Traffic policy " + trafficPolicy.getId() + " that you tried to update does not exists.");
        }

        tpd.setName(trafficPolicy.getName());
        tpd.setVersion(tpd.getVersion() + 1);
        tpd.setActiveVersion(tpd.getActiveVersion());
        tpd.setNxActiveVersion(tpd.getVersion());
        slbTrafficPolicyMapper.updateByExample(tpd, new SlbTrafficPolicyExample().createCriteria().andIdEqualTo(tpd.getId()).example());
        trafficPolicy.setId(tpd.getId()).setVersion(tpd.getVersion());

        maintainRelations(trafficPolicy, tpd.getId(), tpd.getVersion());
        return trafficPolicy;
    }

    @Override
    public void updateActiveStatus(IdVersion[] trafficPolicies) throws Exception {
        Long[] ids = new Long[trafficPolicies.length];
        for (int i = 0; i < trafficPolicies.length; i++) {
            ids[i] = trafficPolicies[i].getId();
        }

        if (ids.length == 0) return;

        List<SlbTrafficPolicy> tpd = slbTrafficPolicyMapper.selectByExample(new SlbTrafficPolicyExample().createCriteria().andIdIn(Arrays.asList(ids)).example());
        for (SlbTrafficPolicy e : tpd) {
            int i = Arrays.binarySearch(ids, e.getId());
            e.setActiveVersion(trafficPolicies[i].getVersion());
        }
        slbTrafficPolicyMapper.batchUpdate(tpd);
    }

    private void maintainRelations(TrafficPolicy trafficPolicy, Long id, int version) {
        int hashCode = getHashCode(id, version);
        SlbTrafficPolicyGroupR[] tpgd = new SlbTrafficPolicyGroupR[trafficPolicy.getControls().size()];
        for (int i = 0; i < trafficPolicy.getControls().size(); i++) {
            TrafficControl c = trafficPolicy.getControls().get(i);
            tpgd[i] = SlbTrafficPolicyGroupR.builder().policyId(id).policyVersion(version).hash(hashCode).groupId(c.getGroup().getId()).weight(c.getWeight()).build();
        }
        slbTrafficPolicyGroupRMapper.batchInsert(Arrays.asList(tpgd));

        SlbTrafficPolicyVsR[] tpvd = new SlbTrafficPolicyVsR[trafficPolicy.getPolicyVirtualServers().size()];
        for (int i = 0; i < trafficPolicy.getPolicyVirtualServers().size(); i++) {
            PolicyVirtualServer pvs = trafficPolicy.getPolicyVirtualServers().get(i);
            tpvd[i] = SlbTrafficPolicyVsR.builder().policyId(id).policyVersion(version).hash(hashCode).vsId(pvs.getVirtualServer().getId()).path(pvs.getPath()).priority(pvs.getPriority()).build();
        }
        slbTrafficPolicyVsRMapper.batchInsert(Arrays.asList(tpvd));
    }

    @Override
    public void delete(Long id) throws Exception {
        trafficPolicyValidator.removable(id);
        slbTrafficPolicyMapper.deleteByPrimaryKey(id);
        slbTrafficPolicyGroupRMapper.deleteByExample(new SlbTrafficPolicyGroupRExample().createCriteria().andPolicyIdEqualTo(id).example());
        slbTrafficPolicyVsRMapper.deleteByExample(new SlbTrafficPolicyVsRExample().createCriteria().andPolicyIdEqualTo(id).example());
    }

    private static int getHashCode(Long id, int version) {
        return VersionUtils.getHash(id, version);
    }

    public void setSlbTrafficPolicyMapper(SlbTrafficPolicyMapper slbTrafficPolicyMapper) {
        this.slbTrafficPolicyMapper = slbTrafficPolicyMapper;
    }

    public void setSlbTrafficPolicyGroupRMapper(SlbTrafficPolicyGroupRMapper slbTrafficPolicyGroupRMapper) {
        this.slbTrafficPolicyGroupRMapper = slbTrafficPolicyGroupRMapper;
    }

    public void setSlbTrafficPolicyVsRMapper(SlbTrafficPolicyVsRMapper slbTrafficPolicyVsRMapper) {
        this.slbTrafficPolicyVsRMapper = slbTrafficPolicyVsRMapper;
    }

    public void setValidationFacade(ValidationFacade validationFacade) {
        this.validationFacade = validationFacade;
    }

    public void setTrafficPolicyValidator(TrafficPolicyValidator trafficPolicyValidator) {
        this.trafficPolicyValidator = trafficPolicyValidator;
    }
}