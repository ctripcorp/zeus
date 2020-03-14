package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.dao.entity.CertCertificateSlbServerR;
import com.ctrip.zeus.dao.entity.CertCertificateWithBLOBs;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.Certificate;
import com.ctrip.zeus.model.cert.VSCertficate;
import com.ctrip.zeus.model.model.Slb;
import com.ctrip.zeus.model.model.SlbServer;
import com.ctrip.zeus.restful.message.ResponseHandler;
import com.ctrip.zeus.restful.message.view.ExtendedView;
import com.ctrip.zeus.restful.message.view.ViewConstraints;
import com.ctrip.zeus.restful.message.view.ViewDecorator;
import com.ctrip.zeus.service.auth.AuthDefaultValues;
import com.ctrip.zeus.service.auth.AuthService;
import com.ctrip.zeus.service.auth.ResourceDataType;
import com.ctrip.zeus.service.auth.ResourceOperationType;
import com.ctrip.zeus.service.message.queue.MessageQueue;
import com.ctrip.zeus.service.model.IdVersion;
import com.ctrip.zeus.service.model.SelectionMode;
import com.ctrip.zeus.service.model.SlbRepository;
import com.ctrip.zeus.service.nginx.CertificateService;
import com.ctrip.zeus.service.nginx.LocalCertificateInstaller;
import com.ctrip.zeus.service.query.SlbCriteriaQuery;
import com.ctrip.zeus.service.query.VirtualServerCriteriaQuery;
import com.ctrip.zeus.support.C;
import com.ctrip.zeus.support.ObjectJsonParser;
import com.ctrip.zeus.support.ObjectJsonWriter;
import com.ctrip.zeus.util.*;
import com.google.common.base.Joiner;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by zhoumy on 2016/11/8.
 */
@Component
@Path("/")
public class CertificateResource {
    @Resource
    private SlbCriteriaQuery slbCriteriaQuery;
    @Resource
    private VirtualServerCriteriaQuery virtualServerCriteriaQuery;
    @Resource
    private SlbRepository slbRepository;
    @Autowired
    private CertificateService certificateService;
    @Resource
    private LocalCertificateInstaller localCertificateInstaller;
    @Resource
    private ResponseHandler responseHandler;
    @Resource
    private AuthService authService;
    @Resource
    private MessageQueue messageQueue;
    @Resource
    private ViewDecorator viewDecorator;

    /**
     * @api {get} /api/cert/canary: [Write] Canary Install cert
     * @apiDescription Activate slb is required to bring the cert into use.
     * @apiName InstallCertificate
     * @apiGroup Certificate
     * @apiParam {String}     cid      cid to be installed
     * @apiParam {long}     vsId        virtual server that the cert is to be installed for
     * @apiParam {double} percent        percent
     * @apiSuccess {string} message     success message
     */
    @GET
    @Path("/cert/canary")
    public Response canaryInstall(@Context HttpServletRequest request,
                                  @Context HttpHeaders hh,
                                  @QueryParam("certId") Long certId,
                                  @QueryParam("cid") String cid,
                                  @QueryParam("vsId") Long vsId,
                                  @QueryParam("percent") Double percent) throws Exception {
        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.CERT, ResourceDataType.Vs, AuthDefaultValues.ALL);
        if (vsId == null || percent == null) {
            throw new ValidationException("Query param certId, vsId, percent is required.");
        }

        if (percent > 1) {
            throw new ValidationException("Percent large then 1.");
        }

        Long actualCertId = resolveCertId(certId, cid);

        List<String> canaryIps = certificateService.installCanaryCertificate(vsId, actualCertId, percent);

        String slbMessageData = MessageUtil.getMessageBuilder(request, true).bindCertId(certId).bindIps(canaryIps.toArray(new String[canaryIps.size()])).build();
        messageQueue.produceMessage(request.getRequestURI(), vsId, slbMessageData);

        return responseHandler.handle(canaryIps, hh.getMediaType());
    }

    /**
     * @api {get} /api/cert/activate: [Write] Canary Install cert
     * @apiDescription Activate slb is required to bring the cert into use.
     * @apiName InstallCertificate
     * @apiGroup Certificate
     * @apiParam {String}     cid      cid to be installed
     * @apiParam {long}     vsId        virtual server that the cert is to be installed for
     * @apiParam {double} percent        percent
     * @apiSuccess {string} message     success message
     */
    @GET
    @Path("/cert/activate")
    public Response activateInstall(@Context HttpServletRequest request,
                                    @Context HttpHeaders hh,
                                    @QueryParam("certId") Long certId,
                                    @QueryParam("cid") String cid,
                                    @QueryParam("vsId") Long vsId,
                                    @QueryParam("force") Boolean force) throws Exception {
        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.CERT, ResourceDataType.Vs, AuthDefaultValues.ALL);
        if (vsId == null) {
            throw new ValidationException("Query param cid, vsId is required.");
        }
        if (force == null) {
            force = false;
        }

        Long actualCertId = resolveCertId(certId, cid);
        certificateService.activateCertificate(vsId, actualCertId, force);

        String slbMessageData = MessageUtil.getMessageBuilder(request, true).bindCertId(certId).build();
        messageQueue.produceMessage(request.getRequestURI(), vsId, slbMessageData);
        return responseHandler.handle("Certificates uploaded. Re-activate the virtual server to take effect.", hh.getMediaType());
    }

    /**
     * @api {get} /api/cert/rollback: [Write] rollback Canary Install cert
     * @apiDescription rollback slb is required to bring the cert into use.
     * @apiName rollback
     * @apiGroup Certificate
     * @apiParam {String}     cid      cid to be installed
     * @apiParam {long}     vsId        virtual server that the cert is to be installed for
     * @apiParam {boolean} force       force
     * @apiSuccess {string} message     success message
     */
    @GET
    @Path("/cert/rollback")
    public Response activateInstall(@Context HttpServletRequest request,
                                    @Context HttpHeaders hh,
                                    @QueryParam("cid") String cid,
                                    @QueryParam("certId") Long certId,
                                    @QueryParam("force") Boolean force,
                                    @QueryParam("vsId") Long vsId) throws Exception {
        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.CERT, ResourceDataType.Vs, AuthDefaultValues.ALL);
        if (vsId == null) {
            throw new ValidationException("Query param vsId is required.");
        }

        if (force == null) {
            force = false;
        }

        if (force) {
            certificateService.activateCertificate(vsId, resolveCertId(certId, cid), true);
        } else {
            Map<Long, Certificate> map = certificateService.getActivatedCerts(new Long[]{vsId});
            if (map.get(vsId) == null) {
                throw new ValidationException("Not Found Pre Version For Vs : " + vsId);
            }
            certificateService.activateCertificate(vsId, map.get(vsId).getId(), true);
        }

        String slbMessageData = MessageUtil.getMessageBuilder(request, true).bindCertId(certId).build();
        messageQueue.produceMessage(request.getRequestURI(), vsId, slbMessageData);
        return responseHandler.handle("Certificates uploaded. Re-activate the virtual server to take effect.", hh.getMediaType());
    }

    @GET
    @Path("/cert/vs/canary/ips")
    public Response canaryIps(@Context HttpServletRequest request,
                              @Context HttpHeaders hh,
                              @QueryParam("certId") Long certId,
                              @QueryParam("cid") String cid,
                              @QueryParam("vsId") Long vsId) throws Exception {
        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.CERT, ResourceDataType.Vs, AuthDefaultValues.ALL);
        if (vsId == null) {
            throw new ValidationException("Query param vsId is required.");
        }

        return responseHandler.handle(certificateService.getCanaryServerIps(vsId, resolveCertId(certId, cid)), hh.getMediaType());
    }

    /**
     * @api {get} /api/cert/remoteInstall: [Write] Install cert
     * @apiDescription Activate slb is required to bring the cert into use.
     * @apiName InstallCertificate
     * @apiGroup Certificate
     * @apiParam {long}     certId      cert id to be installed
     * @apiParam {long}     vsId        virtual server that the cert is to be installed for
     * @apiParam {string[]} [ip]        specify slb servers that the cert will be installed. `greyscale` is required explicitly set to true if this parameter is set.
     * @apiParam {boolean}  [greyscale] greyscale installing cert. The other parameter ip must be specified if this parameter is set to true.
     * @apiParam {boolean}  [force]     force to overwrite the cert file if exists
     * @apiSuccess {string} message     success message
     */
    @GET
    @Path("/cert/remoteInstall")
    public Response remoteInstall(@Context HttpServletRequest request,
                                  @Context HttpHeaders hh,
                                  @QueryParam("certId") Long certId,
                                  @QueryParam("vsId") Long vsId,
                                  @QueryParam("ip") List<String> greyscaleIps,
                                  @QueryParam("greyscale") Boolean greyscale,
                                  @QueryParam("force") Boolean force) throws Exception {
        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.CERT, ResourceDataType.Vs, AuthDefaultValues.ALL);
        if (certId == null || vsId == null) {
            throw new ValidationException("Query param certId, vsId is required.");
        }
        List<String> installingIps = null;

        boolean greyscaledRequired = (greyscale != null && greyscale) || (greyscaleIps != null && greyscaleIps.size() > 0);
        if (greyscaledRequired) {
            if (greyscale == null) {
                throw new ValidationException("Query param greyscale=true is required for greyscale upgrading certificate.");
            }
            if (greyscaleIps == null || greyscaleIps.size() == 0) {
                throw new ValidationException("No ips are configured for greyscale upgrading certificate.");
            }
            installingIps = greyscaleIps;
        }
        installingIps = configureIps(vsId, installingIps);

        certificateService.remoteInstall(vsId, installingIps, certificateService.getByCertId(certId, true), force != null && force);
        return responseHandler.handle("Certificates uploaded. Re-activate the virtual server to take effect.", hh.getMediaType());
    }

    /**
     * @api {get} /api/cert/default/remoteInstall: [Write] Install default cert
     * @apiDescription Activate slb is required to bring the cert into use.
     * @apiName RemoteInstallDefaultCertificate
     * @apiGroup Certificate
     * @apiParam {long}     slbId       slb that default cert is to be installed for
     * @apiParam {string}   domain      domain name of the default cert
     * @apiParam {boolean}  [force]     force to overwrite the cert file if exists
     * @apiSuccess {string} message     success message
     */
    @GET
    @Path("/cert/default/remoteInstall")
    public Response remoteInstallDefault(@Context HttpServletRequest request,
                                         @Context HttpHeaders hh,
                                         @QueryParam("slbId") Long slbId,
                                         @QueryParam("domain") String domain,
                                         @QueryParam("force") Boolean force) throws Exception {
        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.CERT, ResourceDataType.Vs, AuthDefaultValues.ALL);
        if (slbId == null) {
            throw new ValidationException("Query Param slbId is required.");
        }
        IdVersion[] searchKeys = slbCriteriaQuery.queryByIdAndMode(slbId, SelectionMode.REDUNDANT);

        Set<String> ips = new HashSet<>();
        for (Slb s : slbRepository.list(searchKeys)) {
            for (SlbServer ss : s.getSlbServers()) {
                ips.add(ss.getIp());
            }
        }
        Long certId = certificateService.getCertificateOnBoard(domain);

        certificateService.installDefault(certId, new ArrayList<>(ips), force != null && force);
        return responseHandler.handle("Successfully installed default certificate.", hh.getMediaType());
    }

    @POST
    @Path("/cert/default/localInstall")
    public Response localInstallDefault(@Context HttpServletRequest request,
                                        @Context HttpHeaders hh,
                                        @QueryParam("force") Boolean force,
                                        CertCertificateWithBLOBs cert) throws Exception {
        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.CERT, ResourceDataType.Vs, AuthDefaultValues.ALL);
        if ((force != null && force) || !localCertificateInstaller.defaultExists()) {

            if (cert == null) {
                throw new ValidationException("Must provide default cert to install");
            }
            localCertificateInstaller.installDefault(cert);
            return responseHandler.handle("Default certificate is (re-)installed. Activate slb to take effect.", hh.getMediaType());
        }
        return responseHandler.handle("Default certificate exists. No need to update.", hh.getMediaType());
    }

    @POST
    @Path("/cert/localInstall")
    public Response localInstall(@Context HttpServletRequest request,
                                 @Context HttpHeaders hh,
                                 @QueryParam("vsId") Long vsId,
                                 CertCertificateWithBLOBs certificate) throws Exception {
        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.CERT, ResourceDataType.Vs, AuthDefaultValues.ALL);
        if (vsId == null || certificate == null) {
            throw new ValidationException("vsId and cert are required.");
        }
        if (certificate.getCert() == null || certificate.getKey() == null || certificate.getCert().length == 0 || certificate.getKey().length == 0) {
            throw new ValidationException("Certificate's cert data and key data parsed failed");
        }
        String domain = localCertificateInstaller.localInstall(vsId, certificate);
        return responseHandler.handle("Certificates with domain " + domain + " are installed successfully.", hh.getMediaType());
    }

    @POST
    @Path("/cert/localBatchInstall")
    public Response batchInstall(@Context HttpServletRequest request,
                                 @Context HttpHeaders hh,
                                 @QueryParam("force") Boolean force, String str) throws Exception {
        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.CERT, ResourceDataType.Vs, AuthDefaultValues.ALL);

        VSCertficate body = ObjectJsonParser.parse(str, VSCertficate.class);

        List<Long> vsIds = body.getVsIds();
        Map<Long, String> rVsDomains = body.getVsDomain();
        List<CertCertificateWithBLOBs> certificates = body.getCertificates();
        localCertificateInstaller.localBatchInstall(new HashSet<>(vsIds), rVsDomains, certificates, force != null && force);
        return responseHandler.handle("Certificates are installed successfully.", hh.getMediaType());
    }

    @GET
    @Path("/cert/find")
    public Response find(@Context HttpServletRequest request,
                         @Context HttpHeaders hh,
                         @QueryParam("vsIds") List<Long> vsIds) throws Exception {

        if (vsIds == null) {
            throw new ValidationException("vsIds is required.");
        }
        Map<Long, Certificate> certificateDoMap = certificateService.getActivatedCerts(vsIds.toArray(new Long[vsIds.size()]));
        Map<Long, CertCertificateWithBLOBs> results = new HashMap<>(certificateDoMap.size());
        certificateDoMap.entrySet().forEach(entry -> results.put(entry.getKey(), C.toCertCertificateWithBlobs(entry.getValue())));
        return responseHandler.handle(results, hh.getMediaType());
    }

    @GET
    @Path("/cert/batchInstall")
    public Response batchInstallAll(@Context HttpServletRequest request,
                                    @Context HttpHeaders hh,
                                    @QueryParam("slbId") Long slbId,
                                    @QueryParam("force") Boolean force) throws Exception {
        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.CERT, ResourceDataType.Vs, AuthDefaultValues.ALL);
        if (slbId == null) {
            throw new ValidationException("slbId is required.");
        }
        Slb slb = slbRepository.getById(slbId);
        if (slb == null) {
            throw new ValidationException("slbId is invalidate.");
        }
        List<String> ips = new ArrayList<>();
        for (SlbServer s : slb.getSlbServers()) {
            ips.add(s.getIp());
        }
        certificateService.remoteInstall(slbId, ips, force == null ? false : force);
        return responseHandler.handle("Certificates are installed successfully.", hh.getMediaType());
    }

    /*
     * @Description: batch query certId by vs id
     * @return: Map<Long, List<Long>>
     **/
    @GET
    @Path("/cert/query/cert")
    public Response query(@QueryParam("vsId") List<Long> vsIds,
                          @Context HttpHeaders headers,
                          @Context HttpServletRequest request) throws Exception {
        if (CollectionUtils.isEmpty(vsIds)) {
            throw new ValidationException("vsid must be provided");
        }

        Map<Long, Map<String, Long>> result = certificateService.getCertIdsByVsIds(vsIds.toArray(new Long[0]));

        return responseHandler.handle(result, headers.getMediaType());
    }

    @GET
    @Path("/cert/query/vs")
    public Response queryVsIds(@QueryParam("certId") List<Long> certIds,
                               @QueryParam("cid") List<String> cids,
                               @Context HttpHeaders headers,
                               @Context HttpServletRequest request) throws Exception {
        if (CollectionUtils.isEmpty(certIds)) {
            throw new ValidationException("certId must be provided");
        }

        // join certIds user passed with certIds got from cids
        Set<Long> actualCertIds = new HashSet<>();
        if (!CollectionUtils.isEmpty(certIds)) {
            actualCertIds.addAll(certIds);
        }
        if (!CollectionUtils.isEmpty(cids)) {
            for (String cid : cids) {
                actualCertIds.add(certificateService.getCertIdByCid(cid));
            }
        }

        Map<Long, List<Long>> result = certificateService.batchSearchVsesByCert(new ArrayList<>(actualCertIds));
        return responseHandler.handle(result, headers.getMediaType());
    }

    @GET
    @Path("/cert/query/default")
    public Response getDefaultCertBySlbId(@Context HttpServletRequest request,
                                          @Context HttpHeaders headers,
                                          @QueryParam("slbId") Long slbId) throws Exception {
        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.CERT, ResourceDataType.Vs, AuthDefaultValues.ALL);

        CertCertificateWithBLOBs defaultCertificate = certificateService.getDefaultCert(slbId);
        return responseHandler.handle(defaultCertificate, headers.getMediaType());
    }

    @GET
    @Path("/cert/queryBySlb")
    public Response getCertOfSlb(@Context HttpServletRequest request,
                                 @Context HttpHeaders headers,
                                 @QueryParam("slbId") Long slbId) throws Exception {
        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.CERT, ResourceDataType.Vs, AuthDefaultValues.ALL);

        if (slbId == null || slbId <= 0) {
            throw new ValidationException("slbId must be provided and greater than zero. ");
        }

        Set<IdVersion> keys = virtualServerCriteriaQuery.queryBySlbId(slbId);
        Set<Long> candidates = keys.stream().map(IdVersion::getId).collect(Collectors.toSet());
        candidates.retainAll(virtualServerCriteriaQuery.queryBySsl(true));

        Map<Long,Certificate> vsCertMap = certificateService.getActivatedCerts(candidates.toArray(new Long[candidates.size()]));
        Map<Long, CertCertificateWithBLOBs> results = new HashMap<>(vsCertMap.size());
        vsCertMap.forEach((vsId, certificate) -> results.put(vsId, C.toCertCertificateWithBlobs(certificate)));
        return responseHandler.handle(results, headers.getMediaType());
    }

    @POST
    @Path("/cert/certs/add")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "*/*"})
    public Response add(@FormDataParam("cert") InputStream cert,
                        @FormDataParam("key") InputStream key,
                        @QueryParam("cid") String cid,
                        @QueryParam("domain") List<String> domains,
                        @Context HttpHeaders httpHeaders) throws Exception {

        String certContent = cert == null ? null : IOUtils.inputStreamStringify(cert);
        String keyContent = key == null ? null : IOUtils.inputStreamStringify(key);

        Long certId = certificateService.add(certContent, keyContent, domains, cid);

        Map<String, Long> result = new HashMap<>();
        result.put("certId", certId);
        return responseHandler.handle(result, httpHeaders.getMediaType());
    }

    @GET
    @Path("/cert/certs/get")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "*/*"})
    public Response get(@QueryParam("certId") Long certId,
                        @QueryParam("cid") String cid,
                        @QueryParam("withBlobs") Boolean withBlobs,
                        @QueryParam("type") String type,
                        @Context HttpHeaders headers,
                        @Context HttpServletRequest request) throws Exception {
        if (certId == null && cid == null) {
            throw new ValidationException("Must pass cert id or cid. ");
        }
        if (certId != null && cid != null) {
            throw new ValidationException("Cannot pass both cert id and cid. ");
        }

        if (withBlobs != null && withBlobs) {
            if (ViewConstraints.INFO.equalsIgnoreCase(type)) {
                type = ViewConstraints.NORMAL;
            }
            authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.READ, ResourceDataType.Cert, AuthDefaultValues.ALL);
        }

        if (cid != null && certId == null) {
            certId = certificateService.getCertIdByCid(cid);
        }
        Certificate record = certificateService.getByCertId(certId, withBlobs);
        if (record == null) {
            throw new ValidationException("Cert can not be found. CertId: " + certId + ", cid: " + cid);
        }
        ExtendedView.ExtendedCertificate extendedCertificate = new ExtendedView.ExtendedCertificate(record);

        if (ViewConstraints.EXTENDED.equalsIgnoreCase(type)) {
            viewDecorator.decorate(extendedCertificate, CertConstants.ITEM_TYPE);
        }

        return responseHandler.handleSerializedValue(ObjectJsonWriter.write(extendedCertificate, type), headers.getMediaType());
    }

    @GET
    @Path("/cert/set/status")
    public Response setCertSlbServerStatus(@Context HttpServletRequest request,
                                           @Context HttpHeaders hh, @QueryParam("certId") Long certId,
                                           @QueryParam("vsId") Long vsId) throws Exception {
        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.CERT, ResourceDataType.Vs, AuthDefaultValues.ALL);
        if (vsId == null || certId == null) {
            throw new ValidationException("vsId and certId are required.");
        }
        updateCertSlbServer(vsId, certId);
        return responseHandler.handle("Certificates for vsId " + vsId + " are uninstalled.", hh.getMediaType());
    }

    private void updateCertSlbServer(Long vsId, Long certId) {
        CertCertificateSlbServerR record = new CertCertificateSlbServerR();
        record.setVsId(vsId);
        record.setIp(S.getIp());
        record.setCertId(certId);
        certificateService.insertCertSlbServerOrUpdateCert(record);
    }

    @GET
    @Path("/cert/certs/all")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "*/*"})
    public Response all(@QueryParam("withBlobs") Boolean withBlobs,
                        @QueryParam("type") String type,
                        @QueryParam("expiretime") String expireTime,
                        @QueryParam("domain") List<String> domains,
                        @Context HttpHeaders httpHeaders,
                        @Context HttpServletRequest request) throws Exception {
        withBlobs = withBlobs == null ? false : withBlobs;

        if (withBlobs) {
            if (ViewConstraints.INFO.equalsIgnoreCase(type)) {
                type = ViewConstraints.NORMAL;
            }
            authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.READ, ResourceDataType.Cert, AuthDefaultValues.ALL);
        }

        Date expire = null;
        if (expireTime != null) {
            try {
                expire = new SimpleDateFormat("yyyy-MM-dd").parse(expireTime);
            } catch (ParseException e) {
                throw new ValidationException("Incorrect data format. try using yyyy-MM-dd instead. ");
            }
        }

        List<Certificate> certificates = certificateService.all(withBlobs, expire, domains);

        if (certificates == null || certificates.size() == 0) {
            return responseHandler.handle(new ArrayList<>(), httpHeaders.getMediaType());
        }

        List<ExtendedView.ExtendedCertificate> extendedCertificates = new ArrayList<>(certificates.size());
        for (Certificate certificate : certificates) {
            ExtendedView.ExtendedCertificate extendedCertificate = new ExtendedView.ExtendedCertificate(certificate);
            extendedCertificates.add(extendedCertificate);

            if (ViewConstraints.EXTENDED.equalsIgnoreCase(type)) {
                viewDecorator.decorate(extendedCertificate, CertConstants.ITEM_TYPE);
            }
        }

        return responseHandler.handleSerializedValue(ObjectJsonWriter.write(extendedCertificates, type), httpHeaders.getMediaType());
    }

    /*
     * @Description
     * @return: passed certId if certId is not null, or queries certId with cid if cid is not null
     * @throw: ValidationException if both certId and cid are not null
     **/
    private Long resolveCertId(Long certId, String cid) throws Exception {
        if (certId != null && cid != null) {
            throw new ValidationException("Can not pass both certId and cid");
        }

        if (certId != null) {
            return certId;
        } else if (cid != null) {
            return certificateService.getCertIdByCid(cid);
        }

        throw new ValidationException("Must pass certId or valid cid. ");
    }

    private List<String> configureIps(Long vsId, List<String> greyscaleIps) throws Exception {
        IdVersion[] vsKeys = virtualServerCriteriaQuery.queryByIdAndMode(vsId, SelectionMode.REDUNDANT);
        if (vsKeys == null || vsKeys.length == 0) {
            throw new ValidationException("Virtual server with id " + vsId + " does not exist.");
        }
        Set<Long> slbId = slbCriteriaQuery.queryByVses(vsKeys);
        Set<IdVersion> slbKeys = slbCriteriaQuery.queryByIdsAndMode(slbId.toArray(new Long[slbId.size()]), SelectionMode.REDUNDANT);

        Set<String> serverIps = new HashSet<>();
        for (Slb e : slbRepository.list(slbKeys.toArray(new IdVersion[slbKeys.size()]))) {
            for (SlbServer ss : e.getSlbServers()) {
                serverIps.add(ss.getIp());
            }
        }

        if (greyscaleIps != null) {
            Set<String> copy = new HashSet<>(greyscaleIps);
            copy.removeAll(serverIps);
            if (copy.size() > 0) {
                throw new ValidationException("Some greyscale servers do not match vs-slb information: " + Joiner.on(",").join(copy) + ".");
            }
        }

        return greyscaleIps == null ? new ArrayList<>(serverIps) : greyscaleIps;
    }
}