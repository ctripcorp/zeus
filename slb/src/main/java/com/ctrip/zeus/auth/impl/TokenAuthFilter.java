package com.ctrip.zeus.auth.impl;

import com.ctrip.zeus.auth.util.AuthUtil;
import com.ctrip.zeus.util.AesEncryptionUtil;
import com.ctrip.zeus.util.MessageUtil;
import com.netflix.config.DynamicPropertyFactory;
import org.apache.commons.lang.StringUtils;
import org.jasig.cas.client.util.AbstractCasFilter;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.AssertionImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Created by fanqq on 2016/8/11.
 */
public class TokenAuthFilter implements Filter {

    private DynamicPropertyFactory factory = DynamicPropertyFactory.getInstance();
    private static final Logger logger = LoggerFactory.getLogger(TokenAuthFilter.class);


    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
        final HttpServletRequest request = (HttpServletRequest) servletRequest;
        final HttpServletResponse response = (HttpServletResponse) servletResponse;

        if (!factory.getBooleanProperty("token.auth.filter.enable", true).get()) {
            chain.doFilter(request, response);
            return;
        }
        //1.already assert
        if (request.getAttribute(AuthUtil.AUTH_USER) != null) {
            chain.doFilter(request, response);
            return;
        }

        Cookie[] cookies = request.getCookies();
        String token = null;
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equalsIgnoreCase(AuthUtil.AUTH_TOKEN)) {
                    token = cookie.getValue();
                }
            }
        }
        if (StringUtils.isBlank(token)) {
            token = request.getHeader(AuthUtil.AUTH_TOKEN);
        }

        int splitLen = 2;
        int valueMinLen = 5;
        int emailIndex = 2;
        int buIndex = 3;
        int cnameIndex = 4;

        if (token != null) {
            String data = AesEncryptionUtil.getInstance().decrypt(token);
            if (data != null) {
                String[] values = data.split(";");
                if (values.length >= splitLen) {
                    String clientIp = MessageUtil.getClientIP(request);
                    if (values[1].equals(clientIp)) {
                        String email = null;
                        String bu = null;
                        String cname = null;
                        if (values.length >= valueMinLen) {
                            email = values[emailIndex];
                            bu = values[buIndex];
                            cname = values[cnameIndex];
                        }
                        setAssertion(request, values[0], email, bu, cname);
                        logger.info("Auth by token. username:" + values[0]);
                    }
                }
            }
        }

        if (request.getAttribute(AuthUtil.AUTH_USER) == null) {
            checkHeaderToken(request);
        }
        chain.doFilter(request, response);
    }

    private void checkHeaderToken(HttpServletRequest request) {
        String ht = request.getHeader(AuthUtil.AUTH_HEADER_TOKEN);
        String tokenUser = factory.getStringProperty("auth.header.token." + ht, null).get();
        if (tokenUser != null) {
            setAssertion(request, tokenUser, null, null, null);
        }
    }

    @Override
    public void destroy() {

    }

    private void setAssertion(HttpServletRequest request, String userName, String email, String bu, String cname) {
        Assertion assertion = new AssertionImpl(userName);
        request.setAttribute(AuthUtil.AUTH_USER, userName);
        request.setAttribute(AbstractCasFilter.CONST_CAS_ASSERTION, assertion);
        final HttpSession session = request.getSession(false);
        if (session != null) {
            session.setAttribute(AbstractCasFilter.CONST_CAS_ASSERTION, assertion);
        }
        if (email != null && bu != null) {
            request.setAttribute(AuthUtil.AUTH_USER_EMAIL, email);
            request.setAttribute(AuthUtil.AUTH_USER_BU, bu);
        }
        if (cname != null) {
            request.setAttribute(AuthUtil.AUTH_USER_CNAME, cname);
        }
    }
}
