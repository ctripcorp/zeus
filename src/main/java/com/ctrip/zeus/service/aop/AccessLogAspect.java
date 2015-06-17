package com.ctrip.zeus.service.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by fanqq on 2015/6/17.
 */
@Aspect
@Component
public class AccessLogAspect implements Ordered {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccessLogAspect.class);
    @Before("execution(* com.ctrip.zeus.restful.resource.*Resource.*(..))")
    public void accessLog(JoinPoint point) throws Throwable {
        HttpServletRequest request = findRequestArg(point);
        // not found request parameter
        if (request == null){
            LOGGER.warn("Not Found HttpServletRequest!");
            return;
        }
        addLog(request);
    }

    private void addLog(HttpServletRequest request)
    {
        StringBuilder sb = new StringBuilder(256);
        sb.append("\nHost:").append(request.getRemoteHost())
                .append("\nURI:").append(request.getRequestURI())
                .append("\nRemoteAddr:").append(request.getRemoteAddr())
                .append("\nMethod:").append(request.getMethod())
                .append("\nQueryString:").append(request.getQueryString())
                .append("\nRequestURL:").append(request.getRequestURL())
                .append("\nContentType:").append(request.getContentType());
        LOGGER.info(sb.toString());
    }

    private HttpServletRequest findRequestArg(JoinPoint point) {
        Object[] args = point.getArgs();
        for (Object arg : args) {
            if (arg instanceof HttpServletRequest){
                return (HttpServletRequest)arg;
            }
        }
        return null;
    }

    @Override
    public int getOrder() {
        return 10;
    }
}
