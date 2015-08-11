package com.ctrip.zeus.service.activate;

import com.ctrip.zeus.service.Repository;

import java.util.List;
import java.util.Set;

/**
 * Created by fanqq on 2015/3/30.
 */
public interface ActiveConfService extends Repository {

    /**
     * get config content of App Active by app name
     * @param groupIds the groupIds
     * @return content list
     * @throws Exception
     */
    public List<String> getConfGroupActiveContentByGroupIds(Long []groupIds)throws Exception;
    /**
     * get config content of Slb Active by slb name
     * @param slbId the slb name
     * @return content string
     * @throws Exception
     */
    public String getConfSlbActiveContentBySlbId(Long slbId)throws Exception;

    public Set<Long> getSlbIdsByGroupId(Long groupId)throws Exception;
    public Set<Long> getGroupIdsBySlbId(Long slbId) throws Exception;
}
