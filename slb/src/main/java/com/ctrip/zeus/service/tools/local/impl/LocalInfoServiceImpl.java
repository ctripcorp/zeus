package com.ctrip.zeus.service.tools.local.impl;

import com.ctrip.zeus.client.AgentApiClient;
import com.ctrip.zeus.dao.entity.SlbSlbServerR;
import com.ctrip.zeus.dao.entity.SlbSlbServerRExample;
import com.ctrip.zeus.dao.mapper.SlbSlbServerRMapper;
import com.ctrip.zeus.server.LocalInfoPack;
import com.ctrip.zeus.service.model.IdVersion;
import com.ctrip.zeus.service.tools.local.LocalInfoService;
import com.ctrip.zeus.util.EnvHelper;
import com.netflix.config.DynamicBooleanProperty;
import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by fanqq on 2017/4/7.
 */
@Service("localInfoService")
public class LocalInfoServiceImpl implements LocalInfoService {

    @Resource
    private SlbSlbServerRMapper slbSlbServerRMapper;

    private Long slbId = null;
    private String slbName = null;
    private long lastChangeTime = 0L;
    private static DynamicIntProperty updateInterval = DynamicPropertyFactory.getInstance().getIntProperty("local.info.service.interval", 60000);
    private String env = null;
    private static Long staticSlbId = null;
    private static long staticLastFetchTime = 0L;

    private final DynamicBooleanProperty alwaysFromDb = DynamicPropertyFactory.getInstance().getBooleanProperty("local.info.service.get.slb.id.from.db", false);

    @Override
    public String getLocalIp() {
        return LocalInfoPack.INSTANCE.getIp();
    }

    @Override
    public Long getLocalSlbId() throws Exception {
        long now = System.currentTimeMillis();
        // Query for slb id only when cache expired, no matter whether cached slbId is null or not.
        if (now - lastChangeTime >= updateInterval.get()) {
            if (EnvHelper.portal() || alwaysFromDb.get()) {
                // get slb id from db
                Set<IdVersion> idVersions = getLocalSlbId(LocalInfoPack.INSTANCE.getIp());
                if (idVersions != null && idVersions.size() > 0) {
                    slbId = idVersions.toArray(new IdVersion[0])[0].getId();
                }else{
                    slbId=null;
                }
            } else {
                // get slb info from api cluster
                slbId = getSlbId();
            }
            lastChangeTime = now;
        }
        return slbId;
    }

    @Override
    public Long getLocalSlbIdWithRetry() throws Exception {
        long now = System.currentTimeMillis();
        // fetch when slbId cache is null or cache is expired
        if (slbId == null || now - lastChangeTime >= updateInterval.get()) {
            if (EnvHelper.portal() || alwaysFromDb.get()) {
                // get slb id from db
                Set<IdVersion> idVersions = getLocalSlbId(LocalInfoPack.INSTANCE.getIp());
                if (idVersions != null && idVersions.size() > 0) {
                    slbId = idVersions.toArray(new IdVersion[0])[0].getId();
                } else {
                    slbId = null;
                }
            } else {
                // get slb info from api cluster
                slbId = getSlbId();
            }
            lastChangeTime = now;
        }
        return slbId;
    }

    public static Long getLocalSlbIdStatic() throws Exception {
        long now = System.currentTimeMillis();
        if (staticSlbId == null || now - staticLastFetchTime >= updateInterval.get()) {
            staticSlbId = getSlbId();
            staticLastFetchTime = now;
        }
        return staticSlbId;
    }

    @Override
    public String getEnv() {
        if (env == null) {
            env = System.getProperty("archaius.deployment.environment");
        }
        return env;
    }

    public static Long getSlbId() throws Exception {
        Long slbId = AgentApiClient.getClient().getSlbId();
        if (slbId == null) {
            return null;
        } else {
            return slbId;
        }
    }


    private Set<IdVersion> getLocalSlbId(String ip) {
        Set<IdVersion> result = new HashSet<>();
        for (SlbSlbServerR server : slbSlbServerRMapper.selectByExample(new SlbSlbServerRExample().
                createCriteria().
                andIpEqualTo(ip).
                example())) {
            result.add(new IdVersion(server.getSlbId(), server.getSlbVersion()));
        }

        return result;
    }
}
