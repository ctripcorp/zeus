package com.ctrip.zeus.service.model;

/**
 * Created by zhoumy on 2015/12/23.
 */
public class VersionUtils {
    public static int getHash(Long id, int version) {
        return id.hashCode() * 31 + version;
    }

    public static int[] getVersionByMode(SelectionMode mode, int offline, int online) {
        switch (mode) {
            case ONLINE_EXCLUSIVE: {
                return online == 0 ? new int[0] : new int[]{online};
            }
            case OFFLINE_EXCLUSIVE: {
                return online == offline ? new int[0] : new int[]{offline};
            }
            case REDUNDANT: {
                return online == 0 ? new int[]{offline} : new int[]{offline, online};
            }
            case ONLINE_FIRST: {
                return online == 0 ? new int[]{offline} : new int[]{online};
            }
            case OFFLINE_FIRST:
            default: {
                return new int[]{offline};
            }
        }
    }
}