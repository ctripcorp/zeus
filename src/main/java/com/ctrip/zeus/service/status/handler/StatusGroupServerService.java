package com.ctrip.zeus.service.status.handler;

import com.ctrip.zeus.dal.core.StatusGroupServerDo;

import java.util.List;

/**
 * @author:xingchaowang
 * @date: 3/14/2015.
 */
public interface StatusGroupServerService {

    List<StatusGroupServerDo> list() throws Exception;

    List<StatusGroupServerDo> listAllDownBySlbId(Long slbId) throws Exception;

    List<StatusGroupServerDo> listByGroupId(Long groupId) throws Exception;

    List<StatusGroupServerDo> listByServer(String ip) throws Exception;
    List<StatusGroupServerDo> listAllUpBySlbId(Long slbId) throws Exception;

    List<StatusGroupServerDo> listBySlbIdAndGroupIdAndIp( Long slbId,Long groupId,String ip) throws Exception;
    List<StatusGroupServerDo> listBySlbIdAndGroupId(Long slbId,Long groupId) throws Exception;
    void deleteBySlbIdAndGroupIdAndVsId(Long slbId,Long groupId,Long vsId) throws Exception;

    void updateStatusGroupServer(StatusGroupServerDo d) throws Exception;
    void deleteByGroupIdAndSlbIdAndIp(Long slbId, Long groupId,String ip)throws Exception;
}
