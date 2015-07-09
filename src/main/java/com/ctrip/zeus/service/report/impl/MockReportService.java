package com.ctrip.zeus.service.report.impl;

import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.service.report.ReportService;
import org.springframework.stereotype.Component;

/**
 * Created by zhoumy on 2015/7/9.
 */
@Component("mockReportService")
public class MockReportService implements ReportService {
    @Override
    public void reportGroup(Group group) throws Exception {

    }

    @Override
    public void sync() throws Exception {

    }

    @Override
    public boolean needSync() throws Exception {
        return false;
    }
}
