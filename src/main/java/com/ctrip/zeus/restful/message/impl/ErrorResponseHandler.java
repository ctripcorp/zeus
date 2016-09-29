package com.ctrip.zeus.restful.message.impl;

import com.ctrip.zeus.exceptions.*;
import com.ctrip.zeus.restful.message.Message;
import com.ctrip.zeus.restful.message.ResponseHandler;
import com.ctrip.zeus.restful.response.entity.ErrorMessage;
import com.ctrip.zeus.support.GenericSerializer;
import com.ctrip.zeus.support.ObjectJsonWriter;
import com.ctrip.zeus.util.ExceptionUtils;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by zhoumy on 2015/4/2.
 */
@Component("errorResponseHandler")
public class ErrorResponseHandler implements ResponseHandler {
    private static final MediaType defaultMediaType = MediaType.APPLICATION_JSON_TYPE;

    public Message generateMessage(Throwable object, MediaType type, boolean printStackTrace) throws Exception {
        ErrorMessage em = ExceptionUtils.getErrorMessage(object, printStackTrace);
        ErrorResponse err = new ErrorResponse();

        if (type.equals(MediaType.APPLICATION_XML)) {
            err.setResponse(GenericSerializer.writeXml(em).replace("%", "%%"));
        } else {
            err.setResponse(ObjectJsonWriter.write(em));
        }
        if (object instanceof NotFoundException) {
            err.setStatus(Response.Status.NOT_FOUND.getStatusCode());
        } else if (object instanceof BadRequestException) {
            err.setStatus(Response.Status.BAD_REQUEST.getStatusCode());
        } else if (object instanceof ValidationException) {
            err.setStatus(Response.Status.BAD_REQUEST.getStatusCode());
        } else if (object instanceof ForbiddenException) {
            err.setStatus(Response.Status.FORBIDDEN.getStatusCode());
        } else if (object instanceof SlbValidatorException) {
            err.setStatus(506);
        } else if (object instanceof Exception) {
            err.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }
        return err;
    }

    @Override
    public Response handle(Object object, MediaType mediaType) throws Exception {
        return handle(object, mediaType, false);
    }

    @Override
    public Response handleSerializedValue(String serializedValue, MediaType mediaType) throws Exception {
        return handle(serializedValue, mediaType, false);
    }

    public Response handle(Object object, MediaType mediaType, boolean printStackTrace) throws Exception {
        if (object == null || !(object instanceof Throwable)) {
            throw new ValidationException("ErrorResponseHandler only accepts Throwable object");
        }
        if (mediaType == null || !MediaType.APPLICATION_XML_TYPE.equals(mediaType)) {
            mediaType = defaultMediaType;
        }
        Message message = generateMessage((Throwable) object, mediaType, printStackTrace);
        return Response.status(message.getStatus()).entity(message.getResponse())
                .type(mediaType).build();
    }
}
