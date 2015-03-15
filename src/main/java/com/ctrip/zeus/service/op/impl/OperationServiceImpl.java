package com.ctrip.zeus.service.op.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.model.entity.MemberAction;
import com.ctrip.zeus.model.entity.ServerAction;
import com.ctrip.zeus.service.build.BuildService;
import com.ctrip.zeus.service.op.OperationService;
import com.ctrip.zeus.service.status.StatusAppServerService;
import com.ctrip.zeus.service.status.StatusServerService;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author:xingchaowang
 * @date: 3/15/2015.
 */
public class OperationServiceImpl implements OperationService {
    @Resource
    private StatusServerService statusServerService;
    @Resource
    private StatusAppServerService statusAppServerService;

    @Resource
    private AppSlbDao appSlbDao;

    @Resource
    private BuildService buildService;

    @Override
    public void upMember(MemberAction action) {
        try {
            String appName = action.getAppName();

            for (String s : action.getIps()) {
                statusAppServerService.updateStatusAppServer(new StatusAppServerDo().setAppName(appName).setIp(s).setUp(true));
            }

            Set<String> buildNames = new HashSet<>();
            List<AppSlbDo> list = appSlbDao.findAllByApp(appName, AppSlbEntity.READSET_FULL);
            for (AppSlbDo appSlbDo : list) {
                buildNames.add(appSlbDo.getSlbName());
            }

            for (String buildName : buildNames) {
                buildService.build(buildName);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void downMember(MemberAction action) {
        try {
            String appName = action.getAppName();

            for (String s : action.getIps()) {
                statusAppServerService.updateStatusAppServer(new StatusAppServerDo().setAppName(appName).setIp(s).setUp(false));
            }

            Set<String> buildNames = new HashSet<>();
            List<AppSlbDo> list = appSlbDao.findAllByApp(appName, AppSlbEntity.READSET_FULL);
            for (AppSlbDo appSlbDo : list) {
                buildNames.add(appSlbDo.getSlbName());
            }

            for (String buildName : buildNames) {
                buildService.build(buildName);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void upServer(ServerAction action) {
        try {
            for (String s : action.getIps()) {
                statusServerService.updateStatusServer(new StatusServerDo().setIp(s).setUp(true));
            }
            //ToDo:
            buildService.build("default");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void downServer(ServerAction action) {
        try {
            for (String s : action.getIps()) {
                statusServerService.updateStatusServer(new StatusServerDo().setIp(s).setUp(true));
            }
            //ToDo:
            buildService.build("default");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
