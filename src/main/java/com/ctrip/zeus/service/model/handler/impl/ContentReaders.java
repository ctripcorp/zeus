package com.ctrip.zeus.service.model.handler.impl;

import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.model.transform.DefaultJsonParser;
import com.ctrip.zeus.model.transform.DefaultSaxParser;
import org.xml.sax.SAXException;

import java.io.IOException;

/**
 * Created by zhoumy on 2015/9/22.
 */
public class ContentReaders {

    public static Group readGroupContent(String content) throws IOException, SAXException {
        if (content.indexOf(0) == '<')
            return DefaultSaxParser.parseEntity(Group.class, content);
        try {
            return DefaultJsonParser.parse(Group.class, content);
        } catch (IOException e) {
            return DefaultSaxParser.parseEntity(Group.class, content);
        }
    }

    public static Slb readSlbContent(String content) throws IOException, SAXException {
        if (content.indexOf(0) == '<')
            return DefaultSaxParser.parseEntity(Slb.class, content);
        try {
            return DefaultJsonParser.parse(Slb.class, content);
        } catch (IOException e) {
            return DefaultSaxParser.parseEntity(Slb.class, content);
        }
    }

    public static VirtualServer readVirtualServerContent(String content) throws IOException, SAXException {
        if (content.indexOf(0) == '<')
            return DefaultSaxParser.parseEntity(VirtualServer.class, content);
        try {
            return DefaultJsonParser.parse(VirtualServer.class, content);
        } catch (IOException e) {
            return DefaultSaxParser.parseEntity(VirtualServer.class, content);
        }
    }
}
