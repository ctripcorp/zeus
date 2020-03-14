package com.ctrip.zeus.util;


/**
 * Created by zhoumy on 2016/6/15.
 */


/**
 * To reduce extra computation of collecting local server infomation.
 */
public enum LocalInfoPack {
    INSTANCE;
    String serverIp;

    LocalInfoPack() {
        int counter = 0;
        while (!isValid(S.getIp()) && counter < 10) {
            counter++;
            try {
                Thread.sleep(500L);
            } catch (InterruptedException e) {
            }
        }
        serverIp = S.getIp();
    }

    private boolean isValid(String ip) {
        return !ip.startsWith("127.0.0");
    }

    public String getIp() {
        return serverIp;
    }
}
