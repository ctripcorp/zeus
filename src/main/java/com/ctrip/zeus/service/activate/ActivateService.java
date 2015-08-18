package com.ctrip.zeus.service.activate;

import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.service.Repository;
import com.ctrip.zeus.task.entity.OpsTask;

import java.util.HashMap;
import java.util.List;

/**
 * @author:xingchaowang
 * @date: 3/15/2015.
 */
public interface ActivateService extends Repository {

    /**
     * update active data by slbname
     * @param slbId the slb id
     * @return
     * @throws Exception
     */
    public void activeSlb(long slbId,int version) throws Exception;
    /**
     * update active data by slbname
     * @param groupId the Group id
     * @return
     * @throws Exception
     */
    public void activeGroup(long groupId ,int version , Long slbId) throws Exception;


    /**
     * deactivate data by groupid
     * @param groupId the Group id
     * @return
     * @throws Exception
     */
    public void deactiveGroup(Long groupId, Long slbId) throws Exception;

    /**
     * group is activated
     * @param groupId the Group id
     * @return
     * @throws Exception
     */
    public boolean isGroupActivated(Long groupId,Long slbId) throws  Exception;
    /**
     * group is activated
     * @param groupIds the Group id
     * @return
     * @throws Exception
     */
    public HashMap<Long,Boolean> isGroupsActivated(Long[] groupIds,Long slbId) throws Exception;

    /**
     * get activating groups
     * @param groupId , the Group id
     * @param version , Version
     * @return groups
     */

    public Group getActivatingGroup(Long groupId, int version);
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
     * @return groups
     */

    public Group getActivatedGroup(Long groupId,Long slbId)throws Exception;
    /**
     * get activated groups
     * @param groupId , the Group id
     * @return groups
     */

    public List<Group> getActivatedGroups(Long[] groupId,Long slbId)throws Exception;

    /**
     * get activated slb
     * @param slbId , the Slb id
     * @return groups
     */

    public Slb getActivatedSlb(Long slbId)throws Exception;

}
