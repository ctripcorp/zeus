package com.ctrip.zeus.service.aop;

import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.service.report.meta.ReportService;
import com.ctrip.zeus.service.report.meta.ReportTopic;
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
@Component("reportAspect")
public class ReportAspect implements Ordered {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final DynamicBooleanProperty cmsSync = DynamicPropertyFactory.getInstance().getBooleanProperty("cms.sync", false);
    private static final DynamicBooleanProperty cmsVserverSync = DynamicPropertyFactory.getInstance().getBooleanProperty("cms.vserver.sync", false);

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
                boolean skip = false;
                try {
                    Object arg = point.getArgs()[0];
                    skip = ((Group) arg).isVirtual();
                } catch (Exception ex) {
                }

                if (skip) return point.proceed();

                Object obj = point.proceed();
                try {
                    // No lock is necessary here, it is covered by add_/update_groupName lock
                    reportService.reportGroupAction((Group) obj);
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
                    reportService.reportGroupDeletion(groupId);
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
        if (!cmsSync.get()) {
            return point.proceed();
        }

        String methodName = point.getSignature().getName();
        Object obj = point.proceed();

        VirtualServer value;
        try {
            switch (methodName) {
                case "add":
                    if (!cmsVserverSync.get()) return obj;

                    value = (VirtualServer) obj;
                    try {
                        reportService.reportMetaDataAction(value.getId(), ReportTopic.VS_CREATE);
                    } catch (Exception ex) {
                        logger.error("Fail to push VS_CREATE(ref-id={}) to report queue.", value.getId(), ex);
                    }
                    break;
                case "update":
                    value = (VirtualServer) obj;
                    try {
                        reportService.reportGroupAction(value);
                    } catch (Exception ex) {
                        logger.error("Fail to push GROUP_UPDATE(ref-vs-id={}) to report queue.", value.getId(), ex);
                    }


                    if (!cmsVserverSync.get()) return obj;

                    try {
                        reportService.reportMetaDataAction(value.getId(), ReportTopic.VS_UPDATE);
                    } catch (Exception ex) {
                        logger.error("Fail to push VS_UPDATE(ref-id={}) to report queue.", value.getId(), ex);
                    }
                    break;
                case "delete":
                    if (!cmsVserverSync.get()) return obj;
                    
                    long vsId = (Long) point.getArgs()[0];
                    try {
                        reportService.reportMetaDataAction(vsId, ReportTopic.VS_DELETE);
                    } catch (Exception ex) {
                        logger.error("Fail to push VS_DELETE(ref-id={}) to report queue.", vsId, ex);
                    }
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
