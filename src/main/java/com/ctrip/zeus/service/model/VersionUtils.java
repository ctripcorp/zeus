package com.ctrip.zeus.service.model;

/**
 * Created by zhoumy on 2015/12/23.
 */
public class VersionUtils {
    public static int getHash(Long id, int version) {
        return id.hashCode() * 31 + version;
    }

    public static int[] getVersionByMode(ModelMode mode, int offline, int online) {
        switch (mode) {
            case MODEL_MODE_ONLINE: {
                return online == 0 ? new int[0] : new int[]{online};
            }
            case MODEL_MODE_REDUNDANT: {
                return online == 0 ? new int[]{offline} : new int[]{offline, online};
            }
            case MODEL_MODE_OFFLINE:
            case MODEL_MODE_MERGE:
            default: {
                return new int[]{offline};
            }
        }
    }
}