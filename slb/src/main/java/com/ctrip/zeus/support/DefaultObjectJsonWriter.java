package com.ctrip.zeus.support;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.text.SimpleDateFormat;

/**
 * Created by fanqq on 2017/3/15.
 */
public class DefaultObjectJsonWriter {
    private static final ObjectMapper defaultObjectMapper;

    static {
        defaultObjectMapper = new ObjectMapper()
                .configure(SerializationFeature.INDENT_OUTPUT, true)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
    }

    public static String write(Object obj) throws JsonProcessingException {
        return defaultObjectMapper.writeValueAsString(obj);
    }
}
