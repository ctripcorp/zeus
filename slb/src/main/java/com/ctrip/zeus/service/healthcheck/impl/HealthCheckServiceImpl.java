package com.ctrip.zeus.service.healthcheck.impl;

import com.ctrip.zeus.dao.entity.SlbConfig;
import com.ctrip.zeus.domain.ClusterInfo;
import com.ctrip.zeus.domain.Config;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.service.config.SlbConfigService;
import com.ctrip.zeus.service.healthcheck.HealthCheckConsts;
import com.ctrip.zeus.service.healthcheck.HealthCheckService;
import com.ctrip.zeus.service.model.IdVersion;
import com.ctrip.zeus.service.model.SelectionMode;
import com.ctrip.zeus.service.query.CriteriaQueryFactory;
import com.ctrip.zeus.service.query.QueryEngine;
import com.google.common.base.Strings;
import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static com.ctrip.zeus.service.healthcheck.HealthCheckConsts.*;

@Service("healthCheckService")
public class HealthCheckServiceImpl implements HealthCheckService {

    @Resource
    private SlbConfigService slbConfigService;
    @Resource
    private CriteriaQueryFactory criteriaQueryFactory;

    private DynamicIntProperty interval = DynamicPropertyFactory.getInstance().getIntProperty(REGISTER_MIN_INTERVAL, 60 * 60000);

    private final String SEP = ",";
    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public String register(String ip, String cluster) throws ValidationException {
        assert (cluster != null);
        String key = clusterConfigKey(cluster);

        Map<String, String> current = slbConfigService.query(Collections.singletonList(key), true);
        if (current.size() == 0) {
            current.put(key, updateClusterList(current.get(key), ip));
            current.put(clusterLatestRegisterKey(cluster, ip), String.valueOf(new Date().getTime()));
            current.putAll(getDefaultConfig(cluster));
            slbConfigService.batchInsert(current, true);
            return current.get(key);
        } else {
            current.put(key, updateClusterList(current.get(key), ip));
            current.put(clusterLatestRegisterKey(cluster, ip), String.valueOf(new Date().getTime()));
            slbConfigService.batchUpsertValue(current, true);
            return current.get(key);
        }
    }

    private Map<String, String> getDefaultConfig(String clusterName) {
        Map<String, String> result = new HashMap<>();
        for (Map.Entry<String, Object> entry : HealthCheckConsts.clusterPropertySuffixMap.entrySet()) {
            String key = buildPropertyKey(clusterName, entry.getKey());

            result.put(key, entry.getValue().toString());
        }

        return result;
    }

    private String buildPropertyKey(String clusterName, String suffix) {
        return HealthCheckConsts.CLUSTER_PROPERTY_PREFIX + clusterName + suffix;
    }

    @Override
    public String properties(String cluster) {
        List<SlbConfig> configs = slbConfigService.all(true);
        StringBuilder builder = new StringBuilder();
        String key = clusterPropertiesKey(cluster);
        configs.forEach(e -> {
            if (e.getPropertyKey().startsWith(key)) {
                builder.append(e.getPropertyKey().replace(key, "")).append("=").append(e.getPropertyValue()).append("\n");
            }
        });
        return builder.toString();
    }

    @Override
    public Set<Long> checkSlbIds(String cluster) throws Exception {
        String key = clusterPropertiesKey(cluster) + "slbs";
        Map<String, String> p = slbConfigService.query(Collections.singletonList(key),true);
        if (p == null || p.isEmpty()) {
            return new HashSet<>();
        }
        String[] tmp = p.get(key).split(";");
        Queue<String[]> params = new LinkedList<>();
        for (String t : tmp) {
            String[] x = t.split("=");
            if (x.length == 2) {
                params.add(x);
            }
        }
        QueryEngine queryRender = new QueryEngine(params, "slb", SelectionMode.OFFLINE_FIRST);
        queryRender.init(true);
        IdVersion[] searchKeys = queryRender.run(criteriaQueryFactory);
        Set<Long> slbIds = new HashSet<>();
        for (IdVersion idv : searchKeys) {
            slbIds.add(idv.getId());
        }
        return slbIds;
    }

    @Override
    public void cleanDisabledMembers() throws Exception {
        List<SlbConfig> configs = slbConfigService.all(true);
        String key = clusterLatestRegisterPrefix();
        long now = new Date().getTime();
        configs.forEach(e -> {
            if (e.getPropertyKey().startsWith(key)) {
                String[] ipCluster = clusterLatestRegisterGetIPCluster(e.getPropertyKey());
                try {
                    long time = Long.parseLong(e.getPropertyValue());
                    if (time - now > interval.get()) {
                        removeIpFromClusterList(ipCluster[0], ipCluster[1]);
                    }
                } catch (Exception e1) {
                    logger.warn("Parser Long Failed.", e1);
                }
            }
        });
    }

    @Override
    public List<ClusterInfo> list() throws Exception {
        Map<String, List<String>> clusterIPMap = new HashMap<>();
        Map<String, List<Config>> clusterConfigsMap = new HashMap<>();

        List<SlbConfig> systemConfigs = slbConfigService.all(true);

        for (SlbConfig systemConfig : systemConfigs) {
            String key = systemConfig.getPropertyKey();

            if (isClusterIPKey(key)) {
                String clusterName = parseClusterName(key);
                List<String> ips = parseClusterIPs(systemConfig);
                clusterIPMap.put(clusterName, ips);
            } else if (isClusterPropertyKey(key)) {
                String clusterName = parseClusterName(key);
                Config config = parseClusterConfig(systemConfig);

                List<Config> configs = clusterConfigsMap.getOrDefault(clusterName, null);
                if (configs == null) {
                    configs = new ArrayList<>();
                    clusterConfigsMap.put(clusterName, configs);
                }

                configs.add(config);
            }
        }

        Set<String> clusterNames = new HashSet<>(clusterIPMap.keySet());

        List<ClusterInfo> results = new ArrayList<>(clusterNames.size());
        for (String name : clusterNames) {
            ClusterInfo info = new ClusterInfo();
            info.setName(name);
            info.setIps(clusterIPMap.get(name));
            info.setConfigs(clusterConfigsMap.getOrDefault(name, new ArrayList<>()));

            results.add(info);
        }

        return results;
    }

    @Override
    public ClusterInfo getClusterInfoByName(String name) throws Exception {
        List<ClusterInfo> clusterInfos = list();

        for (ClusterInfo clusterInfo : clusterInfos) {
            if (clusterInfo.getName().equals(name)) {
                return clusterInfo;
            }
        }

        return null;
    }

    @Override
    public void updateProperties(ClusterInfo info) throws Exception {
        if (info == null || CollectionUtils.isEmpty(info.getConfigs())) {
            return;
        }
        ClusterInfo target = getClusterInfoByName(info.getName());

        if (target != null) {
            List<Config> configs = target.getConfigs();
            Set<String> existedKeys = configs.stream().map(Config::getName).collect(Collectors.toSet());

            Map<String, String> update = new HashMap<>();
            for (Config config : info.getConfigs()) {
                if (config != null && existedKeys.contains(config.getName())) {
                    update.put(clusterPropertiesKey(info.getName()) + config.getName(), config.getValue());
                }
            }

            slbConfigService.batchUpdate(update, true);
        }
    }

    private List<String> parseClusterIPs(SlbConfig config) {
        if (config != null && config.getPropertyValue() != null) {
            List<String> ips = Arrays.asList(config.getPropertyValue().split(SEP));
            ips.removeIf(Strings::isNullOrEmpty);
            return ips;
        }

        return new ArrayList<>();
    }

    private Config parseClusterConfig(SlbConfig config) {
        if (config != null) {
            String key = config.getPropertyKey();
            if (key.startsWith(HealthCheckConsts.CLUSTER_PROPERTY_PREFIX)) {
                String clusterName = parseClusterName(key);
                String actualKey = key.substring(CLUSTER_PROPERTY_PREFIX.length() + clusterName.length() + 1);

                Config result = new Config();
                result.setName(actualKey);
                result.setValue(config.getPropertyValue());

                return result;
            }
        }

        return null;
    }

    private String parseClusterName(String key) {
        if (key != null) {
            for (String prefix : HealthCheckConsts.clusterKeys) {
                if (key.startsWith(prefix)) {
                    String temp = key.substring(prefix.length());
                    return getFirstTokenSplitBy(temp, "\\.");
                }
            }
        }
        return null;
    }

    private String getFirstTokenSplitBy(String original, String separator) {
        if (original != null) {
            String[] tokens = original.split(separator);
            if (tokens.length >= 1) {
                return tokens[0];
            }
        }

        return null;
    }

    private boolean isClusterIPKey(String key) {
        return key != null && key.startsWith(CLUSTER_PREFIX);
    }

    private boolean isClusterPropertyKey(String key) {
        return key != null && key.startsWith(CLUSTER_PROPERTY_PREFIX);
    }

    private String clusterConfigKey(String cluster) {
        return CLUSTER_PREFIX + cluster;
    }

    private String clusterLatestRegisterKey(String cluster, String ip) {
        return clusterLatestRegisterPrefix() + cluster + "." + ip;
    }

    private String clusterLatestRegisterPrefix() {
        return HealthCheckConsts.CLUSTER_REGISTER_PREFIX;
    }

    /**
     * Result : index 0: cluster
     * Index 1: ip
     * @param key
     * @return
     */
    private String[] clusterLatestRegisterGetIPCluster(String key) {
        return key.replace(key, "").split("\\.");
    }

    private String clusterPropertiesKey(String cluster) {
        return HealthCheckConsts.CLUSTER_PROPERTY_PREFIX + cluster + ".";
    }

    private String updateClusterList(String base, String ip) {
        if (base == null) {
            base = "";
        }
        if (ip == null) {
            return base;
        }
        ip = ip.trim();
        StringBuilder sb = new StringBuilder(128);
        boolean ipInList = false;
        String[] baseList = base.split(SEP);
        for (String i : baseList) {
            if (i.isEmpty() || i.trim().isEmpty()) {
                continue;
            }
            if (i.equalsIgnoreCase(ip)) {
                ipInList = true;
            }
            sb.append(i).append(SEP);
        }
        if (!ipInList) {
            sb.append(ip);
        }
        return sb.toString();
    }

    public String removeIpFromClusterList(String base, String ip) {
        if (base == null) {
            return "";
        }
        ip = ip.trim();
        StringBuilder sb = new StringBuilder(128);
        String[] baseList = base.split(SEP);
        for (String i : baseList) {
            if (i.isEmpty() || i.trim().isEmpty()) {
                continue;
            }
            if (i.trim().equalsIgnoreCase(ip)) {
                continue;
            }
            sb.append(i).append(SEP);
        }
        return sb.toString();
    }
}
