package com.ctrip.zeus.service.report;

import com.ctrip.zeus.model.entity.Group;

/**
 * Created by zhoumy on 2015/7/9.
 */
public interface ReportService {

    void reportGroup(Group group, boolean isNew) throws Exception;
}
