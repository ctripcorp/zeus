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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
    public NginxResponse rollbackBackupConf()throws Exception{
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
            String rollbackConfDir  = nginxConfDir + "/Rollbacks/" + sdf.format(new Date());
            makeSurePathExist(rollbackConfDir);
            String mvVhostCommand = " mv "+nginxConfDir+"/vhosts " + rollbackConfDir + "/ ;";
            String mvUpstreamCommand = " mv "+nginxConfDir+"/upstreams "+ rollbackConfDir + "/ ;";
            String mvNginxConfCommand = " mv "+nginxConfDir+"/nginx.conf" + rollbackConfDir + "/ ;";
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
            CommandLine commandline = CommandLine.parse(mvVhostCommand+mvUpstreamCommand+mvNginxConfCommand);
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

            LOGGER.info("Back Up rollback conf",response.toString());
            if (!response.getSucceed()){
                throw new Exception("Fail to backup rollback conf. Response:"+response.toString());
            }
            return response;
        } catch (IOException e) {
            throw new Exception("Fail to backup rollback conf",e);
        }
    }
    public NginxResponse cleanConf(List<Long> vsid) throws IOException{
        try {
            List<String> confFileList = new ArrayList<>();
            for (Long vs : vsid)
            {
                confFileList.add(vs+CONF_SUFFIX);
            }
            String cleanVhostCommand = " ls "+nginxConfDir+"/vhosts";
            String cleanUpstreamCommand = " ls "+nginxConfDir+"/upstreams";
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
            CommandLine commandline = CommandLine.parse(cleanVhostCommand);
            DefaultExecutor exec = new DefaultExecutor();
            exec.setExitValues(null);
            PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream,errorStream);
            exec.setStreamHandler(streamHandler);

            //vhost ls command
            int exitVal = exec.execute(commandline);
            String out = outputStream.toString("UTF-8");
            String error = errorStream.toString("UTF-8");
            NginxResponse response = new NginxResponse();
            response.setOutMsg(out);
            response.setErrMsg(error);
            response.setSucceed(0==exitVal);

            //vhost rm command
            if (response.getSucceed())
            {
                String[] lsObj = out.split("\n");
                StringBuilder sb = new StringBuilder(128);
                sb.append("rm ");
                for (String rm : lsObj)
                {
                    if (!confFileList.contains(rm.trim()))
                    {
                        sb.append(nginxConfDir).append("/vhosts/").append(rm).append(" ");
                    }
                }
                if (!sb.toString().trim().equals("rm")){
                    commandline = CommandLine.parse(sb.toString());
                    exitVal = exec.execute(commandline);
                    out = outputStream.toString("UTF-8");
                    error = errorStream.toString("UTF-8");
                    response.setOutMsg(out);
                    response.setErrMsg(error);
                    response.setSucceed(0==exitVal);
                }
            }

            //upstream ls command
            commandline = CommandLine.parse(cleanUpstreamCommand);
            exitVal = exec.execute(commandline);
            out = outputStream.toString("UTF-8");
            error = errorStream.toString("UTF-8");
            NginxResponse upstreamResponse = new NginxResponse();
            upstreamResponse.setOutMsg(out);
            upstreamResponse.setErrMsg(error);
            upstreamResponse.setSucceed(0==exitVal);
            //upstream rm command
            if (upstreamResponse.getSucceed())
            {
                String[] lsObj = out.split("\n");
                StringBuilder sb = new StringBuilder(128);
                sb.append("rm ");
                for (String rm : lsObj)
                {
                    if (!confFileList.contains(rm.trim()))
                    {
                        sb.append(nginxConfDir).append("/upstreams/").append(rm).append(" ");
                    }
                }
                if (!sb.toString().trim().equals("rm")){
                    commandline = CommandLine.parse(sb.toString());
                    exitVal = exec.execute(commandline);
                    out = outputStream.toString("UTF-8");
                    error = errorStream.toString("UTF-8");
                    upstreamResponse.setOutMsg(out);
                    upstreamResponse.setErrMsg(error);
                    upstreamResponse.setSucceed(0==exitVal);
                }
            }
            LOGGER.info(response.toString());
            LOGGER.info(upstreamResponse.toString());
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


