package com.ctrip.zeus.service.model;

import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.TrafficPolicy;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.restful.message.view.ExtendedView;

import java.util.List;

/**
 * Created by zhoumy on 2016/5/17.
 */
public interface ArchiveRepository {

    void archiveGroup(Group group) throws Exception;

    void archiveGroup(ExtendedView.ExtendedGroup group) throws Exception;

    void archiveSlb(Slb slb) throws Exception;

    void archiveVs(VirtualServer vs) throws Exception;

    void archivePolicy(TrafficPolicy trafficPolicy) throws Exception;

    String getGroupArchiveRaw(Long id, int version) throws Exception;

    Group getGroupArchive(Long id, int version) throws Exception;

    Group getGroupArchive(String name, int version) throws Exception;

    Slb getSlbArchive(Long id, int version) throws Exception;

    VirtualServer getVsArchive(Long id, int version) throws Exception;

    TrafficPolicy getPolicyArchive(Long id, String name) throws Exception;

    List<Archive<Group>> getAllGroupArchives(Long id) throws Exception;

    List<Archive<Slb>> getAllSlbArchives(Long id) throws Exception;

    List<Archive<VirtualServer>> getAllVsArchives(Long id) throws Exception;
}
