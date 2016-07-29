package com.ctrip.zeus.service.model.handler.impl;

import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.support.GenericSerializer;

/**
 * Created by zhoumy on 2015/9/22.
 */
public class ContentWriters {
    public static String writeVirtualServerContent(VirtualServer vs) {
        return GenericSerializer.writeJson(vs);
    }

    public static String writeGroupContent(Group g) {
        return GenericSerializer.writeJson(g, false);
    }

    public static String writeSlbContent(Slb s) {
        return GenericSerializer.writeJson(s, false);
    }
}