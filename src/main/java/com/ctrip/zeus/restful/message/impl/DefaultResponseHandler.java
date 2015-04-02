package com.ctrip.zeus.restful.message.impl;

import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.restful.message.Message;
import com.ctrip.zeus.restful.message.ResponseHandler;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by zhoumy on 2015/4/2.
 */
@Component("responseHandler")
public class DefaultResponseHandler implements ResponseHandler {
    private static final List<MediaType> registeredMediaTypes = registerDefault();
    private final Set<MediaType> acceptedMediaTypes;
    private MediaType mediaType;

    public DefaultResponseHandler() {
        acceptedMediaTypes = new HashSet<>();
        acceptedMediaTypes.addAll(registeredMediaTypes);
    }

    @Override
    public Message generateMessage(Object object) throws Exception {
        if (object instanceof Serializable) {
            return new ZeusResponse().setResponse((Serializable)object);
        }
        throw new ValidationException("Response object is not serializable");
    }

    @Override
    public Response handle(Object object, MediaType mediaType) throws Exception {
        Message response = generateMessage(object);
        if (acceptedMediaTypes.contains(mediaType)) {
            this.mediaType = mediaType;
            return Response.status(response.getStatus())
                    .entity(response.getResponse())
                    .type(mediaType.getType())
                    .build();
        }
        throw new ValidationException("Unaccepted media type: " + mediaType.getType());
    }

    @Override
    public MediaType getMediaType() {
        return mediaType;
    }

    private static List<MediaType> registerDefault() {
        List<MediaType> list = new ArrayList<>();
        list.add(MediaType.APPLICATION_JSON_TYPE);
        list.add(MediaType.APPLICATION_XML_TYPE);
        return list;
    }
}
