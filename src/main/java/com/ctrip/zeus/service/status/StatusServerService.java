package com.ctrip.zeus.service.status;

import com.ctrip.zeus.dal.core.StatusServerDo;
import org.unidal.dal.jdbc.DalException;

import java.util.List;

/**
 * @author:xingchaowang
 * @date: 3/14/2015.
 */
public interface StatusServerService {

    List<StatusServerDo> list() throws DalException;

    List<StatusServerDo> listAllDown() throws DalException;

    List<StatusServerDo> listByServer(String ip) throws DalException;

    void updateStatusServer(StatusServerDo statusServerDo) throws DalException;
}
