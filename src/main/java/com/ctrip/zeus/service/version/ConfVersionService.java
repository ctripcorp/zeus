package com.ctrip.zeus.service.version;

import com.ctrip.zeus.dal.core.ConfSlbVersionDo;
import org.unidal.dal.jdbc.DalException;

/**
 * Created by lu.wang on 2016/3/15.
 */
public interface ConfVersionService {

    /**
     * get slbServer's current version by slb-id and ip
     */
    Long getSlbServerCurrentVersion(Long sid, String ip) throws DalException;

    /**
     * update slbServer's current version by slb-id and ip
     */
    void updateSlbServerCurrentVersion(Long slbId , String ip , Long version) throws DalException;

    /**
     * get slb's current version
     */
    Long getSlbCurrentVersion(Long slbId) throws DalException;

    /**
     * get slb's previous version
     */
    Long getSlbPreviousVersion(Long slbId) throws DalException;

    /**
     * update slb's current version
     */
    void updateSlbCurrentVersion(Long slbId , Long version) throws DalException;

    /**
     * insert a record of confSlbVersion
     */
    void addConfSlbVersion(ConfSlbVersionDo confSlbVersionDo) throws DalException;

}
