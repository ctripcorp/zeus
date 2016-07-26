package com.ctrip.zeus.support;

import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.GroupVirtualServer;
import com.ctrip.zeus.restful.message.view.ExtendedView;
import com.ctrip.zeus.restful.message.view.GroupView;
import com.ctrip.zeus.restful.message.view.ViewConstraints;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Created by zhoumy on 2016/7/25.
 */
public class ObjectJsonWriter {
    private static final ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper()
                .configure(SerializationFeature.INDENT_OUTPUT, true)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .setPropertyNamingStrategy(new LowerCaseWithHyphenStrategy());
        objectMapper.addMixInAnnotations(ExtendedView.ExtendedGroup.class, GroupView.class);
        objectMapper.addMixInAnnotations(GroupVirtualServer.class, GroupView.GroupVirtualServerView.class);
    }

    public static String write(Object obj, String type) throws ValidationException, JsonProcessingException {
        return objectMapper.writerWithView(ViewConstraints.getContraintType(type)).writeValueAsString(obj);
    }
}
