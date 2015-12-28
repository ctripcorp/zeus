package com.ctrip.zeus.service.report.meta.impl;

import com.ctrip.zeus.service.report.meta.ReportSyncService;

/**
 * Created by zhoumy on 2015/7/10.
 */
public class MockReportSyncService implements ReportSyncService {

    @Override
    public void sync(Long slbId) throws Exception {

    }

    @Override
    public String syncGroup(Long groupId) throws Exception {
        return "";
    }

    @Override
    public String reportDeletionByGroup(Long groupId) throws Exception {
        return "";
    }

    @Override
    public boolean needSync() throws Exception {
        return false;
    }
}
