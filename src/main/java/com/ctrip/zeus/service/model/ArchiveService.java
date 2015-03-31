package com.ctrip.zeus.service.model;

import com.ctrip.zeus.model.entity.App;
import com.ctrip.zeus.model.entity.Archive;
import com.ctrip.zeus.model.entity.Slb;
import java.util.List;

/**
 * @author:xingchaowang
 * @date: 3/14/2015.
 */
public interface ArchiveService {
    int archiveSlb(Slb slb) throws Exception;
    int archiveApp(App app) throws Exception;

    int deleteSlbArchive(String slbName) throws Exception;
    int deleteAppArchive(String appName) throws Exception;

    Slb getSlb(String name, int version) throws Exception;
    App getApp(String name, int version) throws Exception;

    Slb getMaxVersionSlb(String name) throws Exception;
    App getMaxVersionApp(String name) throws Exception;

    List<Slb> getAllSlb(String name) throws Exception;
    List<App> getAllApp(String name) throws Exception;

    Archive getLatestAppArchive(String appName) throws Exception;
    Archive getLatestSlbArchive(String slbName) throws Exception;
}
