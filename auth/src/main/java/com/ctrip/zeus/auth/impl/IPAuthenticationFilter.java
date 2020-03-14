package com.ctrip.zeus.auth.impl;

import com.ctrip.zeus.auth.util.AuthUserUtil;
import com.ctrip.zeus.auth.util.AuthUtil;
import com.ctrip.zeus.util.ClientIpUtil;
import com.netflix.config.DynamicPropertyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Authenticate with ip.
 * <p>
 * User: mag
 * Date: 4/21/2015
 * Time: 3:00 PM
 */
public class IPAuthenticationFilter implements Filter {

    private static final String IP_AUTHENTICATION_PREFIX = "ip.authentication";
    private static final Logger logger = LoggerFactory.getLogger(IPAuthenticationFilter.class);
    private DynamicPropertyFactory factory = DynamicPropertyFactory.getInstance();

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse, final FilterChain filterChain) throws IOException, ServletException {
        final HttpServletRequest request = (HttpServletRequest) servletRequest;
        final HttpServletResponse response = (HttpServletResponse) servletResponse;
        //1. ip auth flag
        if (!factory.getBooleanProperty("ip.authentication.filter.enable", true).get()) {
            filterChain.doFilter(request, response);
            return;
        } else if (request.getAttribute(AuthUtil.AUTH_USER) != null) {
            //2.already assert
            filterChain.doFilter(request, response);
            return;
        }

        //3. if the request is from in ip white list, then authenticate it using the ip white list.
        String clientIP = ClientIpUtil.getClientIP(request);
        String ipUser = getIpUser(IP_AUTHENTICATION_PREFIX, AuthUserUtil.getAuthUsers(), clientIP);
        if (ipUser != null) {
            logger.info("Authenticated by IP: " + clientIP + " Assigned userName:" + ipUser);
            request.setAttribute(AuthUtil.AUTH_USER, ipUser);
        }
        filterChain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // nothing to do
    }

    private Map<String, String> parseIpUserStr(String ipConfig) {
        Map<String, String> result = new HashMap<>();
        if (ipConfig == null || ipConfig.isEmpty()) {
            return result;
        }
        int subLen = 2;
        String[] configs = ipConfig.split("#");
        for (String config : configs) {
            String[] parts = config.split("=", -1);
            if (parts.length != subLen) {
                logger.error("fail to parse {}", config);
                continue;
            }
            String[] ips = parts[0].split(",");
            String userName = parts[1];
            for (String ip : ips) {
                result.put(ip, userName);
            }
        }
        return result;
    }

    private String getIpUser(String prefix, Set<String> types, String value) {
        if (prefix == null || types == null || types.size() == 0 || value == null)
            return null;

        String typeValue;
        for (String typeName : types) {
            typeValue = factory.getStringProperty(prefix + "." + typeName, null).get();
            if (typeValue != null && Arrays.asList(typeValue.split(",")).contains(value)) {
                return typeName;
            }
        }

        String defaultValue = factory.getStringProperty(prefix + ".default", null).get();
        Map<String, String> valueKeyMap = parseIpUserStr(defaultValue);

        for (Map.Entry entry : valueKeyMap.entrySet()) {
            if (entry.getKey().equals(value))
                return entry.getValue().toString();
        }

        return null;
    }
}
