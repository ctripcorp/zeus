package com.ctrip.zeus.service.report.meta.impl;

import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.report.entity.ReportGroup;
import com.ctrip.zeus.service.report.meta.ReportService;
import com.ctrip.zeus.service.report.meta.ReportTopic;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by zhoumy on 2015/7/9.
 */
@Component("mockReportService")
public class MockReportService implements ReportService {
    @Override
    public void reportGroupAction(Group group) throws Exception {

    }

    @Override
    public void reportGroupAction(VirtualServer virtualServer) throws Exception {

    }

    @Override
    public void reportGroupDeletion(Long groupId) throws Exception {

    }

    @Override
    public void reportMetaDataAction(Long targetId, ReportTopic reportTopic) throws Exception {

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
