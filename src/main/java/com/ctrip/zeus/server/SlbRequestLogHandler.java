package com.ctrip.zeus.server;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.HandlerWrapper;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author:xingchaowang
 * @date: 12/8/2015.
 */

public class SlbRequestLogHandler extends HandlerWrapper {

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        super.handle(target, baseRequest, request, response);
    }
}
