package com.ctrip.zeus.restful.message.impl;

import com.ctrip.zeus.restful.message.Message;
import java.io.Serializable;

/**
 * Created by zhoumy on 2015/4/2.
 */
public class ZeusResponse implements Message {
    public static final int OK = 200;
    private Serializable response;

    @Override
    public int getStatus() {
        return OK;
    }

    @Override
    public Serializable getResponse() {
        return response;
    }

    public ZeusResponse setResponse(Serializable response) {
        this.response = response;
        return this;
    }
}