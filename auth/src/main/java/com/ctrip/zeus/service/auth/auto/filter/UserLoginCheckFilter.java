package com.ctrip.zeus.service.auth.auto.filter;

import com.ctrip.zeus.auth.util.AuthUtil;
import com.netflix.config.DynamicPropertyFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @Discription: filter that check whether user has been logged in.
 * Redirect user to login page when necessary
 **/
public class UserLoginCheckFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (!DynamicPropertyFactory.getInstance().getBooleanProperty("filter.login.enable", true).get()) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        if (!shouldPass(request) && request.getAttribute(AuthUtil.AUTH_USER) == null) {
            // jump to login page
            response.setContentType("TEXT_HTML");
            response.sendRedirect("/portal/login");
        } else {
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

    @Override
    public void destroy() {

    }

    private boolean shouldPass(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.startsWith("/api/user") || uri.startsWith("/portal/login") || uri.startsWith("/portal/env") ||uri.contains("/static") || uri.contains("/init") || uri.startsWith("/api/config/all");
    }
}
