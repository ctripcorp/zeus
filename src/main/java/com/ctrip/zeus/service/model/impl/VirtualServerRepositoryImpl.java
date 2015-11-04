package com.ctrip.zeus.service.model.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.Domain;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.service.model.VirtualServerRepository;
import com.ctrip.zeus.service.model.handler.SlbQuery;
import com.ctrip.zeus.service.model.handler.SlbValidator;
import com.ctrip.zeus.service.model.handler.VirtualServerValidator;
import com.ctrip.zeus.service.model.handler.impl.ContentReaders;
import com.ctrip.zeus.service.model.handler.impl.VirtualServerEntityManager;
import com.ctrip.zeus.service.nginx.CertificateService;
import com.ctrip.zeus.service.query.VirtualServerCriteriaQuery;
import com.ctrip.zeus.support.C;
import org.springframework.stereotype.Component;
import org.unidal.dal.jdbc.DalException;

import javax.annotation.Resource;
import java.util.*;

/**
 * Created by zhoumy on 2015/7/27.
 */
@Component("virtualServerRepository")
public class VirtualServerRepositoryImpl implements VirtualServerRepository {
    @Resource
    private VirtualServerCriteriaQuery virtualServerCriteriaQuery;
    @Resource
    private VirtualServerEntityManager virtualServerEntityManager;
    @Resource
    private MVsContentDao mVsContentDao;
    @Resource
    private SlbVirtualServerDao slbVirtualServerDao;
    @Resource
    private SlbDomainDao slbDomainDao;
    @Resource
    private VirtualServerValidator virtualServerModelValidator;
    @Resource
    private SlbValidator slbModelValidator;
    @Resource
    private SlbQuery slbQuery;
    @Resource
    private CertificateService certificateService;

    @Override
    public List<VirtualServer> listAll(Long[] vsIds) throws Exception {
        List<VirtualServer> result = new ArrayList<>();
        for (MetaVsContentDo metaVsContentDo : mVsContentDao.findAllByIds(vsIds, MVsContentEntity.READSET_FULL)) {
            result.add(ContentReaders.readVirtualServerContent(metaVsContentDo.getContent()));
        }
        return result;
    }

    @Override
    public VirtualServer getById(Long virtualServerId) throws Exception {
        MetaVsContentDo d = mVsContentDao.findById(virtualServerId, MVsContentEntity.READSET_FULL);
        return d == null ? null : ContentReaders.readVirtualServerContent(d.getContent());
    }

    @Override
    public VirtualServer addVirtualServer(Long slbId, VirtualServer virtualServer) throws Exception {
        virtualServer.setSlbId(slbId);
        for (Domain domain : virtualServer.getDomains()) {
            domain.setName(domain.getName().toLowerCase());
        }
        Set<Long> checkIds = virtualServerCriteriaQuery.queryBySlbId(virtualServer.getSlbId());
        List<VirtualServer> check = listAll(checkIds.toArray(new Long[checkIds.size()]));
        check.add(virtualServer);
        virtualServerModelValidator.validateVirtualServers(check);
        virtualServerEntityManager.addVirtualServer(virtualServer);
        if (virtualServer.getSsl().booleanValue()) {
            installCertificate(virtualServer);
        }
        return virtualServer;
    }

    @Override
    public void updateVirtualServer(VirtualServer virtualServer) throws Exception {
        VirtualServer origin = getById(virtualServer.getId());
        if (origin == null)
            throw new ValidationException("Virtual server with id " + virtualServer.getId() + " does not exist.");
        for (Domain domain : virtualServer.getDomains()) {
            domain.setName(domain.getName().toLowerCase());
        }
        Set<Long> checkIds = virtualServerCriteriaQuery.queryBySlbId(virtualServer.getSlbId());
        Map<Long, VirtualServer> check = new HashMap<>();
        for (VirtualServer vs : listAll(checkIds.toArray(new Long[checkIds.size()]))) {
            check.put(vs.getId(), vs);
        }
        if (!check.containsKey(virtualServer.getId())) {
            if (!slbModelValidator.exists(virtualServer.getSlbId())) {
                throw new ValidationException("Slb with id " + virtualServer.getSlbId() + " does not exist.");
            }
        }
        check.put(virtualServer.getId(), virtualServer);
        virtualServerModelValidator.validateVirtualServers(new ArrayList<>(check.values()));
        virtualServerEntityManager.updateVirtualServer(virtualServer);
        if (virtualServer.getSsl().booleanValue()) {
            installCertificate(virtualServer);
        }
    }

    @Override
    public void deleteVirtualServer(Long virtualServerId) throws Exception {
        virtualServerModelValidator.removable(getById(virtualServerId));
        virtualServerEntityManager.deleteVirtualServer(virtualServerId);
    }

    @Override
    public List<Long> portVirtualServerRel() throws Exception {
        List<SlbVirtualServerDo> l = slbVirtualServerDao.findAll(SlbVirtualServerEntity.READSET_FULL);
        VirtualServer[] vses = new VirtualServer[l.size()];
        for (int i = 0; i < l.size(); i++) {
            vses[i] = createVirtualServer(l.get(i));
        }
        return virtualServerEntityManager.port(vses);
    }

    @Override
    public void portVirtualServerRel(Long vsId) throws Exception {
        SlbVirtualServerDo d = slbVirtualServerDao.findByPK(vsId, SlbVirtualServerEntity.READSET_FULL);
        VirtualServer vs = createVirtualServer(d);
        virtualServerEntityManager.port(vs);
    }

    private void installCertificate(VirtualServer virtualServer) throws Exception {
        List<String> ips = slbQuery.getSlbIps(virtualServer.getSlbId());
        List<Domain> vsDomains = virtualServer.getDomains();
        String[] domains = new String[vsDomains.size()];
        for (int i = 0; i < domains.length; i++) {
            domains[i] = vsDomains.get(i).getName();
        }
        Long certId = certificateService.pickCertificate(domains);
        certificateService.command(virtualServer.getId(), ips, certId);
        certificateService.install(virtualServer.getId());
    }

    private VirtualServer createVirtualServer(SlbVirtualServerDo d) throws DalException {
        VirtualServer vs = C.toVirtualServer(d);
        querySlbDomains(d.getId(), vs);
        return vs;
    }

    private void querySlbDomains(Long slbVirtualServerId, VirtualServer virtualServer) throws DalException {
        List<SlbDomainDo> list = slbDomainDao.findAllBySlbVirtualServer(slbVirtualServerId, SlbDomainEntity.READSET_FULL);
        for (SlbDomainDo d : list) {
            virtualServer.addDomain(new Domain().setName(d.getName().toLowerCase()));
        }
    }
}
