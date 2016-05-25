package com.ctrip.zeus.service.build;

import com.ctrip.zeus.nginx.entity.NginxConfEntry;
import com.ctrip.zeus.service.Repository;

import java.util.List;

/**
 * @author:xingchaowang
 * @date: 3/15/2015.
 */
public interface NginxConfService extends Repository {

    /**
     * get nginx main config by slb name and version number
     *
     * @param slbId   the slb name
     * @param version the version number
     * @return nginx main config string
     * @throws Exception
     */
    public String getNginxConf(Long slbId, Long version) throws Exception;

    /**
     * Get nginx conf under dir upstreams/ and vhosts/
     * @param slbId
     * @param version
     * @return nginx conf entry which contains upstream and vhost conf files
     * @throws Exception
     */
    public NginxConfEntry getUpstreamsAndVhosts(Long slbId, Long version) throws Exception;

      /**
     * get nginx upstream&server config by slb id , vsIds and version number
     *
     * @param slbId
     * @param version selected slb conf version
     * @param vsIds selected vs ids
     * @return nginx conf entry which contains upstream and vhost conf files of selected vs ids
     * @throws Exception
     */
    public NginxConfEntry getUpstreamsAndVhosts(Long slbId, Long version, List<Long> vsIds) throws Exception;

    /**
     * get current building version by slb name
     *
     * @param slbId the slb slbId
     * @return current version
     * @throws Exception
     */
    public int getCurrentVersion(Long slbId) throws Exception;

}
