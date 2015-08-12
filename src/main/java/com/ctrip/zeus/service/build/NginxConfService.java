package com.ctrip.zeus.service.build;

import com.ctrip.zeus.model.entity.DyUpstreamOpsData;
import com.ctrip.zeus.model.entity.NginxConfServerData;
import com.ctrip.zeus.model.entity.NginxConfUpstreamData;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.service.Repository;

import java.util.List;

/**
 * @author:xingchaowang
 * @date: 3/15/2015.
 */
public interface NginxConfService extends Repository {
    /**
     * build config by slb name and version number
     * @param slbId the slb id
     * @param version the ticket number , the version
     * @return void
     * @throws Exception
     */
    public void build(Long slbId, int version) throws Exception;

    /**
     * get nginx main config by slb name and version number
     * @param slbId the slb name
     * @param version the version number
     * @return nginx main config string
     * @throws Exception
     */
    public String getNginxConf(Long slbId, int version) throws Exception;

    /**
     * get nginx server config by slb name and version number
     * @param slbId the slb name
     * @param version the version number
     * @return nginx server config data list
     * @throws Exception
     */
    public List<NginxConfServerData> getNginxConfServer(Long slbId, int version) throws Exception;

    /**
     * get nginx upstream config by slb name and version number
     * @param slbId the slb name
     * @param version the version number
     * @return nginx upstream config data list
     * @throws Exception
     */
    public List<NginxConfUpstreamData> getNginxConfUpstream(Long slbId, int version) throws Exception;

    /**
     * get current building version by slb name
     * @param slbId the slb slbId
     * @return current version
     * @throws Exception
     */
    public int getCurrentBuildingVersion(Long slbId) throws Exception;

    /**
     * get current building version by slb name
     * @param slbId the slb slbId
     * @return current version
     * @throws Exception
     */
    public int getCurrentVersion(Long slbId) throws Exception;

}
