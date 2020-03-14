package com.ctrip.zeus.service.mail.reporter.impl;

import com.ctrip.zeus.model.model.App;
import com.ctrip.zeus.model.model.Group;
import com.ctrip.zeus.model.model.TrafficControl;
import com.ctrip.zeus.model.model.TrafficPolicy;
import com.ctrip.zeus.service.app.AppService;
import com.ctrip.zeus.service.mail.MailService;
import com.ctrip.zeus.service.mail.model.SlbMail;
import com.ctrip.zeus.service.mail.reporter.AbstractMailReporter;
import com.ctrip.zeus.service.mail.templet.DefaultSlbReporterTemplet;
import com.ctrip.zeus.service.model.GroupRepository;
import com.ctrip.zeus.service.model.IdVersion;
import com.ctrip.zeus.service.model.SelectionMode;
import com.ctrip.zeus.service.model.TrafficPolicyRepository;
import com.ctrip.zeus.service.query.GroupCriteriaQuery;
import com.ctrip.zeus.service.query.TrafficPolicyQuery;
import com.ctrip.zeus.tag.PropertyService;
import com.ctrip.zeus.tag.TagService;
import com.netflix.config.DynamicBooleanProperty;
import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * Created by fanqq on 2017/4/17.
 */
@Service("netToJavaWeeklyReporter")
public class NetToJavaWeeklyReporter extends AbstractMailReporter {
    @Resource
    PropertyService propertyService;
    @Resource
    TagService tagService;
    @Autowired
    GroupRepository groupRepository;
    @Resource
    GroupCriteriaQuery groupCriteriaQuery;
    @Autowired
    private AppService appService;
    @Resource
    TrafficPolicyRepository trafficPolicyRepository;
    @Resource
    private TrafficPolicyQuery trafficPolicyQuery;
    @Autowired
    private MailService mailService;

    private DynamicBooleanProperty shouldReport = DynamicPropertyFactory.getInstance().getBooleanProperty("NetToJavaWeeklyReporter.enable", true);
    private DynamicBooleanProperty forceReportEveryDay = DynamicPropertyFactory.getInstance().getBooleanProperty("NetToJavaWeeklyReporter.report.every.day.enable", false);
    private DynamicStringProperty recipients = DynamicPropertyFactory.getInstance().getStringProperty("slb.team.mail", "alias@domain.com");
    private DynamicStringProperty skipBus = DynamicPropertyFactory.getInstance().getStringProperty("slb.net2java.mail.skip.sbu", "框架");
    private DynamicIntProperty reportDay = DynamicPropertyFactory.getInstance().getIntProperty("slb.net2java.mail.report.day", 1);

    @Override
    public boolean should() {
        if (forceReportEveryDay.get()) return true;
        if (shouldReport.get()) {
            int day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
            if (reportDay.get() == day) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void report() throws Exception {
        Set<IdVersion> groupIdVersions = groupCriteriaQuery.queryAll(SelectionMode.ONLINE_EXCLUSIVE);
        List<Group> groups = groupRepository.list(groupIdVersions.toArray(new IdVersion[groupIdVersions.size()]));
        Map<Long, Group> groupMap = new HashMap<>();
        Map<String, App> appMap = new HashMap<>();
        Set<String> appIds = new HashSet<>();
        for (Group g : groups) {
            groupMap.put(g.getId(), g);
            appIds.add(g.getAppId());
        }
        List<App> apps = appService.getAllAppsByAppIds(appIds);
        for (App app : apps) {
            appMap.put(app.getAppId(), app);
        }
        List<Long> soaGroups = tagService.query("soa", "group");
        List<Long> netGroups = propertyService.queryTargets("language", ".net", "group");
        List<Long> javaGroups = propertyService.queryTargets("language", "java", "group");
        javaGroups.retainAll(soaGroups);
        netGroups.retainAll(soaGroups);
        final Map<String, Long> sbuNetAppsCount = new HashMap<>();
        final Map<String, Long> sbuJavaAppsCount = new HashMap<>();
        final Map<String, Long> sbuPolicyCount = new HashMap<>();
        for (Long gid : netGroups) {
            if (!groupMap.containsKey(gid)) continue;
            String appId = groupMap.get(gid).getAppId();
            App app = appMap.get(appId);
            if (app == null) continue;
            Long count = sbuNetAppsCount.get(app.getSbu());
            if (count == null) {
                sbuNetAppsCount.put(app.getSbu(), 1L);
            } else {
                sbuNetAppsCount.put(app.getSbu(), ++count);
            }
        }
        for (Long gid : javaGroups) {
            if (!groupMap.containsKey(gid)) continue;
            String appId = groupMap.get(gid).getAppId();
            App app = appMap.get(appId);
            if (app == null) continue;
            Long count = sbuJavaAppsCount.get(app.getSbu());
            if (count == null) {
                sbuJavaAppsCount.put(app.getSbu(), 1L);
            } else {
                sbuJavaAppsCount.put(app.getSbu(), ++count);
            }
        }
        List<Long> policyList = propertyService.queryTargets("target", ".NET 转 Java", "policy");
        Set<IdVersion> pIdvs = trafficPolicyQuery.queryByIdsAndMode(policyList.toArray(new Long[policyList.size()]), SelectionMode.ONLINE_FIRST);
        List<TrafficPolicy> policies = trafficPolicyRepository.list(pIdvs.toArray(new IdVersion[pIdvs.size()]));
        for (TrafficPolicy p : policies) {
            for (TrafficControl control : p.getControls()) {
                Long gid = control.getGroup().getId();
                if (!netGroups.contains(gid)) continue;
                String appId = groupMap.get(gid).getAppId();
                App app = appMap.get(appId);
                if (app == null) continue;
                Long count = sbuPolicyCount.get(app.getSbu());
                if (count == null) {
                    sbuPolicyCount.put(app.getSbu(), 1L);
                    break;
                } else {
                    sbuPolicyCount.put(app.getSbu(), ++count);
                    break;
                }
            }
        }
        List<String> sbus = new ArrayList<>(sbuNetAppsCount.keySet());
        List<String> netSoaList = new ArrayList<>(sbuNetAppsCount.keySet());
        for (String bu : skipBus.get().split(";")) {
            sbus.remove(bu);
            netSoaList.remove(bu);
        }
        Collections.sort(sbus, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                Long c1 = sbuPolicyCount.get(o1) == null ? 0L : sbuPolicyCount.get(o1);
                Long c2 = sbuPolicyCount.get(o2) == null ? 0L : sbuPolicyCount.get(o2);
                return (int) (c2 - c1);
            }
        });
        Collections.sort(netSoaList, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                Long c1 = sbuNetAppsCount.get(o1) == null ? 0L : sbuNetAppsCount.get(o1);
                Long c2 = sbuNetAppsCount.get(o2) == null ? 0L : sbuNetAppsCount.get(o2);
                return (int) (c2 - c1);
            }
        });
        sendMail(sbus,sbuJavaAppsCount,sbuNetAppsCount,sbuPolicyCount,netSoaList);
    }

    private void sendMail(List<String> sbus, Map<String, Long> sbuJavaAppsCount, Map<String, Long> sbuNetAppsCount, Map<String, Long> sbuPolicyCount, List<String> netSoaList) throws Exception {
        SlbMail mail = new SlbMail();
        String env = System.getProperty("archaius.deployment.environment");
        mail.setSubject("【SLB 周报】Soa服务.Net转Java进度周报 [" + env.toUpperCase() + "]");
        mail.getRecipients().add(recipients.get());
        mail.setHtml(true);
        DefaultSlbReporterTemplet defaultSlbReporterTemplet = new DefaultSlbReporterTemplet();
        defaultSlbReporterTemplet.setTitle(mail.getSubject());
        defaultSlbReporterTemplet.addTitleLink("http://locahost:8099/portal/user/user-trafficpolicy#?env=pro", "[开启流量灰度]");
        defaultSlbReporterTemplet.setTitle("Soa服务.Net转Java进度周报");
        defaultSlbReporterTemplet.setPanelHeader("进度 Top10", "BU", "Soa Java/Soa .Net", "流量灰度", "完成度");
        for (int i = 0; i < 10; i++) {
            String sbu = sbus.get(i);
            Long javaCount = sbuJavaAppsCount.get(sbu) == null ? 0L : sbuJavaAppsCount.get(sbu);
            Long netCount = sbuNetAppsCount.get(sbu) == null ? 0L : sbuNetAppsCount.get(sbu);
            Long policy = sbuPolicyCount.get(sbu) == null ? 0L : sbuPolicyCount.get(sbu);
            double complete = policy * 100.0 / netCount;
            defaultSlbReporterTemplet.addPanelRow("进度 Top10", sbu, javaCount.toString() + "/" + netCount, policy.toString(), String.format("%.1f", complete) + "%");
        }
        defaultSlbReporterTemplet.setPanelHeader("Soa服务 Top10", "BU", "Soa Java/Soa .Net", "流量灰度", "完成度");
        for (int i = 0; i < 10; i++) {
            String sbu = netSoaList.get(i);
            Long javaCount = sbuJavaAppsCount.get(sbu) == null ? 0L : sbuJavaAppsCount.get(sbu);
            Long netCount = sbuNetAppsCount.get(sbu) == null ? 0L : sbuNetAppsCount.get(sbu);
            Long policy = sbuPolicyCount.get(sbu) == null ? 0L : sbuPolicyCount.get(sbu);
            double complete = policy * 100.0 / netCount;
            defaultSlbReporterTemplet.addPanelRow("Soa服务 Top10", sbu, javaCount.toString() + "/" + netCount, policy.toString(), String.format("%.1f", complete) + "%");
        }
        mail.setBody(defaultSlbReporterTemplet.build());
        mailService.sendEmail(mail);
    }
}
