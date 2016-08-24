package com.ctrip.zeus.service.report.meta;

/**
 * Created by zhoumy on 2016/8/24.
 */
public interface ReportEventListener {
   void reportMetaEvent(Object target, ReportTopic reportTopic);
}
