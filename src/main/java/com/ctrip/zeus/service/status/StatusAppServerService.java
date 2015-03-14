package com.ctrip.zeus.service.status;

import com.ctrip.zeus.dal.core.StatusAppServerDo;
import org.unidal.dal.jdbc.DalException;

import java.util.List;

/**
 * @author:xingchaowang
 * @date: 3/14/2015.
 */
public interface StatusAppServerService {

    List<StatusAppServerDo> list() throws DalException;

    List<StatusAppServerDo> listByAppName(String appName) throws DalException;

    List<StatusAppServerDo> listByServer(String ip) throws DalException;

    void updateStatusAppServer(StatusAppServerDo d) throws DalException;
}
