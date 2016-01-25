package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.auth.Authorize;
import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.exceptions.SlbValidatorException;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.executor.TaskManager;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.model.transform.DefaultSaxParser;
import com.ctrip.zeus.restful.message.ResponseHandler;
import com.ctrip.zeus.service.activate.ActivateService;
import com.ctrip.zeus.service.activate.ActiveConfService;
import com.ctrip.zeus.service.model.ArchiveService;
import com.ctrip.zeus.service.model.GroupRepository;
import com.ctrip.zeus.service.model.SlbRepository;
import com.ctrip.zeus.service.query.GroupCriteriaQuery;
import com.ctrip.zeus.service.query.SlbCriteriaQuery;
import com.ctrip.zeus.service.query.VirtualServerCriteriaQuery;
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
import java.util.*;

/**
 * Created by fanqq on 2015/3/20.
 */

@Component
@Path("/activate")
public class ActivateResource {

    @Resource
    private TagBox tagBox;
    @Resource
    private SlbCriteriaQuery slbCriteriaQuery;
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
    private GroupCriteriaQuery groupCriteriaQuery;
    @Resource
    private ActivateService activateService;
    @Resource
    private ConfSlbVirtualServerActiveDao confSlbVirtualServerActiveDao;
    @Resource
    private ConfGroupActiveDao confGroupActiveDao;
    @Resource
    private SlbRepository slbRepository;
    @Resource
    private VirtualServerCriteriaQuery virtualServerCriteriaQuery;

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
                _slbIds.add(slbCriteriaQuery.queryByName(slbName));
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

            Set<Long> vsIds = virtualServerCriteriaQuery.queryBySlbId(id);
            Map<Long,VirtualServer> activatedVses = activateService.getActivatedVirtualServerBySlb(id);
            List<Archive> list = archiveService.getLastestVsArchives(vsIds.toArray(new Long[]{}));
            for (Archive a : list){
                if (activatedVses.containsKey(a.getId()) && !activatedVses.get(a.getId()).getVersion().equals(a.getVersion())
                        || !activatedVses.containsKey(a.getId())){
                    task = new OpsTask();
                    task.setSlbVirtualServerId(a.getId());
                    task.setOpsType(TaskOpsType.ACTIVATE_VS);
                    task.setTargetSlbId(id);
                    task.setVersion(a.getVersion());
                    task.setCreateTime(new Date());
                    tasks.add(task);
                }
            }
        }

        List<Long> taskIds = taskManager.addTask(tasks);
        List<TaskResult> results = taskManager.getResult(taskIds,30000L);

        TaskResultList resultList = new TaskResultList();
        for (TaskResult t : results){
            resultList.addTaskResult(t);
        }
        resultList.setTotal(results.size());
        return responseHandler.handle(resultList, hh.getMediaType());
    }

    @GET
    @Path("/group")
    @Authorize(name="activate")
    public Response activateGroup(@Context HttpServletRequest request,@Context HttpHeaders hh,@QueryParam("groupId") List<Long> groupIds,  @QueryParam("groupName") List<String> groupNames)throws Exception{
        List<Long> _groupIds = new ArrayList<>();

        if ( groupIds!=null && !groupIds.isEmpty())
        {
            _groupIds.addAll(groupIds);
        }
        if ( groupNames!=null && !groupNames.isEmpty() )
        {
            for (String groupName : groupNames)
            {
                _groupIds.add(groupCriteriaQuery.queryByName(groupName));
            }
        }
        for (Long id : _groupIds){
            if (groupRepository.getById(id)==null){
                throw new ValidationException("Group Id Not Found : "+id);
            }
        }
        List<OpsTask> tasks = new ArrayList<>();
        for (Long id : _groupIds) {
            Group group = groupRepository.getById(id);
            AssertUtils.assertNotNull(group,"Group Not Found! GroupId:"+id);
            AssertUtils.assertNotNull(group.getGroupVirtualServers(),"Group Virtual Servers Not Found! GroupId:"+id);
            for (GroupVirtualServer gv : group.getGroupVirtualServers()){
                if (!activateService.isVSActivated(gv.getVirtualServer().getId())){
                    throw new ValidationException("Related VS has not been activated.VS: "+gv.getVirtualServer().getId());
                }
            }

            Set<Long> slbIds = slbCriteriaQuery.queryByGroups(new Long[]{id});
            Set<Long> activatedSlbId = activeConfService.getSlbIdsByGroupId(id);
            activatedSlbId.removeAll(slbIds);

            for (Long slbId : activatedSlbId){
                OpsTask task = new OpsTask();
                task.setGroupId(id);
                task.setOpsType(TaskOpsType.DEACTIVATE_GROUP);
                task.setTargetSlbId(slbId);
                task.setVersion(group.getVersion());
                tasks.add(task);
            }

            for (Long slbId : slbIds){
                OpsTask task = new OpsTask();
                task.setGroupId(id);
                task.setOpsType(TaskOpsType.ACTIVATE_GROUP);
                task.setTargetSlbId(slbId);
                task.setVersion(group.getVersion());
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
    @Path("/vs")
    @Authorize(name="activate")
    public Response activateVirtualServer(@Context HttpServletRequest request,
                                          @Context HttpHeaders hh,
                                          @QueryParam("vsId") Long vsId)throws Exception {
        Archive archive = archiveService.getLatestVsArchive(vsId);
        Long slbId = slbCriteriaQuery.queryByVs(vsId);
        List<VirtualServer> vses = activateService.getActivatedVirtualServer(vsId);
        List<Long> taskIds = new ArrayList<>();
        if ((vses.size()==1&&vses.get(0).getSlbId().equals(slbId))||vses.size() == 0){
            OpsTask task = new OpsTask();
            task.setSlbVirtualServerId(vsId);
            task.setCreateTime(new Date());
            task.setOpsType(TaskOpsType.ACTIVATE_VS);
            task.setTargetSlbId(slbId);
            task.setVersion(archive.getVersion());
            taskIds.add(taskManager.addTask(task));
        }else if (vses.size()==1 && !vses.get(0).getSlbId().equals(slbId)){
            VirtualServer vs = vses.get(0);
            OpsTask deactivateTask = new OpsTask();
            deactivateTask.setSlbVirtualServerId(vsId);
            deactivateTask.setCreateTime(new Date());
            deactivateTask.setOpsType(TaskOpsType.DEACTIVATE_VS);
            deactivateTask.setTargetSlbId(vs.getSlbId());
            taskIds.add(taskManager.addTask(deactivateTask));

            OpsTask activateTask = new OpsTask();
            activateTask.setSlbVirtualServerId(vsId);
            activateTask.setCreateTime(new Date());
            activateTask.setOpsType(TaskOpsType.ACTIVATE_VS);
            activateTask.setTargetSlbId(slbId);
            activateTask.setResources(String.valueOf(vs.getSlbId()));
            taskIds.add(taskManager.addTask(activateTask));
        }else {
            throw new ValidationException("Activated Date Of Virtual Server ["+vsId +"] Is Incorrect.");
        }
        List<TaskResult> results = taskManager.getResult(taskIds,30000L);
        TaskResultList resultList = new TaskResultList();
        for (TaskResult t : results){
            resultList.addTaskResult(t);
        }
        resultList.setTotal(results.size());
        return responseHandler.handle(resultList, hh.getMediaType());
    }
}
