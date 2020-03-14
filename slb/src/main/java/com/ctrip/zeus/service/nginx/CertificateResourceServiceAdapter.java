package com.ctrip.zeus.service.nginx;

import com.ctrip.zeus.dao.entity.CertCertificate;
import com.ctrip.zeus.dao.entity.CertCertificateWithBLOBs;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.Certificate;

import java.util.List;

/**
 * @Discription: adapter interface for cert-module CertificateService
 **/
public interface CertificateResourceServiceAdapter {

    Long getCertIdByCid(String cid) throws Exception;

    Long getCertificateOnBoard(String domain) throws Exception;

    CertCertificateWithBLOBs findCertByCid(String cid) throws Exception;

    CertCertificateWithBLOBs selectByPrimaryKey(Long certId);

    List<CertCertificateWithBLOBs> findAllByDomain(String domain);

    Long insertCertificate(CertCertificateWithBLOBs record);

    int insertCertificateWithId(CertCertificateWithBLOBs record);

    List<CertCertificateWithBLOBs> findAllByDomainAndState(String[] domains, boolean state) throws ValidationException;

    void deleteByPrimaryKey(Long certId);

    // cert module service methods
    /*
     * @Description
     * @return: certId of inserted db record
     **/
    Long add(String cert, String key, List<String> domains, String cid) throws Exception;

    Certificate get(Long certId, Boolean withBlobs) throws Exception;

    String getCidByCertId(Long certId) throws Exception;

    CertCertificate findMaxByCidAndState(String cid, boolean state) throws Exception;
}
