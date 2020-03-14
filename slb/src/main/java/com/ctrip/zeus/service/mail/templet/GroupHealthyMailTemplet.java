package com.ctrip.zeus.service.mail.templet;

import com.ctrip.zeus.service.mail.templet.util.HtmlBuilder;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;

import java.util.List;

/**
 * Created by fanqq on 2017/4/26.
 */
public class GroupHealthyMailTemplet {
    private HtmlBuilder htmlBuilder;

    private static GroupHealthyMailTemplet instance = new GroupHealthyMailTemplet();

    DynamicStringProperty slbPortalGroup = DynamicPropertyFactory.getInstance().getStringProperty("slb.portal.url", "http://localhost/portal/");

    public GroupHealthyMailTemplet() {
        htmlBuilder = new HtmlBuilder("Warn");
    }

    public static GroupHealthyMailTemplet getInstance() {
        return instance;
    }

    public String build(String op, String groupName, Long groupId, String appId, List<String> ips, String des) {
        StringBuilder sb = new StringBuilder(516);
        htmlBuilder.addTitle(sb, "集群可用性");
        htmlBuilder.startPanel(sb);
        htmlBuilder.setPanelHeader(sb, op);
        htmlBuilder.startPanelBody(sb);
        htmlBuilder.addLine(sb, "GroupName:" + groupName);
        htmlBuilder.addLine(sb, "AppId:" + appId);
        htmlBuilder.addLine(sb, "Ips:" + ips.toString());
        htmlBuilder.addLine(sb, "Description:" + des);
        htmlBuilder.addLink(sb, new String[]{slbPortalGroup.get() + "group#?groupId=" + groupId+"&env=pro", "点击查看应用详情"});
        htmlBuilder.endPanelBody(sb);
        htmlBuilder.endPanel(sb);
        return sb.toString();
    }
}
