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

    int deleteSlbArchive(long slbId) throws Exception;
    int deleteGroupArchive(long groupId) throws Exception;

    Slb getSlb(long slbId, int version) throws Exception;
    Group getGroup(long groupId, int version) throws Exception;

    Slb getMaxVersionSlb(long slbId) throws Exception;
    Group getMaxVersionGroup(long groupId) throws Exception;

    List<Slb> getAllSlb(long slbId) throws Exception;
    List<Group> getAllGroup(long groupId) throws Exception;

    Archive getLatestSlbArchive(long slbId) throws Exception;
    Archive getLatestGroupArchive(long groupId) throws Exception;
}
