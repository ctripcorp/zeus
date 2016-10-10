package com.ctrip.zeus.restful.message.impl;

import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.restful.message.Message;
import com.ctrip.zeus.restful.message.ResponseHandler;
import com.ctrip.zeus.restful.response.entity.SuccessMessage;
import com.ctrip.zeus.support.GenericSerializer;
import com.ctrip.zeus.support.ObjectJsonWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final Set<MediaType> acceptedMediaTypes = getDefault();
    private static final MediaType defaultMediaType = MediaType.APPLICATION_JSON_TYPE;

    public Message generateMessage(Object object, MediaType type) throws Exception {
        ZeusResponse zr = new ZeusResponse();
        if (object == null)
            return zr;
        if (object instanceof String) {
            object = new SuccessMessage().setMessage((String) object);
        }

        try {
            if (type.equals(MediaType.APPLICATION_XML_TYPE)) {
                zr.setResponse(GenericSerializer.writeXml(object).replace("%", "%%"));
                return zr;
            }
            if (type.equals(MediaType.APPLICATION_JSON_TYPE)) {
                zr.setResponse(ObjectJsonWriter.write(object));
                return zr;
            }
            throw new ValidationException("Unaccepted media type " + type.getType() + ".");
        } catch (Exception ex) {
            if (object instanceof Serializable) {
                zr.setResponse((Serializable) object);
                return zr;
            } else {
                String error = "Response object cannot be serialized.";
                logger.error(error, ex);
                throw new ValidationException(error);
            }
        }
    }

    @Override
    public Response handle(Object object, MediaType mediaType) throws Exception {
        if (object == null)
            return Response.status(Response.Status.OK).type(mediaType).build();
        if (mediaType != null && acceptedMediaTypes.contains(mediaType)) {
            Message response = generateMessage(object, mediaType);
            return Response.status(response.getStatus()).entity(response.getResponse())
                    .type(mediaType.withCharset("utf-8")).build();
        }
        try {
            Message response = generateMessage(object, defaultMediaType);
            return Response.status(response.getStatus()).entity(response.getResponse())
                    .type(defaultMediaType.withCharset("utf-8")).build();
        } catch (Exception ex) {
            String error = "Response cannot be serialized using application/json by default.";
            logger.error(error, ex);
            throw new ValidationException(error);
        }
    }

    @Override
    public Response handleSerializedValue(String serializedValue, MediaType mediaType) throws Exception {
        if (mediaType != null && acceptedMediaTypes.contains(mediaType)) {
            return Response.status(Response.Status.OK).entity(serializedValue)
                    .type(mediaType.withCharset("utf-8")).build();
        } else {
            return Response.status(Response.Status.OK).entity(serializedValue)
                    .type(defaultMediaType.withCharset("utf-8")).build();
        }
    }

    private static Set<MediaType> getDefault() {
        Set<MediaType> set = new HashSet<>();
        set.add(MediaType.APPLICATION_JSON_TYPE);
        set.add(MediaType.APPLICATION_XML_TYPE);
        return set;
    }
}
