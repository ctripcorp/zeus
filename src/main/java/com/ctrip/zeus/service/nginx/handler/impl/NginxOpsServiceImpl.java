package com.ctrip.zeus.service.nginx.handler.impl;

import com.ctrip.zeus.client.LocalClient;
import com.ctrip.zeus.model.entity.DyUpstreamOpsData;
import com.ctrip.zeus.nginx.entity.NginxResponse;
import com.ctrip.zeus.service.nginx.handler.NginxOpsService;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by fanqq on 2016/3/14.
 */
@Service("nginxOpsService")
public class NginxOpsServiceImpl implements NginxOpsService {

    private Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    private final String DEFAULT_NGINX_BIN_DIR = "/opt/app/nginx/sbin";
    private final String SUDO = "sudo ";

    @Override
    public NginxResponse reload() throws Exception {
        long start = System.currentTimeMillis();
        LOGGER.info("[NginxTest] Start Nginx reload");
        try {
            String command = SUDO + DEFAULT_NGINX_BIN_DIR + "/nginx -s reload";
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
            CommandLine commandline = CommandLine.parse(command);

            DefaultExecutor exec = new DefaultExecutor();
            exec.setExitValues(null);

            PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream, errorStream);
            exec.setStreamHandler(streamHandler);

            int exitVal = exec.execute(commandline);
            String out = outputStream.toString("UTF-8");
            String error = errorStream.toString("UTF-8");
            NginxResponse response = new NginxResponse();
            response.setOutMsg(out);
            response.setErrMsg(error);
            response.setSucceed(0 == exitVal);

            return response;
        } catch (IOException e) {
            LOGGER.error("Fail to reload", e);
            throw e;
        } finally {
            long cost = System.currentTimeMillis() - start;
            LOGGER.info("[NginxTest] Finish Nginx reload .Cost:" + cost);
        }
    }

    @Override
    public NginxResponse test() throws Exception {
        long start = System.currentTimeMillis();
        try {
            LOGGER.info("[NginxTest] Start Nginx -t");
            String command = SUDO + DEFAULT_NGINX_BIN_DIR + "/nginx -t";
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
            CommandLine commandline = CommandLine.parse(command);

            DefaultExecutor exec = new DefaultExecutor();
            exec.setExitValues(null);

            PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream, errorStream);
            exec.setStreamHandler(streamHandler);

            int exitVal = exec.execute(commandline);
            String out = outputStream.toString("UTF-8");
            String error = errorStream.toString("UTF-8");

            NginxResponse response = new NginxResponse();
            response.setOutMsg(out);
            response.setErrMsg(error);
            response.setSucceed(0 == exitVal);

            return response;
        } catch (IOException e) {
            LOGGER.error("Test Nginx Conf Failed", e);
            throw e;
        } finally {
            long cost = System.currentTimeMillis() - start;
            LOGGER.info("[NginxTest] Finish Nginx -t.Cost:" + cost);
        }
    }

    @Override
    public List<NginxResponse> dyups(DyUpstreamOpsData[] dyups) throws Exception {
        List<NginxResponse> responses = new ArrayList<>();
        if (dyups == null) return responses;
        long start = System.currentTimeMillis();
        for (DyUpstreamOpsData d : dyups) {
            NginxResponse response;
            try {
                response = LocalClient.getInstance().dyups(d.getUpstreamName(), d.getUpstreamCommands());
                responses.add(response);
            } catch (Exception ex) {
                // retry if get SocketTimeoutException
                //TODO do we tolerate failure
                if (ex.getCause() instanceof java.net.SocketTimeoutException) {
                    try {
                        Thread.sleep(50L);
                    } catch (InterruptedException e) {
                    }
                    response = LocalClient.getInstance().dyups(d.getUpstreamName(), d.getUpstreamCommands());
                    responses.add(response);
                } else {
                    throw ex;
                }
            }
            LOGGER.info("[DyupsOps] Dyups success. upstreamName:" + d.getUpstreamName());
        }
        long cost = System.currentTimeMillis() - start;
        LOGGER.info("[NginxTest] Finish dyupses. Total Cost:" + cost);
        return responses;
    }
}
