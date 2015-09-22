package com.ctrip.zeus.service.model.handler.impl;

import com.ctrip.zeus.model.entity.VirtualServer;

/**
 * Created by zhoumy on 2015/9/22.
 */
public class ContentWriters {
    public static String writeVirtualServerContent(VirtualServer vs) {
        return String.format(VirtualServer.XML, vs);
    }
}