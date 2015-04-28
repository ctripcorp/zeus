package com.ctrip.zeus.service.model.impl;

import com.ctrip.zeus.dal.core.AppDo;
import com.ctrip.zeus.lock.DistLock;
import com.ctrip.zeus.lock.impl.MysqlDistLock;
import com.ctrip.zeus.model.entity.App;
import com.ctrip.zeus.model.entity.AppServer;
import com.ctrip.zeus.service.model.handler.AppQuery;
import com.ctrip.zeus.service.model.AppRepository;
import com.ctrip.zeus.service.model.handler.AppSync;
import com.ctrip.zeus.service.model.ArchiveService;
import com.ctrip.zeus.support.C;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

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
    public List<App> list() throws Exception {
        List<App> list = new ArrayList<>();
        for (App app : appQuery.getAll()) {
            list.add(app);
        }
        return list;

    }

    @Override
    public List<App> list(String slbName, String virtualServerName) throws Exception {
        List<App> list = new ArrayList<>();
        for (App app : appQuery.getBySlbAndVirtualServer(slbName, virtualServerName)) {
            list.add(app);
        }
        return list;
    }

    @Override
    public List<App> listLimit(long fromId, int maxCount) throws Exception {
        List<App> list = new ArrayList<>();
        for (App app : appQuery.getLimit(fromId, maxCount)) {
            list.add(app);
        }
        return list;
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
        archiveService.archiveApp(appQuery.getById(d.getId()));
        return d.getKeyId();

    }

    @Override
    public void update(App app) throws Exception {
        if (app == null)
            return;
        DistLock lock = new MysqlDistLock(app.getName() + "_update");
        lock.lock();
        AppDo d = appSync.update(app);
        app = appQuery.getById(d.getId());
        archiveService.archiveApp(app);
        lock.unlock();
    }

    @Override
    public int delete(String appName) throws Exception {
        int count = appSync.delete(appName);
        archiveService.deleteAppArchive(appName);
        return count;

    }

    @Override
    public List<String> listAppsByAppServer(String appServerIp) throws Exception {
        return appQuery.getByAppServer(appServerIp);
    }

    @Override
    public List<String> listAppServersByApp(String appName) throws Exception {
        return appQuery.getAppServersByApp(appName);
    }

    @Override
    public List<AppServer> getAppServersByApp(String appName) throws Exception {
        return appQuery.listAppServersByApp(appName);
    }
}
