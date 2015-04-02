package com.ctrip.zeus.restful.message.impl;

import com.ctrip.zeus.restful.message.Message;
import com.ctrip.zeus.restful.message.ResponseHandler;
import com.ctrip.zeus.restful.response.entity.ErrorMessage;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.Serializable;

/**
 * Created by zhoumy on 2015/4/2.
 */
@Component("errorResponseHandler")
public class ErrorResponseHandler implements ResponseHandler {
    private static final MediaType defaultMediaType = MediaType.APPLICATION_JSON_TYPE;
    private MediaType mediaType = defaultMediaType;

    private int status;

    @Override
    public Message generateMessage(Object object) throws Exception {
        return new ErrorResponse().setStatus(status).setResponse((Serializable)object);
    }

    @Override
    public Response handle(Object object, MediaType mediaType) throws Exception {
        if (mediaType != null)
            this.mediaType = mediaType;
        Message message = generateMessage(object);
        return Response.status(message.getStatus()).entity(message.getResponse()).build();
    }

    @Override
    public MediaType getMediaType() {
        return mediaType;
    }

    public void setResponseStatus(int status) {
        this.status = status;
    }
}
