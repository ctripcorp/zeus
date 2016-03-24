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
@Service("ninxOpsService")
public class NginxOpsServiceImpl implements NginxOpsService {

    private Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    private final String DEFAULT_NGINX_BIN_DIR = "/opt/app/nginx/sbin";

    @Override
    public NginxResponse reload() throws Exception {
        try {
            String command = DEFAULT_NGINX_BIN_DIR + "/nginx -s reload";
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
        }
    }

    @Override
    public NginxResponse test() throws Exception {
        try {
            String command = DEFAULT_NGINX_BIN_DIR + "/nginx -t";
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
        }
    }

    @Override
    public List<NginxResponse> dyups(DyUpstreamOpsData[] dyups) throws Exception {
        List<NginxResponse> responses = new ArrayList<>();
        if (dyups == null) return responses;
        for (DyUpstreamOpsData d : dyups) {
            NginxResponse response = LocalClient.getInstance().dyups(d.getUpstreamName(), d.getUpstreamCommands());
            responses.add(response);
            LOGGER.info("[DyupsOps] Dyups success. upstreamName:" + d.getUpstreamName());
        }
        return responses;
    }
}
