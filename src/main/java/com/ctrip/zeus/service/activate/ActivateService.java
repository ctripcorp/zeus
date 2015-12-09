package com.ctrip.zeus.service.activate;

import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.service.Repository;
import java.util.List;
import java.util.Map;

/**
 * @author:xingchaowang
 * @date: 3/15/2015.
 */
public interface ActivateService extends Repository {

    /**
     * active Slb
     * @param slbId the slb id
     * @return
     * @throws Exception
     */
    public void activeSlb(long slbId,int version) throws Exception;
    /**
     * activate group
     * @param groupId the Group id
     * @param version the version
     * @param vsId the vsId
     * @param slbId the slbId
     * @throws Exception
     */
    public void activeGroup(long groupId ,int version , Long vsId , Long slbId) throws Exception;
    /**
     * activate group
     * @param groupId the Group id
     * @param group the group obj
     * @param vsId the vsId
     * @param slbId the slbId
     * @throws Exception
     */
    public void activeGroup(long groupId ,Group group ,int version, Long vsId , Long slbId) throws Exception;
    /**
     * activate group
     * @param version the version
     * @param vsId the vsId
     * @param slbId the slbId
     * @throws Exception
     */
    public void activeVirtualServer(long vsId ,VirtualServer vs , int version , Long slbId) throws Exception;


    /**
     * deactivate data by groupid
     * @param groupId the Group id
     * @param slbId the slbId
     * @throws Exception
     */
    public void deactiveGroup(Long groupId, Long slbId) throws Exception;

    /**
     * deactivate data by groupid
     * @param vsId the vsId
     * @param slbId the slbId
     * @throws Exception
     */
    public void deactiveVirtualServer(Long vsId , Long slbId) throws Exception;

    /**
     * group is activated
     * @param groupId the Group id
     * @param vsId the Group vsId
     * @return
     * @throws Exception
     */
    public boolean isGroupActivated(Long groupId,Long vsId) throws  Exception;
    /**
     * group is activated
     * @param groupIds the Group id
     * @param vsId the Group vsId
     * @throws Exception
     */
    public Map<Long,Boolean> isGroupsActivated(Long[] groupIds,Long vsId) throws Exception;

    /**
     * group is activated
     * @param vsId the Group vsId
     * @return
     * @throws Exception
     */
    public boolean isVSActivated(Long vsId) throws  Exception;
    /**
     * group is activated
     * @param vsId the  vsId
     * @param slbId the  slbId
     * @return
     * @throws Exception
     */
    public boolean isVsActivated(Long vsId,Long slbId) throws  Exception;

    /**
     * group is activated
     * @param vsId the  vsId
     * @return
     * @throws Exception
     */
    public boolean hasActivatedGroupWithVsId(Long vsId) throws  Exception;

    /**
     * get activating groups
     * @param groupId , the Group id
     * @param version , Version
     * @return groups
     */

    public Group getActivatingGroup(Long groupId, int version);
    /**
     * get activating groups
     * @param groupIds , the Group id
     * @param versions , Version
     * @return groups
     */

    public List<Group> getActivatingGroups(Long[] groupIds, Integer[] versions);
    /**
     * get activating slb
     * @param vsId the slb ID
     * @param version the version
     * @return groups
     */

    public VirtualServer getActivatingVirtualServer(Long vsId,int version);
    /**
     * get activating slb
     * @param vsIds the slb ID
     * @param versions the version
     * @return vses
     */

    public List<VirtualServer> getActivatingVirtualServers(Long[] vsIds,Integer[] versions);

    /**
     * get activating slb
     * @param slbId the slb ID
     * @param version the version
     * @return groups
     */

    public Slb getActivatingSlb(Long slbId,int version);

    /**
     * get activated groups
     * @param groupId , the Group id
     * @param vsId , the vsId
     * @return groups
     */

    public Group getActivatedGroup(Long groupId,Long vsId)throws Exception;
    /**
     * get activated groups
     * @param groupIds , the Group ids
     * @param slbId , the slbId
     * @return groups
     */

    public List<Group> getActivatedGroups(Long[] groupIds,Long slbId)throws Exception;
    /**
     * get activated groups by vses
     * @param vsIds , the vsIds
     * @return groups
     */

    public Map<Long,List<Group>> getActivatedGroupsByVses(Long[] vsIds)throws Exception;
    /**
     * get activated vs
     * @param vsId , the vsId
     * @return groups
     */

    public List<VirtualServer> getActivatedVirtualServer(Long vsId)throws Exception;
    /**
     * get activated vs by slbId
     * @param slbId , the vsId
     * @return groups
     */

    public Map<Long,VirtualServer> getActivatedVirtualServerBySlb(Long slbId)throws Exception;


    /**
     * get activated slb
     * @param slbId , the Slb id
     * @return groups
     */

    public Slb getActivatedSlb(Long slbId)throws Exception;

}
