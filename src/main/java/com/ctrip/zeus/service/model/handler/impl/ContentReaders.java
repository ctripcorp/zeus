package com.ctrip.zeus.service.model.handler.impl;

import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.model.transform.DefaultSaxParser;
import com.ctrip.zeus.support.ObjectJsonParser;
import org.xml.sax.SAXException;

import java.io.IOException;

/**
 * Created by zhoumy on 2015/9/22.
 */
public class ContentReaders {
    public static Group readGroupContent(String content) throws IOException, SAXException {
        if (content.charAt(0) == '<') {
            return DefaultSaxParser.parseEntity(Group.class, content);
        } else {
            return ObjectJsonParser.parse(content, Group.class);
        }
    }

    public static Slb readSlbContent(String content) throws IOException, SAXException {
        if (content.charAt(0) == '<') {
            return DefaultSaxParser.parseEntity(Slb.class, content);
        } else {
            return ObjectJsonParser.parse(content, Slb.class);
        }
    }

    public static VirtualServer readVirtualServerContent(String content) throws IOException, SAXException {
        VirtualServer vs;
        if (content.charAt(0) == '<') {
            vs = DefaultSaxParser.parseEntity(VirtualServer.class, content);
        } else {
            vs = ObjectJsonParser.parse(content, VirtualServer.class);
        }
        //render for history records
        if (vs != null) {
            if ((vs.getSlbIds() == null || vs.getSlbIds().isEmpty())
                    && vs.getSlbId() != null) {
                vs.getSlbIds().add(vs.getSlbId());
                vs.setSlbId(null);
            }
        }
        return vs;
    }
}
