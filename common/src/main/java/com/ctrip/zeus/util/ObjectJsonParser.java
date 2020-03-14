package com.ctrip.zeus.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;


public class ObjectJsonParser {
    private static ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"))
            .setPropertyNamingStrategy(new LowerCaseWithHyphenStrategy());

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
