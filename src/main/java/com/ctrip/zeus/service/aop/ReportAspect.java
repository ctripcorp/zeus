package com.ctrip.zeus.service.aop;

import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.service.report.ReportService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Created by zhoumy on 2015/7/9.
 */
@Aspect
@Component
public class ReportAspect implements Ordered {

    @Resource
    private ReportService reportService;

    @Around("execution(* com.ctrip.zeus.service.model.GroupRepository.*(..))")
    public Object injectReportAction(ProceedingJoinPoint point) throws Throwable {
        String methodName = point.getSignature().getName();
        switch (methodName) {
            case "add":
            case "update":
                break;
            default:
                return point.proceed();
        }
        Object obj = point.proceed();
        reportService.reportGroup((Group)obj);
        return obj;
    }

    @Override
    public int getOrder() {
        return AspectOrder.MessageReport;
    }
}
