package com.ctrip.zeus.service.conf.impl;

import com.ctrip.zeus.dal.core.AppSlbDao;
import com.ctrip.zeus.dal.core.AppSlbDo;
import com.ctrip.zeus.dal.core.AppSlbEntity;
import com.ctrip.zeus.service.build.BuildService;
import com.ctrip.zeus.service.conf.ActivateService;
import com.ctrip.zeus.service.conf.ConfService;
import com.ctrip.zeus.service.model.AppRepository;
import org.springframework.stereotype.Service;
import org.unidal.dal.jdbc.DalException;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author:xingchaowang
 * @date: 3/15/2015.
 */
@Service("confService")
public class ConfServiceImpl implements ConfService {
    @Resource
    private ActivateService activateService;

    @Resource
    private AppSlbDao appSlbDao;

    @Resource
    private BuildService buildService;

    @Override
    public void activate(List<String> slbNames, List<String> appNames) {
        try {
            for (String slbName : slbNames) {
                activateService.activeSlb(slbName);
            }
            for (String appName : appNames) {
                activateService.activeApp(appName);
            }


            Set<String> buildNames = new HashSet<>();
            buildNames.addAll(slbNames);

            List<AppSlbDo> list = appSlbDao.findAllByApps(appNames.toArray(new String[]{}), AppSlbEntity.READSET_FULL);
            for (AppSlbDo appSlbDo : list) {
                buildNames.add(appSlbDo.getSlbName());
            }

            for (String buildName : buildNames) {
                buildService.build(buildName);
            }

        } catch (DalException e) {
            throw new RuntimeException(e);
        }
    }
}
