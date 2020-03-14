package com.ctrip.zeus.service.version;

import com.ctrip.zeus.model.commit.ConfSlbVersion;

/**
 * Created by lu.wang on 2016/3/15.
 */
public interface ConfVersionService {

    /**
     * get slbServer's current version by slb-id and ip
     */
    Long getSlbServerCurrentVersion(Long sid, String ip) throws Exception;

    /**
     * update slbServer's current version by slb-id and ip
     */
    void updateSlbServerCurrentVersion(Long slbId , String ip , Long version) throws Exception;

    /**
     * get slb's current version
     */
    Long getSlbCurrentVersion(Long slbId) throws Exception;

    /**
     * get slb's previous version
     */
    Long getSlbPreviousVersion(Long slbId) throws Exception;

    /**
     * update slb's current version
     */
    void updateSlbCurrentVersion(Long slbId , Long version) throws Exception;

    /**
     * insert a record of confSlbVersion
     */
    void addConfSlbVersion(ConfSlbVersion confSlbVersion) throws Exception;

}
