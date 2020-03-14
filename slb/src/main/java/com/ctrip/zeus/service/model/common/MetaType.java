package com.ctrip.zeus.service.model.common;

/**
 * Created by zhoumy on 2017/1/9.
 */
public enum MetaType {
    GROUP(1), VS(2), SLB(3), MEMBER(4), TRAFFIC_POLICY(5), DR(6), RULE(7);

    int id;

    MetaType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static MetaType getMetaType(int id) {
        switch (id) {
            case 1:
                return GROUP;
            case 2:
                return VS;
            case 3:
                return SLB;
            case 4:
                return MEMBER;
            case 5:
                return TRAFFIC_POLICY;
            case 6:
                return DR;
            case 7:
                return RULE;
        }
        return null;
    }


    public static MetaType getMetaType(String type) {
        if (type == null) return null;

        type = type.toUpperCase();

        switch (type) {
            case "GROUP":
                return GROUP;
            case "VS":
                return VS;
            case "SLB":
                return SLB;
            case "MEMBER":
                return MEMBER;
            case "POLICY":
                return TRAFFIC_POLICY;
            case "DR":
                return DR;
            case "RULE":
                return RULE;
        }
        return null;
    }
}
