package com.ctrip.zeus.service.aop.util;

import org.aspectj.lang.JoinPoint;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by lu.wang on 2016/3/28.
 */
public class AopUtil {

    public static HttpServletRequest findRequestArg(JoinPoint point) {
        if (point == null || point.getArgs() == null)
            return null;
        Object[] args = point.getArgs();
        for (Object arg : args) {
            if (arg != null && arg instanceof HttpServletRequest){
                return (HttpServletRequest)arg;
            }
        }
        return null;
    }

}
