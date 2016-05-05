package com.ctrip.zeus.service.clean.impl;

import com.ctrip.zeus.dal.core.ReportDao;
import com.ctrip.zeus.dal.core.ReportDo;
import com.ctrip.zeus.service.clean.AbstractCleanFilter;
import com.ctrip.zeus.service.clean.CleanFilter;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Created by fanqq on 2015/10/26.
 */
@Service("reportCleanFilter")
public class ReportCleanFilter extends AbstractCleanFilter {

    @Resource
    private ReportDao reportDao;
    @Override
    public void runFilter() throws Exception {
        reportDao.cleanReportedItems(new ReportDo());
    }
}
