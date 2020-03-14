package com.ctrip.zeus.service.message.queue.entity;

/**
 * Created by fanqq on 2017/2/14.
 */
public class ChangeDataEntity {
    String entity;
    String from;
    String to;
    String description;

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
