package com.ctrip.zeus.service.model.handler.impl;

import com.ctrip.zeus.model.entity.*;
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
                .setSerializationInclusion(JsonInclude.Include.NON_DEFAULT)
                .setPropertyNamingStrategy(new LowerCaseWithHyphenStrategy());
    }

    public static String writeVirtualServerContent(VirtualServer vs) {
        //TODO switch option for deprecated fields
        if (!n2nPersistentEnabled.get()) {
            VirtualServer tmp = new VirtualServer()
                    .setId(vs.getId()).setName(vs.getName()).setVersion(vs.getVersion())
                    .setSsl(vs.getSsl()).setPort(vs.getPort()).setSlbId(vs.getSlbId());
            for (Long slbId : vs.getSlbIds()) {
                tmp.getSlbIds().add(slbId);
            }
            for (Domain d : vs.getDomains()) {
                tmp.getDomains().add(d);
            }
            if (tmp.getSlbId() == null || tmp.getSlbId().equals(0L)) {
                if (tmp.getSlbIds().size() == 1) {
                    tmp.setSlbId(vs.getSlbIds().get(0));
                    tmp.getSlbIds().clear();
                } else {
                    logger.error("Multiple slb relations are found on vs-" + vs.getId() + ".");
                }
            }
            return write(tmp);
        } else {
            return write(vs);
        }
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