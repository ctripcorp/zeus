package com.ctrip.zeus.service.nginx.impl;

import com.ctrip.zeus.dao.entity.CertCertificate;
import com.ctrip.zeus.dao.entity.CertCertificateWithBLOBs;
import com.ctrip.zeus.exceptions.NotFoundException;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.Certificate;
import com.ctrip.zeus.service.CertificateResourceService;
import com.ctrip.zeus.service.model.VirtualServerRepository;
import com.ctrip.zeus.service.nginx.CertificateHelper;
import com.ctrip.zeus.service.nginx.CertificateResourceServiceAdapter;
import com.ctrip.zeus.service.query.VirtualServerCriteriaQuery;
import com.ctrip.zeus.support.C;
import com.ctrip.zeus.tag.PropertyService;
import com.ctrip.zeus.tag.TagBox;
import com.ctrip.zeus.tag.TagService;
import com.ctrip.zeus.util.CertConstants;
import com.ctrip.zeus.util.CertUtil;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Discription
 **/
@Service("certificateResourceServiceAdapter")
public class CertificateResourceServiceAdapterImpl implements CertificateResourceServiceAdapter {

    // cert-module provided interface
    @Resource
    private CertificateResourceService certificateResourceService;
    @Resource
    private TagBox tagBox;
    @Resource
    private TagService tagService;
    @Resource
    private PropertyService propertyService;
    @Resource
    private VirtualServerCriteriaQuery virtualServerCriteriaQuery;
    @Resource
    private VirtualServerRepository virtualServerRepository;
    @Resource
    private CertificateHelper certificateHelper;

    private final Logger logger = LoggerFactory.getLogger(CertificateResourceServiceAdapter.class);

    @Override
    public Long getCertIdByCid(String cid) throws Exception {
        List<Long> certIds = propertyService.queryTargets(CertConstants.CID_PROPERTY_NAME, cid, CertConstants.ITEM_TYPE);

        if (certIds.size() > 1) {
            throw new IllegalStateException("Multiple certids found by cid: " + cid);
        } else if (CollectionUtils.isEmpty(certIds)) {
            return null;
        }

        return certIds.get(0);
    }

    /*
     * @Description: get certificate's id by domain
     * Return the one with biggest certId if multiple certificates found.
     * Or throws exception when no certificate is found.
     * @Param domain: a string of format 'domain1|domain2|domain3'
     * @return
     **/
    @Override
    public Long getCertificateOnBoard(String domain) throws Exception {
        if (Strings.isNullOrEmpty(domain)) {
            throw new ValidationException("Domain can not be null or empty when looking for certificate. ");
        }
        domain = CertUtil.standardizeDomain(domain);

        String tag = CertUtil.buildTagOf(domain);

        List<Long> targets = tagService.query(tag, CertConstants.ITEM_TYPE);
        if (targets == null || targets.size() == 0) {
            throw new ValidationException("Cannot find corresponding certificate referring domain " + domain + ".");
        }

        Long[] certIds = targets.toArray(new Long[0]);
        Arrays.sort(certIds);

        return certIds[certIds.length - 1];
    }

    /*
     * @Description: return certificate or throw NotFoundException
     * @return
     **/
    @Override
    public CertCertificateWithBLOBs findCertByCid(String cid) throws Exception {
        Long certId = getCertIdByCid(cid);

        if (certId != null) {
            return selectByPrimaryKey(certId);
        }

        throw new NotFoundException("Not Found Cid Data.Please Load Cert First.CID:" + cid);
    }

    /*
     * @Description: use in anywhere that certmapper's selectByPrimaryKey method is called
     * @return
     **/
    @Override
    public CertCertificateWithBLOBs selectByPrimaryKey(Long certId) {
        Certificate certificate = certificateResourceService.get(certId, true);

        return C.toCertCertificateWithBlobs(certificate);
    }

    @Override
    public List<CertCertificateWithBLOBs> findAllByDomain(String domain) {
        String tag = CertUtil.buildTagOf(domain);

        List<Long> targets = tagService.query(tag, CertConstants.ITEM_TYPE);
        if (targets == null || targets.size() == 0) {
            return new ArrayList<>();
        }

        return targets.stream().map(this::selectByPrimaryKey).collect(Collectors.toList());
    }

    @Override
    public Long insertCertificate(CertCertificateWithBLOBs record) {
        if (record == null) {
            return null;
        }

        List<String> domains = new ArrayList<>();
        if (record.getDomain() != null) {
            domains.addAll(Arrays.asList(CertUtil.splitDomain(record.getDomain())));
        }

        if (record.getCert() == null || record.getKey() == null) {
            logger.warn("cert and key column can not be null");
            return null;
        }
        try {
            return certificateResourceService.add(new String(record.getCert()), new String(record.getKey()), domains, record.getCid());
        } catch (Exception e) {
            logger.warn("exception happens when adding certificate. message: " + e.getMessage());
            return null;
        }
    }

    @Override
    public int insertCertificateWithId(CertCertificateWithBLOBs record) {
        if (record == null) {
            return 0;
        }

        List<String> domains = new ArrayList<>();
        if (record.getDomain() != null) {
            domains.addAll(Arrays.asList(CertUtil.splitDomain(record.getDomain())));
        }

        if (record.getCert() == null || record.getKey() == null) {
            logger.warn("cert and key column can not be null");
            return 0;
        }

        if (record.getId() == null) {
            logger.warn("cert id must be included when calling insertCertificateWithId. ");
            return 0;
        }
        try {
            certificateResourceService.sync(new ByteArrayInputStream(record.getCert()), new ByteArrayInputStream(record.getKey()), domains, record.getCid(), record.getId());
        } catch (Exception e) {
            logger.warn("exception happens when adding certificate with id. message: " + e.getMessage());
            return 0;
        }
        return 1;
    }

    @Override
    public List<CertCertificateWithBLOBs> findAllByDomainAndState(String[] domains, boolean state) throws ValidationException {
        if (!state) {
            throw new ValidationException("certificate's state can not be false. ");
        }
        // For default, state is all true for all certificate
        if (domains == null || domains.length == 0) {
            return new ArrayList<>();
        }

        List<String> tags = Arrays.stream(domains).map(CertUtil::buildTagOf).collect(Collectors.toList());

        try {
            Set<Long> certIds = tagService.unionQuery(tags, CertConstants.ITEM_TYPE);
            return certIds.stream().map(this::selectByPrimaryKey).collect(Collectors.toList());
        } catch (Exception e) {
            logger.warn("exception happens when call tagService.unionQuery. message: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public Long add(String cert, String key, List<String> domains, String cid) throws Exception {
        return certificateResourceService.add(cert, key, domains, cid);
    }

    @Override
    public Certificate get(Long certId, Boolean withBlobs) throws Exception {
        return certificateResourceService.get(certId, withBlobs);
    }

    @Override
    public void deleteByPrimaryKey(Long certId) {
        certificateResourceService.deleteByPrimaryKey(certId);
    }

    @Override
    public String getCidByCertId(Long certId){
        try {
            return propertyService.getPropertyValue(CertConstants.CID_PROPERTY_NAME, certId, CertConstants.ITEM_TYPE, null);
        } catch (Exception e) {
            logger.warn("exception happens when trying to get cert's cid property. CertId: " + certId);
        }
        return null;
    }

    @Override
    public CertCertificate findMaxByCidAndState(String cid, boolean state) throws Exception {
        if (!state) {
            throw new ValidationException("Can not find certificate whose state is false");
        }

        Long certId = getCertIdByCid(cid);
        if (certId == null) {
            return null;
        }
        return C.toCertCertificateWithBlobs(certificateResourceService.get(certId, true));
    }
}
