package com.ctrip.zeus.service.mail.templet.util;

/**
 * Created by fanqq on 2017/4/14.
 */
public class HtmlTheme {
    private String titleColor;
    private String panelBorderColor;
    private String panelTitleColor;
    private String panelBgColor;
    private String headBgColor;
    private String headColor;
    private String bodyColor;
    private String bodyTrColor;
    private String bodyTdColor;
    private String specialColor;
    private String strongColor;

    private final static HtmlTheme DEFAULT = new HtmlTheme("black", "#bce8f1", "#31708f", "#d9edf7", "white", "black", "black", "black", "black", "red|green", "red");
    private final static HtmlTheme WARN = new HtmlTheme("#8a6d3b", "#faebcc", "#8a6d3b", "#fcf8e3", "white", "black", "black", "black", "black", "red|green", "red");

    public HtmlTheme(String titleColor,
                     String panelBorderColor,
                     String panelTitleColor,
                     String panelBgColor,
                     String headBgColor,
                     String headColor,
                     String bodyColor,
                     String bodyTrColor,
                     String bodyTdColor,
                     String specialColor,
                     String strongColor) {

        this.titleColor = titleColor;
        this.panelBorderColor = panelBorderColor;
        this.panelTitleColor = panelTitleColor;
        this.panelBgColor = panelBgColor;
        this.headBgColor = headBgColor;
        this.headColor = headColor;
        this.bodyColor = bodyColor;
        this.bodyTrColor = bodyTrColor;
        this.bodyTdColor = bodyTdColor;
        this.specialColor = specialColor;
        this.strongColor = strongColor;
    }

    public static HtmlTheme getTheme(String type) {
        if (type.equalsIgnoreCase("default")) {
            return DEFAULT;
        } else if (type.equalsIgnoreCase("warn")) {
            return WARN;
        } else {
            return DEFAULT;
        }
    }

    public String getStrongColor() {
        return strongColor;
    }

    public void setStrongColor(String strongColor) {
        this.strongColor = strongColor;
    }

    public String getTitleColor() {
        return titleColor;
    }

    public void setTitleColor(String titleColor) {
        this.titleColor = titleColor;
    }

    public String getPanelBorderColor() {
        return panelBorderColor;
    }

    public void setPanelBorderColor(String panelBorderColor) {
        this.panelBorderColor = panelBorderColor;
    }

    public String getPanelTitleColor() {
        return panelTitleColor;
    }

    public void setPanelTitleColor(String panelTitleColor) {
        this.panelTitleColor = panelTitleColor;
    }

    public String getPanelBgColor() {
        return panelBgColor;
    }

    public void setPanelBgColor(String panelBgColor) {
        this.panelBgColor = panelBgColor;
    }

    public String getHeadBgColor() {
        return headBgColor;
    }

    public void setHeadBgColor(String headBgColor) {
        this.headBgColor = headBgColor;
    }

    public String getHeadColor() {
        return headColor;
    }

    public void setHeadColor(String headColor) {
        this.headColor = headColor;
    }

    public String getBodyColor() {
        return bodyColor;
    }

    public void setBodyColor(String bodyColor) {
        this.bodyColor = bodyColor;
    }

    public String getBodyTrColor() {
        return bodyTrColor;
    }

    public void setBodyTrColor(String bodyTrColor) {
        this.bodyTrColor = bodyTrColor;
    }

    public String getBodyTdColor() {
        return bodyTdColor;
    }

    public void setBodyTdColor(String bodyTdColor) {
        this.bodyTdColor = bodyTdColor;
    }

    public String getSpecialColor() {
        return specialColor;
    }

    public void setSpecialColor(String specialColor) {
        this.specialColor = specialColor;
    }

    public static HtmlTheme getDEFAULT() {
        return DEFAULT;
    }
}
