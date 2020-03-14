package com.ctrip.zeus.auth.util;

import com.ctrip.zeus.auth.impl.TokenManager;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

public class AuthTokenUtil {

    public static MultivaluedMap<String, Object> getDefaultHeaders() {
        MultivaluedMap<String, Object> map = new MultivaluedHashMap<>();
        map.putSingle(AuthUtil.SERVER_TOKEN_HEADER, TokenManager.generateToken());
        return map;
    }
}
