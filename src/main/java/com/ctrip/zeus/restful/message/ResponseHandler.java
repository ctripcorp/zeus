package com.ctrip.zeus.restful.message;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by zhoumy on 2015/4/1.
 */
public interface ResponseHandler {

    /**
     * Serializing and handle the given object according to its media type
     * @param object the (un)serialized object
     * @param mediaType the media type, accept application/json and application/xml by default
     * @return http response
     * @throws Exception
     */
    Response handle(Object object, MediaType mediaType) throws Exception;

    Response handleSerializedValue(String serializedValue, MediaType mediaType) throws Exception;
}
