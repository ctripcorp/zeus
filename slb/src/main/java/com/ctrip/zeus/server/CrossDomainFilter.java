package com.ctrip.zeus.server;

import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by lu.wang on 2016/6/16.
 */
public class CrossDomainFilter implements Filter {

    private DynamicStringProperty allowOriginList = DynamicPropertyFactory.getInstance().getStringProperty("allow.origin", "");
    private DynamicStringProperty allowHeaderList = DynamicPropertyFactory.getInstance().getStringProperty("allow.header", "Content-Type,Target-Url,slb_token,Target-Method,UserCookie,_stok");
    private String[] allowOrigins = null;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        allowOrigins = allowOriginList.get().split(";");
        allowOriginList.addCallback(new Runnable() {
            @Override
            public void run() {
                allowOrigins = allowOriginList.get().split(";");
            }
        });
    }

    @Override
    public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse, final FilterChain filterChain) throws IOException, ServletException {
        final HttpServletRequest request = (HttpServletRequest) servletRequest;
        final HttpServletResponse response = (HttpServletResponse) servletResponse;

        String origin = request.getHeader("Origin");
        if (origin != null) {
            String allowOrg = null;
            if (allowOrigins == null) {
                allowOrigins = allowOriginList.get().split(";");
            }
            for (String allow : allowOrigins) {
                if (allow.equalsIgnoreCase(origin)) {
                    allowOrg = allow;
                    break;
                }
            }
            // Client request header contains the origin header
            if (allowOrg != null) {
                response.setHeader("Access-Control-Allow-Origin", allowOrg);
                response.setHeader("Access-Control-Allow-Credentials", "true");
            }
            if (request.getMethod().equals("OPTIONS")) {
                response.setHeader("Access-Control-Allow-Headers", allowHeaderList.get());
                response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
            }
        }
        filterChain.doFilter(request, response);
    }

    @Override
    public void destroy() {

    }
}
