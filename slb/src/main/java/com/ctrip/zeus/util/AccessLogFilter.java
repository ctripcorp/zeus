package com.ctrip.zeus.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;

/**
 * Created by fanqq on 2015/6/17.
 */
public class AccessLogFilter implements Filter {
    private static final Logger LOGGER = LoggerFactory.getLogger(AccessLogFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        filterChain.doFilter(servletRequest,servletResponse);
        addLog((HttpServletRequest)servletRequest, (HttpServletResponse) servletResponse);
    }

    @Override
    public void destroy() {

    }
    private void addLog(HttpServletRequest request ,HttpServletResponse response)
    {
        StringBuilder sb = new StringBuilder(256);
        sb.append("Request:\nHost:").append(request.getRemoteHost())
                .append("\nURI:").append(request.getRequestURI())
                .append("\nRemoteAddr:").append(request.getRemoteAddr())
                .append("\nMethod:").append(request.getMethod())
                .append("\nQueryString:").append(request.getQueryString())
                .append("\nRequestURL:").append(request.getRequestURL())
                .append("\nContentType:").append(request.getContentType())
                .append("\nProtocol:").append(request.getProtocol())
                ;
        Enumeration<String> headerNames=request.getHeaderNames();
        for(Enumeration e=headerNames;e.hasMoreElements();){
            String name = e.nextElement().toString();
            sb.append("\n").append(name).append(":").append(request.getHeader(name));
        }

        sb.append("\nResponse:\nStatus:").append(response.getStatus())
                .append("\nContentType:").append(response.getContentType())
                .append("\nCharacterEncoding:").append(response.getCharacterEncoding());
        Collection<String> header=response.getHeaderNames();
        for(String name : header){
            sb.append("\n").append(name).append(":").append(request.getHeader(name));
        }
        LOGGER.info(sb.toString());
    }
}
