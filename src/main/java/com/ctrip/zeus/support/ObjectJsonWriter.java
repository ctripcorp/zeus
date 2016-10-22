package com.ctrip.zeus.support;

import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.restful.message.view.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.text.SimpleDateFormat;

/**
 * Created by zhoumy on 2016/7/25.
 */
public class ObjectJsonWriter {
    private static final ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper()
                .configure(SerializationFeature.INDENT_OUTPUT, true)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .setSerializationInclusion(JsonInclude.Include.NON_DEFAULT)
                .setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"))
                .setPropertyNamingStrategy(new LowerCaseWithHyphenStrategy());
        objectMapper.addMixInAnnotations(ExtendedView.ExtendedGroup.class, GroupView.class);
        objectMapper.addMixInAnnotations(ExtendedView.ExtendedVs.class, VsView.class);
        objectMapper.addMixInAnnotations(ExtendedView.ExtendedSlb.class, SlbView.class);
    }

    public static String write(Object obj, String type) throws ValidationException, JsonProcessingException {
        return objectMapper.writerWithView(ViewConstraints.getContraintType(type)).writeValueAsString(obj);
    }

    public static String write(Object obj) throws JsonProcessingException {
        return objectMapper.writeValueAsString(obj);
    }
}
