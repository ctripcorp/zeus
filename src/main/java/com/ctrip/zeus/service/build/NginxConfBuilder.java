package com.ctrip.zeus.service.build;

import com.ctrip.zeus.model.entity.App;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.service.Repository;

import java.util.List;
import java.util.Set;

/**
 * Created by fanqq on 2015/3/30.
 */
public interface NginxConfBuilder extends Repository {

    /**
     * generate nginx config
     * @param slb the slb entity
     * @return nginx  config
     * @throws Exception
     */
    String generateNginxConf(Slb slb);

    /**
     * get nginx server config
     * @param slb the slb entity
     * @param vs  virtualServer
     * @param apps apps entity
     * @return nginx upstream config data
     * @throws Exception
     */
    String generateServerConf(Slb slb, VirtualServer vs, List<App> apps)throws Exception;

    /**
     * get nginx upstream config
     * @param slb the slb entity
     * @param vs the virtualServer
     * @param vs  app entities
     * @param allDownAppServers  allDownAppServers
     * @param allDownAppServers  allDownAppServers
     * @return nginx upstream config data
     * @throws Exception
     */
    String generateUpstreamsConf(Slb slb, VirtualServer vs, List<App> apps, Set<String> allDownServers, Set<String> allDownAppServers);

}
