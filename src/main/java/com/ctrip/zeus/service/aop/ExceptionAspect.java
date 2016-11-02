package com.ctrip.zeus.service.aop;

import com.ctrip.zeus.queue.entity.GroupData;
import com.ctrip.zeus.queue.entity.SlbData;
import com.ctrip.zeus.queue.entity.SlbMessageData;
import com.ctrip.zeus.queue.entity.VsData;
import com.ctrip.zeus.restful.message.impl.ErrorResponseHandler;
import com.ctrip.zeus.service.message.queue.MessageQueue;
import com.ctrip.zeus.support.ObjectJsonWriter;
import com.ctrip.zeus.util.MessageUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.config.DynamicBooleanProperty;
import com.netflix.config.DynamicPropertyFactory;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.glassfish.jersey.server.ContainerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Created by zhoumy on 2015/3/30.
 */
@Aspect
@Component
public class ExceptionAspect implements Ordered {

    private static DynamicBooleanProperty PrintStackTrace = DynamicPropertyFactory.getInstance().getBooleanProperty("slb.stack.trace", false);
    private static DynamicBooleanProperty SendMessage = DynamicPropertyFactory.getInstance().getBooleanProperty("slb.exception.aspect.send.message", false);

    @Resource
    private ErrorResponseHandler errorResponseHandler;
    @Resource
    private MessageQueue messageQueue;

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
            sendMessage(point, cause.getMessage());
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

    private void sendMessage(JoinPoint point, String message) {
        if (!SendMessage.get()) {
            return;
        }
        HttpServletRequest request = findRequestArg(point);
        SlbMessageData res = new SlbMessageData();
        res.setQuery(request.getQueryString())
                .setUri(request.getRequestURI())
                .setSuccess(false)
                .setErrorMessage(message)
                .setClientIp(MessageUtil.getClientIP(request));
        String postData = getPostBody(request, point);
        Long id = 0L;
        if (postData != null) {
            id = parserId(request, postData, res);
            logger.warn("[[postFail=true]]Post Failed Request Body. Body:" + postData);
        }
        try {
            messageQueue.produceMessage(request.getRequestURI(), id, ObjectJsonWriter.write(res));
        } catch (Throwable e) {
            logger.error("Send Message Failed In Exception Aspect.", e);
        }
    }

    private HttpServletRequest findRequestArg(JoinPoint point) {
        Object[] args = point.getArgs();
        for (Object arg : args) {
            if (arg instanceof HttpServletRequest) {
                return (HttpServletRequest) arg;
            }
        }
        return null;
    }

    private String getPostBody(HttpServletRequest request, JoinPoint point) {
        if (request.getMethod().equalsIgnoreCase("POST")) {
            MethodSignature signature = (MethodSignature) point.getSignature();
            Method method = signature.getMethod();
            Annotation[][] annotations = method.getParameterAnnotations();
            Object[] args = point.getArgs();
            for (int i = 0; i < args.length; i++) {
                if (args[i] instanceof String) {
                    if (annotations[i].length == 0) {
                        return (String) args[i];
                    }
                }
            }
        }
        return null;
    }

    private Long parserId(HttpServletRequest request, String data, SlbMessageData res) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            Map<String, Object> jsonpObject = mapper.readValue(data, Map.class);
            if (jsonpObject == null) {
                return 0L;
            }
            switch (request.getRequestURI()) {
                case "/api/group/new":
                case "/api/vgroup/new":
                case "/api/group/update":
                case "/api/vgroup/update":
                case "/api/group/updateCheckUri":
                    if (jsonpObject.containsKey("id")) {
                        GroupData groupData = new GroupData().setId((Long) jsonpObject.get("id"));
                        res.addGroupData(groupData);
                        return groupData.getId();
                    }
                    break;
                case "/api/group/addMember":
                case "/api/group/updateMember":
                case "/api/group/bindVs":
                    if (jsonpObject.containsKey("group-id")) {
                        GroupData groupData = new GroupData().setId((Long) jsonpObject.get("group-id"));
                        res.addGroupData(groupData);
                        return groupData.getId();
                    }
                    break;
                case "/api/vs/new":
                case "/api/vs/update":
                    if (jsonpObject.containsKey("id")) {
                        VsData vsData = new VsData().setId((Long) jsonpObject.get("id"));
                        res.addVsData(vsData);
                        return vsData.getId();
                    }
                    break;
                case "/api/slb/new":
                case "/api/slb/update":
                case "/api/slb/addServer":
                    if (jsonpObject.containsKey("id")) {
                        SlbData slbData = new SlbData().setId((Long) jsonpObject.get("id"));
                        res.addSlbData(slbData);
                        return slbData.getId();
                    }
                    break;
            }
        } catch (IOException e) {
            logger.warn("Can not parser Request Body.", e);
        }
        return 0L;
    }
}