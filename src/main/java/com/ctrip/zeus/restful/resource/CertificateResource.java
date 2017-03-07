package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.SlbServer;
import com.ctrip.zeus.restful.message.ResponseHandler;
import com.ctrip.zeus.service.model.*;
import com.ctrip.zeus.service.nginx.CertificateConfig;
import com.ctrip.zeus.service.nginx.CertificateInstaller;
import com.ctrip.zeus.service.nginx.CertificateService;
import com.ctrip.zeus.service.query.SlbCriteriaQuery;
import com.ctrip.zeus.service.query.VirtualServerCriteriaQuery;
import com.google.common.base.Joiner;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.*;

/**
 * Created by zhoumy on 2016/11/8.
 */
@Component
@Path("/cert")
public class CertificateResource {
    @Resource
    private SlbCriteriaQuery slbCriteriaQuery;
    @Resource
    private VirtualServerCriteriaQuery virtualServerCriteriaQuery;
    @Resource
    private SlbRepository slbRepository;
    @Resource
    private CertificateService certificateService;
    @Resource
    private CertificateInstaller certificateInstaller;
    @Resource
    private ResponseHandler responseHandler;

    /**
     * @api {post} /api/cert/upload: [Write] Upload cert
     * @apiDescription Upload cert if and only if the domain does not have any history certs.
     * @apiName UploadCertificate
     * @apiGroup Certificate
     * @apiParam {string}   domain      domain name of the cert. Use '|' to join multiple domain values as a whole.
     * @apiParam {formdata} cert        cert file
     * @apiParam {formdata} key         key file
     * @apiSuccess {string} message     success message
     */
    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadCerts(@Context HttpServletRequest request,
                                @Context HttpHeaders hh,
                                @FormDataParam("cert") InputStream cert,
                                @FormDataParam("key") InputStream key,
                                @QueryParam("domain") String domain) throws Exception {
        if (domain == null || domain.isEmpty()) {
            throw new ValidationException("Domain info is required.");
        }
        String[] domainMembers = domain.split("\\|");
        Arrays.sort(domainMembers);
        domain = Joiner.on("|").join(domainMembers);

        certificateService.upload(cert, key, domain, CertificateConfig.ONBOARD);
        return responseHandler.handle("Certificates uploaded. Virtual server creation is permitted.", hh.getMediaType());
    }

    /**
     * @api {post} /api/cert/upgrade: [Write] Upgrade cert
     * @apiDescription Upgrade cert if history cert exists for the domain. Either [Update vs content](#api-VS-FullUpdateVS) or [Install cert](#api-Certificate-InstallCertificate) to trigger cert installation.
     * @apiName UpgradeCertificate
     * @apiGroup Certificate
     * @apiParam {string}   domain      domain name of the cert. Use '|' to join multiple domain values as a whole.
     * @apiParam {boolean}  [greyscale] greyscale upgrading cert. If this parameter is used, set the cert as the canary version. Activation is required for on board use.
     * @apiParam {formdata} cert        cert file
     * @apiParam {formdata} key         key file
     * @apiSuccess {string} message     success message with the newly uploaded cert
     */
    @POST
    @Path("/upgrade")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response upgradeCerts(@Context HttpServletRequest request,
                                 @Context HttpHeaders hh,
                                 @FormDataParam("cert") InputStream cert,
                                 @FormDataParam("key") InputStream key,
                                 @QueryParam("domain") String domain,
                                 @QueryParam("greyscale") Boolean greyscale) throws Exception {
        if (domain == null || domain.isEmpty()) {
            throw new ValidationException("Domain info is required.");
        }
        boolean greyscaleRequired = (greyscale != null && greyscale) ? CertificateConfig.GRAYSCALE : CertificateConfig.ONBOARD;
        String[] domainMembers = domain.split("\\|");
        Arrays.sort(domainMembers);
        domain = Joiner.on("|").join(domainMembers);
        Long certId = certificateService.upgrade(cert, key, domain, greyscaleRequired);
        return responseHandler.handle("Certificate uploaded. Install new certificate with cert-id: " + certId + ".", hh.getMediaType());
    }

    /**
     * @api {get} /api/cert/activate: [Write] Activate canary cert
     * @apiDescription Activate slb is required to bring the cert into use.
     * @apiName ActivateCertificate
     * @apiGroup Certificate
     * @apiParam {long}     certId      id of cert to be activated
     * @apiSuccess {string} message     success message
     */
    @GET
    @Path("/activate")
    public Response activateCertificate(@Context HttpServletRequest request,
                                        @Context HttpHeaders hh,
                                        @QueryParam("certId") Long certId) throws Exception {
        Long prevId = certificateService.update(certId, CertificateConfig.ONBOARD);
        certificateService.replaceAndInstall(prevId, certId);
        return responseHandler.handle("Successfully update certificate state to onboard. Previous", hh.getMediaType());
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
    @Path("/remoteInstall")
    public Response remoteInstall(@Context HttpServletRequest request,
                                  @Context HttpHeaders hh,
                                  @QueryParam("certId") Long certId,
                                  @QueryParam("vsId") Long vsId,
                                  @QueryParam("ip") List<String> greyscaleIps,
                                  @QueryParam("greyscale") Boolean greyscale,
                                  @QueryParam("force") Boolean force) throws Exception {

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
        certificateService.install(vsId, installingIps, certId, force != null && force);
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
    @Path("/default/remoteInstall")
    public Response remoteInstallDefault(@Context HttpServletRequest request,
                                         @Context HttpHeaders hh,
                                         @QueryParam("slbId") Long slbId,
                                         @QueryParam("domain") String domain,
                                         @QueryParam("force") Boolean force) throws Exception {
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

    @GET
    @Path("/default/localInstall")
    public Response localInstallDefault(@Context HttpServletRequest request,
                                        @Context HttpHeaders hh,
                                        @QueryParam("certId") Long certId,
                                        @QueryParam("force") Boolean force) throws Exception {
        if ((force != null && force) || !certificateInstaller.defaultExists()) {
            if (certId == null) {
                throw new ValidationException("Query param certId is required.");
            }
            certificateInstaller.installDefault(certId);
            return responseHandler.handle("Default certificate is (re-)installed. Activate slb to take effect.", hh.getMediaType());
        }
        return responseHandler.handle("Default certificate exists. No need to update.", hh.getMediaType());
    }

    @GET
    @Path("/localInstall")
    public Response installCerts(@Context HttpServletRequest request,
                                 @Context HttpHeaders hh,
                                 @QueryParam("vsId") Long vsId,
                                 @QueryParam("certId") Long certId) throws Exception {
        if (vsId == null || certId == null) {
            throw new ValidationException("vsId and certId are required.");
        }
        String domain = certificateInstaller.localInstall(vsId, certId);
        return responseHandler.handle("Certificates with domain " + domain + " are installed successfully.", hh.getMediaType());
    }

    @GET
    @Path("/localBatchInstall")
    public Response batchInstall(@Context HttpServletRequest request,
                                 @Context HttpHeaders hh,
                                 @QueryParam("slbId") Long slbId,
                                 @QueryParam("force") Boolean force) throws Exception {
        if (slbId == null) {
            throw new ValidationException("slbId is required.");
        }
        certificateInstaller.localBatchInstall(slbId, force != null && force);
        return responseHandler.handle("Certificates are installed successfully.", hh.getMediaType());
    }

    @GET
    @Path("/localUninstall")
    public Response uninstallCerts(@Context HttpServletRequest request,
                                   @Context HttpHeaders hh,
                                   @QueryParam("vsId") Long vsId) throws Exception {
        if (vsId == null) {
            throw new ValidationException("vsId and certId are required.");
        }
        certificateInstaller.localUninstall(vsId);
        return responseHandler.handle("Certificates for vsId " + vsId + " are uninstalled.", hh.getMediaType());
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
