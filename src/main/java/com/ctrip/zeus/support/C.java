package com.ctrip.zeus.support;

import com.ctrip.zeus.auth.entity.*;
import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.task.entity.OpsTask;
import com.ctrip.zeus.task.entity.Task;

/**
 * @author:xingchaowang
 * @date: 3/6/2015.
 */
public class C {


    public static Group toGroup(GroupDo d) {
        return new Group()
                .setId(d.getId())
                .setAppId(d.getAppId())
                .setName(d.getName())
                .setSsl(d.isSsl())
                .setVersion(d.getVersion());
    }

    public static GroupServer toGroupServer(GroupServerDo d) {
        return new GroupServer()
                .setIp(d.getIp())
                .setHostName(d.getHostName())
                .setFailTimeout(d.getFailTimeout())
                .setMaxFails(d.getMaxFails())
                .setPort(d.getPort())
                .setWeight(d.getWeight());
    }

    public static HealthCheck toHealthCheck(GroupHealthCheckDo d) {
        return new HealthCheck()
                .setFails(d.getFails())
                .setIntervals(d.getIntervals())
                .setPasses(d.getPasses())
                .setUri(d.getUri());
    }

    public static LoadBalancingMethod toLoadBalancingMethod(GroupLoadBalancingMethodDo d) {
        return new LoadBalancingMethod()
                .setType(d.getType())
                .setValue(d.getValue());
    }


    public static Slb toSlb(SlbDo d) {
        return new Slb()
                .setId(d.getId())
                .setName(d.getName())
                .setNginxBin(d.getNginxBin())
                .setNginxConf(d.getNginxConf())
                .setNginxWorkerProcesses(d.getNginxWorkerProcesses())
                .setStatus(d.getStatus())
                .setVersion(d.getVersion());
    }

    public static SlbServer toSlbServer(SlbServerDo d) {
        return new SlbServer()
                .setHostName(d.getHostName())
                .setIp(d.getIp());
    }

    public static Vip toVip(SlbVipDo d) {
        return new Vip()
                .setIp(d.getIp());
    }

    public static VirtualServer toVirtualServer(SlbVirtualServerDo d) {
        return new VirtualServer()
                .setId(d.getId())
                .setSlbId(d.getSlbId())
                .setPort(d.getPort())
                .setName(d.getName())
                .setSsl(d.isIsSsl());
    }

    public static Archive toGroupArchive(ArchiveGroupDo d) {
        return new Archive()
                .setId(d.getGroupId())
                .setContent(d.getContent())
                .setVersion(d.getVersion());
    }

    public static Archive toSlbArchive(ArchiveSlbDo d) {
        return new Archive()
                .setId(d.getSlbId())
                .setContent(d.getContent())
                .setVersion(d.getVersion());
    }

    public static Role toRole(AuthRoleDo roleDo){
        return new Role()
                .setId(roleDo.getId())
                .setRoleName(roleDo.getRoleName())
                .setDescription(roleDo.getDescription());
    }

    public static User toUser(AuthUserDo userDo){
        return new User()
                .setUserName(userDo.getUserName())
                .setDescription(userDo.getDescription());
    }

    public static Resource toResource(AuthResourceDo resourceDo){
        return new Resource()
                .setResourceName(resourceDo.getResourceName())
                .setDescription(resourceDo.getDescription())
                .setResourceType(resourceDo.getResourceType());
    }

    /*Entity to Do*/

    public static GroupDo toGroupDo(Long groupId, Group e) {
        return new GroupDo()
                .setId(groupId)
                .setAppId(e.getAppId())
                .setName(e.getName())
                .setSsl(e.isSsl())
                .setVersion(e.getVersion() == null ? 0 : e.getVersion());
    }

    public static GroupServerDo toGroupServerDo(GroupServer e) {
        return new GroupServerDo()
                .setIp(e.getIp())
                .setHostName(e.getHostName())
                .setFailTimeout(e.getFailTimeout())
                .setMaxFails(e.getMaxFails())
                .setPort(e.getPort())
                .setWeight(e.getWeight());
    }

    public static GroupHealthCheckDo toGroupHealthCheckDo(HealthCheck e) {
        return new GroupHealthCheckDo()
                .setUri(e.getUri())
                .setIntervals(e.getIntervals())
                .setFails(e.getFails())
                .setPasses(e.getPasses());
    }

    public static GroupLoadBalancingMethodDo toGroupLoadBalancingMethodDo(LoadBalancingMethod e) {
        return new GroupLoadBalancingMethodDo()
                .setType(e.getType())
                .setValue(e.getValue());
    }

    public static SlbDo toSlbDo(Long slbId, Slb e) {
        return new SlbDo()
                .setId(slbId)
                .setName(e.getName())
                .setNginxBin(e.getNginxBin())
                .setNginxConf(e.getNginxConf())
                .setNginxWorkerProcesses(e.getNginxWorkerProcesses())
                .setStatus(e.getStatus())
                .setVersion(e.getVersion() == null ? 0 : e.getVersion());
    }

    public static SlbServerDo toSlbServerDo(Long slbId, SlbServer e) {
        return new SlbServerDo()
                .setSlbId(slbId)
                .setHostName(e.getHostName())
                .setIp(e.getIp());
    }

    public static SlbVipDo toSlbVipDo(Long slbId, Vip e) {
        return new SlbVipDo()
                .setSlbId(slbId)
                .setIp(e.getIp());
    }

    public static SlbVirtualServerDo toSlbVirtualServerDo(Long virtualServerId, Long slbId, VirtualServer e) {
        return new SlbVirtualServerDo()
                .setId(virtualServerId)
                .setSlbId(slbId)
                .setPort(e.getPort())
                .setIsSsl(e.isSsl())
                .setName(e.getName());
    }

    public static AuthRoleDo toRoleDo(Role role) {
        return new AuthRoleDo()
                .setDescription(role.getDescription())
                .setRoleName(role.getRoleName());
    }

    public static AuthUserDo toUserDo(User user) {
        return new AuthUserDo()
                .setUserName(user.getUserName())
                .setDescription(user.getDescription());
    }

    public static AuthResourceDo toResourceDo(Resource resource){
        return new AuthResourceDo()
                .setResourceName(resource.getResourceName())
                .setResourceType(resource.getResourceType())
                .setDescription(resource.getDescription());
    }

    public static OpsTask toOpsTask(TaskDo task){
        OpsTask result = new OpsTask();
        result.setId(task.getId())
                .setTargetSlbId(task.getTargetSlbId())
                .setUp(task.isUp())
                .setVersion(task.getVersion())
                .setStatus(task.getStatus())
                .setCreateTime(task.getCreateTime())
                .setFailCause(task.getFailCause())
                .setIpList(task.getIpList())
                .setGroupId(task.getGroupId())
                .setOpsType(task.getOpsType())
                .setSlbId(task.getSlbId());
        return result;
    }
    public static TaskDo toTaskDo(OpsTask opsTask){
        TaskDo result = new TaskDo();
        result.setId(opsTask.getId()==null?0L:opsTask.getId())
                .setTargetSlbId(opsTask.getTargetSlbId()==null?0:opsTask.getTargetSlbId())
                .setUp(opsTask.isUp())
                .setVersion(opsTask.getVersion()==null?0:opsTask.getVersion())
                .setStatus(opsTask.getStatus())
                .setCreateTime(opsTask.getCreateTime())
                .setFailCause(opsTask.getFailCause())
                .setIpList(opsTask.getIpList())
                .setGroupId(opsTask.getGroupId()==null?0:opsTask.getGroupId())
                .setOpsType(opsTask.getOpsType())
                .setSlbId(opsTask.getSlbId()==null?0:opsTask.getSlbId());
        return result;
    }
}
