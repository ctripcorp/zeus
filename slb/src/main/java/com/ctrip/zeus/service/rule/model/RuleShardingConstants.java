package com.ctrip.zeus.service.rule.model;

public class RuleShardingConstants {
    public final static String NAMED_GROUP_NAME = "Sharding_SHA";
    public final static String SHARDING_PROPERTY_KEY_NAME = "REGION_TRAFFIC_SHARDING";
    public final static String SHARDING_GROUP_KEY_NAME = "REGION_TRAFFIC_GROUP_ID";
    public final static Integer SHARDING_GROUP_DEFAULT_PRIORITY = -2000;
}
