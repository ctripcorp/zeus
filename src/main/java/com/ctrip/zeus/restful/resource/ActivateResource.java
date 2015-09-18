package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.auth.Authorize;
import com.ctrip.zeus.exceptions.SlbValidatorException;
import com.ctrip.zeus.executor.TaskManager;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.model.transform.DefaultSaxParser;
import com.ctrip.zeus.restful.message.ResponseHandler;
import com.ctrip.zeus.service.activate.ActiveConfService;
import com.ctrip.zeus.service.activate.GroupActivateConfRewrite;
import com.ctrip.zeus.service.model.ArchiveService;
import com.ctrip.zeus.service.model.GroupRepository;
import com.ctrip.zeus.service.model.SlbRepository;
import com.ctrip.zeus.service.task.constant.TaskOpsType;
import com.ctrip.zeus.service.validate.SlbValidator;
import com.ctrip.zeus.tag.TagBox;
import com.ctrip.zeus.task.entity.OpsTask;
import com.ctrip.zeus.task.entity.TaskResult;
import com.ctrip.zeus.task.entity.TaskResultList;
import com.ctrip.zeus.util.AssertUtils;
import com.netflix.config.DynamicBooleanProperty;
import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by fanqq on 2015/3/20.
 */

@Component
@Path("/activate")
public class ActivateResource {

    @Resource
    private TagBox tagBox;
    @Resource
    private SlbRepository slbRepository;
    @Resource
    private GroupRepository groupRepository;
    @Resource
    private ArchiveService archiveService;
    @Resource
    private SlbValidator slbValidator;
    @Resource
    private TaskManager taskManager;
    @Resource
    private ResponseHandler responseHandler;
    @Resource
    private ActiveConfService activeConfService;
    @Resource
    private GroupActivateConfRewrite groupActivateConfRewrite;


    private static DynamicIntProperty lockTimeout = DynamicPropertyFactory.getInstance().getIntProperty("lock.timeout", 5000);
    private static DynamicBooleanProperty writable = DynamicPropertyFactory.getInstance().getBooleanProperty("activate.writable", true);

    @GET
    @Path("/slb")
    @Authorize(name="activate")
    public Response activateSlb(@Context HttpServletRequest request,@Context HttpHeaders hh,@QueryParam("slbId") List<Long> slbIds,  @QueryParam("slbName") List<String> slbNames)throws Exception{
        List<Long> _slbIds = new ArrayList<>();
        SlbValidateResponse validateResponse = null;
        if ( slbIds!=null && !slbIds.isEmpty() )
        {
            _slbIds.addAll(slbIds);
        }
        if ( slbNames!=null && !slbNames.isEmpty() )
        {
            for (String slbName : slbNames)
            {
                _slbIds.add(slbRepository.get(slbName).getId());
            }
        }
        for (Long id : _slbIds){
            validateResponse=slbValidator.validate(id);
            if (!validateResponse.getSucceed()){
                throw new SlbValidatorException("msg:"+validateResponse.getMsg()+"\nslbId:"+validateResponse.getSlbId()
                +"\nip:"+validateResponse.getIp());
            }
        }
        List<OpsTask> tasks = new ArrayList<>();
        for (Long id : _slbIds) {
            OpsTask task = new OpsTask();
            task.setSlbId(id);
            task.setOpsType(TaskOpsType.ACTIVATE_SLB);
            task.setTargetSlbId(id);
            Archive archive = archiveService.getLatestSlbArchive(id);
            task.setVersion(archive.getVersion());
            tasks.add(task);
        }
        List<Long> taskIds = taskManager.addTask(tasks);
        List<TaskResult> results = taskManager.getResult(taskIds,30000L);

        TaskResultList resultList = new TaskResultList();
        for (TaskResult t : results){
            resultList.addTaskResult(t);
        }
        resultList.setTotal(results.size());
        return responseHandler.handle(resultList,hh.getMediaType());
    }

    @GET
    @Path("/group")
    @Authorize(name="activate")
    public Response activateGroup(@Context HttpServletRequest request,@Context HttpHeaders hh,@QueryParam("groupId") List<Long> groupIds,  @QueryParam("groupName") List<String> groupNames)throws Exception{
        List<Long> _groupIds = new ArrayList<>();

        if ( groupIds!=null && !groupIds.isEmpty() )
        {
            _groupIds.addAll(groupIds);
        }
        if ( groupNames!=null && !groupNames.isEmpty() )
        {
            for (String groupName : groupNames)
            {
                _groupIds.add(groupRepository.get(groupName).getId());
            }
        }
        List<OpsTask> tasks = new ArrayList<>();
        for (Long id : _groupIds) {
            Archive archive = archiveService.getLatestGroupArchive(id);
            Group group = DefaultSaxParser.parseEntity(Group.class, archive.getContent());
            AssertUtils.assertNotNull(group,"Archive Group Parser Failed! GroupId:");
            List<GroupVirtualServer> virtualServers = group.getGroupVirtualServers();

            Set<Long> slbIds = new HashSet<>();
            for (GroupVirtualServer virtualServer : virtualServers){
                slbIds.add(virtualServer.getVirtualServer().getSlbId());
            }
            slbIds.addAll(activeConfService.getSlbIdsByGroupId(id));
            for (Long slbId : slbIds){
                OpsTask task = new OpsTask();
                task.setGroupId(id);
                task.setOpsType(TaskOpsType.ACTIVATE_GROUP);
                task.setTargetSlbId(slbId);
                task.setVersion(archive.getVersion());
                tasks.add(task);
            }
        }
        List<Long> taskIds = taskManager.addTask(tasks);
        List<TaskResult> results = taskManager.getResult(taskIds,30000L);

        TaskResultList resultList = new TaskResultList();
        for (TaskResult t : results){
            resultList.addTaskResult(t);
        }
        resultList.setTotal(results.size());
        try {
            tagBox.tagging("active", "group", _groupIds.toArray(new Long[_groupIds.size()]));
            tagBox.untagging("deactive", "group", _groupIds.toArray(new Long[_groupIds.size()]));
        } catch (Exception ex) {
        }
        return responseHandler.handle(resultList,hh.getMediaType());

    }
    @GET
    @Path("/group/rewriteConf")
    public Response rewriteConf(@Context HttpServletRequest request,@Context HttpHeaders hh) throws Exception {
        groupActivateConfRewrite.rewriteAllGroupActivteConf();
        return Response.ok().entity("RewriteSuccess").build();
    }

}
