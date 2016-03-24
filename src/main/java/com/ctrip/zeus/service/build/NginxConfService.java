package com.ctrip.zeus.service.build;

import com.ctrip.zeus.model.entity.DyUpstreamOpsData;
import com.ctrip.zeus.model.entity.NginxConfServerData;
import com.ctrip.zeus.model.entity.NginxConfUpstreamData;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.nginx.entity.VsConfData;
import com.ctrip.zeus.service.Repository;

import java.util.List;
import java.util.Map;

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
     * get nginx server config by slb name and version number
     *
     * @param slbId   the slb name
     * @param version the version number
     * @return nginx server config data list
     * @throws Exception
     */
    public List<NginxConfServerData> getNginxConfServer(Long slbId, Long version) throws Exception;

    /**
     * get nginx upstream config by slb name and version number
     *
     * @param slbId   the slb name
     * @param version the version number
     * @return nginx upstream config data list
     * @throws Exception
     */
    public List<NginxConfUpstreamData> getNginxConfUpstream(Long slbId, Long version) throws Exception;

    /**
     * get nginx upstream&server config by slb id and version number
     *
     * @param slbId   the slb name
     * @param version the version number
     * @return nginx upstream config data list
     * @throws Exception
     */
    public Map<Long,VsConfData> getVsConfBySlbId(Long slbId, Long version) throws Exception;

      /**
     * get nginx upstream&server config by slb id , vsIds and version number
     *
     * @param slbId   the slb name
     * @param version the version number
     * @param vsIds the vsIds
     * @return nginx upstream config data list
     * @throws Exception
     */
    public Map<Long,VsConfData> getVsConfByVsIds(Long slbId, List<Long> vsIds, Long version) throws Exception;

    /**
     * get current building version by slb name
     *
     * @param slbId the slb slbId
     * @return current version
     * @throws Exception
     */
    public int getCurrentBuildingVersion(Long slbId) throws Exception;

    /**
     * get current building version by slb name
     *
     * @param slbId the slb slbId
     * @return current version
     * @throws Exception
     */
    public int getCurrentVersion(Long slbId) throws Exception;

}
