package com.ctrip.zeus.auth.impl;

import com.ctrip.zeus.auth.util.AuthUtil;
import com.ctrip.zeus.util.AesEncryptionUtil;
import com.ctrip.zeus.util.MessageUtil;
import com.netflix.config.DynamicPropertyFactory;
import org.jasig.cas.client.util.AbstractCasFilter;
import org.jasig.cas.client.validation.Assertion;
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
        final HttpSession session = request.getSession(false);

        int defaultMaxAge = 600;

        if (!factory.getBooleanProperty("set.auth.token.filter.enable", false).get()) {
            chain.doFilter(request, response);
            return;
        }

        //1.already assert
        Assertion assertion = session != null ? (Assertion) session.getAttribute(AbstractCasFilter.CONST_CAS_ASSERTION) : null;

        if (assertion == null && request.getAttribute(AuthUtil.AUTH_USER) == null) {
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

        String userName = null;
        if (assertion != null && assertion.getPrincipal() != null) {
            userName = assertion.getPrincipal().getName();
        }
        if (userName == null) {
            userName = request.getAttribute(AuthUtil.AUTH_USER) == null ? null : request.getAttribute(AuthUtil.AUTH_USER).toString();
        }

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
                String ip = MessageUtil.getClientIP(request);
                String email = null;
                String bu = null;
                String cname = null;
                if (assertion != null && assertion.getPrincipal() != null) {
                    email = assertion.getPrincipal().getAttributes().get("mail") == null ? null : assertion.getPrincipal().getAttributes().get("mail").toString();
                    bu = assertion.getPrincipal().getAttributes().get("department") == null ? null : assertion.getPrincipal().getAttributes().get("department").toString();
                    cname = assertion.getPrincipal().getAttributes().get("displayName") == null ? null : assertion.getPrincipal().getAttributes().get("displayName").toString();
                }
                String tmpToken = userName + ";" + ip;
                if (email != null && bu != null) {
                    tmpToken = tmpToken + ";" + email + ";" + bu + ";" + cname;
                }

                String newToken = AesEncryptionUtil.getInstance().encrypt(tmpToken);
                Cookie tcookie = new Cookie(AuthUtil.AUTH_TOKEN, newToken);
                tcookie.setDomain(factory.getStringProperty("cookies.domain", ".localhost").get());
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
