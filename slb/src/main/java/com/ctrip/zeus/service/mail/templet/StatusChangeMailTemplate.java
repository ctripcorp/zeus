package com.ctrip.zeus.service.mail.templet;

import com.ctrip.zeus.service.mail.templet.util.HtmlBuilder;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;

import java.util.LinkedHashMap;
import java.util.Map;

public class StatusChangeMailTemplate {

    DynamicStringProperty env =
            DynamicPropertyFactory.getInstance().getStringProperty("slb.portal.url.env", "pro");

    DynamicStringProperty slbPortal =
            DynamicPropertyFactory.getInstance().getStringProperty("slb.portal.url", "http://localhost/portal/");


    private HtmlBuilder htmlBuilder;

    private static StatusChangeMailTemplate instance = new StatusChangeMailTemplate();

    public StatusChangeMailTemplate() {
        htmlBuilder = new HtmlBuilder("Warn");
    }

    public static StatusChangeMailTemplate getInstance() {
        return instance;
    }

    public String build(String groupName, Long groupId, String appId, String online, String offline, String appearTime, String performer, String type, String title) {

        String slbPortalGroup = slbPortal.get() + "group#?env=" + env.get() + "&compareVersions=true&";
        String slbPortalPolicy = slbPortal.get() + "policy#?env=" + env.get() + "&compareVersions=true&";

        StringBuilder sb = new StringBuilder(516);
        htmlBuilder.addTitle(sb, title);
        htmlBuilder.startPanel(sb);
        htmlBuilder.setPanelHeader(sb, "变更详情");
        htmlBuilder.startPanelBody(sb);
        htmlBuilder.startTable(sb);

        LinkedHashMap<String, String> description = new LinkedHashMap<>();
        if ("group".equalsIgnoreCase(type)) {
            description.put("Group名称", groupName);
            description.put("link_Group名称", slbPortalGroup + "groupId=" + groupId);

            description.put("AppId", appId);

            description.put("link_变更", slbPortalGroup + "groupId=" + groupId + "&online=" + online + "&offline=" + offline);
        }

        if ("policy".equalsIgnoreCase(type)) {
            description.put("策略名称", groupName);
            description.put("link_策略名称", slbPortalPolicy + "policyId=" + groupId);

            description.put("link_变更", slbPortalPolicy + "policyId=" + groupId + "&online=" + online + "&offline=" + offline);
        }

        description.put("变更时间", appearTime);
        description.put("变更", "点击查看变更");
        description.put("变更者", performer);

        int c = 0;
        for (String key : description.keySet()) {
            if (key.startsWith("link_")) {
                c++;
            }
        }
        String[][] body = new String[description.size() - c][2];
        int i = 0;
        for (Map.Entry<String, String> p : description.entrySet()) {
            if (p.getKey().startsWith("link_")) {
                continue;
            }
            body[i][0] = p.getKey();
            if (description.containsKey("link_" + p.getKey())) {
                body[i][1] = htmlBuilder.buildLink(p.getValue(), description.get("link_" + p.getKey()));
            } else {
                body[i][1] = p.getValue();
            }
            i++;
        }

        htmlBuilder.setTableBody(sb, body);
        htmlBuilder.endTable(sb);

        htmlBuilder.endPanelBody(sb);
        htmlBuilder.endPanel(sb);
        return sb.toString();
    }
}
