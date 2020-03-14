package com.ctrip.zeus.restful.resource.tools;

import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.flow.vsmigration.FlowVsMigrationService;
import com.ctrip.zeus.model.tools.*;
import com.ctrip.zeus.restful.message.ResponseHandler;
import com.ctrip.zeus.restful.message.impl.ErrorResponseHandler;
import com.ctrip.zeus.service.auth.AuthDefaultValues;
import com.ctrip.zeus.service.auth.AuthService;
import com.ctrip.zeus.service.auth.ResourceDataType;
import com.ctrip.zeus.service.auth.ResourceOperationType;
import com.ctrip.zeus.service.model.IdVersion;
import com.ctrip.zeus.service.model.SelectionMode;
import com.ctrip.zeus.service.query.CriteriaQueryFactory;
import com.ctrip.zeus.service.query.QueryEngine;
import com.ctrip.zeus.service.tools.cert.FlowCertUpgradeService;
import com.ctrip.zeus.service.tools.check.*;
import com.ctrip.zeus.service.tools.local.LocalInfoService;
import com.ctrip.zeus.service.tools.redirect.FlowVsRedirectService;
import com.ctrip.zeus.support.ObjectJsonParser;
import com.ctrip.zeus.tag.PropertyService;
import com.ctrip.zeus.util.UserUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by ygshen on 2016/11/30.
 */
@Component
@Path("/tools")
public class ToolsResource {
    @Resource
    ResponseHandler responseHandler;
    @Resource
    private ErrorResponseHandler errorResponseHandler;
    @Resource
    private CheckClientService checkClientService;
    @Resource
    private CheckSlbReleaseInfoService checkSlbReleaseInfoService;
    @Resource
    private PingVsVpnService pingVsVpnService;
    @Resource
    private VisitUrlClientService visitUrlClientService;
    @Resource
    private CriteriaQueryFactory criteriaQueryFactory;
    @Resource
    private FlowVsMigrationService flowVsMigrationService;

    @Resource
    private FlowVsRedirectService flowVsRedirectService;

    @Resource
    private FlowCertUpgradeService flowCertUpgradeService;

    @Resource
    private AuthService authService;
    @Resource
    private PropertyService propertyService;
    @Resource
    private LocalInfoService localInfoService;
    @Resource
    private HealthCheckerStatusService healthCheckerStatusService;

    private static DynamicIntProperty checkHealthDefaultTimeout = DynamicPropertyFactory.getInstance().getIntProperty("slb.tool.check.timeout", 5000);
    private static DynamicStringProperty visitUrlDefaultHeader = DynamicPropertyFactory.getInstance().getStringProperty("slb.tool.url.header", "thisfieldusedforslburlcheck=true");
    private static DynamicStringProperty slbClientProxyServer = DynamicPropertyFactory.getInstance().getStringProperty("slb.client.proxy.server", "proxyserver:8080");
    private static DynamicStringProperty privateZoneList = DynamicPropertyFactory.getInstance().getStringProperty("slb.client.private.zone.list", "privatedomain1;privatedomain2");

    @GET
    @Path("/healthcheck/status")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response healthcheckStatus(@Context HttpServletRequest request,
                                      @Context HttpHeaders hh,
                                      @QueryParam("groupId") String groupId) throws Exception {
        try {
            if (groupId == null) {
                throw new ValidationException("missing groupId");
            }

            Queue<String[]> renderedQueries = new LinkedList<>();
            renderedQueries.add(new String[]{"groupId", groupId});
            QueryEngine queryRender = new QueryEngine(renderedQueries, "slb", SelectionMode.ONLINE_EXCLUSIVE);
            queryRender.init(true);
            IdVersion[] searchKeys = queryRender.run(criteriaQueryFactory);
            if (searchKeys == null || searchKeys.length == 0) {
                throw new ValidationException("no matching slb found for groupId: " + groupId);
            }

            long slbId = searchKeys[0].getId();
            String idcCode = propertyService.getProperty("idc_code", slbId, "slb").getValue().toUpperCase();
            boolean pci = "true".equalsIgnoreCase(propertyService.getProperty("pci", slbId, "slb").getValue());
            String env = localInfoService.getEnv();
            if ("fat".equals(env)) {
                env = "FWS";
            } else if ("uat".equals(env)) {
                env = "UAT";
            } else {
                env = "PRO";
            }
            String name;
            if (pci) {
                name = env + "-PCI-" + idcCode;
            } else {
                name = env + "-" + idcCode;
            }
            String ip = healthCheckerStatusService.getCheckerIP(name);
            if (ip == null) {
                throw new ValidationException("fetch checker ip failed for " + name);
            }
            return responseHandler.handleSerializedValue(healthCheckerStatusService.getGroupStatus(ip, groupId), hh.getMediaType());
        } catch (Exception ex) {
            return errorResponseHandler.handle(ex, hh.getMediaType(), false);
        }
    }


    @POST
    @Path("/check/batch")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response batchCheck(@Context HttpServletRequest request,
                               @Context HttpHeaders hh,
                               @QueryParam("timeout") String timeout,
                               String requestBody) throws Exception {
        CheckTargetList checkTargets = ObjectJsonParser.parse(requestBody, CheckTargetList.class);

        if (checkTargets == null) {
            throw new ValidationException("Could not parse targets checking list");
        }

        // Timeout
        Integer requestTimeOut = parseInterger(timeout, "timeout");
        if (requestTimeOut == null) {
            requestTimeOut = checkHealthDefaultTimeout.get();
        }

        List<CheckTarget> targets = new ArrayList<>();
        targets.addAll(checkTargets.getTargets());

        return responseHandler.handle(checkTargetUrls(targets, requestTimeOut), hh.getMediaType());
    }

    @POST
    @Path("/check")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response check(@Context HttpServletRequest request,
                          @Context HttpHeaders hh,
                          @QueryParam("uri") String uri,
                          @QueryParam("host") String host,
                          @QueryParam("protocol") String protocol,
                          @QueryParam("agent") String agent,
                          @QueryParam("ips") String ips,
                          @QueryParam("port") String port,
                          @QueryParam("timeout") String timeout, String str) throws Exception {
        try {
            if (uri == null || ips == null) {
                throw new ValidationException("Query parameter - uri/ips is not provided.");
            } else {
                if (uri.trim().isEmpty() || ips.trim().isEmpty()) {
                    throw new ValidationException("Query parameter - uri/ips shall not be blank.");
                }
            }
            Set<Header> headers = ObjectJsonParser.parse(str, new TypeReference<Set<Header>>() {
            });


            // Protocol
            String requestProtocol = "http";
            if (protocol != null && !protocol.trim().isEmpty()) requestProtocol = protocol;

            // Request Timeout
            Integer requestTimeOut = parseInterger(timeout, "timeout");
            if (requestTimeOut == null) {
                requestTimeOut = checkHealthDefaultTimeout.get();
            }

            // Port
            Integer requestPort = parseInterger(port, "port");
            if (requestPort == null) {
                requestPort = 80;
            }

            // Uri
            if (!uri.startsWith("/"))
                uri = "/" + uri;

            // Ips
            String[] ipArray = ips.split(",");
            Set<String> unique = new HashSet<>();
            for (int i = 0; i < ipArray.length; i++) {
                unique.add(ipArray[i]);
            }

            // Start check
            List<CheckTarget> targets = new ArrayList<>();
            for (String ip : unique) {
                CheckTarget target = new CheckTarget();
                target.setProtocol(requestProtocol);
                target.setIp(ip.trim());
                target.setPort(requestPort);
                target.setHost(host);
                target.setAgent(agent);
                target.setUri(uri);
                if (headers != null && headers.size() > 0) {
                    for (Header header : headers) {
                        target.addHeader(header);
                    }
                }

                targets.add(target);
            }
            return responseHandler.handle(checkTargetUrls(targets, requestTimeOut), hh.getMediaType());
        } catch (Exception ex) {
            // Error handling
            return errorResponseHandler.handle(ex, hh.getMediaType(), false);
        }
    }

    @POST
    @Path("/check/slbs/release")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response checkServerReleaseInfo(@Context HttpServletRequest request,
                                           @Context HttpHeaders hh,
                                           @QueryParam("timeout") String timeout,
                                           String requestBody
    ) throws Exception {

        if (requestBody == null || requestBody.isEmpty())
            throw new ValidationException("Param required - Check SLB Release info body shall not be empty");

        // Port
        Integer requestTimeOut = parseInterger(timeout, "timeout");
        if (requestTimeOut == null) {
            requestTimeOut = checkHealthDefaultTimeout.get();
        }

        CheckTargetList targets = ObjectJsonParser.parse(requestBody, CheckTargetList.class);

        if (targets == null || targets.getTargets().size() == 0)
            throw new ValidationException("Param invalid - Check SLB Release info body is not in a required format");

        // Check result generate
        CheckList list = new CheckList();
        Map<String, CheckSlbreleaseResponse> checks = checkSlbReleaseInfoService.checkSlbReleaseInfo(targets, requestTimeOut);
        for (String key : checks.keySet()) {
            String[] set = key.split("/");
            String ip = set[0];
            String number = set[1];

            CheckSlbreleaseResponse response = checks.get(key);

            Check check = new Check();
            check.setIp(ip);
            check.setPort(Integer.parseInt(number));
            check.setMessage(response.getCommitId());
            check.setCode(response.getCode());

            list.addCheck(check);
        }

        list.setTotal(checks.size());

        // Response
        return responseHandler.handle(list, hh.getMediaType());
    }

    @POST
    @Path("/check/vsvpn")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "*/*"})
    public Response checkVsVpns(@Context HttpServletRequest request,
                                @Context HttpHeaders hh,
                                @QueryParam("timeout") String timeout,
                                String requestBody
    ) throws Exception {

        if (requestBody == null || requestBody.isEmpty())
            throw new ValidationException("Param required - Vs body shall not be empty");

        // Port
        Integer requestTimeOut = parseInterger(timeout, "timeout");
        if (requestTimeOut == null) {
            requestTimeOut = checkHealthDefaultTimeout.get();
        }

        VsPingList targets = ObjectJsonParser.parse(requestBody, VsPingList.class);

        if (targets == null || targets.getVses().size() == 0)
            throw new ValidationException("Param invalid - Ping Vs vpn info body is not in a required format");

        // Check result generate
        VsPingList list = new VsPingList();
        Map<String, VsPing> checks = pingVsVpnService.pingVses(targets, requestTimeOut);
        for (String key : checks.keySet()) {
            VsPing response = checks.get(key);

            list.addVsPing(response);
        }

        // Response
        return responseHandler.handle(list, hh.getMediaType());
    }

    @POST
    @Path("/butest")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response testUrl(@Context HttpServletRequest request,
                            @Context HttpHeaders hh,
                            @QueryParam("timeout") String timeout,
                            String body) throws Exception {
        try {


            AbtestTarget abtestTarget = ObjectJsonParser.parse(body, AbtestTarget.class);

            String requestMethod = abtestTarget.getMethod();
            String requestUrl = abtestTarget.getUrl();
            String requestBody = abtestTarget.getBody();
            String requestCookie = abtestTarget.getCookie();
            String vip = abtestTarget.getVip();

            // Get Proxy server
            String host;
            String urlPatern = "(http(s)?)://([[a-zA-Z0-9.]+]*)(/?.*)?";
            Pattern pattern = Pattern.compile(urlPatern);
            Matcher matcher = pattern.matcher(requestUrl.trim());

            if (matcher.find()) {
                host = matcher.group(3);

                // replace the request url's host with vip set
                requestUrl = matcher.group(1) + "://" + vip + matcher.group(4);
            } else {
                throw new ValidationException("Query parameter - url should be in format of http(s)://domain.test.com/blah.");
            }
            String proxyServer = getProxyServer(vip);

            // Get custom headers
            List<Header> requestHeaders = abtestTarget.getCustomHeaders();
            Map<String, String> headers = new HashMap<>();
            for (int i = 0; i < requestHeaders.size(); i++) {
                Header h = requestHeaders.get(i);
                headers.put(h.getKey(), h.getValue());
            }

            // set host into header
            headers.put("Host", host);

            // Get url params
            List<Param> requestParams = abtestTarget.getParams();
            Map<String, String> params = new HashMap<>();
            for (int i = 0; i < requestParams.size(); i++) {
                Param param = requestParams.get(i);
                params.put(param.getKey(), param.getValue());
            }

            // Send the request
            Map<String, String> response = visitUrlClientService.visit(requestMethod, requestUrl, params, headers, requestCookie, requestBody, proxyServer);

            return Response.status(Integer.parseInt(response.get("code"))).entity(response.get("response"))
                    .type(MediaType.APPLICATION_JSON_TYPE.withCharset("utf-8")).build();
        } catch (Exception ex) {
            // Error handling
            return errorResponseHandler.handle(ex, hh.getMediaType(), false);
        }
    }

    @GET
    @Path("/visit")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response visit(@Context HttpServletRequest request,
                          @Context HttpHeaders hh,
                          @QueryParam("url") String url,
                          @QueryParam("host") String customHost,
                          @QueryParam("timeout") Integer timeout) throws Exception {
        String protocol;
        String host;
        String target;
        String uri;

        try {

            // Parameter validation
            if (url == null || url.isEmpty()) {
                throw new ValidationException("Query parameter - url is not provided.");
            }
            if (timeout == null || timeout < 0) {
                timeout = checkHealthDefaultTimeout.get();
            }

            // Compile the rest client field

            String urlPatern = "(http(s)?)://([[-a-zA-Z0-9+&@#%?=~_|!:,.;]+]*)(/?.*)?";
            Pattern pattern = Pattern.compile(urlPatern);
            Matcher matcher = pattern.matcher(url.trim());

            if (matcher.find()) {
                protocol = matcher.group(1);
                host = matcher.group(3);
                uri = matcher.group(4);

                target = protocol + "://" + host;
            } else {
                throw new ValidationException("Query parameter - url should be in format of http(s)://domain.test.com/blah.");
            }

            // Set Request Headers
            String slbHeader = visitUrlDefaultHeader.getValue();
            Map<String, String> requestHeader = new HashMap<>();
            if (customHost != null) {
                requestHeader.put("Host", customHost);
            } else {
                requestHeader.put("Host", host);
            }
            if (slbHeader != null && !slbHeader.trim().isEmpty()) {
                String[] keys = slbHeader.split("=");
                if (keys.length != 2) {
                    throw new ValidationException("slb.tool.url.header configuration value should be in the format of \"key value\"");
                }
                requestHeader.put(keys[0], keys[1]);
            } else {
                throw new ValidationException("slb.tool.url.header configuration value should not be empty");
            }

            String proxyServer = getProxyServer(host);
            // Start the check
            CheckResponse response = visitUrlClientService.visit(target, uri, proxyServer, timeout, requestHeader);
            return responseHandler.handle(response, hh.getMediaType());
        } catch (Exception ex) {
            return errorResponseHandler.handle(ex, hh.getMediaType(), false);
        }
    }


    @POST
    @Path("/vs/redirect/new")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "*/*"})
    public Response newRedirect(@Context HttpHeaders hh, @Context HttpServletRequest request,
                            String body) throws Exception {
        if (body == null)
            throw new ValidationException("Param required - migration body is required");
        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.UPDATE, ResourceDataType.Flow, AuthDefaultValues.ALL);
        try {
            VsRedirect migration = ObjectJsonParser.parse(body, VsRedirect.class);
            // Start the check
            VsRedirect result = flowVsRedirectService.add(migration);
            return responseHandler.handle(result, hh.getMediaType());
        } catch (Exception ex) {
            return errorResponseHandler.handle(ex, hh.getMediaType(), false);
        }
    }

    @POST
    @Path("/vs/redirect/update")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response updateRedirect(@Context HttpHeaders hh, @Context HttpServletRequest request,
                                   String body) throws Exception {

        if (body == null)
            throw new ValidationException("Param required - VsRedirect body is required");

        try {
            VsRedirect migration = ObjectJsonParser.parse(body, VsRedirect.class);
            if (migration.getId() == null) {
                throw new ValidationException("Update Vs VsRedirect's Id should not be empty");
            }

            authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.UPDATE, ResourceDataType.Flow, migration.getId());

            // Start the check
            VsRedirect response = flowVsRedirectService.update(migration);
            return responseHandler.handle(response, hh.getMediaType());
        } catch (Exception ex) {
            return errorResponseHandler.handle(ex, hh.getMediaType(), false);
        }
    }

    // Get Vs migration object by id
    @GET
    @Path("/vs/redirect")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getRedirect(@Context HttpHeaders hh, @Context HttpServletRequest request,
                                @QueryParam("redirectId") String id) throws Exception {

        if (id == null)
            throw new ValidationException("Param required - Redirect id is required");

        long longId;
        try {
            longId = Long.parseLong(id);
        } catch (Exception ex) {
            throw new ValidationException("Param type - Redirect id is not a long type");
        }

        try {
            VsRedirect migration = flowVsRedirectService.get(longId);
            if (migration == null) {
                throw new ValidationException(String.format("VS Redirect With Id=%d does not exists", longId));
            }
            return responseHandler.handle(migration, hh.getMediaType());
        } catch (Exception ex) {
            return errorResponseHandler.handle(ex, hh.getMediaType(), false);
        }
    }

    @GET
    @Path("/vs/redirect/list")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response listRedirect(@Context HttpHeaders hh, @Context HttpServletRequest request) throws Exception {
        try {
            VsRedirectList list = flowVsRedirectService.list();
            return responseHandler.handle(list, hh.getMediaType());
        } catch (Exception ex) {
            return errorResponseHandler.handle(ex, hh.getMediaType(), false);
        }
    }


    @GET
    @Path("/vs/redirect/delete")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response deleteRedirect(@Context HttpHeaders hh, @Context HttpServletRequest request,
                                   @QueryParam("redirectId") String id) throws Exception {

        if (id == null)
            throw new ValidationException("Param required - Redirect id is required");

        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.DELETE, ResourceDataType.Flow, id);

        long longId;
        try {
            longId = Long.parseLong(id);
        } catch (Exception ex) {
            throw new ValidationException("Param type - Redirect id is not a long type");
        }

        try {
            VsRedirect migration = flowVsRedirectService.get(longId);
            if (migration == null) {
                throw new ValidationException(String.format("VS Redirect With Id=%d does not exists", longId));
            }
            flowVsRedirectService.delete(longId);

            return responseHandler.handle(migration, hh.getMediaType());
        } catch (Exception ex) {
            return errorResponseHandler.handle(ex, hh.getMediaType(), false);
        }
    }


    @POST
    @Path("/cert/upgrade/new")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "*/*"})
    public Response newCertUpgrade(@Context HttpHeaders hh, @Context HttpServletRequest request, String body) throws Exception {

        if (body == null)
            throw new ValidationException("Param required - migration body is required");

        //authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.NEW, ResourceDataType.Flow, AuthDefaultValues.ALL);
        try {
            CertUpgrade certUpgrade = ObjectJsonParser.parse(body, CertUpgrade.class);
            return responseHandler.handle(flowCertUpgradeService.create(certUpgrade), hh.getMediaType());
        } catch (Exception ex) {
            return errorResponseHandler.handle(ex, hh.getMediaType(), false);
        }
    }

    @POST
    @Path("/cert/upgrade/update")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "*/*"})
    public Response updateCertUpgrade(@Context HttpHeaders hh, @Context HttpServletRequest request, String body) throws Exception {
        if (body == null)
            throw new ValidationException("Param required - migration body is required");
        //authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.NEW, ResourceDataType.Flow, AuthDefaultValues.ALL);
        CertUpgrade certUpgrade = ObjectJsonParser.parse(body, CertUpgrade.class);
        return responseHandler.handle(flowCertUpgradeService.update(certUpgrade), hh.getMediaType());
    }

    @GET
    @Path("/cert/upgrade")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "*/*"})
    public Response getCertUpgrade(@Context HttpHeaders hh, @Context HttpServletRequest request, @QueryParam("upgradeId") Long id) throws Exception {
        if (id == null)
            throw new ValidationException("Param required - Id is required");

        //authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.NEW, ResourceDataType.Flow, AuthDefaultValues.ALL);
        return responseHandler.handle(flowCertUpgradeService.get(id), hh.getMediaType());
    }

    @GET
    @Path("/cert/upgrades")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "*/*"})
    public Response listUpgrade(@Context HttpHeaders hh, @Context HttpServletRequest request) throws Exception {
        //authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.NEW, ResourceDataType.Flow, AuthDefaultValues.ALL);
        return responseHandler.handle(flowCertUpgradeService.list(), hh.getMediaType());
    }

    @POST
    @Path("/vs/migrate/new")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "*/*"})
    public Response migrate(@Context HttpHeaders hh, @Context HttpServletRequest request,
                            String body) throws Exception {
        if (body == null)
            throw new ValidationException("Param required - migration body is required");

        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.UPDATE, ResourceDataType.Flow, AuthDefaultValues.ALL);

        try {
            VsMigration migration = ObjectJsonParser.parse(body, VsMigration.class);
            // Start the check
            VsMigration result = flowVsMigrationService.newVsMigration(migration);
            return responseHandler.handle(result, hh.getMediaType());
        } catch (Exception ex) {
            return errorResponseHandler.handle(ex, hh.getMediaType(), false);
        }
    }

    @POST
    @Path("/vs/migrate/update")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response updateMigrate(@Context HttpHeaders hh, @Context HttpServletRequest request, String body) throws Exception {
        if (body == null)
            throw new ValidationException("Param required - migration body is required");
        try {
            VsMigration migration = ObjectJsonParser.parse(body, VsMigration.class);
            if (migration.getId() == null) {
                throw new ValidationException("Update Vs Migration's Id should not be empty");
            }

            authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.UPDATE, ResourceDataType.Flow, migration.getId());

            // Start the check
            VsMigration response = flowVsMigrationService.updateVsMigration(migration);
            return responseHandler.handle(response, hh.getMediaType());
        } catch (Exception ex) {
            return errorResponseHandler.handle(ex, hh.getMediaType(), false);
        }
    }

    // Get Vs migration object by id
    @GET
    @Path("/vs/migrate")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getMigrate(@Context HttpHeaders hh, @Context HttpServletRequest request,
                               @QueryParam("migrationId") String id
    ) throws Exception {

        if (id == null)
            throw new ValidationException("Param required - migration id is required");

        long longId;
        try {
            longId = Long.parseLong(id);
        } catch (Exception ex) {
            throw new ValidationException("Param type - migration id is not a long type");
        }

        try {
            VsMigration migration = flowVsMigrationService.getVsMigration(longId);
            if (migration == null) {
                throw new ValidationException(String.format("VS Migration With Id=%d does not exists", longId));
            }
            return responseHandler.handle(migration, hh.getMediaType());
        } catch (Exception ex) {
            return errorResponseHandler.handle(ex, hh.getMediaType(), false);
        }
    }

    // Get Vs migration object by id
    @GET
    @Path("/vs/migrates")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getMigrates(@Context HttpHeaders hh, @Context HttpServletRequest request) throws Exception {
        try {
            // Start the check
            List<VsMigration> migrationList = flowVsMigrationService.getAllMigration();

            VsMigrationList list = new VsMigrationList();
            list.setTotal(migrationList.size());
            for (VsMigration migration : migrationList) {
                list.addVsMigration(migration);
            }

            return responseHandler.handle(list, hh.getMediaType());
        } catch (Exception ex) {
            return errorResponseHandler.handle(ex, hh.getMediaType(), false);
        }
    }

    // Delete Vs migration object by id
    @GET
    @Path("/vs/migrate/delete")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response deleteMigrate(@Context HttpHeaders hh, @Context HttpServletRequest request,
                                  @QueryParam("migrationId") String id
    ) throws Exception {

        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.DELETE, ResourceDataType.Flow, id);

        if (id == null)
            throw new ValidationException("Param required - migration id is required");

        long longId;
        try {
            longId = Long.parseLong(id);
        } catch (Exception ex) {
            throw new ValidationException("Param type - migration id is not a long type");
        }

        try {
            // Start the check
            VsMigration migration = flowVsMigrationService.getVsMigration(longId);
            if (migration == null) {
                throw new ValidationException(String.format("VS Migration With Id=%d does not exists", longId));
            }

            boolean deleteSuccess = flowVsMigrationService.deleteVsMigration(migration);

            String msg;

            if (deleteSuccess) {
                msg = String.format("Successfully delete Vs migration with id=%d", longId);
            } else {
                msg = String.format("Failed to delete Vs migration with id=%d", longId);
            }

            return responseHandler.handle(msg, hh.getMediaType());
        } catch (Exception ex) {
            return errorResponseHandler.handle(ex, hh.getMediaType(), false);
        }
    }


    @GET
    @Path("/vs/migrate/clear")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response clearMigrate(@Context HttpHeaders hh, @Context HttpServletRequest request,
                                 @QueryParam("migrationId") String id
    ) throws Exception {

        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.DELETE, ResourceDataType.Flow, id);

        if (id == null)
            throw new ValidationException("Param required - migration id is required");

        long longId;
        try {
            longId = Long.parseLong(id);
        } catch (Exception ex) {
            throw new ValidationException("Param type - migration id is not a long type");
        }

        try {
            // Start the check
            VsMigration migration = flowVsMigrationService.getVsMigration(longId);
            if (migration == null) {
                throw new ValidationException(String.format("VS Migration With Id=%d does not exists", longId));
            }

            boolean deleteSuccess = flowVsMigrationService.clearVsMigration(migration);

            String msg;

            if (deleteSuccess) {
                msg = String.format("Successfully clear Vs migration with id=%d", longId);
            } else {
                msg = String.format("Failed to clear Vs migration with id=%d", longId);
            }

            return responseHandler.handle(msg, hh.getMediaType());
        } catch (Exception ex) {
            return errorResponseHandler.handle(ex, hh.getMediaType(), false);
        }
    }

    private Integer parseInterger(String str, String key) {
        Integer temp = null;
        if (str != null && !str.trim().isEmpty()) {
            try {
                temp = Integer.parseInt(str);
            } catch (Exception ex) {
                throw new IllegalArgumentException("Query parameter - " + key + " shall be an Interger value");
            }
            if (temp == 0 || temp < 0) {
                throw new IllegalArgumentException("Query parameter - " + key + " shall be greater than 0");
            }
        }
        return temp;
    }

    private String getProxyServer(String host) {
        String[] pzone = privateZoneList.get().split(";");
        String hostLowerCase = host.toLowerCase();
        for (String zone : pzone) {
            if (hostLowerCase.endsWith(zone)) {
                return null;
            }
        }

        String ip = "([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}";
        Pattern pattern = Pattern.compile(ip);
        Matcher matcher = pattern.matcher(host);

        if (matcher.matches()) {
            if (isIntranet(host)) {
                return null;
            }
        }

        final byte[] address;
        try {
            address = InetAddress.getByName(host).getAddress();
        } catch (UnknownHostException e) {
            return null;
        }

        if (isIntranet(address)) {
            return null;
        }

        return slbClientProxyServer.get();
    }

    private boolean isIntranet(String ip) {
        if (ip == null) return false;
        return ip.startsWith("10.") || ip.startsWith("192.168.") || (ip.compareTo("172.16.") > 0 && ip.compareTo("172.32.") < 0);
    }

    private CheckList checkTargetUrls(List<CheckTarget> targets, int requestTimeOut) throws Exception {
        // Check result generate
        CheckList list = new CheckList();
        Map<CheckTarget, CheckResponse> checks = checkClientService.checkUrl(targets, requestTimeOut);
        for (CheckTarget key : checks.keySet()) {
            String ip = key.getIp();
            int number = key.getPort();
            CheckResponse response = checks.get(key);
            Check check = new Check();
            check.setIp(ip);
            check.setPort(number);
            check.setMessage(response.getStatus());
            check.setCode(response.getCode());
            check.setTime(response.getTime());

            if (key.getGroupId() != null) {
                check.setGroupId(key.getGroupId());
            }

            if (key.getVsId() != null) {
                check.setVsId(key.getVsId());
            }

            list.addCheck(check);
        }

        list.setTotal(checks.size());

        return list;
    }

    private static boolean isIntranet(byte[] address) {
        if (address == null || address.length != 4) {
            return false;
        }
        if (address[0] == 10) {
            return true;
        }
        if (address[0] == -64 && address[1] == -88) {
            return true;
        }
        if (address[0] == -84 && address[1] >= 16 && address[1] < 32) {
            return true;
        }
        return false;
    }
}