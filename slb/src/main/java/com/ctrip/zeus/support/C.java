package com.ctrip.zeus.support;

import com.ctrip.zeus.dao.entity.*;
import com.ctrip.zeus.model.Certificate;
import com.ctrip.zeus.model.alert.AlertItem;
import com.ctrip.zeus.model.approval.Approval;
import com.ctrip.zeus.model.commit.Commit;
import com.ctrip.zeus.model.queue.Message;
import com.ctrip.zeus.model.task.OpsTask;
import com.ctrip.zeus.model.tools.VsMigration;
import com.ctrip.zeus.util.CertUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author:xingchaowang
 * @date: 3/6/2015.
 */
public class C {

    private static Logger logger = LoggerFactory.getLogger(C.class);

    public static Message toMessage(MessageQueue msg) {
        return new Message()
                .setTargetData(msg.getTargetData())
                .setTargetId(msg.getTargetId())
                .setStatus(msg.getStatus())
                .setType(msg.getType())
                .setCreateTime(msg.getCreateTime())
                .setPerformer(msg.getPerformer());
    }

    public static OpsTask toOpsTask(TaskTask task) {
        OpsTask result = new OpsTask();
        result.setId(task.getId())
                .setTargetSlbId(task.getTargetSlbId())
                .setUp(task.getUp())
                .setVersion(task.getVersion())
                .setStatus(task.getStatus())
                .setCreateTime(task.getCreateTime())
                .setFailCause(task.getFailCause())
                .setIpList(task.getIpList())
                .setGroupId(task.getGroupId())
                .setPolicyId(task.getPolicyId())
                .setDrId(task.getDrId())
                .setOpsType(task.getOpsType())
                .setResources(task.getResources())
                .setSlbVirtualServerId(task.getSlbVirtualServerId())
                .setSlbId(task.getSlbId())
                .setSkipValidate(task.getSkipValidate());
        result.setTaskList(task.getTaskList() == null ? null : new String(task.getTaskList()));
        return result;
    }

    public static TaskTask toTaskTask(OpsTask opsTask) {
        TaskTask result = new TaskTask();
        if (opsTask == null) {
            return null;
        }
        result.setId(opsTask.getId() == null ? 0L : opsTask.getId());
        result.setTargetSlbId(opsTask.getTargetSlbId() == null ? 0 : opsTask.getTargetSlbId());
        result.setUp(opsTask.isUp());
        result.setVersion(opsTask.getVersion() == null ? 0 : opsTask.getVersion());
        result.setStatus(opsTask.getStatus());
        result.setCreateTime(opsTask.getCreateTime());
        result.setFailCause(opsTask.getFailCause());
        result.setIpList(opsTask.getIpList());
        result.setGroupId(opsTask.getGroupId() == null ? 0 : opsTask.getGroupId());
        result.setPolicyId(opsTask.getPolicyId() == null ? 0 : opsTask.getPolicyId());
        result.setDrId(opsTask.getDrId() == null ? 0 : opsTask.getDrId());
        result.setOpsType(opsTask.getOpsType());
        result.setResources(opsTask.getResources());
        result.setSlbVirtualServerId(opsTask.getSlbVirtualServerId() == null ? 0 : opsTask.getSlbVirtualServerId());
        result.setSlbId(opsTask.getSlbId() == null ? 0 : opsTask.getSlbId());
        result.setSkipValidate(opsTask.getSkipValidate() == null ? false : opsTask.getSkipValidate());
        result.setTaskList(opsTask.getTaskList() == null ? null : opsTask.getTaskList().getBytes());
        return result;
    }

    public static Commit slbBuildCommitDoToCommit(SlbBuildCommit commit) {
        final Commit result = new Commit();

        result.setId(commit.getId()).
                setSlbId(commit.getSlbId()).
                setVersion(commit.getVersion())
                .setType(commit.getType())
                .setDataChangeLastTime(commit.getDatachangeLasttime());

        if (commit.getVsIds() != null && !commit.getVsIds().equals("")) {
            for (String vid : commit.getVsIds().split(",")) {
                result.addVsId(Long.parseLong(vid));
            }
        }
        if (commit.getGroupIds() != null) {
            for (String vid : commit.getGroupIds().split(",")) {
                result.addGroupId(Long.parseLong(vid));
            }
        }

        if (commit.getTaskIds() != null) {
            for (String vid : commit.getTaskIds().split(",")) {
                result.addTaskId(Long.parseLong(vid));
            }
        }

        if (commit.getCleanvsIds() != null) {
            for (String vid : commit.getCleanvsIds().split(",")) {
                result.addCleanvsId(Long.parseLong(vid));
            }
        }

        return result;
    }

    public static SlbBuildCommit toSlbBuildCommitDo(Commit commit) {
        SlbBuildCommit result = SlbBuildCommit.builder().
                id(commit.getId() == null ? 0L : commit.getId()).
                slbId(commit.getSlbId()).
                version(commit.getVersion()).
                type(commit.getType()).
                datachangeLasttime(commit.getDataChangeLastTime()).
                build();

        if (commit.getVsIds() != null) {
            result.setVsIds(subIds(commit.getVsIds()));
        }
        if (commit.getGroupIds() != null) {
            result.setGroupIds(subIds(commit.getGroupIds()));
        }

        if (commit.getTaskIds() != null) {
            result.setTaskIds(subIds(commit.getTaskIds()));
        }

        if (commit.getCleanvsIds() != null) {
            result.setCleanvsIds(subIds(commit.getCleanvsIds()));
        }

        return result;
    }

    public static Approval toApproval(AuthApprove approvalDo) {
        Approval approval = new Approval();

        if (approvalDo == null) return null;
        approval.
                setId(approvalDo.getId()).
                setApproved(approvalDo.getApproved()).
                setApprovedBy(approvalDo.getApprovedBy()).
                setApprovedTime(approvalDo.getApprovedTime()).
                setApplyBy(approvalDo.getApplyBy()).
                setApplyTime(approvalDo.getApplyTime()).
                setApplyType(approvalDo.getApplyType());

        String ops = approvalDo.getApplyOps();

        if (ops != null) {
            String[] opsArray = ops.split(";");

            for (String op : opsArray) {
                if (!op.isEmpty()) {
                    approval.addApplyOp(op);
                }
            }
        }

        String targets = approvalDo.getApplyTargets();

        if (targets != null) {
            String[] targetsArray = targets.split(";");

            for (String target : targetsArray) {
                if (!target.isEmpty()) {
                    approval.addApplyTarget(Long.parseLong(target));
                }
            }
        }

        return approval;
    }

    public static VsMigration toVsMigration(ToolsVsMigration original) throws UnsupportedEncodingException {
        VsMigration migration = new VsMigration();

        if (original == null) return null;

        migration.
                setContent(new String(original.getContent(), "UTF-8")).
                setId(original.getId()).
                setName(original.getName()).
                setStatus(original.getStatus());

        return migration;
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

    public static UnhealthyAlertItem toUnhealthyAlertItem(AlertItem original) throws JsonProcessingException {
        if (original == null) {
            return null;
        }
        UnhealthyAlertItem result = new UnhealthyAlertItem();
        result.setId(original.getId());
        result.setStatus(original.getStatus());
        result.setTarget(original.getTarget());
        result.setType(original.getType());
        result.setContent(ObjectJsonWriter.write(original).getBytes());
        return result;
    }

    public static AlertItem toAlertItem(UnhealthyAlertItem original) {
        if (original == null) {
            return null;
        }
        AlertItem result = new AlertItem();
        if (original.getContent() != null) {
            AlertItem parsed = ObjectJsonParser.parse(new String(original.getContent()), AlertItem.class);
            result.setPerformer(parsed.getPerformer());
            result.setVersions(parsed.getVersions());
            result.setAppearTime(parsed.getAppearTime());
            result.setLastNoticeTime(parsed.getLastNoticeTime());
            result.setAlertType(parsed.getAlertType());
            result.setSolvedTime(parsed.getSolvedTime());
        }
        result.setId(original.getId());
        result.setType(original.getType());
        result.setStatus(original.getStatus());
        result.setTarget(original.getTarget());

        return result;
    }

    public static ToolsVsMigration toToolsVsMigration(VsMigration original) {
        ToolsVsMigration result = ToolsVsMigration.builder()
                .content(original.getContent() == null ? null : original.getContent().getBytes())
                .id(original.getId())
                .name(original.getName())
                .status(original.getStatus())
                .build();
        
        return result;
    }

    public static CertCertificateWithBLOBs toCertCertificateWithBlobs(Certificate certificate) {
        if (certificate == null) {
            return null;
        }

        return CertCertificateWithBLOBs.builder()
                .domain(certificate.getDomain())
                .id(certificate.getId())
                .cert(certificate.getCertData() == null ? null : certificate.getCertData().getBytes())
                .key(certificate.getKeyData() == null ? null : certificate.getKeyData().getBytes())
                .cid(certificate.getCid())
                .state(true)
                .version(1).build();
    }

    public static Certificate toCertificate(CertCertificateWithBLOBs record, List<Long> vsIds, List<String> slbServers) {
        Certificate certificate = new Certificate();

        certificate.setCertData(record.getCert() == null ? null : new String(record.getCert()));
        certificate.setKeyData(record.getKey() == null ? null : new String(record.getKey()));

        try {
            CertUtil.CertWrapper wrapper = CertUtil.getCertWrapper(new ByteArrayInputStream(record.getCert()));
            certificate.setIssueTime(wrapper.getIssueTime());
            certificate.setExpireTime(wrapper.getExpireTime());

        } catch (CertificateException e) {
            logger.error("CertificateException is thrown when trying to parse expiretime out of cert. ssl.crt may be broken. ");
        }
        certificate.setId(record.getId());
        certificate.setVsIds(vsIds == null ? new ArrayList<>(): vsIds);
        certificate.setSlbServers(slbServers == null ? new ArrayList<>(): slbServers);

        certificate.setDomain(record.getDomain());
        certificate.setCid(record.getCid());

        return certificate;
    }

    public static Certificate toCertificate(CertCertificate record, List<Long> vsIds, List<String> slbServers) {
        Certificate certificate = new Certificate();

        certificate.setId(record.getId());
        certificate.setVsIds(vsIds == null ? new ArrayList<>(): vsIds);
        certificate.setSlbServers(slbServers == null ? new ArrayList<>(): slbServers);

        certificate.setDomain(record.getDomain());
        certificate.setCid(record.getCid());

        return certificate;
    }
}


