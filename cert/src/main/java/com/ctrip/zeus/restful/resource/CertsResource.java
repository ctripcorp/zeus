package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.Certificate;
import com.ctrip.zeus.restful.message.ResponseHandler;
import com.ctrip.zeus.service.CertificateResourceService;
import com.ctrip.zeus.service.auth.AuthDefaultValues;
import com.ctrip.zeus.service.auth.AuthService;
import com.ctrip.zeus.service.auth.ResourceDataType;
import com.ctrip.zeus.service.auth.ResourceOperationType;
import com.ctrip.zeus.util.IOUtils;
import com.ctrip.zeus.util.UserUtils;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Discription: This resource should not be used for now. Use APIs in CertificateResource instead.
 **/
@Component
@Path("/certs")
public class CertsResource {
    @Resource
    private CertificateResourceService certificateResourceService;
    @Resource
    private ResponseHandler responseHandler;
    @Resource
    private AuthService authService;

    @POST
    @Path("/add")
    public Response add(@FormDataParam("cert") InputStream cert,
                        @FormDataParam("key") InputStream key,
                        @FormDataParam("domain") List<String> domains,
                        @Context HttpHeaders httpHeaders) throws Exception {

        String certContent = cert == null ? null : IOUtils.inputStreamStringify(cert);
        String keyContent = key == null ? null : IOUtils.inputStreamStringify(key);

        Long certId = certificateResourceService.add(certContent, keyContent, domains, null);

        Map<String, Long> result = new HashMap<>();
        result.put("certId", certId);
        return responseHandler.handle(result, httpHeaders.getMediaType());
    }

    @GET
    @Path("/get")
    public Response get(@QueryParam("certId") Long certId,
                        @QueryParam("withBlobs") Boolean withBlobs,
                        @Context HttpHeaders headers,
                        @Context HttpServletRequest request) throws Exception {
        if (certId == null || certId <= 0) {
            throw new ValidationException("Invalid certId passed: " + certId);
        }

        if (withBlobs != null && withBlobs) {
            authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.READ, ResourceDataType.Cert, new Long[]{certId});
        }
        Certificate record = certificateResourceService.get(certId, withBlobs);

        return responseHandler.handle(record, headers.getMediaType());
    }

    @GET
    @Path("/all")
    public Response all(@QueryParam("withBlobs") Boolean withBlobs,
                        @QueryParam("expiretime") String expireTime,
                        @QueryParam("domain") List<String> domains,
                        @Context HttpHeaders httpHeaders,
                        @Context HttpServletRequest request) throws Exception {
        withBlobs = withBlobs == null ? false : withBlobs;
        if (withBlobs) {
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

        List<Certificate> certificates = certificateResourceService.all(withBlobs, expire, domains);

        return responseHandler.handle(certificates, httpHeaders.getMediaType());
    }
}
