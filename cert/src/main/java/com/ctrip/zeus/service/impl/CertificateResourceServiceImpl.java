package com.ctrip.zeus.service.impl;

import com.ctrip.zeus.dao.entity.CertNewCertificate;
import com.ctrip.zeus.dao.entity.CertNewCertificateExample;
import com.ctrip.zeus.dao.mapper.CertNewCertificateMapper;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.Certificate;
import com.ctrip.zeus.model.Property;
import com.ctrip.zeus.service.CertificateResourceService;
import com.ctrip.zeus.tag.PropertyBox;
import com.ctrip.zeus.tag.PropertyService;
import com.ctrip.zeus.tag.TagBox;
import com.ctrip.zeus.tag.TagService;
import com.ctrip.zeus.util.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Discription: service class for cert_new_certificate table
 **/
@Service("certificateResourceService")
public class CertificateResourceServiceImpl implements CertificateResourceService {

    @Resource
    private CertNewCertificateMapper certNewCertificateMapper;
    @Resource
    private TagBox tagBox;
    @Resource
    private PropertyBox propertyBox;
    @Resource
    private TagService tagService;
    @Resource
    private PropertyService propertyService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public Long add(String cert, String key, List<String> domains, String cid) throws Exception {
        CertUtil.CertWrapper wrapper = CertUtil.getCertWrapper(new ByteArrayInputStream(cert.getBytes()));

        Date issueTime = wrapper.getIssueTime();
        Date expireTime = wrapper.getExpireTime();

        CertNewCertificate record = toCertNewCertificate(buildCertificate(cert, key, issueTime, expireTime));
        certNewCertificateMapper.insert(record);

        if (domains != null && domains.size() > 0) {
            domains = domains.stream().filter(domain -> !Strings.isNullOrEmpty(domain)).collect(Collectors.toList());
            domains.sort(String::compareTo);

            // add domains as certificate's tags
            String tag = CertUtil.buildTagOf(domains);
            logger.info("Add domain tag: " + tag);
            tagBox.tagging(tag, CertConstants.ITEM_TYPE, new Long[]{record.getId()});
        }

        // add cid as certificate's property
        if (!Strings.isNullOrEmpty(cid)) {
            boolean result = propertyBox.set(CertConstants.CID_PROPERTY_NAME, cid, CertConstants.ITEM_TYPE, record.getId());
            logger.info("set cid property result : " + result);
        }

        return record.getId();
    }

    @Override
    public List<Certificate> all(Boolean withBlobs, Date certExpire, List<String> domains) {
        CertNewCertificateExample example = new CertNewCertificateExample();
        CertNewCertificateExample.Criteria criteria = example.createCriteria();

        // filter through domains
        if (!CollectionUtils.isEmpty(domains)) {
            List<String> tags = new ArrayList<>(domains.size());
            for (String domain: domains) {
                tags.add(CertUtil.buildTagOf(domain));
            }
            try {
                Set<Long> candidates = tagService.joinQuery(tags, CertConstants.ITEM_TYPE);
                if (!CollectionUtils.isEmpty(candidates)) {
                    criteria.andIdIn(new ArrayList<>(candidates));
                }
            } catch (Exception e) {
                logger.warn("Exception when query domain tags for cert. Msg: " + e.getMessage());
            }
        }
        // filter by expire time
        if (certExpire != null) {
            criteria.andExpireTimeLessThanOrEqualTo(certExpire);
        }
        List<CertNewCertificate> records = certNewCertificateMapper.selectByExampleWithBLOBs(example);

        List<Certificate> certificates = records.stream().map(CertificateResourceServiceImpl::toCertificate).filter(Objects::nonNull).collect(Collectors.toList());

        fillDomainsAndCid(certificates);
        if (withBlobs == null || !withBlobs) {
            certificates.forEach(certificate -> {
                certificate.setKeyData(null);
                certificate.setCertData(null);
            });
        }

        return certificates;
    }

    @Override
    public Certificate get(Long certId, Boolean withBlobs) {
        if (certId != null) {
            Certificate certificate = toCertificate(certNewCertificateMapper.selectByPrimaryKey(certId));
            if (certificate == null) {
                return null;
            }
            if (withBlobs == null || !withBlobs) {
                certificate.setCertData(null);
                certificate.setKeyData(null);
            }

            fillDomainsAndCid(Collections.singletonList(certificate));

            return certificate;
        }
        return null;
    }

    private void fillDomainsAndCid(List<Certificate> certificates) {
        if (certificates == null || certificates.size() == 0) {
            return;
        }
        List<Long> certIds = new ArrayList<>(certificates.size());
        certificates.forEach(certificate -> certIds.add(certificate.getId()));
        try {
            Map<Long, List<String>> certTagsMap = tagService.getTags(CertConstants.ITEM_TYPE, certIds.toArray(new Long[0]));
            Map<Long, Property> certCidMap = propertyService.getProperties(CertConstants.CID_PROPERTY_NAME, CertConstants.ITEM_TYPE, certIds.toArray(new Long[0]));

            for (Certificate certificate: certificates) {
                Long id = certificate.getId();
                List<String> tags = certTagsMap.get(id);
                for (String tag : tags) {
                    if (CertUtil.isCertDomainTag(tag)) {
                        certificate.setDomain(CertUtil.parseDomainOutOfTag(tag));
                        break;
                    }
                }

                if (certCidMap.containsKey(id) && certCidMap.get(id) != null) {
                    certificate.setCid(certCidMap.get(id).getValue());
                }
            }
        } catch (Exception e) {
            logger.error("Querying tags and properties throws exception. Msg: " + e.getMessage());
        }
    }

    @Override
    public List<Certificate> batchGet(List<Long> ids) {
        if (ids == null || ids.size() == 0) {
            return new ArrayList<>();
        }

        List<Certificate> certificates = new ArrayList<>(ids.size());
        List<CertNewCertificate> records = certNewCertificateMapper.selectByExampleWithBLOBs(
                CertNewCertificateExample.newAndCreateCriteria().andIdIn(ids).example());

        records.forEach(record -> certificates.add(toCertificate(record)));
        fillDomainsAndCid(certificates);
        return certificates;
    }

    @Override
    public void sync(InputStream cert, InputStream key, List<String> domains, String cid, Long certId) throws Exception {
        if (cert == null || key == null) {
            throw new ValidationException("Cert file and key file must be provided");
        }
        if (certId == null) {
            throw new ValidationException("Cert id must be provided");
        }

        String certContent = IOUtils.inputStreamStringify(cert);
        String keyContent = IOUtils.inputStreamStringify(key);

        CertUtil.CertWrapper wrapper = CertUtil.getCertWrapper(new ByteArrayInputStream(certContent.getBytes()));
        Date issueTime = wrapper.getIssueTime();
        Date expireTime = wrapper.getExpireTime();

        CertNewCertificate record = toCertNewCertificate(buildCertificate(certContent, keyContent, issueTime, expireTime));
        record.setId(certId);
        certNewCertificateMapper.insertWithId(record);

        domains = domains.stream().filter(domain -> !Strings.isNullOrEmpty(domain)).collect(Collectors.toList());
        domains.sort(String::compareTo);

        // add domains as certificate's tags
        String tag = CertUtil.buildTagOf(domains);
        logger.info("Add domain tag: " + tag);
        tagBox.tagging(tag, CertConstants.ITEM_TYPE, new Long[]{record.getId()});

        // add cid as certificate's property
        if (cid != null) {
            propertyBox.set(CertConstants.CID_PROPERTY_NAME, cid, CertConstants.ITEM_TYPE, record.getId());
        }
    }

    @Override
    public int deleteByPrimaryKey(Long certId) {
        if (certId != null) {
            Certificate certificate = get(certId, false);
            if (certificate == null) {
                return 0;
            }
            String domain = certificate.getDomain();
            int result = certNewCertificateMapper.deleteByPrimaryKey(certId);

            tagBox.untagging(CertUtil.buildTagOf(domain), CertConstants.ITEM_TYPE, new Long[]{certId});

            return result;
        }

        return 0;
    }

    public static Certificate buildCertificate(String certContent, String keyContent, Date issueTime, Date expireTime) {
        Certificate certificate = new Certificate();

        certificate.setCertData(certContent);
        certificate.setKeyData(keyContent);
        certificate.setIssueTime(issueTime);
        certificate.setExpireTime(expireTime);

        return certificate;
    }

    public static CertNewCertificate toCertNewCertificate(Certificate certificate) throws JsonProcessingException {
        return CertNewCertificate.builder().
                issueTime(certificate.getIssueTime()).
                expireTime(certificate.getExpireTime()).
                content(ObjectJsonWriter.write(certificate).getBytes()).
                build();
    }

    public static Certificate toCertificate(CertNewCertificate record) {
        if (record == null || record.getContent() == null) {
            return null;
        }
        Certificate result = ObjectJsonParser.parse(new String(record.getContent()), Certificate.class);
        if (result != null) {
            result.setId(record.getId());
        }
        return result;
    }
}
