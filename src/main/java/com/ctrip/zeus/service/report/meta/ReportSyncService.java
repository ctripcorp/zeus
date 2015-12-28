package com.ctrip.zeus.service.report.meta;

/**
 * Created by zhoumy on 2015/7/10.
 */
public interface ReportSyncService {

    void sync(Long slbId) throws Exception;

    String syncGroup(Long groupId) throws Exception;

    String reportDeletionByGroup(Long groupId) throws Exception;

    boolean needSync() throws Exception;
}
