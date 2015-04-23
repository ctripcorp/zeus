package com.ctrip.zeus.restful.message.impl;

import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.restful.message.Message;
import com.ctrip.zeus.restful.message.ResponseHandler;
import com.ctrip.zeus.support.GenericSerializer;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by zhoumy on 2015/4/2.
 */
@Component("responseHandler")
public class DefaultResponseHandler implements ResponseHandler {
    private static final Set<MediaType> acceptedMediaTypes  = getDefault();
    private static final MediaType defaultMediaType = MediaType.APPLICATION_JSON_TYPE;

    public Message generateMessage(Object object, String type) throws Exception {
        ZeusResponse zr = new ZeusResponse();
        if (object == null)
            return zr;

        if (type.equals(MediaType.APPLICATION_JSON)) {
            zr.setResponse(GenericSerializer.writeJson(object));
        } else if (type.equals(MediaType.APPLICATION_XML)) {
            zr.setResponse(GenericSerializer.writeXml(object));
        } else if (object instanceof Serializable) {
            zr.setResponse((Serializable)object);
        } else {
            throw new ValidationException("Response object cannot be serialized.");
        }
        return zr;
    }

    @Override
    public Response handle(Object object, MediaType mediaType) throws Exception {
        if (mediaType != null && acceptedMediaTypes.contains(mediaType)) {
            Message response = generateMessage(object, mediaType.toString());
            return Response.status(response.getStatus()).entity(response.getResponse())
                    .type(mediaType).build();
        }
        try {
            Message response = generateMessage(object, defaultMediaType.toString());
            return Response.status(response.getStatus()).entity(response.getResponse())
                    .type(defaultMediaType).build();
        } catch (Exception ex) {
            throw new ValidationException("Unaccepted media type.");
        }
    }

    private static Set<MediaType> getDefault() {
        Set<MediaType> set = new HashSet<>();
        set.add(MediaType.APPLICATION_JSON_TYPE);
        set.add(MediaType.APPLICATION_XML_TYPE);
        return set;
    }
}
