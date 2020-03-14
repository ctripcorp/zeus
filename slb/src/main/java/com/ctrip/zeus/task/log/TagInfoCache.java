package com.ctrip.zeus.task.log;

import com.ctrip.zeus.model.model.Domain;
import com.ctrip.zeus.model.model.Group;
import com.ctrip.zeus.model.model.GroupVirtualServer;
import com.ctrip.zeus.service.model.GroupRepository;
import com.ctrip.zeus.service.model.IdVersion;
import com.ctrip.zeus.service.model.SelectionMode;
import com.ctrip.zeus.service.query.GroupCriteriaQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by fanqq on 2016/7/27.
 */
@Component("tagInfoCache")
public class TagInfoCache {
    @Autowired
    private GroupRepository groupRepository;
    @Resource
    private GroupCriteriaQuery groupCriteriaQuery;

    private Map<String, String> groupAppIdCache = new ConcurrentHashMap<>();
    private Map<String, String> domainVsCache = new ConcurrentHashMap<>();

    private TagInfoCacheUpdater infoUpdater = new TagInfoCacheUpdater();

    public String getAppId(String groupId) {
        try {
            String appId = groupAppIdCache.get(groupId);
            if (appId == null) {
                infoUpdater.startRunning();
                appId = "unknown";
            }
            return appId;
        } catch (Exception e) {
            return "unknown";
        }
    }

    public String getVsId(String slbId, String domain, String port) {
        try {
            String vsId = null;
            if (!domainVsCache.isEmpty()) {
                for (; domain != null && vsId == null; domain = getPreLevelWildcardDomain(domain)) {
                    vsId = domainVsCache.get(slbId + domain + port);
                }
            }
            if (vsId == null) {
                infoUpdater.startRunning();
                vsId = "unknown";
            }
            return vsId;
        } catch (Exception e) {
            return "unknown";
        }
    }

    private static String getPreLevelWildcardDomain(String domain) {
        int nextDotIndex;
        if (domain.charAt(0) == '*' && domain.charAt(1) == '.') {
            nextDotIndex = domain.indexOf('.', 2);
        } else {
            nextDotIndex = domain.indexOf('.');
        }
        if (nextDotIndex == -1) {
            return null;
        }
        return "*" + domain.substring(nextDotIndex);
    }

    void updateCache(List<Group> groups) {
        for (Group g : groups) {
            if (g.getAppId() != null) {
                groupAppIdCache.put(g.getId().toString(), g.getAppId());
            }
            if (g.getGroupVirtualServers() != null && g.getGroupVirtualServers().size() > 0) {
                for (GroupVirtualServer gvs : g.getGroupVirtualServers()) {
                    for (Domain d : gvs.getVirtualServer().getDomains()) {
                        for (Long slbId : gvs.getVirtualServer().getSlbIds()) {
                            domainVsCache.put(slbId + d.getName() + gvs.getVirtualServer().getPort(), gvs.getVirtualServer().getId().toString());
                        }
                    }
                }
            }
        }
    }

    class TagInfoCacheUpdater extends Thread {
        private final AtomicBoolean isRunning;

        private Logger logger = LoggerFactory.getLogger(TagInfoCacheUpdater.class);

        TagInfoCacheUpdater() {
            isRunning = new AtomicBoolean(false);
            setName("AppIdUpdater");
            setDaemon(true);
        }

        public void startRunning() {
            if (isRunning.compareAndSet(false, true)) {
                start();
            }
        }

        public void stopRunning() {
            isRunning.compareAndSet(true, false);
        }

        @Override
        public void run() {
            while (isRunning.get()) {
                try {
                    Set<IdVersion> idVersionSet = groupCriteriaQuery.queryAll(SelectionMode.ONLINE_EXCLUSIVE);
                    List<Group> groups = groupRepository.list(idVersionSet.toArray(new IdVersion[idVersionSet.size()]));
                    if (groups != null) {
                        TagInfoCache.this.updateCache(groups);
                    }
                } catch (Exception e) {
                    logger.warn("TagInfoCacheUpdater update group info failed.", e);
                } finally {
                    try {
                        sleep(60000 * 60);
                    } catch (Exception e) {
                        logger.warn("Encounter an error while TagInfoCacheUpdater sleeping.", e);
                    }
                }
            }
        }
    }


}
