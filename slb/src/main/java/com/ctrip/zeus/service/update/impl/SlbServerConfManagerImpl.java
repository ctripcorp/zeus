package com.ctrip.zeus.service.update.impl;

import com.alibaba.fastjson.JSON;
import com.ctrip.zeus.exceptions.NginxProcessingException;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.commit.Commit;
import com.ctrip.zeus.model.model.DyUpstreamOpsData;
import com.ctrip.zeus.model.nginx.ConfFile;
import com.ctrip.zeus.model.nginx.NginxConfEntry;
import com.ctrip.zeus.model.nginx.NginxResponse;
import com.ctrip.zeus.server.LocalInfoPack;
import com.ctrip.zeus.service.build.ConfigHandler;
import com.ctrip.zeus.service.build.NginxConfService;
import com.ctrip.zeus.service.build.util.ResolverUtils;
import com.ctrip.zeus.service.commit.CommitMergeService;
import com.ctrip.zeus.service.commit.CommitService;
import com.ctrip.zeus.service.commit.util.CommitType;
import com.ctrip.zeus.service.message.queue.MessageQueue;
import com.ctrip.zeus.service.message.queue.MessageTypeConsts;
import com.ctrip.zeus.service.model.snapshot.ModelSnapshotEntity;
import com.ctrip.zeus.service.nginx.NginxService;
import com.ctrip.zeus.service.nginx.util.UpstreamConfPicker;
import com.ctrip.zeus.service.tools.local.LocalInfoService;
import com.ctrip.zeus.service.update.AgentApiRefactorSwitches;
import com.ctrip.zeus.service.update.SlbServerConfManager;
import com.ctrip.zeus.service.version.ConfVersionService;
import com.ctrip.zeus.client.AgentApiClient;
import com.ctrip.zeus.service.build.ConfSnapshotBuildService;
import com.ctrip.zeus.service.build.util.LocalConfComparator;
import com.google.common.base.Joiner;
import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by fanqq on 2016/3/15.
 */
@Service("slbServerConfManager")
public class SlbServerConfManagerImpl implements SlbServerConfManager {

    @Resource
    private CommitService commitService;
    @Resource
    private ConfVersionService confVersionService;
    @Autowired
    private NginxService nginxService;
    @Resource
    private NginxConfService nginxConfService;
    @Resource
    private CommitMergeService commitMergeService;
    @Resource
    private UpstreamConfPicker upstreamConfPicker;
    @Resource
    private ConfigHandler configHandler;

    @Resource
    private LocalInfoService localInfoService;
    @Resource
    private MessageQueue messageQueue;
    @Resource
    private ConfSnapshotBuildService confSnapshotBuildService;
    @Resource
    private AgentApiRefactorSwitches agentApiRefactorSwitches;
    @Resource
    private LocalConfComparator localConfComparator;

    private final Logger logger = LoggerFactory.getLogger(SlbServerConfManagerImpl.class);
    private static DynamicIntProperty maxSpan = DynamicPropertyFactory.getInstance().getIntProperty("commit.max.span", 10);
    private static DynamicIntProperty reviveReloadInterval = DynamicPropertyFactory.getInstance().getIntProperty("check.reload.shut.interval", 2 * 60 * 1000);
    private AtomicLong lastReviveReload = new AtomicLong(0L);

    @Override
    public synchronized NginxResponse update(boolean refresh, boolean needReload) throws Exception {
        NginxResponse response = new NginxResponse();
        //1. get slbId by local ip
        String ip = LocalInfoPack.INSTANCE.getIp();
        Long slbId = localInfoService.getLocalSlbIdWithRetry();
        if (slbId == null) {
            logger.warn("[SlbServerConfManagerImpl] Not found slb id by ip. ip:" + ip);
            return response.setSucceed(false).setServerIp(ip).setErrMsg("[SlbServerConfManagerImpl] Not found slb id by ip. ip:" + ip);
        }

        //2. get slbVersion and serverVersion
        Long slbVersion, serverVersion;
        if (agentApiRefactorSwitches.isSwitchOn(slbId)) {
            Map<String, Long> versionMap = AgentApiClient.getClient().getConfVersions();
            slbVersion = versionMap.get("slb-version");
            serverVersion = versionMap.get("slb-server-version");
        } else {
            slbVersion = confVersionService.getSlbCurrentVersion(slbId);
            serverVersion = confVersionService.getSlbServerCurrentVersion(slbId, ip);
        }

        logger.info("SlbVersion: " + slbVersion + " ServerVersion: " + serverVersion);
        if (slbVersion.equals(serverVersion)) {
            return response.setSucceed(true).setServerIp(ip).setOutMsg("[SlbServerConfManagerImpl] slb version == slb server version. Not need to update.");
        }
        //3. force rollback to slbVersion
        //case 1: force refresh or flag needRefresh is true
        //case 2: server version is large then slb version
        //case 3: version gap larger than max span
        ModelSnapshotEntity snapshot = null;
//        if (agentApiRefactorSwitches.isSwitchOn(slbId)) {
//            snapshot = AgentApiClient.getClient().getModelSnapshotEntity(slbVersion);
//            logger.info("Snapshot got from api: " + JSON.toJSONString(snapshot));
//            if (snapshot == null) {
//                logger.error("Fail to get snapshot from api cluster.");
//                return response.setServerIp(ip).setSucceed(false).setOutMsg("[SlbServerConfManagerImpl] Fail to get snapshot from api cluster. ");
//            }
//        }
        if (refresh || slbVersion < serverVersion || slbVersion - serverVersion > maxSpan.get()) {
            try {
                if (agentApiRefactorSwitches.isSwitchOn(slbId)) {
                    snapshot = AgentApiClient.getClient().getModelSnapshotEntity(slbVersion);
                    logger.info("Snapshot got from api: " + JSON.toJSONString(snapshot));
                    if (snapshot == null) {
                        logger.error("Fail to get snapshot from api cluster.");
                        return response.setServerIp(ip).setSucceed(false).setOutMsg("[SlbServerConfManagerImpl] Fail to get snapshot from api cluster. ");
                    }
                }
                //3.1 need reload in case 3
                if (slbVersion - serverVersion > maxSpan.get()) {
                    needReload = true;
                }
                //3.2 get nginx config
                String nginxConf = null;
                if (agentApiRefactorSwitches.isSwitchOn(slbId)) {
                    nginxConf = confSnapshotBuildService.buildNginxConf(snapshot);
                    logger.info("nginx conf built: " + nginxConf);
                } else {
                    try {
                        nginxConf = nginxConfService.getNginxConf(slbId, slbVersion);
                    } catch (ValidationException ex) {
                    }
                }

                NginxConfEntry entry;
                if (agentApiRefactorSwitches.isSwitchOn(slbId)) {
                    entry = confSnapshotBuildService.buildFullConfEntry(snapshot);
                    logger.info("entry built: " + JSON.toJSONString(entry));
                } else {
                    entry = nginxConfService.getUpstreamsAndVhosts(slbId, slbVersion);
                }
                if (nginxConf == null || entry == null) {
                    String err = "Nginx conf file records are missing of slb-version " + slbId + "-" + slbVersion + ".";
                    logger.error(err);
                    throw new ValidationException(err);
                }
                //3.3 refresh config
                logger.info("[[refresh=true]]Refresh Nginx Conf. Reload: " + needReload);

                nginxConf = http2ConfCheck(nginxConf, slbId);
                nginxConf = backlogConfCheck(nginxConf);
                nginxConf = autoResolvers(nginxConf, slbId);

                NginxResponse res = nginxService.refresh(nginxConf, entry, needReload);
                //3.3.1 if failed, set need refresh flag and return result.
                if (!res.getSucceed()) {
                    logger.error("[SlbServerUpdate] Refresh conf failed. SlbId:" + slbId + ";Version:" + slbVersion + response.toString());
                    return res.setServerIp(ip);
                }
                if (needReload) {
                    sendReloadMessage(slbId);
                }
            } catch (Exception e) {
                //3.3.2 if throws exception, set need refresh flag.
                logger.error("[SlbServerUpdate] Refresh conf failed. SlbId:" + slbId + ";Version:" + slbVersion + " msg:" + e.getMessage(), e);
                throw new NginxProcessingException("Refresh conf failed. SlbId:" + slbId + ";Version:" + slbVersion + " msg:" + e.getMessage(), e);
            }
        } else {
            try {
                if (agentApiRefactorSwitches.isSwitchOn(slbId)) {
                    snapshot = AgentApiClient.getClient().getModelSnapshotEntity(slbVersion, serverVersion);
                    logger.info("Snapshot got from api: " + JSON.toJSONString(snapshot));
                    if (snapshot == null) {
                        logger.error("Fail to get snapshot from api cluster.");
                        return response.setServerIp(ip).setSucceed(false).setOutMsg("[SlbServerConfManagerImpl] Fail to get snapshot from api cluster. ");
                    }
                }
                //4. execute commits
                //4.1 get commits and merge to one commit
                Commit commit = null;
                if (agentApiRefactorSwitches.isSwitchOn(slbId)) {
                    commit = snapshot.getCommits();
                } else {
                    commit = commitMergeService.mergeCommit(commitService.getCommitList(slbId, serverVersion, slbVersion));
                }
                if (commit == null) {
                    logger.info("[SlbServerUpdate] Not found commit. return success instead.");
                    return new NginxResponse().setSucceed(true).setServerIp(ip);
                }
                //4.2 get all configs by vs ids in commit
                String nginxConf = null;
                if (agentApiRefactorSwitches.isSwitchOn(slbId)) {
                    nginxConf = confSnapshotBuildService.buildNginxConf(snapshot);
                    logger.info("nginx conf built: " + nginxConf);
                } else {
                    try {
                        nginxConf = nginxConfService.getNginxConf(slbId, slbVersion);
                    } catch (ValidationException ex) {
                        logger.warn("Validate Exception:", ex);
                    }
                }

                if (agentApiRefactorSwitches.isSwitchOn(slbId)) {
                    //4.5 update nginx configs
                    nginxConf = http2ConfCheck(nginxConf, slbId);
                    nginxConf = backlogConfCheck(nginxConf);
                    nginxConf = autoResolvers(nginxConf, slbId);
                }

                NginxConfEntry entry = null;
                if (agentApiRefactorSwitches.isSwitchOn(slbId)) {
                    if (commit.getType().equals(CommitType.COMMIT_TYPE_FULL_UPDATE)) {
                        entry = confSnapshotBuildService.buildFullConfEntry(snapshot);
                        logger.info("full entry built: " + JSON.toJSONString(entry));
                    } else {
                        entry = confSnapshotBuildService.buildIncrementalEntry(snapshot);
                        logger.info("entry built: " + JSON.toJSONString(entry));
                    }
                } else {
                    if (commit.getType().equals(CommitType.COMMIT_TYPE_FULL_UPDATE)) {
                        entry = nginxConfService.getUpstreamsAndVhosts(slbId, slbVersion);
                        logger.info("get full entry: " + JSON.toJSONString(entry));
                    } else {
                        entry = nginxConfService.getUpstreamsAndVhosts(slbId, slbVersion, commit.getVsIds());
                        logger.info("get Incremental entry: " + JSON.toJSONString(entry));
                    }
                }
                if (nginxConf == null || entry == null) {
                    String err = "Nginx conf file records are missing of slb-version " + slbId + "-" + slbVersion + ".";
                    logger.error(err);
                    throw new ValidationException(err);
                }
                //4.3 get clean vs ids
                Set<Long> cleanSet = new HashSet<>(commit.getCleanvsIds());
                //4.4 get flags
                boolean forceReload = commit.getType().equals(CommitType.COMMIT_TYPE_FULL_UPDATE) || commit.getType().equals(CommitType.COMMIT_TYPE_RELOAD);
                boolean fullUpdate = commit.getType().equals(CommitType.COMMIT_TYPE_FULL_UPDATE);
                boolean softReload = commit.getType().equals(CommitType.COMMIT_TYPE_SOFT_RELOAD);
                boolean reload;
                boolean test;
                boolean dyups;
                //4.4 get dyups data if needed
                //4.4.1 if reload is true, try to use dyups instead.
                DyUpstreamOpsData[] dyUpstreamOpsDatas = null;
                if (forceReload || needReload) {
                    reload = true;
                    test = false;
                    dyups = false;
                } else if (checkReload(slbId, commit, serverVersion, entry, nginxConf, softReload)) {
                    reload = true;
                    test = false;
                    dyups = false;
                } else {
                    Set<Long> gids = new HashSet<>(commit.getGroupIds());
                    try {
                        dyUpstreamOpsDatas = upstreamConfPicker.pickByGroupIds(entry, gids);
                        randomSortUpstreamServers(dyUpstreamOpsDatas, gids);
                        reload = false;
                        test = true;
                        dyups = true;
                        logger.info("[[needReload=false]]No Need reload.SlbId:" + slbId + "\nMerged Commit:" + commit.toString());
                    } catch (Exception e) {
                        reload = true;
                        test = false;
                        dyups = false;
                        logger.warn("[[needReload=true]]Need reload failed. Because not found upstream data for some groups. Msg:" + e.getMessage(), e);
                    }
                }

                if (test && !configHandler.getEnable("nginx.test.command", slbId, null, null, true)) {
                    test = false;
                }

                if (!agentApiRefactorSwitches.isSwitchOn(slbId)) {
                    //4.5 update nginx configs
                    nginxConf = http2ConfCheck(nginxConf, slbId);
                    nginxConf = backlogConfCheck(nginxConf);
                    nginxConf = autoResolvers(nginxConf, slbId);
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

                if (fullUpdate) {
                    NginxResponse res = nginxService.refresh(nginxConf, entry, true);
                    if (!res.getSucceed()) {
                        logger.error("[SlbServerUpdate] Update Refresh conf failed. SlbId:" + slbId + ";Version:" + slbVersion + response.toString());
                        return res.setServerIp(ip);
                    }
                } else {
                    List<NginxResponse> responses = nginxService.update(nginxConf, entry, new HashSet<>(commit.getVsIds()), cleanSet, dyUpstreamOpsDatas, reload, test, dyups);
                    //4.6 check response, if failed. set need refresh flag.
                    for (NginxResponse r : responses) {
                        if (!r.getSucceed()) {
                            logger.error("[SlbServerUpdate] Update conf failed. SlbId:" + slbId + ";Version:" + slbVersion + response.toString());
                            return r.setServerIp(ip);
                        }
                    }
                }
                if (fullUpdate || reload) {
                    sendReloadMessage(slbId);
                }
            } catch (Exception e) {
                //4.7  if failed. set need refresh flag.
                logger.error("[SlbServerUpdate] Execute commits failed. SlbId:" + slbId + ";Version:" + slbVersion + " msg:" + e.getMessage(), e);
                throw new NginxProcessingException("Execute commits failed. SlbId:" + slbId + ";Version:" + slbVersion + " msg:" + e.getMessage(), e);
            }
        }
        //5. update server version
        if (agentApiRefactorSwitches.isSwitchOn(slbId)) {
            AgentApiClient.getClient().updateServerVersion(snapshot.getVersion());
        } else {
            confVersionService.updateSlbServerCurrentVersion(slbId, ip, slbVersion);
            return response.setServerIp(ip).setSucceed(true).setOutMsg("update success.");
        }
        return response.setServerIp(ip).setSucceed(true).setOutMsg("update success.");
    }

    private String autoResolvers(String nginxConf, Long slbId) throws Exception {
        if (configHandler.getEnable("auto.resolver", slbId, null, null, true)) {
            List<String> resolvers = ResolverUtils.getLocalResolvers();
            if (resolvers != null && resolvers.size() > 0) {
                StringBuilder sb = new StringBuilder(128);
                sb.append("resolver ").append(Joiner.on(' ').join(resolvers)).append(";\n");
                nginxConf = nginxConf.replace(ResolverUtils.RESOLVER_SPACE, sb.toString());
                logger.info("[[resolver=true]]Replace Resolver Success. Resolvers:" + sb.toString());
            }
        }
        return nginxConf;
    }

    private void sendReloadMessage(Long slbId) {
        try {
            messageQueue.produceMessage(MessageTypeConsts.RELOAD, slbId, null);
        } catch (Exception e) {
            logger.warn("Send Reload Message Failed.", e);
        }
    }


    private String backlogConfCheck(String nginxConf) throws Exception {
        String result = nginxConf;
        if (configHandler.getEnable("backlog.canary", null, null, null, false) && !configHandler.getEnable("slb.listen.backlog", true)) {
            int backlog = configHandler.getIntValue("slb.listen.backlog", null, null, null, 511);
            if (result.contains("listen *:443 default_server")) {
                result = result.replace("listen *:443 default_server", "listen *:443 default_server backlog=" + backlog);
            }
            if (result.contains("listen *:80 default_server")) {
                result = result.replace("listen *:80 default_server", "listen *:80 default_server backlog=" + backlog);
            }
            if (result.contains("listen *:443 http2 default_server")) {
                result = result.replace("listen *:443 http2 default_server", "listen *:443 http2 default_server backlog=" + backlog);
            }
        }
        return result;
    }

    private String http2ConfCheck(String nginxConf, Long slbId) throws Exception {
        String result = nginxConf;
        if (configHandler.getEnable("http.version.2.bastion", slbId, null, null, false)) {
            if (result.contains("listen *:443 default_server")) {
                result = result.replace("listen *:443 default_server", "listen *:443 http2 default_server");
            }
            if (result.contains("proxy_request_buffering off")) {
                result = result.replace("proxy_request_buffering off", "proxy_request_buffering on");
            }
        }

        if (configHandler.getEnable("http.spdy.bastion", slbId, null, null, false)) {
            if (result.contains("listen *:443 http2 default_server")) {
                result = result.replace("listen *:443 http2 default_server", "listen *:443 http2 spdy default_server");
            }
        }
        return result;
    }

    private void randomSortUpstreamServers(DyUpstreamOpsData[] dyUpstreamOpsDatas, Set<Long> gids) {
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
                if (o.trim().isEmpty()) {
                    continue;
                }
                sb.append(o).append(";");
            }
            d.setUpstreamCommands(sb.toString());
        }
    }

    @Override
    public NginxResponse update() throws Exception {
        return update(false, false);
    }

    //1. Return True: nginx conf or  vhosts files have changed, need reload.
    //1. Return False: nginx conf or vhosts files have not changed, no need reload.
    private boolean checkReload(Long slbId, Commit commit, Long serverVersion, NginxConfEntry nextEntry, String nginxConf, boolean softReload) throws Exception {
        //Revive reload happens while softReload is false but has diff config files.
        //Revive reload skipped while interval time is less then interval threshold.
        long last = lastReviveReload.get();
        if (!softReload && System.currentTimeMillis() - last < reviveReloadInterval.get()) {
            return false;
        }
        boolean isDiffConfigFiles = checkDiffConfigFiles(slbId, commit, serverVersion, nextEntry, nginxConf);
        if (isDiffConfigFiles && !softReload) {
            lastReviveReload.compareAndSet(last, System.currentTimeMillis());
        }
        return isDiffConfigFiles;
    }

    private boolean checkDiffConfigFiles(Long slbId, Commit commit, Long serverVersion, NginxConfEntry nextEntry, String nginxConf) throws Exception {
        //1. clean vs config, need reload.
        if (commit.getCleanvsIds().size() > 0) {
            // update last check reload time, Check Reload is true while softReload is false.
            return true;
        }
        //2. if nginx config file is changed, need reload.
        //2.1 if server version <= 0, need reload. first time update config files.
        if (serverVersion <= 0) {
            logger.info("[[needReload=true]] cause: serverVerion <= 0");
            return true;
        }
        if (agentApiRefactorSwitches.isSwitchOn(slbId)) {
            if (!localConfComparator.isNginxConfSame(nginxConf)) {
                logger.info("[[needReload=true]] cause: built nginxConf not same with local one. slbId: " + slbId);
                return true;
            }
        } else {
            String serverNginxConf = nginxConfService.getNginxConf(slbId, serverVersion);
            if (!serverNginxConf.equals(nginxConf)) {
                logger.info("[[needReload=true]] cause: nginxConf not equels. slbId:" + slbId + "serverVersion:" + serverVersion);
                return true;
            }
        }

        //3. compare vhost files
        if (agentApiRefactorSwitches.isSwitchOn(slbId)) {
            if (!localConfComparator.isVhostConfsSame(nextEntry)) {
                logger.info("[[needReload=true]] vhost conf not same with local ones. ");
                return true;
            }
        } else {
            NginxConfEntry serverCurrentEntry = nginxConfService.getUpstreamsAndVhosts(slbId, serverVersion, commit.getVsIds());
            if (serverCurrentEntry == null) {
                logger.info("[[needReload=true]] cause: serverCurrentEntry == null. slbId:" + slbId + "serverVersion:" + serverVersion);
                return true;
            }
            if (serverCurrentEntry.getVhosts().getFiles().size() != nextEntry.getVhosts().getFiles().size()) {
                logger.info("[[needReload=true]] cause: entries have different size. slbId:" + slbId + "serverVersion:" + serverVersion);
                return true;
            }

            Map<String, ConfFile> nextIndex = new HashMap<>();
            List<ConfFile> vhosts = nextEntry.getVhosts().getFiles();
            for (ConfFile vhost : vhosts) {
                nextIndex.put(vhost.getName(), vhost);
            }

            Map<String, ConfFile> currentIndex = new HashMap<>();
            List<ConfFile> vh = serverCurrentEntry.getVhosts().getFiles();
            for (ConfFile aVh : vh) {
                currentIndex.put(aVh.getName(), aVh);
            }
            for (Long vsId : commit.getVsIds()) {
                for (String name : currentIndex.keySet()) {
                    if (!name.contains(vsId.toString())) continue;
                    if (!nextIndex.containsKey(name)) {
                        logger.info("[[needReload=true]] cause: nextEntry has Less vhosts. slbId:" + slbId + "serverVersion:" + serverVersion);
                        return true;
                    }
                    if (!nextIndex.get(name).getContent().equals(currentIndex.get(name).getContent())) {
                        logger.info("[[needReload=true]] cause: nextEntry has different vhosts. slbId:" + slbId + "serverVersion:" + serverVersion + "ConfName:" + name);
                        return true;
                    }
                }
            }
        }

        return false;
    }

}
