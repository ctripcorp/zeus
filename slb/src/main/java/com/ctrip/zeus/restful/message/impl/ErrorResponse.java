package com.ctrip.zeus.restful.message.impl;

import com.ctrip.zeus.restful.message.Message;

import java.io.Serializable;

/**
 * Created by zhoumy on 2015/4/2.
 */
public class ErrorResponse implements Message {
    private int status;
    private Serializable response;

    @Override
    public int getStatus() {
        return status;
    }

    @Override
    public Serializable getResponse() {
        return response;
    }

    public ErrorResponse setStatus(int status) {
        this.status = status;
        return this;
    }

    public ErrorResponse setResponse(Serializable response) {
        this.response = response;
        return this;
    }

}
