package com.ctrip.zeus.restful.message.impl;

import com.ctrip.zeus.restful.message.Message;

import javax.ws.rs.core.Response;
import java.io.Serializable;

/**
 * Created by zhoumy on 2015/4/2.
 */
public class ZeusResponse implements Message {
    private Serializable response;

    @Override
    public int getStatus() {
        return Response.Status.OK.getStatusCode();
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