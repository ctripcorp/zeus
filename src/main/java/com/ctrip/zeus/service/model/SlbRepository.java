package com.ctrip.zeus.service.model;

import com.ctrip.zeus.model.entity.GroupSlb;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.service.Repository;

import java.util.List;

/**
 * @author:xingchaowang
 * @date: 3/4/2015.
 */
public interface SlbRepository extends Repository {

    List<Slb> list() throws Exception;

    Slb getById(Long slbId) throws Exception;

    Slb get(String slbName) throws Exception;

    /**
     * get the slb by its server ip
     * @param slbServerIp the server ip where slb is deployed
     * @return the slb entity
     * @throws Exception
     */
    Slb getBySlbServer(String slbServerIp) throws Exception;

    /**
     * get the slb list which manage the group server ip or/and group id
     * @param groupServerIp the group server ip
     * @param groupId the group name
     * @return the list of slbs
     * @throws Exception
     */
    List<Slb> listByGroupServerAndGroup(String groupServerIp, Long groupId) throws Exception;

    /**
     * get the slb list which manage the groups
     * @param groupIds the group names
     * @return the list of slbs
     * @throws Exception
     */
    List<Slb> listByGroups(Long[] groupIds) throws Exception;

    /**
     * get the list of group related slb information by group primary keys
     * @param groupIds the group primary keys
     * @return the list of group related slb information
     * @throws Exception
     */
    List<GroupSlb> listGroupSlbsByGroups(Long[] groupIds) throws Exception;

    /**
     * get thr list of group related slb information by slb name
     * @param slbId the slb name
     * @return the list of group related slb information
     * @throws Exception
     */
    List<GroupSlb> listGroupSlbsBySlb(Long slbId) throws Exception;

    Slb add(Slb slb) throws Exception;

    Slb update(Slb slb) throws Exception;

    /**
     * delete the slb by its primary id
     * @param slbId the slb primary id
     * @return the number of rows deleted
     * @throws Exception
     */
    int delete(Long slbId) throws Exception;

    /**
     * get the server list managed by the given slb
     * @param slbName the slb name
     * @return the list of server ips
     * @throws Exception
     */
    List<String> listGroupServersBySlb(String slbName) throws Exception;
}
