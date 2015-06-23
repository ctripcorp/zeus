package com.ctrip.zeus.nginx;

import com.ctrip.zeus.client.LocalClient;
import com.ctrip.zeus.nginx.entity.Nginx;
import com.ctrip.zeus.nginx.entity.NginxResponse;
import com.ctrip.zeus.nginx.entity.NginxServerStatus;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;

public class NginxOperator {
    private static final Logger LOGGER = LoggerFactory.getLogger(NginxOperator.class);

    private static final String DEF_NGINX_CONF = "nginx.conf";
    private static final String CONF_SUFFIX = ".conf";
    private String nginxConfDir;
    private String nginxBinDir;



    public NginxOperator(String nginxConfDir, String nginxBinDir){
        this.nginxConfDir = nginxConfDir;
        this.nginxBinDir = nginxBinDir;
    }
    public NginxOperator()
    {
        this.nginxConfDir = null;
        this.nginxBinDir = null;
    }

    public void writeNginxConf(String conf) throws IOException {
        doWriteConf(nginxConfDir, DEF_NGINX_CONF,conf);
    }

    public void writeServerConf(Long vsId, String conf) throws IOException {
        String fileName =  String.valueOf(vsId) + CONF_SUFFIX;
        doWriteConf(nginxConfDir + "/vhosts", fileName,conf);
    }


    public void writeUpstreamsConf(Long vsId, String conf) throws IOException {
        String fileName = vsId + CONF_SUFFIX;
        doWriteConf(nginxConfDir + "/upstreams", fileName, conf);
    }

    public NginxResponse dyupsLocal(String upsname,String upsdata) throws IOException {
        return LocalClient.getInstance().dyups(upsname,upsdata);
    }

    public NginxResponse reloadConfTest()throws IOException{
        try {
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
        } catch (IOException e) {
            LOGGER.error("Test Nginx Conf Failed",e);
            throw e;
        }
    }
    public NginxResponse cleanConf(List<Long> vsid) throws IOException{
        try {
            StringBuilder sb = new StringBuilder(128);
            sb.append("'");
            for (Long vs : vsid)
            {
                 sb.append(vs).append(".conf\\|");
            }
            sb.append("'");

            String cleanVhostCommand = "sudo ls "+nginxConfDir+"/vhosts | grep -vw "+sb.toString()+"|sed \"s:^:"+nginxConfDir+"/vhosts/: \" | xargs sudo rm ";
            String cleanUpstreamCommand = "sudo ls "+nginxConfDir+"/upstreams | grep -vw "+sb.toString()+"|sed \"s:^:"+nginxConfDir+"/upstreams/: \" | xargs sudo rm ";

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
            CommandLine commandline = CommandLine.parse(cleanVhostCommand+";"+cleanUpstreamCommand);

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
        } catch (IOException e) {
            LOGGER.error("Fail to clean conf",e);
            throw e;
        }
    }
    public NginxResponse reloadConf() throws IOException{
        try {
            String command = nginxBinDir + "/nginx -s reload";
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
        } catch (IOException e) {
            LOGGER.error("Fail to reload",e);
            throw e;
        }
    }

    public NginxServerStatus getRuntimeStatus() {
        NginxServerStatus status = new NginxServerStatus();
        return status;
    }


    private void doWriteConf(String confPath, String confFile, String confContent) throws IOException {
        makeSurePathExist(confPath);
        Writer writer = null;
        try {
            writer = new FileWriter(new File(confPath + File.separator + confFile));
            writer.write(confContent);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    private void makeSurePathExist(String confDir) {
        File f = new File(confDir);
        f.mkdirs();
    }

}


