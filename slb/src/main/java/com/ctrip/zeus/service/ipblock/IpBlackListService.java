package com.ctrip.zeus.service.ipblock;

import java.util.Map;

public interface IpBlackListService {
    void setLocalIpBlackList(String data) throws Exception;

    Map<String, Boolean> setIpBlackList(BlackIpListEntity entity) throws Exception;

}
