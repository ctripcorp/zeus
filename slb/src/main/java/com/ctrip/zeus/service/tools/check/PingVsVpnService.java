package com.ctrip.zeus.service.tools.check;

import com.ctrip.zeus.model.tools.VsPing;
import com.ctrip.zeus.model.tools.VsPingList;

import java.util.Map;
import java.util.concurrent.ExecutionException;

public interface PingVsVpnService {

    Map<String, VsPing> pingVses(VsPingList vses, int timeOut) throws ExecutionException, InterruptedException;
}
