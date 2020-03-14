package com.ctrip.zeus.service.nginx.handler;

import com.ctrip.zeus.model.model.DyUpstreamOpsData;
import com.ctrip.zeus.model.nginx.NginxResponse;

import java.util.List;

/**
 * Created by fanqq on 2016/3/14.
 */
public interface NginxOpsService {
    /**
     * execute nginx reload command
     *
     * @return NginxResponse
     * @throws Exception
     */
    NginxResponse reload() throws Exception;

    /**
     * execute nginx test command
     *
     * @return NginxResponse
     * @throws Exception
     */
    NginxResponse test() throws Exception;

    /**
     * execute nginx dyups command
     *
     * @return List<NginxResponse>
     * @throws Exception
     */
    List<NginxResponse> dyups(DyUpstreamOpsData[] dyups) throws Exception;

}
