package com.ctrip.zeus.service.status;

import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.service.model.IdVersion;
import com.ctrip.zeus.status.entity.GroupServerStatus;
import com.ctrip.zeus.status.entity.GroupStatus;
import com.ctrip.zeus.status.entity.GroupStatusList;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * User: mag
 * Date: 4/1/2015
 * Time: 10:54 AM
 */
public interface GroupStatusService {
     /**
      * Find all online groups status
      * @return status list
      * @throws Exception
      */
     List<GroupStatus> getAllOnlineGroupsStatus() throws Exception;

     /**
      * Find all online group status in the specific slb cluster
      * @param slbId
      * @return status list
      * @throws Exception
      */
     List<GroupStatus> getOnlineGroupsStatusBySlbId(IdVersion slbId) throws Exception;

    /**
     * Find online group status by groupId
     * @param groups groupId
     * @param vsIds vsids
     * @param slbId slbId
     * @return status list
     * @throws Exception
     */
    List<GroupStatus> getOnlineGroupsStatus(Map<Long,Group> groups ,Set<Long> vsIds ,  IdVersion slbId) throws Exception

    /**
     * Find all online group status by groupIds and slb id
     * @param groupIds groupIds
     * @param slbId slbId
     * @return status list
     * @throws Exception
     */
     List<GroupStatus> getOnlineGroupsStatus(Set<Long> groupIds,Long slbId) throws Exception;

    /**
     * Find all online groups status
     * @return status list
     * @throws Exception
     */
    List<GroupStatus> getAllOfflineGroupsStatus() throws Exception;

    /**
     * Find all online group status in the specific slb cluster
     * @param slbId
     * @return status list
     * @throws Exception
     */
    List<GroupStatus> getOfflineGroupsStatusBySlbId(Long slbId) throws Exception;

    /**
     * Find online group status by groupId
     * @param groupId groupId
     * @return status list
     * @throws Exception
     */
    List<GroupStatus> getOfflineGroupStatus(Long groupId) throws Exception;

    /**
     * Find all online group status by groupIds and slb id
     * @param groupIds groupIds
     * @param slbId slbId
     * @return status list
     * @throws Exception
     */
    List<GroupStatus> getOfflineGroupsStatus(Set<Long> groupIds,Long slbId) throws Exception;
}
