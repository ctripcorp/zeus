package com.ctrip.zeus.service.aop;

import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.service.report.meta.ReportService;
import com.netflix.config.DynamicBooleanProperty;
import com.netflix.config.DynamicPropertyFactory;
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
@Component
public class ReportAspect implements Ordered {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final DynamicBooleanProperty cmsSync = DynamicPropertyFactory.getInstance().getBooleanProperty("cms.sync", false);

    @Resource
    private ReportService reportService;

    @Around("execution(* com.ctrip.zeus.service.model.GroupRepository.*(..))")
    public Object injectReportGroupAction(ProceedingJoinPoint point) throws Throwable {
        if (!cmsSync.get())
            return point.proceed();
        String methodName = point.getSignature().getName();
        switch (methodName) {
            case "add":
            case "update": {
                Object obj = point.proceed();
                try {
                    // No lock is necessary here, it is covered by add_/update_groupName lock
                    reportService.reportGroup((Group) obj);
                } catch (Exception ex) {
                    logger.error("Fail to report group to queue.", ex);
                }
                return obj;
            }
            case "delete": {
                Object obj = point.proceed();
                try {
                    Long groupId = (Long) point.getArgs()[0];
                    // No lock is necessary here, it is covered by delete_groupId lock
                    reportService.reportDeletion(groupId);
                } catch (Exception ex) {
                    logger.error("Fail to push group to queue.", ex);
                }
                return obj;
            }
            default:
                return point.proceed();
        }
    }

    @Around("execution(* com.ctrip.zeus.service.model.VirtualServerRepository.update(*))")
    public Object injectReportGroupsAction(ProceedingJoinPoint point) throws Throwable {
        if (!cmsSync.get())
            return point.proceed();
        Object obj = point.proceed();
        try {
            reportService.reportByVs((VirtualServer) point.getArgs()[0]);
        } catch (Exception ex) {
            logger.error("Fail to push groups to queue by virtual server.", ex);
        }
        return obj;
    }

    @Override
    public int getOrder() {
        return AspectOrder.MessageReport;
    }
}
