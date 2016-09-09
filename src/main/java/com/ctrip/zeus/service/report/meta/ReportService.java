package com.ctrip.zeus.service.report.meta;

import com.ctrip.zeus.report.entity.ReportFeed;
import com.ctrip.zeus.report.entity.ReportGroup;

import java.util.List;

/**
 * Created by zhoumy on 2015/7/9.
 */
public interface ReportService {

    void reportMetaDataAction(Object target, ReportTopic reportTopic);

    List<ReportGroup> listErrors() throws Exception;

    List<ReportFeed> listFeeds() throws Exception;
}