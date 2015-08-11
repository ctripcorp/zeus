package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.auth.Authorize;
import com.ctrip.zeus.exceptions.*;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.nginx.entity.ReqStatus;
import com.ctrip.zeus.nginx.entity.TrafficStatus;
import com.ctrip.zeus.nginx.entity.TrafficStatusList;
import com.ctrip.zeus.restful.message.ResponseHandler;
import com.ctrip.zeus.service.model.GroupRepository;
import com.ctrip.zeus.service.model.SlbRepository;
import com.ctrip.zeus.service.nginx.NginxService;
import com.ctrip.zeus.service.status.GroupStatusService;
import com.ctrip.zeus.status.entity.GroupStatus;
import com.ctrip.zeus.status.entity.GroupStatusList;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

/**
 * @author:xingchaowang
 * @date: 3/4/2015.
 */
@Component
@Path("/status")
public class StatusResource {

    @Resource
    private GroupStatusService groupStatusService;
    @Resource
    private SlbRepository slbRepository;
    @Resource
    private GroupRepository groupRepository;
    @Resource
    private NginxService nginxService;
    @Resource
    private ResponseHandler responseHandler;


    @GET
    @Path("/groups")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Authorize(name = "getGroupStatus", uriGroupHint = -1)
    public Response allGroupStatusInSlb(@Context HttpServletRequest request, @Context HttpHeaders hh,
                                        @QueryParam("slbId") Long slbId,
                                        @QueryParam("slbName") String slbName) throws Exception {
        Long _slbId = null; //slbRepository.get(slbName).getId();
        List<GroupStatus> statusList = null;
        if (slbId != null) {
            _slbId = slbId;
        } else if (slbName != null) {
            _slbId = slbRepository.get(slbName).getId();
        }
        if (null == _slbId) {
            statusList = groupStatusService.getAllGroupStatus();
        } else {
            statusList = groupStatusService.getAllGroupStatus(_slbId);
        }

        GroupStatusList result = new GroupStatusList();
        for (GroupStatus groupStatus : statusList) {
            result.addGroupStatus(groupStatus);
        }
        return responseHandler.handle(result, hh.getMediaType());
    }

    @GET
    @Path("/group")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Authorize(name = "getGroupStatus", uriGroupHint = -1)
    public Response groupStatus(@Context HttpServletRequest request, @Context HttpHeaders hh,
                                @QueryParam("groupId") Long groupId,
                                @QueryParam("groupName") String groupName,
                                @QueryParam("slbId") Long slbId,
                                @QueryParam("slbName") String slbName) throws Exception {
        Long _groupId = null;
        Long _slbId = null;
        GroupStatus statusResult = null;

        if (groupId != null) {
            _groupId = groupId;
        } else if (groupName != null) {
            _groupId = groupRepository.get(groupName).getId();
        }
        if (null == _groupId) {
            throw new Exception("Group Id or Name not found!");
        }
        if (slbId != null) {
            _slbId = slbId;
        } else if (slbName != null) {
            _slbId = slbRepository.get(slbName).getId();
        }
        if (null == _slbId) {
            List<GroupStatus> statusList = groupStatusService.getGroupStatus(_groupId);
            if (statusList != null && statusList.size() > 0) {
                statusResult = statusList.get(0);
            } else {
                throw new ValidationException("SlbId param is needed!");
            }
        } else {
            statusResult = groupStatusService.getGroupStatus(_groupId, _slbId);
        }

        return responseHandler.handle(statusResult, hh.getMediaType());
    }

    @GET
    @Path("/groupStatus")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Authorize(name = "getGroupStatus", uriGroupHint = -1)
    public Response groupStatus(@Context HttpServletRequest request, @Context HttpHeaders hh,
                                @QueryParam("groupId") List<Long> groupId,
                                @QueryParam("slbId") Long slbId) throws Exception {
        GroupStatusList groupStatus = groupStatusService.getLocalGroupStatus(groupId, slbId);

        return responseHandler.handle(groupStatus, hh.getMediaType());
    }


    @GET
    @Path("/traffic")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getTrafficStatusBySlb(@Context HttpServletRequest request, @Context HttpHeaders hh,
                                          @QueryParam("slbId") Long slbId,
                                          @QueryParam("group") Boolean group,
                                          @QueryParam("slbServer") Boolean slbServer,
                                          @QueryParam("count") int count) throws Exception {
        if (slbId == null) {
            throw new ValidationException("Missing parameters.");
        }
        boolean aggregatedByGroup = group == null ? false : group.booleanValue();
        boolean aggregateBySlbServer = slbServer == null ? false : slbServer.booleanValue();
        if (group == null && slbServer == null)
            aggregatedByGroup = aggregateBySlbServer = true;
        count = count == 0 ? 1 : count;
        List<ReqStatus> statuses = nginxService.getTrafficStatusBySlb(slbId, count, aggregatedByGroup, aggregateBySlbServer);
        TrafficStatusList list = new TrafficStatusList().setTotal(statuses.size());
        for (ReqStatus rs : statuses) {
            list.addReqStatus(rs);
        }
        return responseHandler.handle(list, hh.getMediaType());
    }

    @GET
    @Path("/traffic/group")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getTrafficStatusByGroupAndSlb(@Context HttpServletRequest request, @Context HttpHeaders hh,
                                                  @QueryParam("groupId") Long groupId,
                                                  @QueryParam("groupName") String groupName,
                                                  @QueryParam("slbId") Long slbId,
                                                  @QueryParam("count") int count) throws Exception {

        if (slbId == null || (groupName == null && groupId == null)) {
            throw new ValidationException("Missing parameters.");
        }
        count = count == 0 ? 1 : count;
        if (groupId != null) {
            Group g = groupRepository.getById(groupId);
            if (g == null)
                throw new com.ctrip.zeus.exceptions.NotFoundException("Group cannot be found by Id.");
            groupName = g.getName();
        }
        List<ReqStatus> statuses = nginxService.getTrafficStatusBySlb(groupName, slbId, count);
        TrafficStatusList list = new TrafficStatusList().setTotal(statuses.size());
        for (ReqStatus rs : statuses) {
            list.addReqStatus(rs);
        }
        return responseHandler.handle(list, hh.getMediaType());
    }
}
