package com.ctrip.zeus.tag;

public final class PropertyValues {

    private PropertyValues() {
    }

    public static class RelationTypes {

        public static final String TRAFFIC_MIGRATION = "trafficMigration";

        public static final String EXTENDED_ROUTING = "extendedRouting";

        public static final String DEFAULT = TRAFFIC_MIGRATION;
    }
}
