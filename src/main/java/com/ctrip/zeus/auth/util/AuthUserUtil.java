package com.ctrip.zeus.auth.util;

import com.google.common.base.Strings;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by lu.wang on 2016/5/6.
 */
public class AuthUserUtil {
    public static final String SLB_SERVER_USER = "slbServer";
    private static DynamicStringProperty authUsers = DynamicPropertyFactory.getInstance().getStringProperty("ip.authentication.users", "");

    public static Set<String> getAuthUsers() {
        String authUsersValue = authUsers.get();

        Set<String> result = new HashSet<>();
        if (!Strings.isNullOrEmpty(authUsersValue)) {
            result.addAll(Arrays.asList(authUsersValue.split(",")));
        }
        return result;
    }
}
