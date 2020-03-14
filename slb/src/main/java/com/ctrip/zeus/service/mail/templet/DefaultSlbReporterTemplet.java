package com.ctrip.zeus.service.mail.templet;

import com.ctrip.zeus.service.mail.templet.util.HtmlBuilder;
import com.google.common.base.Joiner;

import java.util.*;

/**
 * Created by fanqq on 2017/4/5.
 */
public class DefaultSlbReporterTemplet {

    private String title;
    private List<String> panelTitles = new ArrayList<>();
    private List<String> titleLinks = new ArrayList<>();
    private Map<String, List<String>> panelsTableRows = new HashMap<>();
    private Map<String, List<String>> panelsTableHeads = new HashMap<>();
    private HtmlBuilder htmlBuilder;

    public DefaultSlbReporterTemplet() {
        htmlBuilder = new HtmlBuilder();
    }

    private StringBuilder sb = new StringBuilder(516);

    public DefaultSlbReporterTemplet setTitle(String title) {
        this.title = title;
        return this;
    }

    public DefaultSlbReporterTemplet addTitleLink(String url, String des) {
        titleLinks.add(url);
        titleLinks.add(des);
        return this;
    }

    public DefaultSlbReporterTemplet addPanel(String pTitle) {
        if (!panelTitles.contains(pTitle)) {
            panelTitles.add(pTitle);
        }
        return this;
    }

    public DefaultSlbReporterTemplet addPanelRow(String pTitle, String... col) {
        if (!panelTitles.contains(pTitle)) {
            panelTitles.add(pTitle);
        }
        List<String> tmp = panelsTableRows.get(pTitle);
        if (tmp == null) {
            tmp = new ArrayList<>();
            panelsTableRows.put(pTitle, tmp);
        }
        String res = Joiner.on("#").join(col);
        tmp.add(res);
        return this;
    }

    public DefaultSlbReporterTemplet setPanelHeader(String pTitle, String... col) {
        if (!panelTitles.contains(pTitle)) {
            panelTitles.add(pTitle);
        }
        panelsTableHeads.put(pTitle, Arrays.asList(col));
        return this;
    }

    public String build() {
        sb.setLength(0);
        htmlBuilder.addTitle(sb, title);
        if (titleLinks.size() > 0) {
            htmlBuilder.addLink(sb, titleLinks);
        }
        for (String pT : panelTitles) {
            htmlBuilder.startPanel(sb);
            htmlBuilder.setPanelHeader(sb, pT);
            htmlBuilder.startPanelBody(sb);
            htmlBuilder.startTable(sb);
            htmlBuilder.setTableHead(sb, panelsTableHeads.get(pT));
            htmlBuilder.setTableBody(sb, panelsTableRows.get(pT));
            htmlBuilder.endTable(sb);
            htmlBuilder.endPanelBody(sb);
            htmlBuilder.endPanel(sb);
        }
        return sb.toString();
    }

}
