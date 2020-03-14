package com.ctrip.zeus.service.model.impl;

import com.ctrip.zeus.dao.entity.CertCertificateWithBLOBs;
import com.ctrip.zeus.dao.entity.NginxServer;
import com.ctrip.zeus.dao.entity.SlbArchiveSlb;
import com.ctrip.zeus.dao.entity.SlbArchiveSlbExample;
import com.ctrip.zeus.dao.mapper.NginxServerMapper;
import com.ctrip.zeus.dao.mapper.SlbArchiveSlbMapper;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.model.Rule;
import com.ctrip.zeus.model.model.Slb;
import com.ctrip.zeus.model.model.SlbServer;
import com.ctrip.zeus.service.build.ConfigHandler;
import com.ctrip.zeus.service.model.*;
import com.ctrip.zeus.service.model.common.ValidationContext;
import com.ctrip.zeus.service.model.handler.SlbSync;
import com.ctrip.zeus.service.model.handler.impl.ContentReaders;
import com.ctrip.zeus.service.model.validation.SlbValidator;
import com.ctrip.zeus.service.nginx.CertificateService;
import com.ctrip.zeus.service.query.SlbCriteriaQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author:xingchaowang
 * @date: 3/5/2015.
 */
@Repository("slbRepository")
public class SlbRepositoryImpl implements SlbRepository {
    @Resource
    private SlbSync slbEntityManager;
    @Resource
    private SlbCriteriaQuery slbCriteriaQuery;
    @Resource
    private SlbValidator slbModelValidator;
    @Resource
    private ValidationFacade validationFacade;
    @Resource
    private AutoFiller autoFiller;
    @Autowired
    private CertificateService certificateService;
    @Resource
    private EntityFactory entityFactory;
    @Resource
    private NginxServerMapper nginxServerMapper;
    @Resource
    private SlbArchiveSlbMapper slbArchiveSlbMapper;
    @Resource
    private ConfigHandler configHandler;

    @Override
    public List<Slb> list(Long[] slbIds) throws Exception {
        Set<IdVersion> keys = slbCriteriaQuery.queryByIdsAndMode(slbIds, SelectionMode.OFFLINE_FIRST);
        return list(keys.toArray(new IdVersion[keys.size()]));
    }

    @Override
    public List<Slb> list(IdVersion[] keys) throws Exception {
        List<Slb> result = new ArrayList<>();
        if (keys == null || keys.length == 0) return result;
        Long[] slbIds = new Long[keys.length];
        Integer[] hashes = new Integer[keys.length];
        String[] values = new String[keys.length];
        for (int i = 0; i < hashes.length; i++) {
            hashes[i] = keys[i].hashCode();
            values[i] = keys[i].toString();
            slbIds[i] = keys[i].getId();
        }
        for (SlbArchiveSlb d : slbArchiveSlbMapper.findAllByIdVersion(Arrays.asList(hashes), Arrays.asList(values))) {
            try {
                Slb slb = ContentReaders.readSlbContent(d.getContent());
                slb.setCreatedTime(d.getDatachangeLasttime());
                autoFiller.autofill(slb);
                result.add(slb);
            } catch (Exception e) {
            }
        }

        return result;
    }

    @Override
    public Slb getById(Long slbId) throws Exception {
        IdVersion[] key = slbCriteriaQuery.queryByIdAndMode(slbId, SelectionMode.OFFLINE_FIRST);
        if (key.length == 0)
            return null;
        return getByKey(key[0]);
    }

    @Override
    public Slb getByKey(IdVersion key) throws Exception {
        SlbArchiveSlb d = slbArchiveSlbMapper.selectOneByExampleWithBLOBs(new SlbArchiveSlbExample().createCriteria().andSlbIdEqualTo(key.getId()).andVersionEqualTo(key.getVersion()).example());
        if (d == null) return null;

        Slb result = ContentReaders.readSlbContent(d.getContent());
        result.setCreatedTime(d.getDatachangeLasttime());
        autoFiller.autofill(result);
        return result;
    }

    @Override
    public Slb add(Slb slb) throws Exception {
        slb.setId(0L);
        ValidationContext context = new ValidationContext();
        validationFacade.validateSlb(slb, context);
        if (context.getErrorSlbs().contains(slb.getId())) {
            throw new ValidationException(context.getSlbErrorReason(slb.getId()));
        }
        autoFiller.autofill(slb);
        slbEntityManager.add(slb);
        List<String> servers = new ArrayList<>();
        for (SlbServer slbServer : slb.getSlbServers()) {
            NginxServer nginxServer = new NginxServer();
            nginxServer.setVersion(0);
            nginxServer.setIp(slbServer.getIp());
            nginxServer.setSlbId(slb.getId());
            nginxServer.setCreatedTime(new Date());
            nginxServerMapper.insertOrUpdate(nginxServer);
            servers.add(slbServer.getIp());
        }

        if (configHandler.getEnable("install.default.certificate", true)) {
            CertCertificateWithBLOBs certificate= certificateService.getDefaultCert(slb.getId());
            if (certificate == null) {
                throw new Exception("Default certificate not found in DB.");
            }
            certificateService.installDefault(certificate.getId(), servers, false);
        }

        return slb;
    }

    @Override
    public Slb update(Slb slb) throws Exception {
        return updateSlb(slb, true);
    }


    @Override
    public int delete(Long slbId) throws Exception {
        slbModelValidator.removable(slbId);
        return slbEntityManager.delete(slbId);
    }

    @Override
    public void updateStatus(IdVersion[] slbs, SelectionMode state) throws Exception {
        switch (state) {
            case ONLINE_EXCLUSIVE:
                List<Slb> result = new ArrayList<>();
                for (int i = 0; i < slbs.length; i++) {
                    if (slbs[i].getVersion() == 0) {
                        result.add(new Slb().setId(slbs[i].getId()).setVersion(slbs[i].getVersion()));
                    }
                }
                result.addAll(list(slbs));
                slbEntityManager.updateStatus(result);
                return;
            default:
                throw new NotImplementedException();
        }
    }

    @Override
    public void updateStatus(IdVersion[] slbs) throws Exception {
        updateStatus(slbs, SelectionMode.ONLINE_EXCLUSIVE);
    }

    @Override
    public Slb updateSlbRules(Slb slb) throws Exception {
        return updateSlb(slb, false);
    }

    private Slb updateSlb(Slb slb, boolean escapeRuleSet) throws Exception {
        slbModelValidator.checkRestrictionForUpdate(slb);
        ValidationContext context = new ValidationContext();
        validationFacade.validateSlb(slb, context);
        if (context.getErrorSlbs().contains(slb.getId())) {
            throw new ValidationException(context.getSlbErrorReason(slb.getId()));
        }

        autoFiller.autofill(slb);

        if (escapeRuleSet) {
            syncSlbRules(slb);
        }

        slbEntityManager.update(slb);

        List<String> servers = new ArrayList<>();
        for (SlbServer server : slb.getSlbServers()) {
            servers.add(server.getIp());
        }

        if (configHandler.getEnable("install.default.certificate", true)) {
            certificateService.installDefault(certificateService.getDefaultCert(slb.getId()).getId(), servers, false);
        }

        certificateService.remoteInstall(slb.getId(), servers, false);

        for (SlbServer slbServer : slb.getSlbServers()) {
            NginxServer nginxServer = new NginxServer();
            nginxServer.setVersion(0);
            nginxServer.setIp(slbServer.getIp());
            nginxServer.setSlbId(slb.getId());
            nginxServer.setCreatedTime(new Date());
            nginxServerMapper.insertOrUpdate(nginxServer);
        }
        return slb;
    }

    private void syncSlbRules(Slb slb) throws Exception {
        Long slbId = slb.getId();
        ModelStatusMapping<Slb> slbModelStatusMapping = entityFactory.getSlbsByIds(new Long[]{slbId});
        Slb offline = slbModelStatusMapping.getOfflineMapping().get(slbId);
        if (offline == null) {
            throw new ValidationException("Slb does not has offline version. SlbId:" + slbId);
        }
        List<Rule> offlineRules = offline.getRuleSet();
        slb.getRuleSet().clear();
        slb.getRuleSet().addAll(offlineRules);
    }
}
