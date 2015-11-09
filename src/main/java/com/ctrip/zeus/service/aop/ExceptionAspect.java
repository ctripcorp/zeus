package com.ctrip.zeus.service.aop;

import com.ctrip.zeus.restful.message.impl.ErrorResponseHandler;
import com.ctrip.zeus.restful.response.entity.ErrorMessage;
import com.ctrip.zeus.util.ExceptionUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.glassfish.jersey.server.ContainerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.ws.rs.core.MediaType;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by zhoumy on 2015/3/30.
 */
@Aspect
@Component
public class ExceptionAspect implements Ordered {
    @Resource
    private ErrorResponseHandler errorResponseHandler;
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Around("execution(* com.ctrip.zeus.restful.resource.*Resource.*(..))")
    public Object interceptException(ProceedingJoinPoint point) {
        String objectName = point.getSignature().getDeclaringTypeName();
        String methodName = point.getSignature().getName();
        try {
            return point.proceed();
        } catch (Throwable throwable) {
            logger.error(objectName + " throws an error when calling " + methodName + ".");
            Throwable cause = (throwable instanceof InvocationTargetException) ? ((InvocationTargetException) throwable).getTargetException() : throwable;
            try {
                StringBuilder builder = new StringBuilder();
                for (StackTraceElement ste : cause.getStackTrace()) {
                    builder.append(ste.toString() + "\n");
                }
                logger.error(builder.toString());

                MediaType mediaType = null;
                boolean printStackTrace = false;
                for (Object arg : point.getArgs()) {
                    if (arg instanceof ContainerRequest) {
                        ContainerRequest cr = (ContainerRequest) arg;
                        mediaType = cr.getMediaType();
                        try {
                            printStackTrace = Boolean.parseBoolean(cr.getHeaderString("slb-stack-trace"));
                        } catch (Exception ex) {
                            printStackTrace = false;
                        }
                        break;
                    }
                }
                if (mediaType == null) {
                    logger.warn("Request media type cannot be found - use json by default.");
                }
                return errorResponseHandler.handle(cause, mediaType, printStackTrace);
            } catch (Exception e) {
                logger.error("Error response handler doesn't work.");
                return null;
            }
        }
    }

    @Override
    public int getOrder() {
        return AspectOrder.InterceptException;
    }
}