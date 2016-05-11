package com.ctrip.zeus.service.aop;

import com.ctrip.zeus.auth.Authorize;
import com.ctrip.zeus.auth.Authorizer;
import com.ctrip.zeus.auth.ResourceGroupProvider;
import com.ctrip.zeus.restful.message.impl.ErrorResponseHandler;
import com.ctrip.zeus.service.aop.util.AopUtil;
import com.netflix.config.DynamicBooleanProperty;
import com.netflix.config.DynamicPropertyFactory;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.glassfish.jersey.server.ContainerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * User: mag
 * Date: 4/22/2015
 * Time: 2:59 PM
 */
@Aspect
@Component
public class AuthorizationAspect implements Ordered{

    private DynamicBooleanProperty enableAuthorize = DynamicPropertyFactory.getInstance().getBooleanProperty("server.authorization.enable", false);

    @Resource
    private Authorizer authorizer;

    @Before("execution(* com.ctrip.zeus.restful.resource.*Resource.*(..))")
    public void interceptAuthorize(JoinPoint point) throws Throwable {
        if (!enableAuthorize.get()){
            return;
        }
        MethodSignature signature = (MethodSignature)point.getSignature();
        Method method = signature.getMethod();
        Authorize authorize = method.getAnnotation(Authorize.class);
        if (authorize != null){
            String resourceName = authorize.name();
            HttpServletRequest request = AopUtil.findRequestArg(point);
            // not found request parameter
            if (request == null){
                return;
            }
            Class<? extends ResourceGroupProvider> groupProviderClass = authorize.groupProvider();
            ResourceGroupProvider groupProvider = groupProviderClass.newInstance();

            String resourceGroup = groupProvider.provideResourceGroup(method, request);
            String userName = request.getRemoteUser();
            authorizer.authorize(userName,resourceName,resourceGroup);
        }
    }

    @Override
    public int getOrder() {
        return AspectOrder.Authorization;
    }
}