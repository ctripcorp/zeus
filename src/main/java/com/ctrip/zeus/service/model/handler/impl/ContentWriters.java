package com.ctrip.zeus.service.model.handler.impl;

import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.support.LowerCaseWithHyphenStrategy;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.netflix.config.DynamicBooleanProperty;
import com.netflix.config.DynamicPropertyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by zhoumy on 2015/9/22.
 */
public class ContentWriters {
    private static Logger logger = LoggerFactory.getLogger(ContentWriters.class);
    private static DynamicBooleanProperty n2nPersistentEnabled = DynamicPropertyFactory.getInstance().getBooleanProperty("slb.slb-vs-n2n.persistent.enabled", false);
    private static final ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper()
                .configure(SerializationFeature.INDENT_OUTPUT, false)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .setPropertyNamingStrategy(new LowerCaseWithHyphenStrategy());
    }

    public static String writeVirtualServerContent(VirtualServer vs) {
        if (!n2nPersistentEnabled.get()) {
            if (vs.getSlbId() == null || vs.getSlbId().equals(0L)) {
                if (vs.getSlbIds().size() == 1) {
                    vs.setSlbId(vs.getSlbIds().get(0));
                    vs.getSlbIds().clear();
                } else {
                    logger.error("Multiple slb relations are found on vs-" + vs.getId() + ".");
                }
            }
        }
        return write(vs);
    }

    public static String writeGroupContent(Group g) {
        return write(g);
    }

    public static String writeSlbContent(Slb s) {
        return write(s);
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