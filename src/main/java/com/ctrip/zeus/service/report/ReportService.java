package com.ctrip.zeus.service.report;

import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.report.entity.ReportGroup;

import java.util.List;

/**
 * Created by zhoumy on 2015/7/9.
 */
public interface ReportService {

    void reportGroup(Group group) throws Exception;

    void reportDeletion(Long groupId) throws Exception;

    List<ReportGroup> listErrors() throws Exception;

    ReportGroup getReportGroupById(Long groupId) throws Exception;
}