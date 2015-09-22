package com.ctrip.zeus.service.model.handler.impl;

import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.model.transform.DefaultSaxParser;
import org.xml.sax.SAXException;

import java.io.IOException;

/**
 * Created by zhoumy on 2015/9/22.
 */
public class ContentReaders {

    public static VirtualServer readVirtualServerContent(String content) throws IOException, SAXException {
        return DefaultSaxParser.parseEntity(VirtualServer.class, content);
    }
}
