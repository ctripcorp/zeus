package com.ctrip.zeus.server;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.DelegatingFilterProxy;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 * Created by zhoumy on 2016/8/9.
 */
@Component
public class PreCheckFilter extends DelegatingFilterProxy {
    private static boolean green;

    public PreCheckFilter() {
        this("preCheckFilter");
    }

    public PreCheckFilter(String targetBeanName) {
        super(targetBeanName);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (!green) {
            if (response instanceof HttpServletResponse) {
                HttpServletResponse res = (HttpServletResponse) response;
                res.reset();
                res.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
                res.getWriter().write("Service has not prepared.");
                return;
            }
        }
        chain.doFilter(request, response);
    }

    public void setGreenLight(boolean green) {
        this.green = green;
    }
}