package com.ctrip.zeus.auth.util;

import com.google.common.collect.Sets;

import java.util.Set;

/**
 * Created by lu.wang on 2016/5/6.
 */
public class AuthUserConstants {
    public static final String OP_SYS_USER = "opSys";
    public static final String RELEASE_SYS_USER = "releaseSys";
    public static final String SLB_TEAM_USER = "slbTeam";
    public static final String SLB_SERVER_USER = "slbServer";

    public static Set<String> getAuthUsers() {
        return Sets.newHashSet(
                OP_SYS_USER,
                RELEASE_SYS_USER,
                SLB_TEAM_USER,
                SLB_SERVER_USER
        );
    }
}
