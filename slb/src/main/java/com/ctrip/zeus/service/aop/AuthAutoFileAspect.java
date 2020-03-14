package com.ctrip.zeus.service.aop;

import com.ctrip.zeus.auth.entity.User;
import com.ctrip.zeus.service.auth.AuthDefaultValues;
import com.ctrip.zeus.service.auth.auto.AutoFillService;
import com.ctrip.zeus.util.UserUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by fanqq on 2016/8/26.
 */
@Aspect
@Component
public class AuthAutoFileAspect implements Ordered {
    @Autowired
    private AutoFillService autoFillService;

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public int getOrder() {
        return AspectOrder.AuthAutoFill;
    }

    @Around("execution(* com.ctrip.zeus.restful.resource..*Resource.*(..))")
    public Object autoFill(ProceedingJoinPoint point) throws Throwable {
        User user = null;
        try {
            HttpServletRequest request = findRequestArg(point);
            if (request != null) {
                user = UserUtils.getUser(request);
                if (user != null && user.getUserName() != null && !skipSysUser(user.getUserName())) {
                    autoFillService.autoFill(user, UserUtils.getEmployee(request));
                }
            }
        } catch (Exception e) {
            logger.warn("Auth Auto Fill Failed. UserName:" + (user == null ? "unknown" : user.getUserName()));
        }
        return point.proceed();
    }

    private HttpServletRequest findRequestArg(ProceedingJoinPoint point) {
        Object[] args = point.getArgs();
        for (Object arg : args) {
            if (arg instanceof HttpServletRequest) {
                return (HttpServletRequest) arg;
            }
        }
        return null;
    }

    private boolean skipSysUser(String name) {
        if (name.equalsIgnoreCase(AuthDefaultValues.SLB_SERVER_USER)) {
            return true;
        } else if (name.equalsIgnoreCase(AuthDefaultValues.SLB_HealthCheck_USER) ||
                name.equalsIgnoreCase(AuthDefaultValues.SLB_OPS_USER) ||
                name.equalsIgnoreCase(AuthDefaultValues.SLB_RELEASE_USER)) {
            return true;
        }
        return false;
    }
}
