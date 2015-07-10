package com.ctrip.zeus.service.status;

import com.ctrip.zeus.model.entity.GroupServerStatus;
import com.ctrip.zeus.model.entity.GroupStatus;
import com.ctrip.zeus.model.entity.GroupStatusList;

import java.util.List;

/**
 * User: mag
 * Date: 4/1/2015
 * Time: 10:54 AM
 */
public interface GroupStatusService {
     /**
      * Find all app status
      * @return
      * @throws Exception
      */
     List<GroupStatus> getAllGroupStatus() throws Exception;

     /**
      * Find all app status in the specific slb cluster
      * @param slbId
      * @return
      * @throws Exception
      */
     List<GroupStatus> getAllGroupStatus(Long slbId) throws Exception;

     List<GroupStatus> getGroupStatus(Long groupId) throws Exception;

     GroupStatusList getGroupStatus(List<Long> groupId,Long slbId) throws Exception;

     GroupStatus getGroupStatus(Long groupId,Long slbId) throws Exception;

     GroupStatusList getLocalGroupStatus(List<Long> groupId , Long slbId) throws Exception;

     GroupServerStatus getGroupServerStatus(Long groupId, Long slbId, String ip, Integer port) throws Exception;
}
