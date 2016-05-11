package com.ctrip.zeus.auth.impl;

import com.ctrip.zeus.auth.util.AuthUserUtil;
import com.netflix.config.DynamicPropertyFactory;
import org.jasig.cas.client.util.AbstractCasFilter;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.AssertionImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Authenticate with ip.
 *
 * User: mag
 * Date: 4/21/2015
 * Time: 3:00 PM
 */
public class IPAuthenticationFilter implements Filter{

    private static final String IP_AUTHENTICATION_PREFIX = "ip.authentication";
    private static final Logger logger = LoggerFactory.getLogger(IPAuthenticationFilter.class);
    public static final String SERVER_TOKEN_HEADER = "SlbServerToken";
    private DynamicPropertyFactory factory = DynamicPropertyFactory.getInstance();

    @Override
    public void init(FilterConfig filterConfig) {
    }

    public final void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse, final FilterChain filterChain) throws IOException, ServletException {
        final HttpServletRequest request = (HttpServletRequest) servletRequest;
        final HttpServletResponse response = (HttpServletResponse) servletResponse;
        final HttpSession session = request.getSession(false);
        //1. turn off auth
        if (!factory.getBooleanProperty("server.authorization.enable", false).get()){
            setAssertion(request, AuthUserUtil.SLB_SERVER_USER);
            filterChain.doFilter(request,response);
            return;
        }
        //2.already assert
        Assertion assertion = session != null ? (Assertion) session.getAttribute(AbstractCasFilter.CONST_CAS_ASSERTION) : null;
        if (assertion != null) {
            filterChain.doFilter(request, response);
            return;
        }

        //3. check whether it is called from other slb servers.
        String slbServerToken = request.getHeader(SERVER_TOKEN_HEADER);
        if (slbServerToken != null){
            if (TokenManager.validateToken(slbServerToken)){
                setAssertion(request, AuthUserUtil.SLB_SERVER_USER);
                filterChain.doFilter(request,response);
                return;
            }
        }

        //4. if the request is from in ip white list, then authenticate it using the ip white list.
        String clientIP = getClientIP(request);
        String ipUser = getIpUser(IP_AUTHENTICATION_PREFIX, AuthUserUtil.getAuthUsers(), clientIP);
        if (ipUser != null){
            logger.info("Authenticated by IP: " + clientIP + " Assigned userName:" + ipUser);
            setAssertion(request, ipUser);
        }
        filterChain.doFilter(request,response);
    }

    private void setAssertion(HttpServletRequest request, String userName) {
        Assertion assertion = new AssertionImpl(userName);
        request.setAttribute(AbstractCasFilter.CONST_CAS_ASSERTION, assertion);
        request.getSession().setAttribute(AbstractCasFilter.CONST_CAS_ASSERTION, assertion);
    }

    @Override
    public void destroy() {
        // nothing to do
    }

    private Map<String,String> parseIpUserStr(String ipConfig){
        Map<String, String> result = new HashMap<>();
        if (ipConfig == null || ipConfig.isEmpty()) {
            return result;
        }
        String[] configs = ipConfig.split("#");
        for(String config : configs) {
            String[] parts = config.split("=", -1);
            if (parts == null || parts.length != 2){
                logger.error("fail to parse {}", config);
                continue;
            }
            String[] ips = parts[0].split(",");
            String userName = parts[1];
            for (String ip : ips) {
                result.put(ip,userName);
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

    private String getClientIP(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

}
