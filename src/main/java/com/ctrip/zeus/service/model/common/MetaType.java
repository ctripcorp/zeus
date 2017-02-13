package com.ctrip.zeus.service.model.common;

/**
 * Created by zhoumy on 2017/1/9.
 */
public enum MetaType {
    GROUP(1), VS(2), SLB(3), MEMBER(4), TRAFFIC_POLICY(5);

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
        }
        return null;
    }
}
