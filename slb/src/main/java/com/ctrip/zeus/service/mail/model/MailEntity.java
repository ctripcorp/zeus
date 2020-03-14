package com.ctrip.zeus.service.mail.model;


import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;
import java.util.List;

/**
 * Created by fanqq on 2017/3/20.
 */
public class MailEntity {
    String sendCode;
    String appId;
    String sender;
    List<String> recipient;
    List<String> cc;
    List<String> bcc;
    String senderName;
    String recipientName;
    String subject;
    String bodyTemplateID;
    String bodyContent;
    String charset;
    @JsonProperty("isBodyHtml")
    Boolean isBodyHtml;
    Date expiredTime;

    public List<String> getCc() {
        return cc;
    }

    public void setCc(List<String> cc) {
        this.cc = cc;
    }

    public List<String> getBcc() {
        return bcc;
    }

    public void setBcc(List<String> bcc) {
        this.bcc = bcc;
    }

    public String getSendCode() {
        return sendCode;
    }

    public void setSendCode(String sendCode) {
        this.sendCode = sendCode;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public List<String> getRecipient() {
        return recipient;
    }

    public void setRecipient(List<String> recipient) {
        this.recipient = recipient;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBodyTemplateID() {
        return bodyTemplateID;
    }

    public void setBodyTemplateID(String bodyTemplateID) {
        this.bodyTemplateID = bodyTemplateID;
    }

    public String getBodyContent() {
        return bodyContent;
    }

    public void setBodyContent(String bodyContent) {
        this.bodyContent = bodyContent;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public boolean isBodyHtml() {
        return isBodyHtml;
    }

    public void setBodyHtml(boolean bodyHtml) {
        isBodyHtml = bodyHtml;
    }

    public Date getExpiredTime() {
        return expiredTime;
    }

    public void setExpiredTime(Date expiredTime) {
        this.expiredTime = expiredTime;
    }
}
