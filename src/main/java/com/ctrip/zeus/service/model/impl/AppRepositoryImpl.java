package com.ctrip.zeus.service.model.impl;

import com.ctrip.zeus.dal.core.AppDo;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.App;
import com.ctrip.zeus.model.entity.AppList;
import com.ctrip.zeus.service.model.AppQuery;
import com.ctrip.zeus.service.model.AppRepository;
import com.ctrip.zeus.service.model.AppSync;
import com.ctrip.zeus.service.model.ArchiveService;
import com.ctrip.zeus.support.C;
import org.springframework.stereotype.Repository;
import org.unidal.dal.jdbc.DalException;

import javax.annotation.Resource;

/**
 * @author:xingchaowang
 * @date: 3/7/2015.
 */
@Repository("appRepository")
public class AppRepositoryImpl implements AppRepository {
    @Resource
    private AppSync appSync;
    @Resource
    private AppQuery appQuery;
    @Resource
    private ArchiveService archiveService;

    @Override
    public AppList list() throws Exception {
        AppList appList = new AppList();

        for (App app : appQuery.getAll()) {
            appList.addApp(app);
        }
        appList.setTotal(appList.getApps().size());
        return appList;

    }

    @Override
    public AppList list(String slbName, String virtualServerName) throws Exception {
        AppList appList = new AppList();

        for (App app : appQuery.getBy(slbName, virtualServerName)) {
            appList.addApp(app);
        }
        appList.setTotal(appList.getApps().size());
        return appList;

    }

    @Override
    public AppList listLimit(long fromId, int maxCount) throws Exception {
        AppList appList = new AppList();

        for (App app : appQuery.getLimit(fromId, maxCount)) {
            appList.addApp(app);
        }
        appList.setTotal(appList.getApps().size());
        return appList;

    }

    @Override
    public App get(String appName) throws Exception {
        return appQuery.get(appName);
    }

    @Override
    public App getByAppId(String appId) throws Exception {
        return appQuery.getByAppId(appId);
    }

    @Override
    public long add(App app) throws Exception {
        AppDo d = appSync.add(app);
        archiveService.archiveApp(C.toApp(d));
        return d.getKeyId();

    }

    @Override
    public void update(App app) throws Exception {
        app = C.toApp(appSync.update(app));
        archiveService.archiveApp(app);

    }

    @Override
    public int delete(String appName) throws Exception {
        int count = appSync.delete(appName);
        archiveService.deleteAppArchive(appName);
        return count;

    }
}
