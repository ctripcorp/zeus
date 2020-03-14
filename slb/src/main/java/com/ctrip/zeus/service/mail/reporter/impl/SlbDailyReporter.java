package com.ctrip.zeus.service.mail.reporter.impl;

import com.ctrip.zeus.service.mail.MailService;
import com.ctrip.zeus.service.mail.model.SlbMail;
import com.ctrip.zeus.service.mail.reporter.AbstractMailReporter;
import com.ctrip.zeus.service.mail.templet.DefaultSlbReporterTemplet;
import com.ctrip.zeus.service.operationLog.OperationLogService;
import com.ctrip.zeus.service.query.GroupCriteriaQuery;
import com.ctrip.zeus.service.query.SlbCriteriaQuery;
import com.ctrip.zeus.service.query.TrafficPolicyQuery;
import com.ctrip.zeus.service.query.VirtualServerCriteriaQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;

/**
 * Created by fanqq on 2017/3/21.
 */
@Service("slbDailyReporter")
public class SlbDailyReporter extends AbstractMailReporter {

    @Resource
    private SlbCriteriaQuery slbCriteriaQuery;
    @Resource
    private GroupCriteriaQuery groupCriteriaQuery;
    @Resource
    private VirtualServerCriteriaQuery virtualServerCriteriaQuery;
    @Resource
    private TrafficPolicyQuery trafficPolicyQuery;
    @Resource
    private OperationLogService operationLogService;
    @Autowired
    private MailService mailService;

    @Override
    public boolean should() {
        return true;
    }

    @Override
    public void report() throws Exception {
        SlbMail mail = new SlbMail();
        String env = System.getProperty("archaius.deployment.environment");
        Calendar calendar = Calendar.getInstance();
        int d = calendar.get(Calendar.DAY_OF_MONTH);
        int m = calendar.get(Calendar.MONTH);
        int y = calendar.get(Calendar.YEAR);
        calendar.set(y, m, d, 0, 0, 0);
        Date day2 = calendar.getTime();
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        Date day1 = calendar.getTime();
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        Date day0 = calendar.getTime();
        mail.setSubject("SLB 日报[" + env.toUpperCase() + "]");
        mail.getRecipients().add(slbTeamMailAddr.get());
        mail.setHtml(true);

        Set<Long> slbIds = slbCriteriaQuery.queryAll();
        Set<Long> vsIds = virtualServerCriteriaQuery.queryAll();
        Set<Long> groupIds = groupCriteriaQuery.queryAll();
        Set<Long> policyIds = trafficPolicyQuery.queryAll();

        long pullCount = operationLogService.count(new String[]{"pullIn", "pullOut"}, "GROUP", day1, day2);
        long pullSuccessCount = operationLogService.count(new String[]{"pullIn", "pullOut"}, "GROUP", true, day1, day2);
        long memberCount = operationLogService.count(new String[]{"memberDown", "memberUp"}, "GROUP", day1, day2);
        long memberSuccessCount = operationLogService.count(new String[]{"memberDown", "memberUp"}, "GROUP", true, day1, day2);
        long healthCount = operationLogService.count(new String[]{"raise", "fall"}, "GROUP", day1, day2);
        long healthSuccessCount = operationLogService.count(new String[]{"raise", "fall"}, "GROUP", true, day1, day2);
        long updateVsCount = operationLogService.count("update", "VS", day1, day2);
        long updateGroupCount = operationLogService.count("update", "GROUP", day1, day2);
        long updateSlbCount = operationLogService.count("update", "SLB", day1, day2);
        long updatePolicyCount = operationLogService.count("update", "POLICY", day1, day2);
        long pullCount0 = operationLogService.count(new String[]{"pullIn", "pullOut"}, "GROUP", day0, day1);
        long pullSuccessCount0 = operationLogService.count(new String[]{"pullIn", "pullOut"}, "GROUP", true, day0, day1);
        long memberCount0 = operationLogService.count(new String[]{"memberDown", "memberUp"}, "GROUP", day0, day1);
        long memberSuccessCount0 = operationLogService.count(new String[]{"memberDown", "memberUp"}, "GROUP", true, day0, day1);
        long healthCount0 = operationLogService.count(new String[]{"raise", "fall"}, "GROUP", day0, day1);
        long healthSuccessCount0 = operationLogService.count(new String[]{"raise", "fall"}, "GROUP", true, day0, day1);
        long updateVsCount0 = operationLogService.count("update", "VS", day0, day1);
        long updateGroupCount0 = operationLogService.count("update", "GROUP", day0, day1);
        long updateSlbCount0 = operationLogService.count("update", "SLB", day0, day1);
        long updatePolicyCount0 = operationLogService.count("update", "POLICY", day0, day1);
        long groupNewCount = operationLogService.count("new", "GROUP", day1, day2);
        long slbNewCount = operationLogService.count("new", "SLB", day1, day2);
        long vsNewCount = operationLogService.count("new", "VS", day1, day2);
        long policyNewCount = operationLogService.count("new", "POLICY", day1, day2);
        long groupDeleteCount = operationLogService.count("delete", "GROUP", day1, day2);
        long slbDeleteCount = operationLogService.count("delete", "SLB", day1, day2);
        long vsDeleteCount = operationLogService.count("delete", "VS", day1, day2);
        long policyDeleteCount = operationLogService.count("delete", "POLICY", day1, day2);

        DefaultSlbReporterTemplet defaultSlbReporterTemplet = new DefaultSlbReporterTemplet();
        defaultSlbReporterTemplet.setTitle(mail.getSubject());
        Long count = 0L;
        defaultSlbReporterTemplet.setPanelHeader("SLB统计信息", "类型", "数量", "环比");
        count = slbNewCount - slbDeleteCount;
        defaultSlbReporterTemplet.addPanelRow("SLB统计信息", "SLBs", String.valueOf(slbIds.size()), count < 0 ? "" + count : "+" + count);
        count = vsNewCount - vsDeleteCount;
        defaultSlbReporterTemplet.addPanelRow("SLB统计信息", "Vses", String.valueOf(vsIds.size()), count < 0 ? "" + count : "+" + count);
        count = groupNewCount - groupDeleteCount;
        defaultSlbReporterTemplet.addPanelRow("SLB统计信息", "Groups", String.valueOf(groupIds.size()), count < 0 ? "" + count : "+" + count);
        count = policyNewCount - policyDeleteCount;
        defaultSlbReporterTemplet.addPanelRow("SLB统计信息", "Policies", String.valueOf(policyIds.size()), count < 0 ? "" + count : "+" + count);
        defaultSlbReporterTemplet.setPanelHeader("模型操作", "类型", "数量", "环比");
        count = updateSlbCount - updateSlbCount0;
        defaultSlbReporterTemplet.addPanelRow("模型操作", "Slb更新", String.valueOf(updateSlbCount), count < 0 ? "" + count : "+" + count);
        count = updateVsCount - updateVsCount0;
        defaultSlbReporterTemplet.addPanelRow("模型操作", "Vses更新", String.valueOf(updateVsCount), count < 0 ? "" + count : "+" + count);
        count = updateGroupCount - updateGroupCount0;
        defaultSlbReporterTemplet.addPanelRow("模型操作", "Group更新", String.valueOf(updateGroupCount), count < 0 ? "" + count : "+" + count);
        count = updatePolicyCount - updatePolicyCount0;
        defaultSlbReporterTemplet.addPanelRow("模型操作", "Policy更新", String.valueOf(updatePolicyCount), count < 0 ? "" + count : "+" + count);
        Double successChange = 0.0;
        Double success = 0.0;
        defaultSlbReporterTemplet.setPanelHeader("运营操作", "类型", "数量/环比", "成功率/环比");
        count = pullCount - pullCount0;
        success = pullCount > 0 ? pullSuccessCount * 100.0 / pullCount : 0;
        successChange = success - (pullCount0 > 0 ? pullSuccessCount0 * 100.0 / pullCount0 : 0);
        defaultSlbReporterTemplet.addPanelRow("运营操作", "发布拉入&拉出", String.valueOf(pullCount) + "/" + getNumberPrefix(count) + count,
                String.format("%.2f", success) + "%/" + getNumberPrefix(successChange) + String.format("%.2f", successChange) + "%");
        count = memberCount - memberCount0;
        success = memberCount > 0 ? memberSuccessCount * 100.0 / memberCount : 0;
        successChange = success - (memberCount0 > 0 ? memberSuccessCount0 * 100.0 / memberCount0 : 0);
        defaultSlbReporterTemplet.addPanelRow("运营操作", "Member拉入&拉出", String.valueOf(memberCount) + "/" + getNumberPrefix(count) + count,
                String.format("%.2f", success) + "%/" + getNumberPrefix(successChange) + String.format("%.2f", successChange) + "%");
        count = healthCount - healthCount0;
        success = healthCount > 0 ? healthSuccessCount * 100.0 / healthCount : 0;
        successChange = success - (healthCount0 > 0 ? healthSuccessCount0 * 100.0 / healthCount0 : 0);
        defaultSlbReporterTemplet.addPanelRow("运营操作", "健康检测拉入&拉出", String.valueOf(healthCount) + "/" + getNumberPrefix(count) + count,
                String.format("%.2f", success) + "%/" + getNumberPrefix(successChange) + String.format("%.2f", successChange) + "%");
        mail.setBody(defaultSlbReporterTemplet.build());
        mailService.sendEmail(mail);
    }

    private <T> String getNumberPrefix(T count) {
        if (count instanceof Double && (Double) count > 0) {
            return "+";
        } else if (count instanceof Long && (Long) count > 0) {
            return "+";
        } else {
            return "";
        }
    }
}
