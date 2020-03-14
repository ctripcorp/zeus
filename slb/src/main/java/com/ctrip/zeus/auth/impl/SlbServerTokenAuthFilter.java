package com.ctrip.zeus.auth.impl;


import com.ctrip.zeus.auth.util.AuthUtil;
import com.ctrip.zeus.service.auth.AuthDefaultValues;
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
                setAssertion(request, AuthDefaultValues.SLB_SERVER_USER);
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

    private void setAssertion(HttpServletRequest request, String userName) {
        Assertion assertion = new AssertionImpl(userName);
        request.setAttribute(AuthUtil.AUTH_USER, userName);
        request.setAttribute(AbstractCasFilter.CONST_CAS_ASSERTION, assertion);
        final HttpSession session = request.getSession(false);
        if (session != null) {
            session.setAttribute(AbstractCasFilter.CONST_CAS_ASSERTION, assertion);
        }
    }
}
