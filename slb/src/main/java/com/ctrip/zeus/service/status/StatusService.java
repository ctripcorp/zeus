package com.ctrip.zeus.service.status;


import com.ctrip.zeus.model.status.UpdateStatusItem;
import com.ctrip.zeus.service.Repository;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author:xingchaowang
 * @date: 3/16/2015.
 */
public interface StatusService extends Repository {

    /**
     * get all down group servers
     * @return group server ip list
     * @throws Exception
     */
    Set<String> findAllDownServers() throws Exception;

    /**
     * get group servers by slbId and status offset
     * @param groupIds groupIds
     * @return map of {vsId}_{groupId}_{ip} offsetList
     * @throws Exception
     */
    Map<String, List<Boolean>> fetchGroupServerStatus(Long[] groupIds) throws Exception;

    /**
     * up server by group server ip
     * @param ip the group server ip
     * @throws Exception
     */
    void upServer(String ip) throws Exception;

    /**
     * down server by group server ip
     * @param ip the group server ip
     * @throws Exception
     */
    void downServer(String ip) throws Exception;

    /**
     * update status by group server ip and slbId and group id
     * @param ips the group server ips
     * @param groupId  group id
     * @param offset  offset [0-30]
     * @param status  status enable = true , disable = false
     * @throws Exception
     */
    void updateStatus(Long groupId, List<String> ips, int offset, boolean status) throws Exception;

    /**
     * update status by group server ip and slbId and group id
     * @param item the update item
     * @throws Exception
     */
    void updateStatus(List<UpdateStatusItem> item) throws Exception;

    /**
     * get server status by server ip
     * @param ip server ip
     * @return true : status=up false : status = down
     * @throws Exception
     */
    boolean getServerStatus(String ip) throws Exception;

    /**
     * Init group server status for new/update group
     * only used in group resource api
     * @param groupId Group Id
     * @param vsIds vsIds
     * @param ips server ips
     * @throws Exception
     */
    void groupServerStatusInit(Long groupId, Long[] vsIds, String[] ips) throws Exception;

    /**
     * clean group Server Status
     * only used in group delete api
     * @param groupId Group Id
     * @throws Exception
     */
    void cleanGroupServerStatus(Long groupId) throws Exception;

    /**
     * clean group Server Status
     * only used in group delete api
     * @param groupId Group Id
     * @throws Exception
     */
    void cleanGroupServerStatus(Long groupId, String[] ip) throws Exception;

    /**
     * clean disabled group Server Status
     * only used in group delete api
     * @param groupId Group Id
     * @throws Exception
     */
    void cleanDisabledGroupServerStatus(Long groupId) throws Exception;
}
