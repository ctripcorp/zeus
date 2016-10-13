package com.ctrip.zeus.crossdomain;

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

        String origin = request.getHeader(HeaderUtil.ORIGIN);
        if (origin != null) {
            // Client request header contains the origin header
            response.setHeader(HeaderUtil.ALLOW_ORIGIN, origin);
            response.setHeader(HeaderUtil.ALLOW_CREDENTIALS, HeaderUtil.HeaderValues.ALLOW_CREDENTIALS_TRUE);

            String requestMethod = request.getHeader(HeaderUtil.REQUEST_METHOD);
            String requestHeaders = request.getHeader(HeaderUtil.REQUEST_HEADERS);
            if (request.getMethod().equals(HeaderUtil.OPTIONS_METHOD) && (requestMethod != null || requestHeaders != null)) {
                response.setHeader(HeaderUtil.ALLOW_HEADERS, requestHeaders);
                response.setHeader(HeaderUtil.ALLOW_METHODS, requestMethod);
            }
        }
        filterChain.doFilter(request, response);
    }

    @Override
    public void destroy() {

    }
}
