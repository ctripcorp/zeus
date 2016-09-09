package com.ctrip.zeus.service.aop;

import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.service.report.meta.ReportService;
import com.ctrip.zeus.service.report.meta.ReportTopic;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Created by zhoumy on 2015/7/9.
 */
@Aspect
@Component("reportAspect")
public class ReportAspect implements Ordered {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource
    private ReportService reportService;

    @Around("execution(* com.ctrip.zeus.service.model.GroupRepository.*(..))")
    public Object injectReportGroupAction(ProceedingJoinPoint point) throws Throwable {
        String methodName = point.getSignature().getName();
        switch (methodName) {
            case "add": {
                Object obj = point.proceed();
                reportService.reportMetaDataAction(obj, ReportTopic.GROUP_CREATE);
                return obj;
            }
            case "update": {
                Object obj = point.proceed();
                reportService.reportMetaDataAction(obj, ReportTopic.GROUP_UPDATE);
                return obj;
            }
            case "delete": {
                Object obj = point.proceed();
                try {
                    Long groupId = (Long) point.getArgs()[0];
                    // No lock is necessary here, it is covered by delete_groupId lock
                    reportService.reportMetaDataAction(new Group().setId(groupId), ReportTopic.GROUP_DELETE);
                } catch (Exception ex) {
                    logger.error("Fail to push group to queue.", ex);
                }
                return obj;
            }
            default:
                return point.proceed();
        }
    }

    @Around("execution(* com.ctrip.zeus.service.model.VirtualServerRepository.*(..))")
    public Object injectVsAction(ProceedingJoinPoint point) throws Throwable {
        String methodName = point.getSignature().getName();
        Object obj = point.proceed();

        VirtualServer value;
        try {
            switch (methodName) {
                case "add":
                    value = (VirtualServer) obj;
                    reportService.reportMetaDataAction(value, ReportTopic.VS_CREATE);
                    break;
                case "update":
                    value = (VirtualServer) obj;
                    reportService.reportMetaDataAction(value, ReportTopic.VS_UPDATE);
                    break;
                case "delete":
                    long vsId = (Long) point.getArgs()[0];
                    reportService.reportMetaDataAction(new VirtualServer().setId(vsId), ReportTopic.VS_DELETE);
                    break;
                default:
                    return obj;
            }
        } catch (Exception ex) {
            logger.error("Fail to execute vs injection.", ex);
        }
        return obj;
    }

    @Override
    public int getOrder() {
        return AspectOrder.MessageReport;
    }
}
