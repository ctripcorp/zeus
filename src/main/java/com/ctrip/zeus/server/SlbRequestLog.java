package com.ctrip.zeus.server;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.RequestLog;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.component.AbstractLifeCycle;

/**
 * @author:xingchaowang
 * @date: 12/8/2015.
 */
public class SlbRequestLog extends AbstractLifeCycle implements RequestLog{

    @Override
    public void log(Request request, Response response) {
        String uri = request.getRequestURI();

        int status = response.getStatus();
        if (status <= 0) {
            status = 404;
        }

        String slbId = request.getParameter("slbId");
        String groupId = request.getParameter("groupId");
        String vsId = request.getParameter("vsId");

        long now = System.currentTimeMillis();
        long cost = now - request.getTimeStamp();
    }

}
