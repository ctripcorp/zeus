package com.ctrip.zeus.service.model.handler;

import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.TrafficPolicy;
import com.ctrip.zeus.service.model.common.LocationEntry;
import org.unidal.dal.jdbc.DalException;

import java.util.List;
import java.util.Map;

/**
 * Created by zhoumy on 2017/2/4.
 */
public interface VsEntryFactory {

    List<LocationEntry> getPolicyEntriesByGroup(Long groupId) throws Exception;

    Map<Long, List<LocationEntry>> compareAndBuildLocationEntries(Long[] vsId, Long escapedGroup) throws ValidationException;

    Map<Long, List<LocationEntry>> compareAndBuildLocationEntries(Long[] vsId, TrafficPolicy newGroupEntry);
}
