package com.ctrip.zeus.service.compare;

import com.ctrip.zeus.model.model.Group;
import com.ctrip.zeus.model.model.Slb;
import com.ctrip.zeus.model.model.VirtualServer;
import com.ctrip.zeus.service.message.queue.entity.ChangeDataEntity;

import java.util.List;

/**
 * Created by fanqq on 2017/2/14.
 */
public interface ModelComparer {
    List<ChangeDataEntity> compareGroup(Group from, Group to);

    List<ChangeDataEntity> compareVs(VirtualServer from, VirtualServer to);

    List<ChangeDataEntity> compareSlb(Slb from, Slb to);
}
