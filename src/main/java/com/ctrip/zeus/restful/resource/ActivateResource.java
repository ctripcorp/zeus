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
    @GET
    @Path("/vs/dataFill")
    @Authorize(name="activate")
    public Response dataRewrite(@Context HttpServletRequest request,
                                          @Context HttpHeaders hh)throws Exception {
        Set<Long> slbIds = slbCriteriaQuery.queryAll();
        for (Long slbId : slbIds){
            Slb slb = activateService.getActivatedSlb(slbId);
            List<VirtualServer> vses = slb.getVirtualServers();
            for (VirtualServer vs : vses){
                Archive archive = archiveService.getLatestVsArchive(vs.getId());
                AssertUtils.assertNotNull(archive, "[activate]get Virtual Server Archive return Null! VsId: " + vs.getId());
                ConfSlbVirtualServerActiveDo confSlbVirtualServerActiveDo = new ConfSlbVirtualServerActiveDo();
                confSlbVirtualServerActiveDo.setContent(archive.getContent())
                        .setSlbId(slbId).setVersion(archive.getVersion())
                        .setSlbVirtualServerId(vs.getId())
                        .setCreatedTime(new Date());
                confSlbVirtualServerActiveDao.insert(confSlbVirtualServerActiveDo);
            }
        }
        return responseHandler.handle("update suc.", hh.getMediaType());
    }
    @GET
    @Path("/group/dataFill")
    @Authorize(name="activate")
    public Response dataFill(@Context HttpServletRequest request,
                                @Context HttpHeaders hh)throws Exception {
        List<ConfGroupActiveDo> confGroupActiveDos = confGroupActiveDao.findAll(ConfGroupActiveEntity.READSET_FULL);
        List<Long> failIds = new ArrayList<>();
        for (ConfGroupActiveDo c : confGroupActiveDos){
            Group group = null;
            try{
                group = DefaultSaxParser.parseEntity(Group.class, c.getContent());
            }catch (Exception e){
                failIds.add(c.getGroupId());
                continue;
            }

            for (GroupVirtualServer gv : group.getGroupVirtualServers()){
                if (gv.getVirtualServer().getSlbId().equals(c.getSlbId())){
                    c.setSlbVirtualServerId(gv.getVirtualServer().getId());
                    confGroupActiveDao.insert(c);
                }
                if (c.getSlbId()==0&&group.getGroupVirtualServers().size()==1){
                    c.setSlbVirtualServerId(gv.getVirtualServer().getId());
                    c.setSlbId(gv.getVirtualServer().getSlbId());
                    confGroupActiveDao.deleteByGroupId(new ConfGroupActiveDo().setGroupId(c.getGroupId()));
                    confGroupActiveDao.insert(c);
                }
            }
        }
        return responseHandler.handle("update suc."+failIds.toString(), hh.getMediaType());
    }
}
