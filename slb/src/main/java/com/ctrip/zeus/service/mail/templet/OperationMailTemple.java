package com.ctrip.zeus.service.mail.templet;

import com.ctrip.zeus.service.mail.templet.util.HtmlBuilder;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by fanqq on 2017/8/7.
 */
public class OperationMailTemple {
    private HtmlBuilder htmlBuilder;

    private static OperationMailTemple instance = new OperationMailTemple();

    public static OperationMailTemple getInstance() {
        return instance;
    }

    private OperationMailTemple() {
        htmlBuilder = new HtmlBuilder("default");
    }

    public String build(String title, LinkedHashMap<String, String> description) {
        return build(title, description, null);
    }

    public String build(String title, LinkedHashMap<String, String> description, String end) {
        return build(title, "操作详情", description, end);
    }

    public String build(String title, String pTitle, LinkedHashMap<String, String> description, String end) {
        StringBuilder sb = new StringBuilder(516);
        htmlBuilder.addTitle(sb, title);
        htmlBuilder.startPanel(sb);
        htmlBuilder.setPanelHeader(sb, pTitle);
        htmlBuilder.startPanelBody(sb);
        htmlBuilder.startTable(sb);
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
        if (end != null) {
            htmlBuilder.addEnd(sb, end);
        }
        return sb.toString();
    }
}
