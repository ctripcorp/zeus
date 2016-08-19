package com.ctrip.zeus.service.model.impl;

import com.ctrip.zeus.dal.core.ArchiveVsDao;
import com.ctrip.zeus.dal.core.ArchiveVsEntity;
import com.ctrip.zeus.dal.core.MetaVsArchiveDo;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.Domain;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.service.model.SelectionMode;
import com.ctrip.zeus.service.model.VirtualServerRepository;
import com.ctrip.zeus.service.model.handler.SlbQuery;
import com.ctrip.zeus.service.model.handler.SlbValidator;
import com.ctrip.zeus.service.model.handler.VirtualServerValidator;
import com.ctrip.zeus.service.model.handler.impl.ContentReaders;
import com.ctrip.zeus.service.model.handler.impl.VirtualServerEntityManager;
import com.ctrip.zeus.service.nginx.CertificateService;
import com.ctrip.zeus.service.model.IdVersion;
import com.ctrip.zeus.service.query.VirtualServerCriteriaQuery;
import org.springframework.stereotype.Component;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

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
    private VirtualServerValidator virtualServerModelValidator;
    @Resource
    private SlbValidator slbModelValidator;
    @Resource
    private SlbQuery slbQuery;
    @Resource
    private CertificateService certificateService;
    @Resource
    private ArchiveVsDao archiveVsDao;

    @Override
    public List<VirtualServer> listAll(Long[] vsIds) throws Exception {
        Set<IdVersion> keys = virtualServerCriteriaQuery.queryByIdsAndMode(vsIds, SelectionMode.OFFLINE_FIRST);
        return listAll(keys.toArray(new IdVersion[keys.size()]));
    }

    @Override
    public List<VirtualServer> listAll(IdVersion[] keys) throws Exception {
        List<VirtualServer> result = new ArrayList<>();
        Integer[] hashes = new Integer[keys.length];
        String[] values = new String[keys.length];
        for (int i = 0; i < hashes.length; i++) {
            hashes[i] = keys[i].hashCode();
            values[i] = keys[i].toString();
        }
        for (MetaVsArchiveDo d : archiveVsDao.findAllByIdVersion(hashes, values, ArchiveVsEntity.READSET_FULL)) {
            VirtualServer vs = ContentReaders.readVirtualServerContent(d.getContent());
            result.add(vs);
        }
        return result;
    }

    @Override
    public VirtualServer getById(Long vsId) throws Exception {
        IdVersion[] key = virtualServerCriteriaQuery.queryByIdAndMode(vsId, SelectionMode.OFFLINE_FIRST);
        return getByKey(key[0]);
    }

    @Override
    public VirtualServer getByKey(IdVersion key) throws Exception {
        MetaVsArchiveDo d = archiveVsDao.findByVsAndVersion(key.getId(), key.getVersion(), ArchiveVsEntity.READSET_FULL);
        return d == null ? null : ContentReaders.readVirtualServerContent(d.getContent());
    }

    @Override
    public VirtualServer add(Long slbId, VirtualServer virtualServer) throws Exception {
        if (!slbModelValidator.exists(slbId)) {
            throw new ValidationException("Slb with id " + slbId + "does not exits.");
        }

        virtualServer.setSlbId(slbId);
        for (Domain domain : virtualServer.getDomains()) {
            domain.setName(domain.getName().toLowerCase());
        }
        Set<Long> retained = new HashSet<>();
        for (IdVersion idVersion : virtualServerCriteriaQuery.queryBySlbId(slbId)) {
            retained.add(idVersion.getId());
        }
        Set<IdVersion> keys = virtualServerCriteriaQuery.queryByIdsAndMode(retained.toArray(new Long[retained.size()]), SelectionMode.REDUNDANT);
        List<VirtualServer> check = listAll(keys.toArray(new IdVersion[keys.size()]));
        check.add(virtualServer);
        virtualServerModelValidator.validateVirtualServers(check);
        virtualServerEntityManager.add(virtualServer);

        if (virtualServer.getSsl()) {
            installCertificate(virtualServer);
        }
        return virtualServer;
    }

    @Override
    public VirtualServer update(VirtualServer virtualServer) throws Exception {
        if (!virtualServerModelValidator.exists(virtualServer.getId())) {
            throw new ValidationException("Virtual server with id " + virtualServer.getId() + " does not exist.");
        }
        if (!slbModelValidator.exists(virtualServer.getSlbId())) {
            throw new ValidationException("Slb with id " + virtualServer.getSlbId() + "does not exits.");
        }

        Set<String> domains = new HashSet<>();
        for (Domain domain : virtualServer.getDomains()) {
            domains.add(domain.getName().toLowerCase());
        }
        virtualServer.getDomains().clear();
        for (String d : domains) {
            virtualServer.getDomains().add(new Domain().setName(d));
        }

        Set<Long> retained = new HashSet<>();
        for (IdVersion idVersion : virtualServerCriteriaQuery.queryBySlbId(virtualServer.getSlbId())) {
            retained.add(idVersion.getId());
        }
        if (retained.size() == 0) {
            if (!slbModelValidator.exists(virtualServer.getSlbId())) {
                throw new ValidationException("Slb with id " + virtualServer.getSlbId() + " does not exist.");
            }
        }
        Set<IdVersion> keys = virtualServerCriteriaQuery.queryByIdsAndMode(retained.toArray(new Long[retained.size()]), SelectionMode.REDUNDANT);
        List<VirtualServer> check = listAll(keys.toArray(new IdVersion[keys.size()]));
        Iterator<VirtualServer> iter = check.iterator();
        while (iter.hasNext()) {
            VirtualServer c = iter.next();
            if (c.getId().equals(virtualServer.getId())) {
                iter.remove();
            }
        }
        check.add(virtualServer);
        virtualServerModelValidator.validateVirtualServers(check);
        virtualServerEntityManager.update(virtualServer);

        if (virtualServer.getSsl().booleanValue()) {
            installCertificate(virtualServer);
        }
        return virtualServer;
    }

    @Override
    public void delete(Long virtualServerId) throws Exception {
        virtualServerModelValidator.removable(virtualServerId);
        virtualServerEntityManager.delete(virtualServerId);
    }

    @Override
    public void installCertificate(VirtualServer virtualServer) throws Exception {
        List<String> ips = slbQuery.getSlbIps(virtualServer.getSlbId());
        List<Domain> vsDomains = virtualServer.getDomains();
        String[] domains = new String[vsDomains.size()];
        for (int i = 0; i < domains.length; i++) {
            domains[i] = vsDomains.get(i).getName();
        }
        Long certId = certificateService.getCertificateOnBoard(domains);
        certificateService.install(virtualServer.getId(), ips, certId);
    }

    @Override
    public void updateStatus(IdVersion[] vses, SelectionMode state) throws Exception {
        switch (state) {
            case ONLINE_EXCLUSIVE:
                List<VirtualServer> result = new ArrayList<>();
                for (int i = 0; i < vses.length; i++) {
                    if (vses[i].getVersion() == 0) {
                        result.add(new VirtualServer().setId(vses[i].getId()).setVersion(vses[i].getVersion()));
                    }
                }
                result.addAll(listAll(vses));
                virtualServerEntityManager.updateStatus(result);
                return;
            default:
                throw new NotImplementedException();
        }
    }

    @Override
    public void updateStatus(IdVersion[] vses) throws Exception {
        updateStatus(vses, SelectionMode.ONLINE_EXCLUSIVE);
    }
}
