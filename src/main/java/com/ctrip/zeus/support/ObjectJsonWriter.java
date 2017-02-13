package com.ctrip.zeus.support;

import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.restful.message.view.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;

import java.text.SimpleDateFormat;

/**
 * Created by zhoumy on 2016/7/25.
 */
public class ObjectJsonWriter {
    private static final ObjectMapper defaultObjectMapper;
    private static final ObjectMapper viewObjectMapper;

    static {
        defaultObjectMapper = new ObjectMapper()
                .configure(SerializationFeature.INDENT_OUTPUT, true)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"))
                .setPropertyNamingStrategy(new LowerCaseWithHyphenStrategy());
        viewObjectMapper = new ObjectMapper()
                .configure(SerializationFeature.INDENT_OUTPUT, true)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
                .setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"))
                .setPropertyNamingStrategy(new LowerCaseWithHyphenStrategy());
        viewObjectMapper.addMixIn(ExtendedView.ExtendedGroup.class, GroupView.class);
        viewObjectMapper.addMixIn(ExtendedView.ExtendedVs.class, VsView.class);
        viewObjectMapper.addMixIn(ExtendedView.ExtendedSlb.class, SlbView.class);
    }

    public static String write(Object obj, String type) throws ValidationException, JsonProcessingException {
        return viewObjectMapper.writerWithView(ViewConstraints.getContraintType(type)).writeValueAsString(obj);
    }

    public static String write(Object obj) throws JsonProcessingException {
        return defaultObjectMapper.writeValueAsString(obj);
    }
}
