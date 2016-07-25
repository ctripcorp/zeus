package com.ctrip.zeus.support;

import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.restful.message.ignore.GroupView;
import com.ctrip.zeus.restful.message.ignore.ViewConstraints;
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
                .setPropertyNamingStrategy(new LowerCaseWithHyphenStrategy());
        objectMapper.addMixInAnnotations(Group.class, GroupView.class);
    }

    public static String write(Object obj, String type) throws ValidationException, JsonProcessingException {
        return objectMapper.writerWithView(ViewConstraints.getContraintType(type)).writeValueAsString(obj);
    }
}
