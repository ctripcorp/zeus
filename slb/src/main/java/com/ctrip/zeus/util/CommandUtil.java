package com.ctrip.zeus.util;

import com.ctrip.zeus.model.nginx.NginxResponse;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;

public class CommandUtil {
    private static Logger logger = LoggerFactory.getLogger(CommandUtil.class);

    public static NginxResponse execute(String command) {
        try {
            if (command == null) {
                return null;
            }
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
            logger.info("Execute Command Success.Command:" + command);
            return response;
        } catch (Exception e) {
            logger.error("Execute Command Error.Command:" + command, e);
            return null;
        }
    }
}
