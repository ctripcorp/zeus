package com.ctrip.zeus.nginx.conf;

import com.ctrip.zeus.domain.LBMethod;
import com.ctrip.zeus.model.entity.App;
import com.ctrip.zeus.model.entity.LoadBalancingMethod;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.VirtualServer;

/**
 * @author:xingchaowang
 * @date: 3/8/2015.
 */
public class LBConf {
    public static String generate(Slb slb, VirtualServer vs, App app) {
        LoadBalancingMethod lb = app.getLoadBalancingMethod();
        String type = lb.getType();
        if (LBMethod.LESS_CONN.toString().equalsIgnoreCase(type)) {
            return "less_conn;";
        }
        if (LBMethod.IP_HASH.toString().equalsIgnoreCase(type)) {
            return "ip_hash;";
        }
        if (LBMethod.HASH.toString().equalsIgnoreCase(type)) {
            return "hash " + lb.getValue() + ";";
        }
        return "round_robin";
    }
}
