package com.ctrip.zeus.auth.impl;

import com.ctrip.zeus.auth.util.AuthUtil;
import com.ctrip.zeus.util.AesEncryptionUtil;
import com.ctrip.zeus.util.ClientIpUtil;
import com.netflix.config.DynamicPropertyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by fanqq on 2016/8/11.
 */
public class SetAuthTokenFilter implements Filter {
    private DynamicPropertyFactory factory = DynamicPropertyFactory.getInstance();
    private static final Logger logger = LoggerFactory.getLogger(TokenAuthFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
        final HttpServletRequest request = (HttpServletRequest) servletRequest;
        final HttpServletResponse response = (HttpServletResponse) servletResponse;

        int defaultMaxAge = 600;

        if (!factory.getBooleanProperty("set.auth.token.filter.enable", false).get()) {
            chain.doFilter(request, response);
            return;
        }

        //1.already assert
        if (request.getAttribute(AuthUtil.AUTH_USER) == null) {
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

        String userName = request.getAttribute(AuthUtil.AUTH_USER) == null ? null : request.getAttribute(AuthUtil.AUTH_USER).toString();
        if (userName != null) {
            String tokenName = null;
            if (token != null) {
                try {
                    String decodeToken = AesEncryptionUtil.getInstance().decrypt(token);
                    tokenName = decodeToken.split(";")[0];
                } catch (Exception e) {
                    logger.error("Token from cookies is invalidate.Token:" + token + " UserName:" + userName);
                }
            }
            if (!userName.equals(tokenName)) {
                String ip = ClientIpUtil.getClientIP(request);
                String email = null;
                String bu = null;
                String cname = null;
                String tmpToken = userName + ";" + ip;
                if (email != null && bu != null) {
                    tmpToken = tmpToken + ";" + email + ";" + bu + ";" + cname;
                }

                String newToken = AesEncryptionUtil.getInstance().encrypt(tmpToken);
                Cookie tcookie = new Cookie(AuthUtil.AUTH_TOKEN, newToken);
                tcookie.setDomain(factory.getStringProperty("cookies.domain", "").get());
                tcookie.setPath("/");
                tcookie.setMaxAge(factory.getIntProperty("token.cookies.max.age", defaultMaxAge).get());
                response.addCookie(tcookie);
            } else {
                Cookie tcookie = new Cookie(AuthUtil.AUTH_TOKEN, token);
                tcookie.setDomain(factory.getStringProperty("cookies.domain", ".localhost").get());
                tcookie.setPath("/");
                tcookie.setMaxAge(factory.getIntProperty("token.cookies.max.age", defaultMaxAge).get());
                response.addCookie(tcookie);
            }
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {

    }
}
