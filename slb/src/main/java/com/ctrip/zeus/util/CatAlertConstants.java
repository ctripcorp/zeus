package com.ctrip.zeus.util;

import com.google.common.collect.Sets;
import com.netflix.config.DynamicPropertyFactory;

import java.util.Set;

/**
 * Created by lu.wang on 2016/4/19.
 */
public class CatAlertConstants {
    public static final String UP_SERVER = "/api/op/upServer";
    public static final String DOWN_SERVER = "/api/op/downServer";

    public static final String UP_MEMBER = "/api/op/upMember";
    public static final String DOWN_MEMBER = "/api/op/downMember";

    public static final String PULLIN = "/api/op/pullIn";
    public static final String PULLOUT = "api/op/pullOut";

    public static final String ACTIVATE_SLB = "/api/activate/slb";
    public static final String ACTIVATE_GROUP = "/api/activate/group";
    public static final String ACTIVATE_VS = "/api/activate/vs";

    public static final String DEACTIVATE_SLB = "/api/deactivate/slb";
    public static final String DEACTIVATE_GROUP = "/api/deactivate/group";
    public static final String DEACTIVATE_SOFT_GROUP = "/api/deactivate/soft/group";
    public static final String DEACTIVATE_VS = "/api/deactivate/vs";

    private static final String PREFIX = "slb.slowRequest.time.";

    public static Set<String> getAllCatAlertUri() {
        return Sets.newHashSet(
                UP_SERVER,
                DOWN_SERVER,
                UP_MEMBER,
                DOWN_MEMBER,
                PULLIN,
                PULLOUT,
                ACTIVATE_SLB,
                ACTIVATE_GROUP,
                ACTIVATE_VS,
                DEACTIVATE_SLB,
                DEACTIVATE_GROUP,
                DEACTIVATE_SOFT_GROUP,
                DEACTIVATE_VS
        );
    }

    public static int getOperationSlowRequestValue(String url) {
        int slowRequestTime = DynamicPropertyFactory.getInstance().getIntProperty(PREFIX + url, -1).get();
        if(slowRequestTime == -1) {
            slowRequestTime = DynamicPropertyFactory.getInstance().getIntProperty(PREFIX + "default", 5000).get();
        }
        return slowRequestTime;
    }
}