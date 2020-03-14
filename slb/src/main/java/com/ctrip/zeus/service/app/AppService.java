package com.ctrip.zeus.service.app;


import com.ctrip.zeus.model.model.App;

import java.util.List;
import java.util.Set;

/**
 * Created by fanqq on 2016/9/12.
 */
public interface AppService {
    List<App> getAllApps() throws Exception;

    List<App> getAllAppsInSlb() throws Exception;

    Set<String> getAllAppIdsInSlb() throws Exception;

    App getAppBySbu(String sbu) throws Exception;

    Set<String> getAllAppIds() throws Exception;

    App getAppByAppid(String appId) throws Exception;

    App getDefaultApp() throws Exception;

    List<App> getAllAppsByAppIds(Set<String> appIds) throws Exception;

    List<App> getAppsBySlbIds(Set<Long> slbIds) throws Exception;

    List<App> getAppsByVsIds(Set<Long> vsIds) throws Exception;

    List<App> getAppsByDomains(Set<String> domains) throws Exception;

    List<App> getAppsByGroupIds(Set<Long> groupIds) throws Exception;

    Set<String> getAppIdsBySlbId(Long slbId) throws Exception;

    Set<String> getAppIdsByVsId(Long vsId) throws Exception;

    Set<String> getAppIdsByDomain(String domain) throws Exception;

    String getAppIdByGroupId(Long groupId) throws Exception;

    Set<String> getAppIdsBySlbIds(Long[] slbId) throws Exception;

    Set<String> getAppIdsByVsIds(Long[] vsId) throws Exception;

    Set<String> getAppIdsByDomains(String[] domain) throws Exception;

    Set<String> getAppIdsByGroupIds(Long[] groupId) throws Exception;

    void refreshAllRelationTable() throws Exception;

    void groupChange(Long groupId) throws Exception;

    void groupDelete(Long groupId) throws Exception;

    void vsChange(Long vsId) throws Exception;

    void refreshByAppId(String appId) throws Exception;

    void updateAllApp() throws Exception;

    void updateApps(Set<String> appIds) throws Exception;

    App updateApp(String appId) throws Exception;

    boolean hasApp(String appId) throws Exception;

    boolean isAppInSlb(String appId) throws Exception;
}
