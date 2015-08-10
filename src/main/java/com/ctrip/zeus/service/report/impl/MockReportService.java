package com.ctrip.zeus.service.report.impl;

import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.report.entity.ReportGroup;
import com.ctrip.zeus.service.report.ReportService;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by zhoumy on 2015/7/9.
 */
@Component("mockReportService")
public class MockReportService implements ReportService {
    @Override
    public void reportGroup(Group group) throws Exception {

    }

    @Override
    public void reportDeletion(Long groupId) throws Exception {

    }

    @Override
    public List<ReportGroup> listErrors() throws Exception {
        return null;
    }

    @Override
    public ReportGroup getReportGroupById(Long groupId) throws Exception {
        return null;
    }
}
