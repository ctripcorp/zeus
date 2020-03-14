package com.ctrip.zeus.service.tools.check;

import com.ctrip.zeus.model.tools.CheckResponse;
import com.ctrip.zeus.model.tools.CheckTarget;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Created by ygshen on 2016/12/15.
 */
public interface CheckClientService {

    /**
     * check group server health status
     *
     * @param targets
     * @param timeOut
     * @return List of Check Response
     * @throws Exception
     */
    Map<CheckTarget, CheckResponse> checkUrl(List<CheckTarget> targets, int timeOut) throws ExecutionException, InterruptedException;
}
