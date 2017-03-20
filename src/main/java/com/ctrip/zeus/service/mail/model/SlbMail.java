package com.ctrip.zeus.service.mail.model;

import java.util.List;

/**
 * Created by fanqq on 2017/3/20.
 */
public class SlbMail {
    String subject;
    List<String> recipients;
    String body;
    boolean isHtml;

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public List<String> getRecipients() {
        return recipients;
    }

    public void setRecipients(List<String> recipients) {
        this.recipients = recipients;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public boolean isHtml() {
        return isHtml;
    }

    public void setHtml(boolean html) {
        isHtml = html;
    }
}
