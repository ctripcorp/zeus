package com.ctrip.zeus.nginx.impl;

import com.ctrip.zeus.nginx.LocalValidate;
import com.ctrip.zeus.nginx.entity.NginxResponse;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;

/**
 * Created by fanqq on 2015/6/25.
 */
@Component("localValidate")
public class LocalValidateImpl implements LocalValidate{

    @Override
    public boolean pathExistValidate(String path , boolean isDirs) throws Exception {
        File pathFile = new File(path);
        if (pathFile.isDirectory()==isDirs)
        {
            return pathFile.exists();
        }else
        {
            return false;
        }
    }

    @Override
    public boolean nginxIsUp(String nginxBinPath) throws Exception {
        File pidFile = new File(nginxBinPath+"/../logs/nginx.pid");
        if (pidFile.exists()&&pidFile.isFile())
        {
            FileReader fr = new FileReader(pidFile);

        }else {
            return false;
        }
        String command = nginxBinDir + "/nginx -t";
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
        CommandLine commandline = CommandLine.parse(command);

        DefaultExecutor exec = new DefaultExecutor();
        exec.setExitValues(null);

        PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream,errorStream);
        exec.setStreamHandler(streamHandler);

        int exitVal = exec.execute(commandline);
        String out = outputStream.toString("UTF-8");
        String error = errorStream.toString("UTF-8");

        NginxResponse response = new NginxResponse();
        response.setOutMsg(out);
        response.setErrMsg(error);
        response.setSucceed(0==exitVal);

        return response;
        return false;
    }
}
