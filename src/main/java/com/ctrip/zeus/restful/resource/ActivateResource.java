package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.auth.Authorize;
import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.exceptions.SlbValidatorException;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.executor.TaskManager;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.restful.message.ResponseHandler;
import com.ctrip.zeus.service.model.*;
import com.ctrip.zeus.service.model.handler.VirtualServerValidator;
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
import com.netflix.config.DynamicLongProperty;
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
    private SlbValidator slbValidator;
    @Resource
    private EntityFactory entityFactory;
    @Resource
    private TaskManager taskManager;
    @Resource
    private VirtualServerValidator virtualServerValidator;
    @Resource
    private ResponseHandler responseHandler;
    @Resource
    private GroupCriteriaQuery groupCriteriaQuery;
    @Resource
    private ConfSlbVirtualServerActiveDao confSlbVirtualServerActiveDao;
    @Resource
    private ConfGroupActiveDao confGroupActiveDao;
    @Resource
    private SlbRepository slbRepository;
    @Resource
    private VirtualServerCriteriaQuery virtualServerCriteriaQuery;

    private static DynamicIntProperty lockTimeout = DynamicPropertyFactory.getInstance().getIntProperty("lock.timeout", 5000);
    private static DynamicLongProperty apiTimeout = DynamicPropertyFactory.getInstance().getLongProperty("api.timeout", 15000L);
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
        ModelStatusMapping<Slb> slbModelStatusMapping = entityFactory.getSlbsByIds(_slbIds.toArray(new Long[]{}));
        if (slbModelStatusMapping.getOfflineMapping() == null || slbModelStatusMapping.getOfflineMapping().size()==0){
            throw new ValidationException("Not Found Slb By Id.");
        }
        for (Long id : _slbIds){
            if (slbModelStatusMapping.getOfflineMapping().get(id) == null){
                throw new ValidationException("Not Found Slb By Id."+id);
            }
            validateResponse=slbValidator.validate(slbModelStatusMapping.getOfflineMapping().get(id));
            if (!validateResponse.getSucceed()){
                throw new SlbValidatorException("msg:"+validateResponse.getMsg()+"\nslbId:"+validateResponse.getSlbId()
                +"\nip:"+validateResponse.getIp());
            }
        }
        List<OpsTask> tasks = new ArrayList<>();
        for (Long id : _slbIds) {
            Slb slb = slbModelStatusMapping.getOfflineMapping().get(id);
            OpsTask task = new OpsTask();
            task.setSlbId(id);
            task.setOpsType(TaskOpsType.ACTIVATE_SLB);
            task.setTargetSlbId(id);
            task.setVersion(slb.getVersion());
            tasks.add(task);
        }

        List<Long> taskIds = taskManager.addTask(tasks);
        List<TaskResult> results = taskManager.getResult(taskIds,apiTimeout.get());

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

        ModelStatusMapping<Group> mapping = entityFactory.getGroupsByIds(_groupIds.toArray(new Long[]{}));
        if (mapping.getOfflineMapping() == null || mapping.getOfflineMapping().size()==0){
            throw new ValidationException("Not Found Group By Id.");
        }
        List<OpsTask> tasks = new ArrayList<>();
        for (Long id : _groupIds) {
            Group offGroup = mapping.getOfflineMapping().get(id);
            Group onGroup = mapping.getOnlineMapping().get(id);

            AssertUtils.assertNotNull(offGroup,"Group Not Found! GroupId:"+id);
            AssertUtils.assertNotNull(offGroup.getGroupVirtualServers(),"Group Virtual Servers Not Found! GroupId:"+id);


            Set<Long> onlineVsIds = new HashSet<>();
            Set<Long> offlinevsIds = new HashSet<>();

            for (GroupVirtualServer gv : offGroup.getGroupVirtualServers()){
                if (!virtualServerValidator.isActivated(gv.getVirtualServer().getId())){
                    throw new ValidationException("Related VS has not been activated.VS: "+gv.getVirtualServer().getId());
                }
                offlinevsIds.add(gv.getVirtualServer().getId());
            }

            if (onGroup != null){
                for (GroupVirtualServer gv : onGroup.getGroupVirtualServers()){
                    onlineVsIds.add(gv.getVirtualServer().getId());
                }
            }

            Set<Long> tmp = new HashSet<>();
            tmp.addAll(onlineVsIds);
            tmp.addAll(offlinevsIds);

            ModelStatusMapping<VirtualServer> vsMaping = entityFactory.getVsesByIds(tmp.toArray(new Long[]{}));

            for (Long vsId : tmp){
                VirtualServer vs = vsMaping.getOnlineMapping().get(vsId);
                if (vs == null){
                    throw new ValidationException("Vs is not activated. vsId:"+vsId);
                }
                if (onlineVsIds.contains(vsId) && !offlinevsIds.contains(vsId)){
                    OpsTask task = new OpsTask();
                    task.setCreateTime(new Date())
                            .setGroupId(id)
                            .setTargetSlbId(vs.getSlbId())
                            .setSlbVirtualServerId(vsId)
                            .setOpsType(TaskOpsType.SOFT_DEACTIVATE_GROUP)
                            .setVersion(offGroup.getVersion());
                    tasks.add(task);
                } else {
                    OpsTask task = new OpsTask();
                    task.setCreateTime(new Date())
                            .setGroupId(id)
                            .setTargetSlbId(vs.getSlbId())
                            .setOpsType(TaskOpsType.ACTIVATE_GROUP)
                            .setVersion(offGroup.getVersion());
                    tasks.add(task);
                }
            }
        }
        List<Long> taskIds = taskManager.addTask(tasks);
        List<TaskResult> results = taskManager.getResult(taskIds,apiTimeout.get());

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
        ModelStatusMapping<VirtualServer> vsMaping = entityFactory.getVsesByIds(new Long[]{vsId});
        VirtualServer offlineVs = vsMaping.getOfflineMapping().get(vsId);
        VirtualServer onlineVs = vsMaping.getOnlineMapping().get(vsId);
        if (offlineVs == null){
            throw new ValidationException("Not Found Vs By ID");
        }
        if (onlineVs!=null && !offlineVs.getSlbId().equals(onlineVs.getSlbId())){
            throw new ValidationException("Has different slb id for online/offline vses.");
        }
        Long slbId = offlineVs.getSlbId();
        ModelStatusMapping<Slb>  slbMap = entityFactory.getSlbsByIds(new Long[]{slbId});
        if (slbMap.getOnlineMapping().get(slbId) == null){
            throw new ValidationException("Related Slb is not activated.");
        }
        OpsTask task = new OpsTask();
        task.setSlbVirtualServerId(vsId);
        task.setCreateTime(new Date());
        task.setOpsType(TaskOpsType.ACTIVATE_VS);
        task.setTargetSlbId(slbId);
        task.setVersion(offlineVs.getVersion());
        List<Long> taskIds = new ArrayList<>();
        taskIds.add(taskManager.addTask(task));

        List<TaskResult> results = taskManager.getResult(taskIds,apiTimeout.get());
        TaskResultList resultList = new TaskResultList();
        for (TaskResult t : results){
            resultList.addTaskResult(t);
        }
        resultList.setTotal(results.size());
        return responseHandler.handle(resultList, hh.getMediaType());
    }
}
