package com.ctrip.zeus.service.aop;

import com.ctrip.zeus.restful.message.impl.ErrorResponseHandler;
import com.ctrip.zeus.restful.response.entity.ErrorMessage;
import com.ctrip.zeus.util.ExceptionUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by zhoumy on 2015/3/30.
 */
@Aspect
@Component
public class ExceptionAspect {
//    @AfterThrowing(throwing = "ex",
//            pointcut = "execution(* com.ctrip.zeus.service..*Repository.*(..)) || " +
//                    "execution(* com.ctrip.zeus.service..*Service.*(..))")

    @Resource
    private ErrorResponseHandler errorResponseHandler;
    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Around("execution(* com.ctrip.zeus.restful.resource.*Resource.*(..))")
    public Object interceptException(ProceedingJoinPoint point) {
        Object target = point.getTarget();
        try {
            return point.proceed();
        } catch (Throwable throwable) {
            Throwable cause = (throwable instanceof InvocationTargetException) ? ((InvocationTargetException) throwable).getTargetException() : throwable;
            ErrorMessage err = ExceptionUtils.getErrorMessage(cause);
            try {
                return (Object) errorResponseHandler.handle(err, MediaType.APPLICATION_XML_TYPE);
            } catch (Exception e) {
                return null;
            }
        }
    }
}