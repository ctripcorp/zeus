package com.ctrip.zeus.service.nginx;

import com.ctrip.zeus.dao.entity.CertCertificateSlbServerR;
import com.ctrip.zeus.dao.entity.CertCertificateWithBLOBs;
import com.ctrip.zeus.model.Certificate;

import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * @Discription
 **/
public interface CertificateService {

    Long add(String cert, String key, List<String> domains, String cid) throws Exception;

    List<Certificate> all(Boolean withBlobs, Date certExpire, List<String> domains) throws Exception;

    /**
     * By default, related vsIds and slbServers are not included in result list
     */
    Certificate getByCertId(Long certId, Boolean withBlobs);

    Long getMaxCertIdByDomain(String domain) throws Exception;

    /**
     * By default, related vsIds and slbServers are not included in result list
     */
    List<Certificate> getByExactDomains(String[] domains, Boolean withBlobs) throws Exception;

    /*
     * @Description: randomly chosen servers to install canary certificate
     * @return: randomly chosen slb servers' ip
     **/
    List<String> installCanaryCertificate(Long vsId, Long certId, double rate) throws Exception;

    List<String> getCanaryServerIps(Long vsId, Long certId) throws Exception;

    Long activateCertificate(Long vsId, Long certId, boolean force) throws Exception;

    /*
     * @Description: return vs's activated and canary cert's ids and cert byte array if withCert is true
     * @return
     **/
    Map<Long, Map<String, Certificate>> findCert(Long[] vsIds, boolean withCert) throws Exception;

    Map<Long, Certificate> getActivatedCerts(Long[] vsIds) throws Exception;

    Long getActivatedCertIdByVsId(Long vsId) throws Exception;

    Map<Long, Map<String, Long>> getCertIdsByVsIds(Long[] vsIds) throws Exception;

    void installDefault(Long certId, List<String> ips, final boolean overwriteIfExist) throws Exception;

    void remoteInstall(final Long vsId, List<String> ips, final Certificate certificate, boolean overwriteIfExist) throws Exception;

    /**
     * For Update Slb.Refresh CertFiles. In case of add slb server.
     */
    void remoteInstall(Long slbId, List<String> ips, boolean overwriteIfExist) throws Exception;

    void uninstall(Long vsId, List<String> ips) throws Exception;

    Set<Long> getVsIdsByCertId(Long certId) throws Exception;

    Map<Long, List<Long>> batchSearchVsesByCert(List<Long> certIds);

    int insertCertSlbServerOrUpdateCert(CertCertificateSlbServerR record);

    CertCertificateWithBLOBs getDefaultCert(Long slbId) throws Exception;

    /* methods used only in ctrip internally */
    Long getCertificateOnBoard(String domain) throws Exception;

    Long loadCertificate(String domain, String cid, Long vsId) throws Exception;

    Long activateCertificate(Long vsId, String cid, boolean force) throws Exception;

    Long getCertIdByCid(String cid) throws Exception;

    Map<Long, Map<String, String>> findCID(Long[] vsIds, boolean withCert) throws Exception;

    String getActivatedCId(Long vsId) throws Exception;

    /***
     * In case of sand box install cert
     * @param cert
     * @param key
     * @param domain
     * @param state
     * @return
     * @throws Exception
     */
    Long upload(InputStream cert, InputStream key, String domain, boolean state, String cid) throws Exception;

    @Deprecated
    Long upgrade(InputStream cert, InputStream key, String domain, boolean state) throws Exception;

    /***
     * In case of sand box uninstall cert
     * @param certId
     * @throws Exception
     */
    void cleanCert(Long certId) throws Exception;

    void deleteCertByCertId(Long certId) throws Exception;

    List<Certificate> getCertByDomains(String[] domains, boolean state) throws Exception;

    List<String> canaryCertificate(Long vsId, String cid, double rate) throws Exception;

    List<String> canaryIps(Long vsId, String cid) throws Exception;

    String getCidByCertId(Long certId);

    // in case of sandbox install
    Long updateCertState(Long certId, boolean state) throws Exception;

    /* methods used only in ctrip internally */
}
