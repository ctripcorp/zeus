package com.ctrip.zeus.service.mail.templet.util;

import java.util.List;

/**
 * Created by fanqq on 2017/4/5.
 */
public class HtmlBuilder {

    private HtmlTheme theme;

    public HtmlBuilder(String themeType) {
        theme = HtmlTheme.getTheme(themeType);
    }

    public HtmlBuilder() {
        this("default");
    }

    public void startTable(StringBuilder sb) {
        sb.append("<table style=\"padding-left: 10px ;border-collapse: collapse;border-radius: 1px;width: 100%;border: 1px;max-width:\n" +
                "100%;background-color: transparent;border-spacing: 0;display: table;\">");
    }

    public void startMinTableWithBoard(StringBuilder sb) {
        sb.append("<table style=\"padding-left: 10px ;border-collapse: collapse;border-radius: 1px;width: 80%;border: 1px solid ")
                .append("gray")
                .append(";max-width:800%;background-color: transparent;border-spacing: 0;display: table;\">");
    }

    public void endTable(StringBuilder sb) {
        sb.append("</table>");
    }

    public void addTitle(StringBuilder sb, String title) {
        sb.append(String.format("<h2 style=\"margin:0;padding:0;color:%s\">%s</h2><hr style=\"margin-bottom:5px;\"/>",
                theme.getTitleColor(), title));
    }

    public void addTitleWithList(StringBuilder sb, String title, String[] list) {
        sb.append(String.format("<h2 style=\"margin:0;padding:0;color:%s\">%s</h2>",
                theme.getTitleColor(), title));
        sb.append("<ul style=\"padding-top:0px\">");
        for (String item : list) {
            sb.append("<li><h3 style=\"color:").append(theme.getTitleColor()).append("\">").append(item).append("</h3></li>");
        }
        sb.append("</ul>");

        sb.append("<hr style=\"margin-bottom:5px;\"/>");
    }

    public void startPanel(StringBuilder sb) {
        sb.append("<table style=\"border: 0px solid ").append(theme.getPanelBorderColor()).append(";width:99%;\">");
    }

    public void setPanelHeader(StringBuilder sb, String pTitle) {
        sb.append("<table style=\"font-size: 18px;color: ").append(theme.getPanelTitleColor())
                .append(";background-color:").append(theme.getPanelBgColor())
                .append(";border-radius: 1px;width: 100%;border: none;display: table;\\\"><tr><td style=\"width:100%;height:40px;\"><h3>")
                .append(pTitle).append("</h3></td></tr></table>");
    }

    public void startPanelBody(StringBuilder sb) {
        sb.append("");
    }

    public void startList(StringBuilder sb) {
        sb.append("<div style=\"margin-top:15px;margin-bottom:15px \"><ul>");
    }

    public void endList(StringBuilder sb) {
        sb.append("</ul></div>");
    }

    public void addList(StringBuilder sb, String title, String[] items) {
        sb.append("<li>");
        sb.append(String.format("<h3 style=\"margin:0;padding:0;color:%s\">%s</h3>",
                theme.getTitleColor(), title));
        sb.append("<ul>");
        int i = 0;
        boolean isTable = false;
        for (String item : items) {
            sb.append("<li><h3 style=\"color:").append(theme.getTitleColor()).append("\">").append(item);
            i++;
            if (i == items.length && !item.contains("table")) {
                sb.append("<br><br>");
            }
            if (item.contains("table")) {
                isTable = true;
            }
            sb.append("</h3></li>");
        }
        sb.append("</ul></li>");
        if (isTable) {
            sb.append("<h3 style=\"margin-top:0cm;margin-right:0cm;margin-bottom:0cm;margin-left:\n" +
                    "  30.0pt;margin-bottom:.0001pt;line-height:1px\">&nbsp;</h3>");
        }
    }

    public void endPanelBody(StringBuilder sb) {
        sb.append("");
    }


    public void endPanel(StringBuilder sb) {
        sb.append("</table><table style=\"border:0px;height:5px;width:99%\"></table>");
    }

    public void setTableHead(StringBuilder sb, List<String> head) {
        if (head == null || head.size() == 0) return;
        sb.append(String.format("<thead><tr style=\"line-height: 35px;background-color:%s;color:%s;\">",
                theme.getHeadBgColor(), theme.getHeadColor()));
        for (String h : head) {
            sb.append("<th style=\"text-align: left;\">").append(h).append(" </th>");
        }
        sb.append("</tr></thead>");
    }

    public void setTableHeadWithBoard(StringBuilder sb, List<String> head) {
        if (head == null || head.size() == 0) return;
        sb.append(String.format("<thead><tr style=\"height: 30px;border: 1px solid gray;background-color:%s;color:%s;\">",
                theme.getHeadBgColor(), theme.getHeadColor()));
        for (String h : head) {
            sb.append("<th style=\"text-align: left;border: 1px solid gray;\">").append(h).append(" </th>");
        }
        sb.append("</tr></thead>");
    }

    public void setTableBody(StringBuilder sb, List<String> body) {
        if (body == null || body.size() == 0) return;
        sb.append(String.format("<tbody style=\"color:%s\">", theme.getBodyColor()));
        for (String h : body) {
            sb.append(String.format("<tr style=\"line-height: 35px;color:%s\">", theme.getBodyTrColor()));
            String[] tmp = h.split("#");
            for (String t : tmp) {
                String color = theme.getBodyTdColor();
                String[] sColors = theme.getSpecialColor().split("\\|");
                StringBuilder tc = new StringBuilder(128);
                boolean first = true;
                for (String tsplit : t.split("/")) {
                    String scolor = color;
                    if (tsplit.startsWith("+")) {
                        scolor = sColors[0];
                    } else if (tsplit.startsWith("-")) {
                        scolor = sColors[1];
                    }
                    if (!first) {
                        tc.append("<span style=\"color:").append(scolor).append(";\">/").append(tsplit).append("</span>");
                    } else {
                        tc.append("<span style=\"color:").append(scolor).append(";\">").append(tsplit).append("</span>");
                        first = false;
                    }
                }
                sb.append("<td style=\"text-align: left;width:").append((100 / tmp.length)).append("%;\">").append(tc.toString()).append(" </td>");
            }
            sb.append("</tr>");
        }
        sb.append("</tbody>");
    }

    public void setTableBody(StringBuilder sb, String[][] body) {
        if (body == null || body.length == 0) return;
        sb.append(String.format("<tbody style=\"color:%s\">", theme.getBodyColor()));
        for (String[] row : body) {
            sb.append(String.format("<tr style=\"line-height: 35px;color:%s\">", theme.getBodyTrColor()));
            String[] tmp = row;
            for (String t : tmp) {
                String color = theme.getBodyTdColor();
                String tc = "<span style=\"color:" + color + ";\">" + t + "</span>";
                sb.append("<td style=\"text-align: left;width:" + (100 / tmp.length) + "%;\">").append(tc).append(" </td>");
            }
            sb.append("</tr>");
        }
        sb.append("</tbody>");
    }

    public void setTableBody(StringBuilder sb, String[][] body, int[] weight, boolean[][] strong) {
        if (body == null || body.length == 0) return;
        sb.append(String.format("<tbody style=\"color:%s\">", theme.getBodyColor()));
        int sum = 0;
        for (int w : weight) {
            sum += w;
        }
        if (strong == null) {
            strong = new boolean[body.length][body[0].length];
        }
        int j = 0;
        for (String[] row : body) {
            sb.append(String.format("<tr style=\"line-height: 35px;color:%s\">", theme.getBodyTrColor()));
            int i = 0;
            for (String t : row) {
                String color = theme.getBodyTdColor();
                if (strong[j][i]) {
                    color = theme.getStrongColor();
                }
                String tc = "<span style=\"color:" + color + ";\">" + t + "</span>";
                sb.append("<td style=\"text-align: left;width:").append(weight[i] * 100 / sum).append("%;\">").append(tc).append(" </td>");
                i++;
            }
            j++;
            sb.append("</tr>");
        }
        sb.append("</tbody>");
    }

    public void addLink(StringBuilder sb, List<String> list) {
        for (int i = 0; i < list.size(); ) {
            sb.append("<a href=\"").append(list.get(i)).append("\" style=\"font-size: 18px;\"><strong>").append(list.get(i + 1)).append("</strong></a>");
            i += 2;
        }
    }

    public String buildLink(String k, String url) {
        StringBuilder sb = new StringBuilder(64);
        sb.append("<a href=\"").append(url).append("\" style=\"font-size: 18px;\" target=\"_blank\">").append(k).append("</a>");
        return sb.toString();
    }

    public void addLink(StringBuilder sb, String[] list) {
        for (int i = 0; i < list.length; ) {
            sb.append("&nbsp<a href=\"").append(list[i]).append("\" style=\"font-size: 18px;\"><strong>").append(list[i + 1]).append("</strong></a>&nbsp");
            i += 2;
        }
    }

    public void addLine(StringBuilder sb, String line) {
        sb.append("<span").append(" style=\"font-size: 18px;\"><strong>&nbsp").append(line).append("</strong></span><br/>");
    }

    public void setTableBodyWithBorder(StringBuilder sb, String[][] body, int[] weight, boolean[][] strong) {
        if (body == null || body.length == 0) return;
        sb.append(String.format("<tbody style=\"color:%s\">", theme.getBodyColor()));
        int sum = 0;
        for (int w : weight) {
            sum += w;
        }
        if (strong == null) {
            strong = new boolean[body.length][body[0].length];
        }
        int j = 0;
        for (String[] row : body) {
            sb.append(String.format("<tr style=\"height: 30px;border: 1px solid gray;color:%s\">", theme.getBodyTrColor()));
            int i = 0;
            for (String t : row) {
                String color = theme.getBodyTdColor();
                if (strong[j][i]) {
                    color = theme.getStrongColor();
                }
                String tc = "<span style=\"color:" + color + ";\">" + t + "</span>";
                sb.append("<td style=\"text-align: left;border: 1px solid gray;width:").append(weight[i] * 100 / sum).append("%;\">").append(tc).append(" </td>");
                i++;
            }
            j++;
            sb.append("</tr>");
        }
        sb.append("</tbody>");
    }

    public void addEnd(StringBuilder sb, String end) {
        sb.append("<p style=\"font-size:16px;\">" + end + "</p>");
    }
}
