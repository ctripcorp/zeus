package com.ctrip.zeus.service.report.meta;

import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.report.entity.ReportGroup;

import java.util.List;

/**
 * Created by zhoumy on 2015/7/9.
 */
public interface ReportService {

    void reportGroupAction(Group group) throws Exception;

    void reportGroupAction(VirtualServer virtualServer) throws Exception;

    void reportGroupDeletion(Long groupId) throws Exception;

    void reportMetaDataAction(Long targetId, ReportTopic reportTopic) throws Exception;

    List<ReportGroup> listErrors() throws Exception;

    ReportGroup getReportGroupById(Long groupId) throws Exception;
}