package com.ctrip.zeus.service.update.impl;

import com.ctrip.zeus.commit.entity.Commit;
import com.ctrip.zeus.exceptions.NginxProcessingException;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.DyUpstreamOpsData;
import com.ctrip.zeus.nginx.entity.ConfFile;
import com.ctrip.zeus.nginx.entity.NginxConfEntry;
import com.ctrip.zeus.nginx.entity.NginxResponse;
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
import java.util.*;

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
        logger.info("SlbVersion: " + slbVersion + " ServerVersion: " + serverVersion);
        if (slbVersion.equals(serverVersion)) {
            return response.setSucceed(true).setServerIp(ip).setOutMsg("[SlbServerConfManagerImpl] slb version == slb server version. Not need to update.");
        }
        //3. force rollback to slbVersion
        //case 1: force refresh or flag needRefresh is true
        //case 2: server version is large then slb version
        //case 3: version gap larger than max span
        if (refresh || slbVersion < serverVersion || slbVersion - serverVersion > maxSpan.get()) {
            try {
                //3.1 need reload in case 3
                if (slbVersion - serverVersion > maxSpan.get()) {
                    needReload = true;
                }
                //3.2 get nginx config
                String nginxConf = null;
                try {
                    nginxConf = nginxConfService.getNginxConf(slbId, slbVersion);
                } catch (ValidationException ex) {
                }
                NginxConfEntry entry = nginxConfService.getUpstreamsAndVhosts(slbId, slbVersion);
                if (nginxConf == null || entry == null) {
                    String err = "Nginx conf file records are missing of slb-version " + slbId + "-" + slbVersion + ".";
                    logger.error(err);
                    throw new ValidationException(err);
                }
                //3.3 refresh config
                logger.info("[[refresh=true]]Refresh Nginx Conf. Reload: " + needReload);
                NginxResponse res = nginxService.refresh(nginxConf, entry, needReload);
                //3.3.1 if failed, set need refresh flag and return result.
                if (!res.getSucceed()) {
                    logger.error("[SlbServerUpdate] Refresh conf failed. SlbId:" + slbId + ";Version:" + slbVersion + response.toString());
                    return res.setServerIp(ip);
                }
            } catch (Exception e) {
                //3.3.2 if throws exception, set need refresh flag.
                logger.error("[SlbServerUpdate] Refresh conf failed. SlbId:" + slbId + ";Version:" + slbVersion + " msg:" + e.getMessage(), e);
                throw new NginxProcessingException("Refresh conf failed. SlbId:" + slbId + ";Version:" + slbVersion + " msg:" + e.getMessage(), e);
            }
        } else {
            try {
                //4. execute commits
                //4.1 get commits and merge to one commit
                Commit commit = commitMergeService.mergeCommit(commitService.getCommitList(slbId, serverVersion, slbVersion));
                if (commit == null) {
                    logger.info("[SlbServerUpdate] Not found commit. return success instead.");
                    return new NginxResponse().setSucceed(true).setServerIp(ip);
                }
                //4.2 get all configs by vs ids in commit
                String nginxConf = null;
                try {
                    nginxConf = nginxConfService.getNginxConf(slbId, slbVersion);
                } catch (ValidationException ex) {
                }
                NginxConfEntry entry = nginxConfService.getUpstreamsAndVhosts(slbId, slbVersion, commit.getVsIds());
                if (nginxConf == null || entry == null) {
                    String err = "Nginx conf file records are missing of slb-version " + slbId + "-" + slbVersion + ".";
                    logger.error(err);
                    throw new ValidationException(err);
                }
                //4.3 get clean vs ids
                Set<Long> cleanSet = new HashSet<>();
                cleanSet.addAll(commit.getCleanvsIds());
                //4.4 get flags
                boolean reload = commit.getType().equals(CommitType.COMMIT_TYPE_RELOAD);
                boolean test = commit.getType().equals(CommitType.COMMIT_TYPE_DYUPS);
                boolean dyups = commit.getType().equals(CommitType.COMMIT_TYPE_DYUPS);
                //4.4 get dyups data if needed
                //4.4.1 if reload is true, try to use dyups instead.
                DyUpstreamOpsData[] dyUpstreamOpsDatas = null;
                if (reload && commit.getGroupIds().size() > 0) {
                    if (checkSkipReload(slbId, commit, serverVersion, entry, nginxConf)) {
                        Set<Long> gids = new HashSet<>();
                        gids.addAll(commit.getGroupIds());
                        try {
                            dyUpstreamOpsDatas = upstreamConfPicker.pickByGroupIds(entry, gids);
                            reload = false;
                            dyups = true;
                            logger.info("[[skipReload=true]]Skipped reload.SlbId:" + slbId + "\nMerged Commit:" + commit.toString());
                        } catch (Exception e) {
                            logger.warn("[[skipReload=false]]Skipped reload failed. Because not found upstream data for some groups. Msg:" + e.getMessage(), e);
                        }
                    }
                }

                if (dyups && dyUpstreamOpsDatas == null) {
                    Set<Long> gids = new HashSet<>();
                    gids.addAll(commit.getGroupIds());
                    dyUpstreamOpsDatas = upstreamConfPicker.pickByGroupIds(entry, gids);
                    randomSortUpstreamServers(dyUpstreamOpsDatas);
                }
                StringBuilder msg = new StringBuilder();
                msg.append("[UpdateConfigInfo] Reload:").append(reload).append("\ntest:").append(test).append("\ndyups:").append(dyups);
                msg.append("\nvsIds:").append(commit.getVsIds().toString());
                msg.append("\ncleanVsIds:").append(commit.getCleanvsIds());
                msg.append("\nversion:").append(commit.getVersion());
                logger.info(msg.toString());
                for (Long taskId : commit.getTaskIds()) {
                    logger.info("[[taskId=" + taskId + "]]Start Update Conf of task. taskId: " + taskId);
                }
                //4.5 update nginx configs
                List<NginxResponse> responses = nginxService.update(nginxConf, entry, new HashSet<>(commit.getVsIds()), cleanSet, dyUpstreamOpsDatas, reload, test, dyups);
                //4.6 check response, if failed. set need refresh flag.
                for (NginxResponse r : responses) {
                    if (!r.getSucceed()) {
                        logger.error("[SlbServerUpdate] Update conf failed. SlbId:" + slbId + ";Version:" + slbVersion + response.toString());
                        return r.setServerIp(ip);
                    }
                }
            } catch (Exception e) {
                //4.7  if failed. set need refresh flag.
                logger.error("[SlbServerUpdate] Execute commits failed. SlbId:" + slbId + ";Version:" + slbVersion + " msg:" + e.getMessage(), e);
                throw new NginxProcessingException("Execute commits failed. SlbId:" + slbId + ";Version:" + slbVersion + " msg:" + e.getMessage(), e);
            }
        }
        //5. update server version
        confVersionService.updateSlbServerCurrentVersion(slbId, ip, slbVersion);
        return response.setServerIp(ip).setSucceed(true).setOutMsg("update success.");
    }

    private void randomSortUpstreamServers(DyUpstreamOpsData[] dyUpstreamOpsDatas) {
        StringBuilder sb = new StringBuilder(256);
        for (DyUpstreamOpsData d : dyUpstreamOpsDatas) {
            sb.setLength(0);
            String upstream = d.getUpstreamCommands();
            if (upstream == null) {
                continue;
            }
            String[] lines = upstream.split(";");
            List<String> servers = new ArrayList<>();
            List<String> others = new ArrayList<>();

            for (String s : lines) {
                if (s.contains("server")) {
                    servers.add(s);
                } else {
                    others.add(s);
                }
            }
            Collections.shuffle(servers);

            for (String n : servers) {
                sb.append(n).append(";");
            }
            for (String o : others) {
                sb.append(o).append(";");
            }
            d.setUpstreamCommands(sb.toString());
        }
    }

    @Override
    public NginxResponse update() throws Exception {
        return update(false, false);
    }

    private boolean checkSkipReload(Long slbId, Commit commit, Long serverVersion, NginxConfEntry nextEntry, String nginxConf) throws Exception {
        //1. clean vs config, need reload.
        if (commit.getCleanvsIds().size() > 0) {
            return false;
        }
        //2. if nginx config file is changed, need reload.
        //2.1 if server version <= 0, need reload. first time update config files.
        if (serverVersion <= 0) {
            logger.info("[[skipReload=false]] cause: serverVerion <= 0");
            return false;
        }
        String serverNginxConf = nginxConfService.getNginxConf(slbId, serverVersion);
        if (!serverNginxConf.equals(nginxConf)) {
            logger.info("[[skipReload=false]] cause: nginxConf not equels. slbId:" + slbId + "serverVersion:" + serverVersion);
            return false;
        }
        //3. compare vhost files
        NginxConfEntry serverCurrentEntry = nginxConfService.getUpstreamsAndVhosts(slbId, serverVersion, commit.getVsIds());
        if (serverCurrentEntry == null) {
            logger.info("[[skipReload=false]] cause: serverCurrentEntry == null. slbId:" + slbId + "serverVersion:" + serverVersion);
            return false;
        }
        if (serverCurrentEntry.getVhosts().getFiles().size() != nextEntry.getVhosts().getFiles().size()) {
            logger.info("[[skipReload=false]] cause: entries have different size. slbId:" + slbId + "serverVersion:" + serverVersion);
            return false;
        }

        Map<String, Integer> index = new HashMap<>();
        List<ConfFile> vhosts = nextEntry.getVhosts().getFiles();
        for (int i = 0; i < vhosts.size(); i++) {
            index.put(vhosts.get(i).getName(), i);
        }
        for (ConfFile cf : serverCurrentEntry.getVhosts().getFiles()) {
            Integer idx = index.get(cf.getName());
            if (idx == null) {
                logger.info("[[skipReload=false]] cause: nextEntry has Less vhosts. slbId:" + slbId + "serverVersion:" + serverVersion);
                return false;
            }

            if (!vhosts.get(idx).getContent().equals(cf.getContent())) {
                logger.info("[[skipReload=false]] cause: nextEntry has different vhosts. slbId:" + slbId + "serverVersion:" + serverVersion + "ConfName:" + cf.getName());
                return false;
            }
        }
        return true;
    }
}
