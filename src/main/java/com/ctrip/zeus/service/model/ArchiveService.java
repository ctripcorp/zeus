package com.ctrip.zeus.service.model;

import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.Archive;
import com.ctrip.zeus.model.entity.Slb;
import java.util.List;

/**
 * @author:xingchaowang
 * @date: 3/14/2015.
 */
public interface ArchiveService {
    int archiveSlb(Slb slb) throws Exception;
    int archiveGroup(Group app) throws Exception;

    int deleteSlbArchive(String slbName) throws Exception;
    int deleteGroupArchive(String appName) throws Exception;

    Slb getSlb(String name, int version) throws Exception;
    Group getGroup(String name, int version) throws Exception;

    Slb getMaxVersionSlb(String name) throws Exception;
    Group getMaxVersionGroup(String name) throws Exception;

    List<Slb> getAllSlb(String name) throws Exception;
    List<Group> getAllGroup(String name) throws Exception;

    Archive getLatestGroupArchive(String appName) throws Exception;
    Archive getLatestSlbArchive(String slbName) throws Exception;
}
