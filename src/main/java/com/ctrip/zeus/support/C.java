package com.ctrip.zeus.support;

import com.ctrip.zeus.auth.entity.Resource;
import com.ctrip.zeus.auth.entity.Role;
import com.ctrip.zeus.auth.entity.User;
import com.ctrip.zeus.commit.entity.Commit;
import com.ctrip.zeus.commit.entity.ConfSlbVersion;
import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.queue.entity.Message;
import com.ctrip.zeus.task.entity.OpsTask;

import java.util.List;

/**
 * @author:xingchaowang
 * @date: 3/6/2015.
 */
public class C {

    public static Role toRole(AuthRoleDo roleDo) {
        return new Role()
                .setId(roleDo.getId())
                .setRoleName(roleDo.getRoleName())
                .setDescription(roleDo.getDescription());
    }

    public static User toUser(AuthUserDo userDo) {
        return new User()
                .setUserName(userDo.getUserName())
                .setDescription(userDo.getDescription());
    }

    public static Resource toResource(AuthResourceDo resourceDo) {
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
                .setVersion(e.getVersion());
    }

    public static SlbDo toSlbDo(Long slbId, Slb e) {
        return new SlbDo()
                .setId(slbId)
                .setName(e.getName())
                .setNginxBin(e.getNginxBin())
                .setNginxConf(e.getNginxConf())
                .setNginxWorkerProcesses(e.getNginxWorkerProcesses())
                .setStatus(e.getStatus())
                .setVersion(e.getVersion());
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

    public static AuthResourceDo toResourceDo(Resource resource) {
        return new AuthResourceDo()
                .setResourceName(resource.getResourceName())
                .setResourceType(resource.getResourceType())
                .setDescription(resource.getDescription());
    }

    public static MessageQueueDo toMessageQueueDo(Message msg) {
        return new MessageQueueDo()
                .setTargetData(msg.getTargetData())
                .setTargetId(msg.getTargetId())
                .setStatus(msg.getStatus())
                .setCreateTime(msg.getCreateTime())
                .setPerformer(msg.getPerformer());
    }

    public static Message toMessage(MessageQueueDo msg) {
        return new Message()
                .setTargetData(msg.getTargetData())
                .setTargetId(msg.getTargetId())
                .setStatus(msg.getStatus())
                .setCreateTime(msg.getCreateTime())
                .setPerformer(msg.getPerformer());
    }


    public static OpsTask toOpsTask(TaskDo task) {
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
                .setPolicyId(task.getPolicyId())
                .setOpsType(task.getOpsType())
                .setResources(task.getResources())
                .setSlbVirtualServerId(task.getSlbVirtualServerId())
                .setSlbId(task.getSlbId());
        return result;
    }

    public static TaskDo toTaskDo(OpsTask opsTask) {
        TaskDo result = new TaskDo();
        result.setId(opsTask.getId() == null ? 0L : opsTask.getId())
                .setTargetSlbId(opsTask.getTargetSlbId() == null ? 0 : opsTask.getTargetSlbId())
                .setUp(opsTask.isUp())
                .setVersion(opsTask.getVersion() == null ? 0 : opsTask.getVersion())
                .setStatus(opsTask.getStatus())
                .setCreateTime(opsTask.getCreateTime())
                .setFailCause(opsTask.getFailCause())
                .setIpList(opsTask.getIpList())
                .setGroupId(opsTask.getGroupId() == null ? 0 : opsTask.getGroupId())
                .setPolicyId(opsTask.getPolicyId() == null ? 0 : opsTask.getPolicyId())
                .setOpsType(opsTask.getOpsType())
                .setResources(opsTask.getResources())
                .setSlbVirtualServerId(opsTask.getSlbVirtualServerId() == null ? 0 : opsTask.getSlbVirtualServerId())
                .setSlbId(opsTask.getSlbId() == null ? 0 : opsTask.getSlbId());
        return result;
    }


    /*Commit DO translation*/
    public static Commit toCommit(CommitDo commitDto) throws Exception {
        final Commit commit = new Commit();

        commit.setId(commitDto.getId()).
                setSlbId(commitDto.getSlbId()).
                setVersion(commitDto.getVersion())
                .setType(commitDto.getType())
                .setDataChangeLastTime(commitDto.getDataChangeLastTime());

        if (commitDto.getVsIds() != null && !commitDto.getVsIds().equals("")) {
            for (String vid : commitDto.getVsIds().split(",")) {
                commit.addVsId(Long.parseLong(vid));
            }
        }
        if (commitDto.getGroupIds() != null) {
            for (String vid : commitDto.getGroupIds().split(",")) {
                commit.addGroupId(Long.parseLong(vid));
            }
        }

        if (commitDto.getTaskIds() != null) {
            for (String vid : commitDto.getTaskIds().split(",")) {
                commit.addTaskId(Long.parseLong(vid));
            }
        }

        if (commitDto.getCleanvsIds() != null) {
            for (String vid : commitDto.getCleanvsIds().split(",")) {
                commit.addCleanvsId(Long.parseLong(vid));
            }
        }

        return commit;
    }

    public static ConfSlbVersionDo toConfSlbVersionDo(ConfSlbVersion confSlbVersion) {
        ConfSlbVersionDo result = new ConfSlbVersionDo();
        if (null != confSlbVersion) {
            result.setSlbId(confSlbVersion.getSlbId())
                    .setPreviousVersion(confSlbVersion.getPreviousVersion())
                    .setCurrentVersion(confSlbVersion.getCurrentVersion());
        }

        return result;
    }

    public static CommitDo toCommitDo(Commit commit) {
        CommitDo commitdo = new CommitDo();

        commitdo.setId(commit.getId() == null ? 0L : commit.getId()).
                setSlbId(commit.getSlbId()).
                setVersion(commit.getVersion())
                .setType(commit.getType())
                .setDataChangeLastTime(commit.getDataChangeLastTime());

        if (commit.getVsIds() != null) {
            commitdo.setVsIds(subIds(commit.getVsIds()));
        }
        if (commit.getGroupIds() != null) {
            commitdo.setGroupIds(subIds(commit.getGroupIds()));
        }

        if (commit.getTaskIds() != null) {
            commitdo.setTaskIds(subIds(commit.getTaskIds()));
        }

        if (commit.getCleanvsIds() != null) {
            commitdo.setCleanvsIds(subIds(commit.getCleanvsIds()));
        }

        return commitdo;
    }

    private static String subIds(List<Long> sub) {
        if (sub == null || sub.size() == 0) return null;

        StringBuilder sb = new StringBuilder();
        for (Long i : sub) {
            sb.append(i + ",");
        }

        String result = sb.toString();
        int lastSpliter = result.lastIndexOf(",");

        return result.substring(0, lastSpliter);
    }
}
