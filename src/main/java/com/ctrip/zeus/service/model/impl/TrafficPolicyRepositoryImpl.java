package com.ctrip.zeus.service.model.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.service.model.IdVersion;
import com.ctrip.zeus.service.model.TrafficPolicyRepository;
import com.ctrip.zeus.service.model.ValidationFacade;
import com.ctrip.zeus.service.model.VersionUtils;
import com.ctrip.zeus.service.model.common.ValidationContext;
import org.springframework.stereotype.Repository;
import org.unidal.dal.jdbc.DalException;

import javax.annotation.Resource;
import java.util.*;

/**
 * Created by zhoumy on 2017/1/11.
 */
@Repository("trafficPolicyRepository")
public class TrafficPolicyRepositoryImpl implements TrafficPolicyRepository {
    @Resource
    private TrafficPolicyDao trafficPolicyDao;
    @Resource
    private RTrafficPolicyGroupDao rTrafficPolicyGroupDao;
    @Resource
    private RTrafficPolicyVsDao rTrafficPolicyVsDao;
    @Resource
    private ValidationFacade validationFacade;

    @Override
    public List<TrafficPolicy> list() throws Exception {
        List<TrafficPolicyDo> list = trafficPolicyDao.findAll(TrafficPolicyEntity.READSET_FULL);
        IdVersion[] currentVersion = new IdVersion[list.size()];
        for (int i = 0; i < list.size(); i++) {
            currentVersion[i] = new IdVersion(list.get(i).getId(), list.get(i).getVersion());
        }
        return list(currentVersion);
    }

    @Override
    public List<TrafficPolicy> list(IdVersion[] key) throws Exception {
        Map<Long, TrafficPolicy> policyById = new HashMap<>();
        for (IdVersion e : key) {
            policyById.put(e.getId(), new TrafficPolicy().setId(e.getId()).setVersion(e.getVersion()));
        }

        int i = 0;
        Integer[] hashes = new Integer[policyById.size()];
        for (TrafficPolicy e : policyById.values()) {
            hashes[i] = getHashCode(e.getId(), e.getVersion());
            i++;
        }

        for (RTrafficPolicyGroupDo e : rTrafficPolicyGroupDao.findAllByHash(hashes, RTrafficPolicyGroupEntity.READSET_FULL)) {
            TrafficPolicy tp = policyById.get(e.getPolicyId());
            if (tp != null && tp.getVersion().equals(e.getPolicyVersion())) {
                tp.getControls().add(new TrafficControl().setGroup(new Group().setId(e.getGroupId())).setWeight(e.getWeight()));
            }
        }
        for (RTrafficPolicyVsDo e : rTrafficPolicyVsDao.findAllByHash(hashes, RTrafficPolicyVsEntity.READSET_FULL)) {
            TrafficPolicy tp = policyById.get(e.getPolicyId());
            if (tp != null && tp.getVersion().equals(e.getPolicyVersion())) {
                tp.getPolicyVirtualServers().add(new PolicyVirtualServer().setPath(e.getPath()).setPriority(e.getPriority()).setVirtualServer(new VirtualServer().setId(e.getVsId())));
            }
        }
        return new ArrayList<>(policyById.values());
    }

    @Override
    public TrafficPolicy getById(Long id) throws Exception {
        TrafficPolicyDo d = trafficPolicyDao.findById(id, TrafficPolicyEntity.READSET_FULL);
        return d == null ? null : getByKey(new IdVersion(d.getId(), d.getVersion()));
    }

    @Override
    public TrafficPolicy getByKey(IdVersion key) throws Exception {
        TrafficPolicy tp = new TrafficPolicy().setId(key.getId()).setVersion(key.getVersion());
        for (RTrafficPolicyGroupDo e : rTrafficPolicyGroupDao.findByPolicy(key.getId(), key.getVersion(), RTrafficPolicyGroupEntity.READSET_FULL)) {
            if (tp != null && tp.getVersion().equals(e.getPolicyVersion())) {
                tp.getControls().add(new TrafficControl().setGroup(new Group().setId(e.getGroupId())).setWeight(e.getWeight()));
            }
        }
        for (RTrafficPolicyVsDo e : rTrafficPolicyVsDao.findByPolicy(key.getId(), key.getVersion(), RTrafficPolicyVsEntity.READSET_FULL)) {
            if (tp != null && tp.getVersion().equals(e.getPolicyVersion())) {
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
            //TODO filter by error type
        } else {
            if (context.getErrorPolicies().contains(trafficPolicy.getId())) {
                throw new ValidationException(context.getPolicyErrorReason(trafficPolicy.getId()));
            }
        }
        TrafficPolicyDo tpd = new TrafficPolicyDo().setName(trafficPolicy.getName()).setVersion(1).setActiveVersion(-1).setNxActiveVersion(1);
        trafficPolicyDao.insert(tpd);
        trafficPolicy.setId(tpd.getId()).setVersion(tpd.getVersion());

        maintainRelations(trafficPolicy, tpd);

        return trafficPolicy;
    }

    @Override
    public TrafficPolicy update(TrafficPolicy trafficPolicy) throws Exception {
        return update(trafficPolicy, false);
    }

    @Override
    public TrafficPolicy update(TrafficPolicy trafficPolicy, boolean force) throws Exception {
        ValidationContext context = new ValidationContext();
        validationFacade.validatePolicy(trafficPolicy, context);
        if (force) {
            //TODO filter by error type
        } else {
            if (context.getErrorGroups().contains(trafficPolicy.getId())) {
                throw new ValidationException(context.getPolicyErrorReason(trafficPolicy.getId()));
            }
        }

        TrafficPolicyDo tpd = trafficPolicyDao.findById(trafficPolicy.getId(), TrafficPolicyEntity.READSET_FULL);
        if (tpd == null) {
            throw new ValidationException("Traffic policy " + trafficPolicy.getId() + " that you tried to update does not exists.");
        }

        tpd.setName(trafficPolicy.getName()).setVersion(tpd.getVersion() + 1).setActiveVersion(tpd.getActiveVersion()).setNxActiveVersion(tpd.getVersion());
        trafficPolicyDao.updateById(tpd, TrafficPolicyEntity.UPDATESET_FULL);
        trafficPolicy.setId(tpd.getId()).setVersion(tpd.getVersion());

        maintainRelations(trafficPolicy, tpd);

        return trafficPolicy;
    }

    @Override
    public void updateActiveStatus(IdVersion[] trafficPolicies) throws Exception {
        Long[] ids = new Long[trafficPolicies.length];
        for (int i = 0; i < trafficPolicies.length; i++) {
            ids[i] = trafficPolicies[i].getId();
        }
        List<TrafficPolicyDo> tpd = trafficPolicyDao.findAllByIds(ids, TrafficPolicyEntity.READSET_FULL);
        for (TrafficPolicyDo e : tpd) {
            int i = Arrays.binarySearch(ids, e.getId());
            e.setActiveVersion(trafficPolicies[i].getVersion());
        }
        trafficPolicyDao.updateById(tpd.toArray(new TrafficPolicyDo[tpd.size()]), TrafficPolicyEntity.UPDATESET_FULL);
    }

    private void maintainRelations(TrafficPolicy trafficPolicy, TrafficPolicyDo tpd) throws DalException {
        int hashCode = getHashCode(tpd.getId(), tpd.getVersion());
        RTrafficPolicyGroupDo[] tpgd = new RTrafficPolicyGroupDo[trafficPolicy.getControls().size()];
        for (int i = 0; i < trafficPolicy.getControls().size(); i++) {
            TrafficControl c = trafficPolicy.getControls().get(i);
            tpgd[i] = new RTrafficPolicyGroupDo().setPolicyId(tpd.getId()).setPolicyVersion(tpd.getVersion()).setHash(hashCode)
                    .setGroupId(c.getGroup().getId()).setWeight(c.getWeight());
        }
        rTrafficPolicyGroupDao.insert(tpgd);

        RTrafficPolicyVsDo[] tpvd = new RTrafficPolicyVsDo[trafficPolicy.getPolicyVirtualServers().size()];
        for (int i = 0; i < trafficPolicy.getPolicyVirtualServers().size(); i++) {
            PolicyVirtualServer pvs = trafficPolicy.getPolicyVirtualServers().get(i);
            tpvd[i] = new RTrafficPolicyVsDo().setPolicyId(tpd.getId()).setPolicyVersion(tpd.getVersion()).setHash(hashCode)
                    .setVsId(pvs.getVirtualServer().getId()).setPath(pvs.getPath()).setPriority(pvs.getPriority());
        }
        rTrafficPolicyVsDao.insert(tpvd);
    }

    @Override
    public void delete(Long id) throws Exception {
        trafficPolicyDao.deleteById(new TrafficPolicyDo().setId(id));
        rTrafficPolicyGroupDao.deleteByPolicy(new RTrafficPolicyGroupDo().setPolicyId(id));
        rTrafficPolicyVsDao.deleteByPolicy(new RTrafficPolicyVsDo().setPolicyId(id));

    }

    private static int getHashCode(Long id, int version) {
        return VersionUtils.getHash(id, version);
    }
}