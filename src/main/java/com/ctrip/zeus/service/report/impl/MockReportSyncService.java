package com.ctrip.zeus.service.report.impl;

import com.ctrip.zeus.service.report.ReportSyncService;

/**
 * Created by zhoumy on 2015/7/10.
 */
public class MockReportSyncService implements ReportSyncService {
    @Override
    public void sync() throws Exception {

    }

    @Override
    public boolean needSync() throws Exception {
        return false;
    }
}
