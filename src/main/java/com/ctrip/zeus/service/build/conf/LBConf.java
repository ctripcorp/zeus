package com.ctrip.zeus.service.build.conf;

import com.ctrip.zeus.domain.LBMethod;
import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.LoadBalancingMethod;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.util.AssertUtils;

/**
 * @author:xingchaowang
 * @date: 3/8/2015.
 */
public class LBConf {
    public static String generate(Group group) throws Exception {
        LoadBalancingMethod lb = group.getLoadBalancingMethod();
        AssertUtils.assertNotNull(lb, "LoadBalancingMethod is null! GroupName: " + group.getName());

        String type = lb.getType();

        if (LBMethod.LESS_CONN.toString().equalsIgnoreCase(type)) {
            return "less_conn;\n";
        }
        if (LBMethod.IP_HASH.toString().equalsIgnoreCase(type)) {
            return "ip_hash;\n";
        }
        if (LBMethod.HASH.toString().equalsIgnoreCase(type)) {
            return "hash " + lb.getValue() + ";\n";
        }
        return "";
    }
}
