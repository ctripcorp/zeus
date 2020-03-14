package com.ctrip.zeus.service.model.impl;

import com.ctrip.zeus.dao.entity.SlbArchiveVs;
import com.ctrip.zeus.dao.entity.SlbArchiveVsExample;
import com.ctrip.zeus.dao.mapper.SlbArchiveVsMapper;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.model.Domain;
import com.ctrip.zeus.model.model.Rule;
import com.ctrip.zeus.model.model.VirtualServer;
import com.ctrip.zeus.service.SmartArchiveVsMapper;
import com.ctrip.zeus.service.model.*;
import com.ctrip.zeus.service.model.common.ValidationContext;
import com.ctrip.zeus.service.model.handler.impl.ContentReaders;
import com.ctrip.zeus.service.model.handler.impl.VirtualServerEntityManager;
import com.ctrip.zeus.service.model.validation.VirtualServerValidator;
import com.ctrip.zeus.service.nginx.CertificateService;
import com.ctrip.zeus.service.query.VirtualServerCriteriaQuery;
import com.google.common.base.Joiner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

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
    @Autowired
    private CertificateService certificateService;
    @Resource
    private SlbArchiveVsMapper slbArchiveVsMapper;
    @Resource
    private AutoFiller autoFiller;
    @Resource
    private EntityFactory entityFactory;
    @Resource
    private SmartArchiveVsMapper smartArchiveVsMapper;

    @Override
    public List<VirtualServer> listAll(Long[] vsIds) throws Exception {
        Set<IdVersion> keys = virtualServerCriteriaQuery.queryByIdsAndMode(vsIds, SelectionMode.OFFLINE_FIRST);
        return listAll(keys.toArray(new IdVersion[keys.size()]));
    }

    @Override
    public List<VirtualServer> listAll(IdVersion[] keys) throws Exception {
        List<VirtualServer> result = new ArrayList<>();
        if (keys==null || keys.length == 0) return result;

        Integer[] hashes = new Integer[keys.length];
        String[] values = new String[keys.length];
        for (int i = 0; i < hashes.length; i++) {
            hashes[i] = keys[i].hashCode();
            values[i] = keys[i].toString();
        }

        for (SlbArchiveVs d : smartArchiveVsMapper.findAllByIdVersion(Arrays.asList(hashes), Arrays.asList(values))) {
            try {
                VirtualServer vs = ContentReaders.readVirtualServerContent(d.getContent());
                vs.setCreatedTime(d.getDatetimeLastchange());
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
        if (key.length == 0) return null;
        return getByKey(key[0]);
    }

    @Override
    public VirtualServer getByKey(IdVersion key) throws Exception {
        SlbArchiveVs d = slbArchiveVsMapper.selectOneByExampleWithBLOBs(new SlbArchiveVsExample().createCriteria().andVsIdEqualTo(key.getId()).andVersionEqualTo(key.getVersion()).example());
        if (d == null) {
            return null;
        }

        VirtualServer vs = ContentReaders.readVirtualServerContent(d.getContent());
        vs.setCreatedTime(d.getDatetimeLastchange());
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
        return updateVirtualServer(virtualServer, true);
    }

    @Override
    public VirtualServer updateVirtualServerRule(VirtualServer virtualServer) throws Exception {
        return updateVirtualServer(virtualServer, false);
    }

    @Override
    public void delete(Long virtualServerId) throws Exception {
        virtualServerModelValidator.removable(virtualServerId);
        virtualServerEntityManager.delete(virtualServerId);
    }

    @Override
    public void installCertificate(VirtualServer virtualServer) throws Exception {
        List<Domain> vsDomains = virtualServer.getDomains();
        if (vsDomains.size() == 0) {
            return;
        }

        //In case of cert has been already installed.
        Long certId = certificateService.getActivatedCertIdByVsId(virtualServer.getId());
        if (certId != null && certId > 0) {
            certificateService.activateCertificate(virtualServer.getId(), certId, true);
            return;
        }

        //In case of cert first time install. Throw Exception if not found matched cert. install cert to disk in case of found matched cert.
        String[] domains = new String[vsDomains.size()];
        for (int i = 0; i < domains.length; i++) {
            domains[i] = vsDomains.get(i).getName();
        }
        String dd = Joiner.on("|").join(domains);
        certId = certificateService.getCertificateOnBoard(dd);
        certificateService.activateCertificate(virtualServer.getId(), certId, true);
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

    private VirtualServer updateVirtualServer(VirtualServer virtualServer, boolean escapeRuleSet) throws Exception {
        // keep slb-id field for history record
        virtualServer.setSlbId(null);
        virtualServerModelValidator.checkRestrictionForUpdate(virtualServer);
        ValidationContext context = new ValidationContext();
        validationFacade.validateVs(virtualServer, context);
        if (context.getErrorVses().contains(virtualServer.getId())) {
            throw new ValidationException(context.getVsErrorReason(virtualServer.getId()));
        }
        autoFiller.autofill(virtualServer);
        if (escapeRuleSet) {
            syncVsRules(virtualServer);
        }
        virtualServerEntityManager.update(virtualServer);

        if (virtualServer.isSsl()) {
            installCertificate(virtualServer);
        }
        return virtualServer;
    }

    private void syncVsRules(VirtualServer vs) throws Exception {
        Long vsId = vs.getId();
        ModelStatusMapping<VirtualServer> virtualServerModelStatusMapping = entityFactory.getVsesByIds(new Long[]{vsId});
        VirtualServer offline = virtualServerModelStatusMapping.getOfflineMapping().get(vsId);
        if (offline == null) {
            throw new ValidationException("Vs does not has offline version. VsId:" + vsId);
        }
        List<Rule> offlineRules = offline.getRuleSet();
        vs.getRuleSet().clear();
        vs.getRuleSet().addAll(offlineRules);
    }
}
