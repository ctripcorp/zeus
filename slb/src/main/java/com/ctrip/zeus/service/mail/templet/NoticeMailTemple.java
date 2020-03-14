package com.ctrip.zeus.service.mail.templet;

import com.ctrip.zeus.service.mail.templet.util.HtmlBuilder;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by fanqq on 2017/6/22.
 */
public final class NoticeMailTemple {
    private HtmlBuilder htmlBuilder;

    private static NoticeMailTemple instance = new NoticeMailTemple();

    public static NoticeMailTemple getInstance() {
        return instance;
    }

    private NoticeMailTemple() {
        htmlBuilder = new HtmlBuilder("warn");
    }

    public String build(String title, LinkedHashMap<String, String> description, String[][] solution) {
        if (description == null || solution == null) return "";
        StringBuilder sb = new StringBuilder(516);
        htmlBuilder.addTitle(sb, title);
        htmlBuilder.startPanel(sb);
        htmlBuilder.setPanelHeader(sb, "诊断详情");
        htmlBuilder.startPanelBody(sb);
        htmlBuilder.startTable(sb);
        int c = 0;
        for (String key : description.keySet()) {
            if (key.startsWith("link_") || key.startsWith("strong_")) {
                c++;
            }
        }
        String[][] body = new String[description.size() - c][2];
        boolean[][] strong = new boolean[description.size() - c][2];
        int i = 0;
        for (Map.Entry<String, String> p : description.entrySet()) {
            if (p.getKey().startsWith("link_") || p.getKey().startsWith("strong_")) {
                continue;
            }
            body[i][0] = p.getKey();
            if (description.containsKey("link_" + p.getKey()) && description.get("link_" + p.getKey()) != null) {
                String[] tmp = description.get("link_" + p.getKey()).split(";");
                if (tmp.length == 1) {
                    body[i][1] = htmlBuilder.buildLink(p.getValue(), tmp[0]);
                } else {
                    body[i][1] = p.getValue() + "&nbsp" + htmlBuilder.buildLink(tmp[0], tmp[1]);
                }
            } else {
                body[i][1] = p.getValue();
            }
            strong[i][1] = description.containsKey("strong_" + p.getKey()) &&
                    description.get("strong_" + p.getKey()).equalsIgnoreCase("true");
            i++;
        }
        htmlBuilder.setTableBody(sb, body, new int[]{1, 2}, strong);
        htmlBuilder.endTable(sb);
        htmlBuilder.endPanelBody(sb);
        htmlBuilder.endPanel(sb);

        htmlBuilder.startPanel(sb);
        htmlBuilder.setPanelHeader(sb, "解决方案");
        htmlBuilder.startPanelBody(sb);
        htmlBuilder.startList(sb);
        for (String[] sub : solution) {
            htmlBuilder.addList(sb, sub[0], Arrays.copyOfRange(sub, 1, sub.length));
        }
        htmlBuilder.endList(sb);
        htmlBuilder.endPanelBody(sb);
        htmlBuilder.endPanel(sb);
        return sb.toString();
    }

    public String buildUserNoticement(String title, List<String> headers, String[][] body, String[] titleList, String ending) {
        StringBuilder sb = new StringBuilder(516);
        htmlBuilder.addTitleWithList(sb, title, titleList);
        htmlBuilder.startPanel(sb);
        htmlBuilder.setPanelHeader(sb, "问题汇总");
        htmlBuilder.startPanelBody(sb);
        htmlBuilder.startTable(sb);
        htmlBuilder.setTableHead(sb, headers);
        htmlBuilder.setTableBody(sb, body, new int[]{2, 1, 3, 2}, null);
        htmlBuilder.endTable(sb);
        htmlBuilder.endPanelBody(sb);
        htmlBuilder.endPanel(sb);
        htmlBuilder.addLine(sb, "");
        htmlBuilder.addLine(sb, ending);
        return sb.toString();
    }
}
