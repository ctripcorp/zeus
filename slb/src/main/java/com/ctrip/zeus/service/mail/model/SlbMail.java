package com.ctrip.zeus.service.mail.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by fanqq on 2017/3/20.
 */
public class SlbMail {
    private String subject;
    private List<String> recipients = new ArrayList<>();
    private List<String> bcc = new ArrayList<>();
    private List<String> cc = new ArrayList<>();
    private Map<String, byte[]> inlineData = new HashMap<>();
    private Map<String, byte[]> attachData = new HashMap<>();


    private Map<String, String> attachContentType = new HashMap<>();
    private String body;
    private boolean isHtml = true;
    private String from;

    public Map<String, byte[]> getAttachData() {
        return attachData;
    }

    public Map<String, String> getAttachContentType() {
        return attachContentType;
    }

    public SlbMail addAttachData(String name, byte[] attachData, String contentType) {
        this.attachData.put(name, attachData);
        this.attachContentType.put(name, contentType);
        return this;
    }

    public List<String> getCc() {
        return cc;
    }

    public SlbMail setCc(List<String> cc) {
        this.cc = cc;
        return this;
    }

    public String getFrom() {
        return from;
    }

    public SlbMail setFrom(String from) {
        this.from = from;
        return this;
    }

    public String getSubject() {
        return subject;
    }

    public SlbMail setSubject(String subject) {
        this.subject = subject;
        return this;
    }

    public Map<String, byte[]> getInlineData() {
        return inlineData;
    }

    public SlbMail addInlineData(String key, byte[] img) {
        this.inlineData.put(key, img);
        return this;
    }

    public List<String> getRecipients() {
        return recipients;
    }

    public SlbMail setRecipients(List<String> recipients) {
        this.recipients = recipients;
        return this;
    }

    public String getBody() {
        return body;
    }

    public SlbMail setBody(String body) {
        this.body = body;
        return this;
    }

    public List<String> getBcc() {
        return bcc;
    }

    public void setBcc(List<String> bcc) {
        this.bcc = bcc;
    }

    public boolean isHtml() {
        return isHtml;
    }

    public SlbMail setHtml(boolean html) {
        isHtml = html;
        return this;
    }
}
