package com.ctrip.zeus.service.status;

import com.ctrip.zeus.model.model.Group;
import com.ctrip.zeus.model.status.GroupStatus;
import com.ctrip.zeus.service.model.ModelStatusMapping;

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
     *
     * @return status list
     * @throws Exception
     */
    List<GroupStatus> getAllOnlineGroupsStatus() throws Exception;

    /**
     * Find all online group status in the specific slb cluster
     *
     * @param slbId
     * @return status list
     * @throws Exception
     */
    List<GroupStatus> getOnlineGroupsStatusBySlbId(Long slbId) throws Exception;

    /**
     * Find online group status by groupId
     *
     * @param groups groupId
     * @return status list
     * @throws Exception
     */
    List<GroupStatus> getOnlineGroupsStatus(Map<Long, Group> groups) throws Exception;

    /**
     * Find all online group status by groupIds and slb id
     *
     * @param groupId groupId
     * @return status list
     * @throws Exception
     */
    GroupStatus getOnlineGroupStatus(Long groupId) throws Exception;

    /**
     * Find all online groups status
     *
     * @return status list
     * @throws Exception
     */
    List<GroupStatus> getAllOfflineGroupsStatus() throws Exception;

    /**
     * Find all online group status in the specific slb cluster
     *
     * @param slbId
     * @return status list
     * @throws Exception
     */
    List<GroupStatus> getOfflineGroupsStatusBySlbId(Long slbId) throws Exception;

    /**
     * Find online group status by groupId
     *
     * @param groupId groupId
     * @return status list
     * @throws Exception
     */
    GroupStatus getOfflineGroupStatus(Long groupId) throws Exception;

    /**
     * Find online group status by groupId
     *
     * @param groupIds groupIds
     * @return status list
     * @throws Exception
     */
    List<GroupStatus> getOfflineGroupsStatus(Set<Long> groupIds) throws Exception;

    /**
     * Find all online group status by groupIds and slb id
     *
     * @return status list
     * @throws Exception
     */
    List<GroupStatus> getOfflineGroupsStatus(ModelStatusMapping<Group> groupMap) throws Exception;
}
