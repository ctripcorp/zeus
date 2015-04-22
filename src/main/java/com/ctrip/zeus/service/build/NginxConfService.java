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
     * build upstream config by app name
     *
     */
    public List<DyUpstreamOpsData> buildUpstream(Slb slb , String appName)throws Exception;

    /**
     * build config by slb name and version number
     * @param slbName the slb name
     * @param version the ticket number , the version
     * @return void
     * @throws Exception
     */
    public void build(String slbName, int version) throws Exception;

    /**
     * get nginx main config by slb name and version number
     * @param slbName the slb name
     * @param version the version number
     * @return nginx main config string
     * @throws Exception
     */
    public String getNginxConf(String slbName, int version) throws Exception;

    /**
     * get nginx server config by slb name and version number
     * @param slbName the slb name
     * @param version the version number
     * @return nginx server config data list
     * @throws Exception
     */
    public List<NginxConfServerData> getNginxConfServer(String slbName, int version) throws Exception;

    /**
     * get nginx upstream config by slb name and version number
     * @param slbName the slb name
     * @param version the version number
     * @return nginx upstream config data list
     * @throws Exception
     */
    public List<NginxConfUpstreamData> getNginxConfUpstream(String slbName, int version) throws Exception;

    /**
     * get current version by slb name
     * @param slbname the slb name
     * @return current version
     * @throws Exception
     */
    public int getCurrentVersion(String slbname) throws Exception;
}
