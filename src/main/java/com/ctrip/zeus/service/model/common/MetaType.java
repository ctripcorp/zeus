package com.ctrip.zeus.service.model.common;

/**
 * Created by zhoumy on 2017/1/9.
 */
public enum MetaType {
    GROUP(1), VS(2), SLB(3);

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
        }
        return null;
    }
}
