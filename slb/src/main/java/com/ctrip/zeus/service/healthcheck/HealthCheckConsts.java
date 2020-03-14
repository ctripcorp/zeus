package com.ctrip.zeus.service.healthcheck;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class HealthCheckConsts {
    public static final String CLUSTER_PREFIX = "HealthChecker.Cluster.";
    public static final String CLUSTER_REGISTER_PREFIX = "HealthChecker.Register.Last.Time.";
    public static final String CLUSTER_PROPERTY_PREFIX = "HealthChecker.Properties.Cluster.";
    public static final String REGISTER_MIN_INTERVAL = "HealthChecker.Register.Min.Interval";
    public static final String PERSISTENCE_CONNECTION_ENABLED_SUFFIX = ".persistent-connection.enabled";
    public static final String SOCKET_TIMEOUT_ENABLED_SUFFIX = ".socket-timeout.enabled";
    public static final String MEMBERS_NEED_STABLE_SUFFIX = ".members.need-stable.time";
    public static final String ITEM_CHECK_ENABLED_SUFFIX = ".reporter.item-check.enabled";
    public static final String CHECK_ALL_ENABLED_SUFFIX = ".slb.check-all.enabled";
    public static final String SKIP_GROUPS_SUFFIX = ".slb.check-escaped.groups";
    public static final String DO_RAISEFALL_ENABLED_SUFFIX = ".slb.doraisefall.enabled";
    public static final String FETCH_GROUP_INTERVAL_SUFFIX = ".slb.fetch-groups.interval";
    public static final String SLB_SYNC_ENABLED_SUFFIX = ".slb.sync.enabled";
    public static final String CHECK_SLBS = ".slbs";

    public static final Set<String> clusterKeys = new HashSet<>();
    public static final HashMap<String, Object> clusterPropertySuffixMap = new HashMap<>();


    static {
        clusterKeys.add(CLUSTER_PREFIX);
        clusterKeys.add(CLUSTER_REGISTER_PREFIX);
        clusterKeys.add(CLUSTER_PROPERTY_PREFIX);

        clusterPropertySuffixMap.put(PERSISTENCE_CONNECTION_ENABLED_SUFFIX, true);
        clusterPropertySuffixMap.put(SOCKET_TIMEOUT_ENABLED_SUFFIX, true);
        clusterPropertySuffixMap.put(MEMBERS_NEED_STABLE_SUFFIX, 120000);
        clusterPropertySuffixMap.put(ITEM_CHECK_ENABLED_SUFFIX, true);
        clusterPropertySuffixMap.put(CHECK_ALL_ENABLED_SUFFIX, false);
        clusterPropertySuffixMap.put(SKIP_GROUPS_SUFFIX, "");
        clusterPropertySuffixMap.put(DO_RAISEFALL_ENABLED_SUFFIX, true);
        clusterPropertySuffixMap.put(FETCH_GROUP_INTERVAL_SUFFIX, 60000);
        clusterPropertySuffixMap.put(SLB_SYNC_ENABLED_SUFFIX, true);
        clusterPropertySuffixMap.put(CHECK_SLBS, "");
    }


}
