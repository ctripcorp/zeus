package com.ctrip.zeus.service.model;

import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.Archive;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.VirtualServer;

import java.util.List;

/**
 * @author:xingchaowang
 * @date: 3/14/2015.
 */
public interface ArchiveService {

    Slb getSlb(Long slbId, int version) throws Exception;

    Group getGroup(Long groupId, int version) throws Exception;

    VirtualServer getVirtualServer(Long vsId, int version) throws Exception;

    Slb getLatestSlb(Long slbId) throws Exception;

    Group getGroupByMode(Long groupId, ModelMode mode) throws Exception;

    VirtualServer getVirtualServerByMode(Long vsId, ModelMode mode) throws Exception;

    Slb getSlbByMode(Long slbId, ModelMode mode) throws Exception;

    List<Slb> getLatestSlbs(Long[] slbIds) throws Exception;

    List<Group> getGroupsByMode(Long[] groupIds, ModelMode mode) throws Exception;

    List<Group> listGroups(IdVersion[] keys) throws Exception;

    List<VirtualServer> getVirtualServersByMode(Long[] vsIds, ModelMode mode) throws Exception;

    List<VirtualServer> listVirtualServers(IdVersion[] keys) throws Exception;

    List<Slb> getSlbsByMode(Long[] slbIds, ModelMode mode) throws Exception;

    List<Slb> listSlbs(IdVersion[] keys) throws Exception;

    Archive getLatestSlbArchive(Long slbId) throws Exception;

    List<Archive> getLastestVsArchives(Long[] vsIds) throws Exception;

    Archive getLatestVsArchive(Long vsId) throws Exception;

    List<Group> getGroupsByIdAndVersion(Long[] groupIds, Integer[] versions) throws Exception;

    List<VirtualServer> getVirtualServersByIdAndVersion(Long[] vsIds, Integer[] versions) throws Exception;

    String upgradeArchives(Long[] slbIds, Long[] groupIds, Long[] vsIds) throws Exception;
}
