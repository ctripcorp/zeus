package com.ctrip.zeus.service;

import com.ctrip.zeus.model.Certificate;

import java.io.InputStream;
import java.util.Date;
import java.util.List;

/**
 * @Discription
 **/
public interface CertificateResourceService {

    /*
     * @Description
     * @return: certId of inserted db record
     **/
    Long add(String cert, String key, List<String> domains, String cid) throws Exception;

    /**
     *
     * @param withBlobs: results contain cert and key data if specified to be true
     * @param certExpire: cert's expire time
     * @param domains: every String is of format aaa.com|bbb.com that specify the domains of the certificate when adding the certificate
     * @return
     * @throws Exception
     */
    List<Certificate> all(Boolean withBlobs, Date certExpire, List<String> domains);

    Certificate get(Long certId, Boolean withBlobs);

    List<Certificate> batchGet(List<Long> ids);

    /*
     * @Description: insert record into table with id. Other behaviours are same with @add method
     * @return
     **/
    void sync(InputStream cert, InputStream key, List<String> domains, String cid, Long certId) throws Exception;

    int deleteByPrimaryKey(Long certId);
}
