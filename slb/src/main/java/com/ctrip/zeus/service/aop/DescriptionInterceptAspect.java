package com.ctrip.zeus.service.aop;

import com.ctrip.zeus.util.MessageUtil;
import com.netflix.config.DynamicBooleanProperty;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Aspect
@Component
public class DescriptionInterceptAspect implements Ordered {
    private static DynamicStringProperty descriptionIntercept = DynamicPropertyFactory.getInstance().getStringProperty("description.intercept", "/api/op/up;/api/op/down;/api/op/fall;/api/group/update;/api/group/bindVs;/api/group/unbindVs;/api/group/addMember;/api/group/removeMember;/api/vs/update;/api/vs/addDomain;/api/vs/removeDomain;/api/policy/update;/api/policy/deactivate;/api/policy/delete");
    private static DynamicBooleanProperty descriptionEnable = DynamicPropertyFactory.getInstance().getBooleanProperty("description.intercept.enable", true);


    @Around("execution(* com.ctrip.zeus.restful.resource..*Resource.*(..))")
    public Object interceptException(ProceedingJoinPoint point) throws Throwable {
        if (descriptionEnable.get()) {
            HttpServletRequest request = findRequestArg(point);
            if (request != null) {
                String[] apis = descriptionIntercept.get().split(";");
                String uri = request.getRequestURI();
                for (String api : apis) {
                    if (uri.startsWith(api.trim())) {
                        MessageUtil.validateDescriptionInQuery(request);
                        break;
                    }
                }
            }
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

    @Override
    public int getOrder() {
        return AspectOrder.DescriptionIntercept;
    }
}
