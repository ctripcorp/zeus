package com.ctrip.zeus.service.model;

import java.util.Date;

/**
 * Created by zhoumy on 2016/12/15.
 */
public class Archive<T> {
    private long id;
    private int version;
    private Date createdTime;
    private T content;
    private String author;
    private String commitMessage;

    public long getId() {
        return id;
    }

    public Archive<T> setId(long id) {
        this.id = id;
        return this;
    }

    public int getVersion() {
        return version;
    }

    public Archive<T> setVersion(int version) {
        this.version = version;
        return this;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public Archive<T> setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
        return this;
    }

    public T getContent() {
        return content;
    }

    public Archive<T> setContent(T content) {
        this.content = content;
        return this;
    }

    public String getAuthor() {
        return author;
    }

    public Archive<T> setAuthor(String author) {
        this.author = author;
        return this;
    }

    public String getCommitMessage() {
        return commitMessage;
    }

    public Archive<T> setCommitMessage(String commitMessage) {
        this.commitMessage = commitMessage;
        return this;
    }
}