package com.ctrip.zeus.service.mail.templet;

import com.ctrip.zeus.service.mail.templet.util.HtmlBuilder;

import java.util.List;

/**
 * Created by fanqq on 2017/8/28.
 */
public final class NoticeMailServerListTemple {
    private HtmlBuilder htmlBuilder;

    private static NoticeMailServerListTemple instance = new NoticeMailServerListTemple();

    public static NoticeMailServerListTemple getInstance() {
        return instance;
    }

    private NoticeMailServerListTemple() {
        htmlBuilder = new HtmlBuilder("warn");
    }

    public String build(String[][] ips, List<String> headers) {
        StringBuilder sb = new StringBuilder(128);
        htmlBuilder.startMinTableWithBoard(sb);
        htmlBuilder.setTableHeadWithBoard(sb, headers);
        htmlBuilder.setTableBodyWithBorder(sb, ips, new int[]{3, 3, 10}, null);
        htmlBuilder.endTable(sb);
        return sb.toString();
    }
}
