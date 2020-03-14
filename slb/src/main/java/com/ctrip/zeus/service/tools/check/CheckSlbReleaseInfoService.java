package com.ctrip.zeus.service.tools.check;

import com.ctrip.zeus.model.tools.CheckSlbreleaseResponse;
import com.ctrip.zeus.model.tools.CheckTargetList;

import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Created by ygshen on 2017/6/23.
 */
public interface CheckSlbReleaseInfoService {
    Map<String, CheckSlbreleaseResponse> checkSlbReleaseInfo(CheckTargetList targets, int timeOut) throws ExecutionException, InterruptedException;

}
