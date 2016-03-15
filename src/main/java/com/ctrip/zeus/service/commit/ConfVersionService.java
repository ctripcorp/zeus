package com.ctrip.zeus.service.commit;

/**
 * Created by lu.wang on 2016/3/15.
 */
public interface ConfVersionService {
    Long getSlbServerCurrentVersion(Long sid, String ip);
    void updateSlbServerCurrentVersion(Long slbId , String ip , Long version);

    Long getSlbCurrentVersion(Long slbId);
    Long updateSlbCurrentVersion(Long slbId , Long version);
    Long getSlbPreviousVersion(Long slbId);

}
