package com.ctrip.zeus.auth.impl;


import com.ctrip.zeus.auth.util.AuthUtil;
import com.ctrip.zeus.service.auth.AuthDefaultValues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by fanqq on 2016/8/11.
 */
public class SlbServerTokenAuthFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(SlbServerTokenAuthFilter.class);


    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
        final HttpServletRequest request = (HttpServletRequest) servletRequest;
        final HttpServletResponse response = (HttpServletResponse) servletResponse;

        //1. check whether it is called from other slb servers.
        String slbServerToken = request.getHeader(AuthUtil.SERVER_TOKEN_HEADER);
        if (slbServerToken != null) {
            if (TokenManager.validateToken(slbServerToken)) {
                request.setAttribute(AuthUtil.AUTH_USER, AuthDefaultValues.SLB_SERVER_USER);
                logger.info("Auth by slb server token filter.");
                chain.doFilter(request, response);
                return;
            }
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {

    }
}
