package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.auth.Authorize;
import com.ctrip.zeus.dal.core.GlobalJobDao;
import com.ctrip.zeus.dal.core.GlobalJobDo;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.restful.message.ResponseHandler;
import com.ctrip.zeus.service.query.GroupCriteriaQuery;
import com.ctrip.zeus.service.query.SlbCriteriaQuery;
import com.ctrip.zeus.service.status.GroupStatusService;
import com.ctrip.zeus.status.entity.GroupStatus;
import com.ctrip.zeus.status.entity.GroupStatusList;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
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
    private GroupCriteriaQuery groupCriteriaQuery;
    @Resource
    private SlbCriteriaQuery slbCriteriaQuery;
    @Resource
    private ResponseHandler responseHandler;
    @Resource
    private GlobalJobDao globalJobDao;

    @GET
    @Path("/groups")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Authorize(name = "getGroupStatus", uriGroupHint = -1)
    public Response allGroupStatusInSlb(@Context HttpServletRequest request, @Context HttpHeaders hh,
                                        @QueryParam("slbId") Long slbId,
                                        @QueryParam("slbName") String slbName) throws Exception {
        List<GroupStatus> statusList = null;
        if (slbId == null) {
            if (slbName != null) {
                slbId = slbCriteriaQuery.queryByName(slbName);
            }
        }
        if (null == slbId) {
            statusList = groupStatusService.getAllOfflineGroupsStatus();
        } else {
            statusList = groupStatusService.getOfflineGroupsStatusBySlbId(slbId);
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
        GroupStatus statusResult = null;

        if (groupId == null) {
            if (groupName != null) {
                groupId = groupCriteriaQuery.queryByName(groupName);
            }
        }
        if (null == groupId) {
            throw new ValidationException("Group Id or Name not found!");
        }
        if (slbId == null) {
            if (slbName != null) {
                slbId = slbCriteriaQuery.queryByName(slbName);
            }
        }
        if (null == slbId) {
            GroupStatus status = groupStatusService.getOfflineGroupStatus(groupId);
            if (status != null) {
                statusResult = status;
            } else {
                throw new ValidationException("Not Found Group Status In Slb!");
            }
        } else {
            GroupStatus res = groupStatusService.getOfflineGroupStatus(groupId, slbId);
            if (res != null) {
                statusResult = res;
            } else {
                throw new ValidationException("Not Found Group Status!");
            }
        }

        return responseHandler.handle(statusResult, hh.getMediaType());
    }

    @GET
    @Path("/job/unlock")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response jobUnlock(@Context HttpServletRequest request, @Context HttpHeaders hh,
                              @QueryParam("key") String key) throws Exception {
        globalJobDao.deleteByPK(new GlobalJobDo().setJobKey(key));
        return responseHandler.handle("success.", hh.getMediaType());
    }
}
