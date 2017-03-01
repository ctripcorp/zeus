package com.ctrip.zeus.service.model.common;

/**
 * Created by zhoumy on 2017/1/9.
 */
public enum RulePhase {
    HTTP_INIT_BY_LUA(5),
    LOC_BEFORE_REWRITE(1),
    LOC_AFTER_REWRITE(2),
    LOC_BEFORE_DYUP(3),
    LOC_AFTER_DYUP(4);

    private int id;

    RulePhase(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static RulePhase getRulePhase(int id) {
        switch (id) {
            case 1:
                return LOC_BEFORE_REWRITE;
            case 2:
                return LOC_AFTER_REWRITE;
            case 3:
                return LOC_BEFORE_DYUP;
            case 4:
                return LOC_AFTER_DYUP;
            case 5:
                return HTTP_INIT_BY_LUA;
        }
        return null;
    }
}
