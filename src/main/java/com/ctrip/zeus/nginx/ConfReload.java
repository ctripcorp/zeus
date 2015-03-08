package com.ctrip.zeus.nginx;

import com.ctrip.zeus.model.entity.Slb;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;

import java.io.ByteArrayOutputStream;

/**
 * @author:xingchaowang
 * @date: 3/8/2015.
 */
public class ConfReload {

    public static String reload(Slb slb) {
        try {

            String command = slb.getNginxBin() + "/nginx -s reload";

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            ByteArrayOutputStream errorStream = new ByteArrayOutputStream();

            CommandLine commandline = CommandLine.parse(command);

            DefaultExecutor exec = new DefaultExecutor();

            exec.setExitValues(null);

            PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream,errorStream);

            exec.setStreamHandler(streamHandler);

            exec.execute(commandline);

            String out = outputStream.toString("gbk");

            String error = errorStream.toString("gbk");

            return out+error;

        } catch (Exception e) {


            return e.toString();

        }

    }
}
