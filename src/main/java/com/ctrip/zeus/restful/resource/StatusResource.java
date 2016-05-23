package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.auth.Authorize;
import com.ctrip.zeus.dal.core.GlobalJobDao;
import com.ctrip.zeus.dal.core.GlobalJobDo;
import com.ctrip.zeus.exceptions.*;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.nginx.entity.ReqStatus;
import com.ctrip.zeus.nginx.entity.TrafficStatus;
import com.ctrip.zeus.nginx.entity.TrafficStatusList;
import com.ctrip.zeus.restful.message.ResponseHandler;
import com.ctrip.zeus.service.model.GroupRepository;
import com.ctrip.zeus.service.model.SlbRepository;
import com.ctrip.zeus.service.nginx.NginxService;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    private GroupRepository groupRepository;
    @Resource
    private GroupCriteriaQuery groupCriteriaQuery;
    @Resource
    private SlbCriteriaQuery slbCriteriaQuery;
    @Resource
    private NginxService nginxService;
    @Resource
    private ResponseHandler responseHandler;
    @Resource
    private GlobalJobDao globalJobDao;
    @Resource
    private GroupServerStatusDao groupServerStatusDao;
    @Resource
    private StatusGroupServerDao statusGroupServerDao;


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
            List<GroupStatus> statusList = groupStatusService.getOfflineGroupStatus(groupId);
            if (statusList != null && statusList.size() > 0) {
                statusResult = statusList.get(0);
            } else {
                throw new ValidationException("Not Found Group Status In Slb!");
            }
        } else {
            List<GroupStatus> res = groupStatusService.getOfflineGroupStatus(groupId, slbId);
            if (res != null && res.size() > 0) {
                statusResult = res.get(0);
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

    @GET
    @Path("/fillData")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response fillData(@Context HttpServletRequest request, @Context HttpHeaders hh,
                             @QueryParam("batch") Boolean batch,
                             @QueryParam("ip") List<String> ips) throws Exception {

        List<Long> sucGroupId = new ArrayList<>();
        List<Long> failGroupId = new ArrayList<>();

        if (batch != null && batch == true) {
            List<StatusGroupServerDo> list = statusGroupServerDao.findAll(StatusGroupServerEntity.READSET_FULL);
            if (list == null) {
                throw new ValidationException("Not found status group server items");
            }
            GroupServerStatusDo[] updateDatas = new GroupServerStatusDo[list.size()];
            for (int i = 0; i < list.size(); i++) {
                updateDatas[i] = new GroupServerStatusDo();
                updateDatas[i].setGroupId(list.get(i).getGroupId());
                updateDatas[i].setIp(list.get(i).getIp());
                updateDatas[i].setStatus(list.get(i).getStatus());
                updateDatas[i].setCreatedTime(list.get(i).getCreatedTime());
                updateDatas[i].setDataChangeLastTime(list.get(i).getDataChangeLastTime());
            }
            groupServerStatusDao.batchUpdate(updateDatas);
            return responseHandler.handle("success.", hh.getMediaType());
        } else if (ips != null && ips.size() > 0) {
            for (String ip : ips) {
                List<StatusGroupServerDo> datas = statusGroupServerDao.findAllByIp(ip,StatusGroupServerEntity.READSET_FULL);
                if (datas == null || datas.size() == 0){
                    throw new ValidationException("Not found data by ip");
                }
                try {
                    for (StatusGroupServerDo d : datas){
                        GroupServerStatusDo g  = new GroupServerStatusDo();
                        g.setGroupId(d.getGroupId());
                        g.setIp(d.getIp());
                        g.setStatus(d.getStatus());
                        g.setCreatedTime(d.getCreatedTime());
                        g.setDataChangeLastTime(d.getDataChangeLastTime());
                        groupServerStatusDao.update(g);
                        sucGroupId.add(g.getGroupId());
                    }
                } catch (Exception e) {
                    failGroupId.add(datas.get(0).getGroupId());
                }
            }
        }
        return responseHandler.handle("success List: " + sucGroupId.toString() + "\nfail:" + failGroupId.toString(), hh.getMediaType());
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
