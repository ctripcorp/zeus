package com.ctrip.zeus.service.tools.check.impl;

import com.ctrip.zeus.model.tools.CheckResponse;
import com.ctrip.zeus.restful.resource.tools.CheckerClient;
import com.ctrip.zeus.service.tools.check.VisitUrlClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.ConnectException;
import java.util.Map;

/**
 * Created by ygshen on 2017/2/6.
 */
@Service("visitUrlClientService")
public class VisitUrlClientServiceImpl implements VisitUrlClientService {
    private final Logger logger = LoggerFactory.getLogger(VisitUrlClientServiceImpl.class);

    @Override
    public CheckResponse visit(String host, String uri, String proxy, int timeout, Map<String, String> headers) {
        try {
            String url = host+uri;
            return CheckerClient.visit(url, proxy, headers);
        } catch (Exception ex) {
            logger.error(String.format("Failed to visit target url {0}{1}", host,uri));
            return null;
        }
    }

    public Map<String, String> visit(String method, String url, Map<String, String> params,Map<String, String> headers, String cookie, String bodyText, String proxy) throws ConnectException {
        try {
            return CheckerClient.visit(method, url, params, headers, cookie, bodyText, proxy);
        } catch (Exception ex) {
            logger.error(String.format("Failed to visit target url %s", url));
            throw new ConnectException(String.format("Failed to connection %s. Excpetion message is: %s",url,ex.getMessage()));
        }
    }
}
