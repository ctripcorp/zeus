package com.ctrip.zeus.service.model.handler.impl;

import com.ctrip.zeus.model.model.*;
import com.ctrip.zeus.support.LowerCaseWithHyphenStrategy;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by zhoumy on 2015/9/22.
 */
public class ContentWriters {
    private static Logger logger = LoggerFactory.getLogger(ContentWriters.class);
    private static final ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper()
                .configure(SerializationFeature.INDENT_OUTPUT, false)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
                .setPropertyNamingStrategy(new LowerCaseWithHyphenStrategy());
    }

    public static String writeVirtualServerContent(VirtualServer vs) {
        return write(vs);
    }

    public static String writeGroupContent(Group g) {
        return write(g);
    }

    public static String writeDrContent(Dr dr) {
        return write(dr);
    }

    public static String writeSlbContent(Slb s) {
        return write(s);
    }

    public static String writeRuleContent(Rule rule){
        return write(rule);
    }

    public static String write(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            logger.error("Fail to serialize object to json.", e);
            return "";
        }
    }
}