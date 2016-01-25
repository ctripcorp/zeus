package com.ctrip.zeus.service.activate.impl;

import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.VirtualServer;

/**
 * Created by zhoumy on 2015/9/22.
 */
public class ContentWriters {
    public static String writeVirtualServerContent(VirtualServer vs) {
        return String.format(VirtualServer.XML, vs);
    }

    public static String writeGroupContent(Group g) {
        return String.format(Group.XML, g);
    }

    public static String writeSlbContent(Slb s) {
        return String.format(Slb.XML, s);
    }
}