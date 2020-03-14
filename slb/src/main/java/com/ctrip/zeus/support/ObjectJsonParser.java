package com.ctrip.zeus.support;

import com.ctrip.zeus.restful.message.view.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhoumy on 2016/7/25.
 */
public class ObjectJsonParser {
    private static ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"))
            .setPropertyNamingStrategy(new LowerCaseWithHyphenStrategy());
    private static final ObjectMapper viewObjectMapper;
    static {
        viewObjectMapper = new ObjectMapper()
                .configure(SerializationFeature.INDENT_OUTPUT, true)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
                .setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"))
                .setPropertyNamingStrategy(new LowerCaseWithHyphenStrategy());
        viewObjectMapper.addMixIn(ExtendedView.ExtendedGroup.class, GroupView.class);
        viewObjectMapper.addMixIn(ExtendedView.ExtendedVs.class, VsView.class);
        viewObjectMapper.addMixIn(ExtendedView.ExtendedSlb.class, SlbView.class);
        viewObjectMapper.addMixIn(ExtendedView.ExtendedDr.class, DrView.class);
        viewObjectMapper.addMixIn(ExtendedView.ExtendedTrafficPolicy.class, TrafficPolicyView.class);
    }

    public static <T> T parse(String jsonValue, Class<T> t) {
        try {
            return objectMapper.readValue(jsonValue, t);
        } catch (IOException e) {
            return null;
        }
    }

    public static <T> T parse(InputStream jsonValue, Class<T> t) {
        try {
            return objectMapper.readValue(jsonValue, t);
        } catch (IOException e) {
            return null;
        }
    }

    public static <T> T parse(String jsonValue, Class<T> t,String type) {
        try {
            return viewObjectMapper.readerWithView(ViewConstraints.getContraintType(type)).forType(t).readValue(jsonValue);
        } catch (IOException e) {
            return null;
        }
    }

    public static <T> List<T> parseArray(String jsonValue, Class<T> t) {
        List<T> result = new ArrayList<>();
        try {
            T[] array = objectMapper.readValue(jsonValue, TypeFactory.defaultInstance().constructArrayType(t));
            for (T t1 : array) {
                result.add(t1);
            }
        } catch (IOException e) {
            return null;
        }
        return result;
    }

    public static <T> T parse(String jsonValue, TypeReference<T> t) {
        if (jsonValue == null || jsonValue.trim().isEmpty()) {
            return null;
        }
        try {
            return (T)objectMapper.readValue(jsonValue, t);

        } catch (IOException e) {
            return null;
        }
    }
}
