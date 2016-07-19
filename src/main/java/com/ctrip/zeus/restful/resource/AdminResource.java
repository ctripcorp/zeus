package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.auth.Authorize;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.service.query.filter.FilterSet;
import com.ctrip.zeus.service.query.filter.QueryExecuter;
import com.ctrip.zeus.restful.message.ResponseHandler;
import com.ctrip.zeus.restful.message.TrimmedQueryParam;
import com.ctrip.zeus.service.model.IdVersion;
import com.ctrip.zeus.service.model.SelectionMode;
import com.ctrip.zeus.service.model.SlbRepository;
import com.ctrip.zeus.service.query.SlbCriteriaQuery;
import com.ctrip.zeus.service.version.ConfVersionService;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by lu.wang on 2016/4/3.
 */
@Component
@Path("/admin")
public class AdminResource {

    @Resource
    private SlbRepository slbRepository;
    @Resource
    private SlbCriteriaQuery slbCriteriaQuery;
    @Resource
    private ResponseHandler responseHandler;
    @Resource
    private ConfVersionService confVersionService;

    @GET
    @Path("/release/confinfos")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Authorize(name = "getAllSlbReleaseInfo")
    public Response listConfInfos(@Context final HttpHeaders hh,
                         @Context HttpServletRequest request,
                         @TrimmedQueryParam("mode") final String mode,
                         @TrimmedQueryParam("diff") final String diff) throws Exception {

        final SelectionMode selectionMode = SelectionMode.getMode(mode);
        final Set<Long> allSlbId = slbCriteriaQuery.queryAll();
        final Long[] slbIds = new QueryExecuter.Builder<Long>()
                .addFilter(new FilterSet<Long>() {
                    @Override
                    public boolean shouldFilter() throws Exception {
                        return true;
                    }

                    @Override
                    public Set<Long> filter() throws Exception {
                        return allSlbId;
                    }
                })
                .addFilter(new FilterSet<Long>() {
                    @Override
                    public boolean shouldFilter() throws Exception {
                        return diff != null && diff.equals("true");
                    }

                    @Override
                    public Set<Long> filter() throws Exception {
                        return getSlbServerVersionDiff(allSlbId);
                    }
                })
                .build(Long.class).run();

        QueryExecuter<IdVersion> executer = new QueryExecuter.Builder<IdVersion>()
                .addFilter(new FilterSet<IdVersion>() {
                    @Override
                    public boolean shouldFilter() throws Exception {
                        return true;
                    }

                    @Override
                    public Set<IdVersion> filter() throws Exception {
                        return slbIds.length == 0 ? new HashSet<IdVersion>() : slbCriteriaQuery.queryByIdsAndMode(slbIds, selectionMode);
                    }
                })
                .build(IdVersion.class);

        final SlbReleaseInfoList slbReleaseInfoList = new SlbReleaseInfoList();
        for (Slb slb : slbRepository.list(executer.run())) {
            SlbReleaseInfo slbReleaseInfo = getSlbReleaseInfoBySlb(slb);
            slbReleaseInfoList.addSlbReleaseInfo(slbReleaseInfo);
        }
        slbReleaseInfoList.setTotal(slbReleaseInfoList.getSlbsInfo().size());
        return responseHandler.handle(slbReleaseInfoList, hh.getMediaType());
    }


    @GET
    @Path("/release/confinfo")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Authorize(name = "getSlbReleaseInfo")
    public Response getConfInfo(@Context final HttpHeaders hh,
                        @Context HttpServletRequest request,
                        @QueryParam("slbId") Long slbId,
                        @TrimmedQueryParam("slbName") String slbName) throws Exception {
        if (slbId == null && slbName == null) {
            throw new Exception("Missing parameter.");
        }
        if (slbId == null && slbName != null) {
            slbId = slbCriteriaQuery.queryByName(slbName);
        }
        if (slbId == null || slbId.longValue() == 0L)
            throw new ValidationException("Slb id cannot be found.");

        Slb slb = slbRepository.getById(slbId);
        if (slb == null)
            throw new ValidationException("Slb cannot be found.");
        return responseHandler.handle(getSlbReleaseInfoBySlb(slb), hh.getMediaType());
    }

    @GET
    @Path("/release/warinfo")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Authorize(name = "getSlbReleaseInfo")
    public Response getWarInfo(@Context final HttpHeaders hh,
                        @Context HttpServletRequest request) throws Exception {

        String commitId = System.getProperty("release.commitId") == null ? "unKnown" : System.getProperty("release.commitId");
        ServerWarInfo serverWarInfo = new ServerWarInfo().setCommitId(commitId);
        return responseHandler.handle(serverWarInfo, hh.getMediaType());
    }

    private SlbReleaseInfo getSlbReleaseInfoBySlb(Slb slb) throws Exception {
        if (slb == null)
            return null;

        SlbReleaseInfo slbReleaseInfo = new SlbReleaseInfo();
        Long slbId = slb.getId();

        slbReleaseInfo.setId(slbId);
        slbReleaseInfo.setName(slb.getName());
        slbReleaseInfo.setVersion(confVersionService.getSlbCurrentVersion(slb.getId()));

        List<SlbServer> slbServerList = slb.getSlbServers();
        for (SlbServer slbServer : slbServerList) {
            SlbServerReleaseInfo releaseInfo = new SlbServerReleaseInfo();

            String slbServerIp = slbServer.getIp();
            releaseInfo.setIp(slbServerIp);
            releaseInfo.setHostName(slbServer.getHostName());
            releaseInfo.setVersion(confVersionService.getSlbServerCurrentVersion(slbId, slbServerIp));

            slbReleaseInfo.addSlbServerReleaseInfo(releaseInfo);
        }
        return slbReleaseInfo;
    }

    private Set<Long> getSlbServerVersionDiff(Set<Long> allSlbId) throws Exception {
        Set<Long> diffVersionSet = new HashSet<>();
        if (allSlbId != null) {
            for (Long slbId : allSlbId) {
                Long slbCurrentVersion = confVersionService.getSlbCurrentVersion(slbId);
                for (SlbServer slbServer : slbRepository.getById(slbId).getSlbServers()) {
                    if (!confVersionService.getSlbServerCurrentVersion(slbId, slbServer.getIp()).equals(slbCurrentVersion)) {
                        diffVersionSet.add(slbId);
                        break;
                    }
                }
            }
        }
        return diffVersionSet;
    }
}
