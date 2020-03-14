package com.ctrip.zeus.service.nginx;

import com.ctrip.zeus.dao.entity.CertCertificateSlbServerR;
import com.ctrip.zeus.dao.entity.CertCertificateSlbServerRExample;
import com.ctrip.zeus.dao.mapper.CertCertificateSlbServerRMapper;
import com.ctrip.zeus.dao.mapper.CertCertificateVsRMapper;
import com.ctrip.zeus.model.Certificate;
import com.ctrip.zeus.model.model.Domain;
import com.ctrip.zeus.model.model.VirtualServer;
import com.ctrip.zeus.service.model.IdVersion;
import com.ctrip.zeus.service.model.SelectionMode;
import com.ctrip.zeus.service.model.VirtualServerRepository;
import com.ctrip.zeus.service.query.VirtualServerCriteriaQuery;
import com.ctrip.zeus.tag.TagBox;
import com.ctrip.zeus.tag.TagService;
import com.ctrip.zeus.util.CertConstants;
import com.ctrip.zeus.util.CertUtil;
import com.google.common.base.Joiner;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Discription
 **/
@Component("certVsServerHandler")
public class CertificateHelper {

    @Resource
    private CertCertificateVsRMapper certificateVsRMapper;
    @Resource
    private CertCertificateSlbServerRMapper certificateSlbServerRMapper;
    @Resource
    private TagBox tagBox;
    @Resource
    private TagService tagService;
    @Resource
    private VirtualServerCriteriaQuery virtualServerCriteriaQuery;
    @Resource
    private VirtualServerRepository virtualServerRepository;

    public Map<Long, Set<Long>> queryVsIdsForCerts(List<Long> certIds) {
        if (CollectionUtils.isEmpty(certIds)) {
            return new HashMap<>();
        }

        Map<Long, Set<Long>> result = new HashMap<>(certIds.size());
        List<CertCertificateSlbServerR> records = certificateSlbServerRMapper.selectByExampleSelective(
                new CertCertificateSlbServerRExample().createCriteria().andCertIdIn(certIds).example(),
                CertCertificateSlbServerR.Column.vsId, CertCertificateSlbServerR.Column.certId);
        for (CertCertificateSlbServerR record : records) {
            result.putIfAbsent(record.getCertId(), new HashSet<>());
            result.get(record.getCertId()).add(record.getVsId());
        }
        return result;
    }

    public Map<Long, Set<String>> querySLBServersForCerts(List<Long> certIds) {
        if (CollectionUtils.isEmpty(certIds)) {
            return new HashMap<>();
        }

        Map<Long, Set<String>> result = new HashMap<>(certIds.size());
        List<CertCertificateSlbServerR> records = certificateSlbServerRMapper.selectByExampleSelective(
                new CertCertificateSlbServerRExample().createCriteria().andCertIdIn(certIds).example(),
                CertCertificateSlbServerR.Column.certId, CertCertificateSlbServerR.Column.ip
        );
        for (CertCertificateSlbServerR record: records) {
            result.putIfAbsent(record.getCertId(), new HashSet<>());
            result.get(record.getCertId()).add(record.getIp());
        }
        return result;
    }

    public void fillVsesAndServers(List<Certificate> certificates) {
        if (CollectionUtils.isEmpty(certificates)) {
            return;
        }
        List<Long> certIds = new ArrayList<>(certificates.size());
        for (Certificate certificate: certificates) {
            certIds.add(certificate.getId());
        }
        Map<Long, Set<Long>> vsIdsByCertId = queryVsIdsForCerts(certIds);
        Map<Long, Set<String>> serversByCertId = querySLBServersForCerts(certIds);

        for (Certificate certificate : certificates) {
            Long id = certificate.getId();
            Set<Long> vsIds = vsIdsByCertId.containsKey(id) ? vsIdsByCertId.get(id) : new HashSet<>();
            Set<String> slbServerIps = serversByCertId.containsKey(id) ? serversByCertId.get(id) : new HashSet<>();
            certificate.setVsIds(new ArrayList<>(vsIds));
            certificate.setSlbServers(new ArrayList<>(slbServerIps));
        }
        }

    public List<Long> getVsIdsByCertId(Long certId) {
        Map<Long, Set<Long>> certVsMap = queryVsIdsForCerts(Collections.singletonList(certId));
        return certVsMap.containsKey(certId) ? new ArrayList<>(certVsMap.get(certId)) : new ArrayList<>();
    }

    public List<String> getSlbServersByCertId(Long certId) {
        Map<Long, Set<String>> certServerMap = querySLBServersForCerts(Collections.singletonList(certId));
        return certServerMap.containsKey(certId) ? new ArrayList<>(certServerMap.get(certId)) : new ArrayList<>();
    }

    public String taggingDomain(Long certId, Long vsId) throws Exception {
        // Since certificate may be activated before vs is activated, thus offline version of vs is desired.
        String domain = offlineDomainOfVs(vsId);

        if (domain != null) {
            String newTag = CertUtil.buildTagOf(domain);
            for (String existingTag : tagService.getTags(CertConstants.ITEM_TYPE, certId)) {
                if (existingTag != null && existingTag.equalsIgnoreCase(newTag)) {
                    return null;
                }
            }

            String tag = CertUtil.buildTagOf(domain);
            // Since multiple vs can use same certificate, thus removal of existed domain-tags is not needed.
            tagBox.tagging(tag, CertConstants.ITEM_TYPE, new Long[]{certId});

            return tag;
        }

        return null;
    }

    private String offlineDomainOfVs(Long vsId) throws Exception {
        if (vsId == null) {
            return null;
        }
        IdVersion[] vsKeys = virtualServerCriteriaQuery.queryByIdAndMode(vsId, SelectionMode.OFFLINE_EXCLUSIVE);

        if (vsKeys != null && vsKeys.length == 1) {
            VirtualServer virtualServer = virtualServerRepository.getByKey(vsKeys[0]);
            List<Domain> domainInstances = virtualServer.getDomains();
            if (domainInstances.size() == 0) {
                return null;
            }
            List<String> domains = domainInstances.stream().map(Domain::getName).collect(Collectors.toList());

            return Joiner.on("|").join(domains);
        }
        return null;
    }
}
