package com.ctrip.zeus.service.aop;

import com.ctrip.zeus.restful.message.impl.ErrorResponseHandler;
import com.netflix.config.DynamicBooleanProperty;
import com.netflix.config.DynamicPropertyFactory;
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

    private static DynamicBooleanProperty PrintStackTrace = DynamicPropertyFactory.getInstance().getBooleanProperty("slb.stack.trace", false);

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
            logger.error(objectName + " throws an error when calling " + methodName + ".", throwable);
            Throwable cause = (throwable instanceof InvocationTargetException) ? ((InvocationTargetException) throwable).getTargetException() : throwable;
            try {
                StringBuilder builder = new StringBuilder();
                for (StackTraceElement ste : cause.getStackTrace()) {
                    builder.append(ste.toString() + "\n");
                }
                MediaType mediaType = null;
                boolean printStackTrace = PrintStackTrace.get();
                for (Object arg : point.getArgs()) {
                    if (arg instanceof ContainerRequest) {
                        ContainerRequest cr = (ContainerRequest) arg;
                        mediaType = cr.getMediaType();
                        try {
                            String stackTrace = cr.getUriInfo().getQueryParameters().getFirst("stackTrace");
                            if (stackTrace != null)
                                printStackTrace = Boolean.parseBoolean(stackTrace);
                        } catch (Exception ex) {
                        }
                        break;
                    }
                }
                return errorResponseHandler.handle(cause, mediaType, printStackTrace);
            } catch (Exception e) {
                logger.error("Error response handler doesn't work.", e);
                return null;
            }
        }
    }

    @Override
    public int getOrder() {
        return AspectOrder.InterceptException;
    }
}