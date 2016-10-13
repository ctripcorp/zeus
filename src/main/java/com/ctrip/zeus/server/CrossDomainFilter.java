package com.ctrip.zeus.server;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by lu.wang on 2016/6/16.
 */
public class CrossDomainFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse, final FilterChain filterChain) throws IOException, ServletException {
        final HttpServletRequest request = (HttpServletRequest) servletRequest;
        final HttpServletResponse response = (HttpServletResponse) servletResponse;

        String origin = request.getHeader("Origin");
        if (origin != null) {
            // Client request header contains the origin header
            response.setHeader("Access-Control-Allow-Origin", origin);
            response.setHeader("Access-Control-Allow-Credentials", "true");

            String requestMethod = request.getHeader("Access-Control-Request-Method");
            String requestHeaders = request.getHeader("Access-Control-Request-Headers");
            if (request.getMethod().equals("Origin") && (requestMethod != null || requestHeaders != null)) {
                response.setHeader("Access-Control-Allow-Headers", requestHeaders);
                response.setHeader("Access-Control-Allow-Methods", requestMethod);
            }
        }
        filterChain.doFilter(request, response);
    }

    @Override
    public void destroy() {

    }
}
