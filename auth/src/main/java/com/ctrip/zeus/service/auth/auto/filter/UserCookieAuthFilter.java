package com.ctrip.zeus.service.auth.auto.filter;

import com.ctrip.zeus.auth.util.AuthUtil;
import com.ctrip.zeus.util.AesEncryptionUtil;
import com.ctrip.zeus.util.ClientIpUtil;
import com.ctrip.zeus.util.UserUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @Discription
 **/
public class UserCookieAuthFilter implements Filter {

    private final Logger logger = LoggerFactory.getLogger(UserCookieAuthFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;

        if (httpRequest.getHeader(AuthUtil.AUTH_USER) != null) {
            // skip checking cookie if AUTH_USER header has been set
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }
        String token = null;
        if (httpRequest.getCookies() != null) {
            for (Cookie cookie : httpRequest.getCookies()) {
                if (AuthUtil.AUTH_USER_TOKEN.equalsIgnoreCase(cookie.getName()) && !StringUtils.isEmpty(cookie.getValue())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        if (StringUtils.isBlank(token)) {
            token = httpRequest.getHeader(AuthUtil.AUTH_USER_TOKEN);
        }
        if (token != null) {
            String decoded = AesEncryptionUtil.getInstance().decrypt(token);
            String[] tokens = decoded.split(";");
            String tokenClientIp = tokens[1];
            if (ClientIpUtil.getClientIP(httpRequest).equalsIgnoreCase(tokenClientIp)) {
                String tokenUserName = tokens[0];
                logger.info("Set user name: " + tokenUserName + "header via token. ");
                servletRequest.setAttribute(AuthUtil.AUTH_USER, tokenUserName);
                // todo set bu, name and email attributes
            }
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {

    }
}
