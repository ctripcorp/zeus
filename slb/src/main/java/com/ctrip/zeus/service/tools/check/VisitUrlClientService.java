package com.ctrip.zeus.service.tools.check;

import com.ctrip.zeus.model.tools.CheckResponse;

import java.net.ConnectException;
import java.util.Map;

/**
 * Created by ygshen on 2017/2/6.
 */
public interface VisitUrlClientService {
    /**
     * check url belonged to group id
     *
     * @param host
     * @param uri
     * @param timeout
     * @param headers
     * @return Check Response
     * @throws Exception
     */
    CheckResponse visit(String host, String uri, String proxy, int timeout, Map<String,String> headers);

    Map<String,String> visit(String method, String url, Map<String,String> params,Map<String,String> headers, String cookie, String bodyText, String proxy) throws ConnectException;
}
