package com.ctrip.zeus.restful.message;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by zhoumy on 2015/4/1.
 */
public interface ResponseHandler {

    Message generateMessage(Object object) throws Exception;

    Response handle(Object object, MediaType mediaType) throws Exception;

    MediaType getMediaType();
}
