package com.ctrip.zeus.service.update.impl;

import com.ctrip.zeus.commit.entity.Commit;
import com.ctrip.zeus.exceptions.NginxProcessingException;
import com.ctrip.zeus.model.entity.DyUpstreamOpsData;
import com.ctrip.zeus.nginx.entity.NginxResponse;
import com.ctrip.zeus.nginx.entity.VsConfData;
import com.ctrip.zeus.service.build.NginxConfService;
import com.ctrip.zeus.service.commit.CommitMergeService;
import com.ctrip.zeus.service.commit.CommitService;
import com.ctrip.zeus.service.commit.util.CommitType;
import com.ctrip.zeus.service.model.EntityFactory;
import com.ctrip.zeus.service.model.SelectionMode;
import com.ctrip.zeus.service.nginx.NginxService;
import com.ctrip.zeus.service.nginx.util.UpstreamConfPicker;
import com.ctrip.zeus.service.update.SlbServerConfManager;
import com.ctrip.zeus.service.version.ConfVersionService;
import com.ctrip.zeus.util.S;
import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by fanqq on 2016/3/15.
 */
@Service("slbServerConfManager")
public class SlbServerConfManagerImpl implements SlbServerConfManager {

    @Resource
    private CommitService commitService;
    @Resource
    private ConfVersionService confVersionService;
    @Resource
    private EntityFactory entityFactory;
    @Resource
    private NginxService nginxService;
    @Resource
    private NginxConfService nginxConfService;
    @Resource
    private CommitMergeService commitMergeService;
    @Resource
    private UpstreamConfPicker upstreamConfPicker;

    private final Logger logger = LoggerFactory.getLogger(SlbServerConfManagerImpl.class);
    private static DynamicIntProperty maxSpan = DynamicPropertyFactory.getInstance().getIntProperty("commit.max.span", 10);

   // private boolean needRefresh = false;

    @Override
    public synchronized NginxResponse update(boolean refresh, boolean needReload) throws Exception {
        NginxResponse response = new NginxResponse();
        //1. get slbId by local ip
        String ip = S.getIp();
        Long[] slbIds = entityFactory.getSlbIdsByIp(ip, SelectionMode.ONLINE_FIRST);
        if (slbIds == null || slbIds.length != 1) {
            logger.warn("[SlbServerConfManagerImpl] Not found slb id by ip. ip:" + ip);
            return response.setSucceed(false).setServerIp(ip).setErrMsg("[SlbServerConfManagerImpl] Not found slb id by ip. ip:" + ip);
        }
        Long slbId = slbIds[0];
        //2. get slbVersion and serverVersion
        Long slbVersion = confVersionService.getSlbCurrentVersion(slbId);
        Long serverVersion = confVersionService.getSlbServerCurrentVersion(slbId, ip);
        if (slbVersion.equals(serverVersion)) {
            return response.setSucceed(true).setServerIp(ip).setOutMsg("[SlbServerConfManagerImpl] slb version == slb server version. Not need to update.");
        }
        //3. if slbVersion < serverVersion , force rollback to slbVersion
        //case 1: force refresh or flag needRefresh is true
        //case 2: server version is large then slb version
        //case 3: version gap large than max span
        if (refresh || slbVersion < serverVersion || slbVersion - serverVersion > maxSpan.get()) {
            try {
                //3.1 need reload in case 3
                if (slbVersion - serverVersion > maxSpan.get()) {
                    needReload = true;
                }
                //3.2 get nginx config
                String nginxConf = nginxConfService.getNginxConf(slbId, slbVersion);
                Map<Long, VsConfData> vsConfDataMap = nginxConfService.getVsConfBySlbId(slbId, slbVersion);
                //3.3 refresh config
                NginxResponse res = nginxService.refresh(nginxConf, vsConfDataMap, needReload);
                //3.3.1 if failed, set need refresh flag and return result.
                if (!res.getSucceed()) {
                    logger.error("[SlbServerUpdate] Refresh conf failed. SlbId:" + slbId + ";Version:" + slbVersion + response.toString());
                    return res.setServerIp(ip);
                }
            } catch (Exception e) {
                //3.3.2 if throws exception, set need refresh flag.
                logger.error("[SlbServerUpdate] Refresh conf failed. SlbId:" + slbId + ";Version:" + slbVersion, e);
                throw new NginxProcessingException("Refresh conf failed. SlbId:" + slbId + ";Version:" + slbVersion, e);
            }
        } else {
            try {
                //4. execute commits
                //4.1 get commits and merge to one commit
                Commit commit = commitMergeService.mergeCommit(commitService.getCommitList(slbId, serverVersion, slbVersion));
                if (commit == null){
                    logger.info("[SlbServerUpdate] Not found commit. return success instead.");
                    return new NginxResponse().setSucceed(true).setServerIp(ip);
                }
                //4.2 get all configs
                String nginxConf = nginxConfService.getNginxConf(slbId, slbVersion);
                Map<Long, VsConfData> dataMap = nginxConfService.getVsConfByVsIds(slbId, commit.getVsIds(), slbVersion);
                //4.3 get clean vs ids
                Set<Long> cleanSet = new HashSet<>();
                cleanSet.addAll(commit.getCleanvsIds());
                //4.4 get flags
                boolean reload = commit.getType().equals(CommitType.COMMIT_TYPE_RELOAD);
                boolean test = commit.getType().equals(CommitType.COMMIT_TYPE_DYUPS);
                boolean dyups = commit.getType().equals(CommitType.COMMIT_TYPE_DYUPS);
                //4.4 get dyups data if needed
                DyUpstreamOpsData[] dyUpstreamOpsDatas = null;
                if (dyups) {
                    Set<Long> gids = new HashSet<>();
                    gids.addAll(commit.getGroupIds());
                    dyUpstreamOpsDatas = upstreamConfPicker.pickByGroupIds(dataMap, gids);
                }
                //4.5 update nginx configs
                List<NginxResponse> responses = nginxService.update(nginxConf, dataMap, cleanSet, dyUpstreamOpsDatas, reload, test, dyups);
                //4.6 check response, if failed. set need refresh flag.
                for (NginxResponse r : responses) {
                    if (!r.getSucceed()) {
                        logger.error("[SlbServerUpdate] Update conf failed. SlbId:" + slbId + ";Version:" + slbVersion + response.toString());
                        return r.setServerIp(ip);
                    }
                }
            } catch (Exception e) {
                //4.7  if failed. set need refresh flag.
                logger.error("[SlbServerUpdate] Execute commits failed. SlbId:" + slbId + ";Version:" + slbVersion, e);
                throw new NginxProcessingException("Execute commits failed. SlbId:" + slbId + ";Version:" + slbVersion, e);
            }
        }
        //5. update server version
        confVersionService.updateSlbServerCurrentVersion(slbId, ip, slbVersion);
        return response.setServerIp(ip).setSucceed(true).setOutMsg("update success.");
    }

    @Override
    public NginxResponse update() throws Exception {
        return update(true, true);
    }
}
