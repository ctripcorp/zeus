package com.ctrip.zeus.service.auto;

import com.ctrip.zeus.service.tools.initialization.impl.InitializationCheckServiceImpl;
import com.netflix.config.DynamicBooleanProperty;
import com.netflix.config.DynamicPropertyFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @Discription
 **/
public class DBInitializationFilter implements Filter {

    private final DynamicBooleanProperty enabled = DynamicPropertyFactory.getInstance().getBooleanProperty("db.initialization.filter.enabled", true);

    private final String TEXT_HTML_CONTENT_TYPE = "text/html; ISO-8859-1";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (!enabled.get()) {
            chain.doFilter(request, response);
            return;
        }
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        HttpServletRequest servletRequest = (HttpServletRequest) request;

        if (!InitializationCheckServiceImpl.staticIsInitialized() && !shouldPass(servletRequest)) {
            httpServletResponse.setContentType(TEXT_HTML_CONTENT_TYPE);
            httpServletResponse.sendRedirect("/portal/env");
        } else {
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {

    }

    private boolean shouldPass(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.startsWith("/portal/env") || uri.contains("/static") || uri.startsWith("/api/init");
    }
}
