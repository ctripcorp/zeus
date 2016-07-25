package com.ctrip.zeus.support;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * Created by zhoumy on 2016/7/25.
 */
public class ObjectJsonParser {
    private static ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .setPropertyNamingStrategy(new LowerCaseWithHyphenStrategy());

    public static <T> T parse(String jsonValue, Class<T> t) {
        try {
            return objectMapper.readValue(jsonValue, t);
        } catch (IOException e) {
            return null;
        }
    }
}
