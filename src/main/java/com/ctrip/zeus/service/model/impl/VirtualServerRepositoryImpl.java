package com.ctrip.zeus.service.model.impl;

import com.ctrip.zeus.dal.core.ArchiveVsDao;
import com.ctrip.zeus.dal.core.ArchiveVsEntity;
import com.ctrip.zeus.dal.core.MetaVsArchiveDo;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.Domain;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.service.model.*;
import com.ctrip.zeus.service.model.common.ValidationContext;
import com.ctrip.zeus.service.model.handler.SlbQuery;
import com.ctrip.zeus.service.model.validation.VirtualServerValidator;
import com.ctrip.zeus.service.model.handler.impl.ContentReaders;
import com.ctrip.zeus.service.model.handler.impl.VirtualServerEntityManager;
import com.ctrip.zeus.service.nginx.CertificateService;
import com.ctrip.zeus.service.query.VirtualServerCriteriaQuery;
import com.dianping.cat.Cat;
import com.google.common.base.Joiner;
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
    private ValidationFacade validationFacade;
    @Resource
    private VirtualServerValidator virtualServerModelValidator;
    @Resource
    private SlbQuery slbQuery;
    @Resource
    private CertificateService certificateService;
    @Resource
    private ArchiveVsDao archiveVsDao;
    @Resource
    private AutoFiller autoFiller;

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
            try {
                VirtualServer vs = ContentReaders.readVirtualServerContent(d.getContent());
                vs.setCreatedTime(d.getDateTimeLastChange());
                autoFiller.autofill(vs);
                result.add(vs);
            } catch (Exception e) {
            }
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
        if (d == null) {
            return null;
        }

        VirtualServer vs = ContentReaders.readVirtualServerContent(d.getContent());
        vs.setCreatedTime(d.getDateTimeLastChange());
        autoFiller.autofill(vs);
        return vs;
    }

    @Override
    public VirtualServer add(VirtualServer virtualServer) throws Exception {
        virtualServer.setId(0L);
        // keep slb-id field for history record
        virtualServer.setSlbId(null);
        ValidationContext context = new ValidationContext();
        validationFacade.validateVs(virtualServer, context);
        if (context.getErrorVses().contains(virtualServer.getId())) {
            throw new ValidationException(context.getVsErrorReason(virtualServer.getId()));
        }
        autoFiller.autofill(virtualServer);
        virtualServerEntityManager.add(virtualServer);
        if (virtualServer.isSsl()) {
            installCertificate(virtualServer);
        }
        return virtualServer;
    }

    @Override
    public VirtualServer update(VirtualServer virtualServer) throws Exception {
        // keep slb-id field for history record
        virtualServer.setSlbId(null);
        virtualServerModelValidator.checkRestrictionForUpdate(virtualServer);
        ValidationContext context = new ValidationContext();
        validationFacade.validateVs(virtualServer, context);
        if (context.getErrorVses().contains(virtualServer.getId())) {
            throw new ValidationException(context.getVsErrorReason(virtualServer.getId()));
        }
        autoFiller.autofill(virtualServer);
        virtualServerEntityManager.update(virtualServer);

        if (virtualServer.isSsl()) {
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
        List<String> ips = new ArrayList<>();
        for (Long slbId : virtualServer.getSlbIds()) {
            ips.addAll(slbQuery.getSlbIps(slbId));
        }
        List<Domain> vsDomains = virtualServer.getDomains();
        String[] domains = new String[vsDomains.size()];
        for (int i = 0; i < domains.length; i++) {
            domains[i] = vsDomains.get(i).getName();
        }
        String dd = Joiner.on("|").join(domains);
        Long certId = certificateService.getCertificateOnBoard(dd);
        certificateService.install(virtualServer.getId(), ips, certId, true);
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
