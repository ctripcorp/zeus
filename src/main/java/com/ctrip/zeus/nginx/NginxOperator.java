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
import org.springframework.stereotype.Component;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component("nginxOperator")
public class NginxOperator {
    private static final Logger LOGGER = LoggerFactory.getLogger(NginxOperator.class);

    private static final String DEF_NGINX_CONF = "nginx.conf";
    private static final String CONF_SUFFIX = ".conf";
    private static final String DEFAULT_CONF_DIR = "/opt/app/nginx/conf";
    private static final String DEFAULT_BIN_DIR = "/opt/app/nginx/sbin";
    private String nginxConfDir;
    private String nginxBinDir;


    public NginxOperator(String nginxConfDir, String nginxBinDir){
        this.nginxConfDir = nginxConfDir;
        this.nginxBinDir = nginxBinDir;
    }
    public NginxOperator()
    {
        this.nginxConfDir = DEFAULT_CONF_DIR;
        this.nginxBinDir = DEFAULT_BIN_DIR;
    }

    public NginxOperator init(String nginxConfDir, String nginxBinDir){
        this.nginxConfDir = nginxConfDir;
        this.nginxBinDir = nginxBinDir;
        return this;
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
            String mvVhostCommand = " cp -r "+nginxConfDir+"/vhosts " + rollbackConfDir + "/ ";
            String mvUpstreamCommand = " cp -r "+nginxConfDir+"/upstreams "+ rollbackConfDir + "/ ";
            String mvNginxConfCommand = " cp "+nginxConfDir+"/nginx.conf " + rollbackConfDir + "/ ";
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
            CommandLine commandline = CommandLine.parse(mvVhostCommand);
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
                throw new Exception("Fail to backup rollback server conf. Response:"+response.toString());
            }

            commandline = CommandLine.parse(mvUpstreamCommand);
            exitVal = exec.execute(commandline);
            out = outputStream.toString("UTF-8");
            error = errorStream.toString("UTF-8");
            response = new NginxResponse();
            response.setOutMsg(out);
            response.setErrMsg(error);
            response.setSucceed(0==exitVal);

            LOGGER.info("Back Up rollback conf",response.toString());
            if (!response.getSucceed()){
                throw new Exception("Fail to backup rollback upstream conf. Response:"+response.toString());
            }

            commandline = CommandLine.parse(mvNginxConfCommand);
            exitVal = exec.execute(commandline);
            out = outputStream.toString("UTF-8");
            error = errorStream.toString("UTF-8");
            response = new NginxResponse();
            response.setOutMsg(out);
            response.setErrMsg(error);
            response.setSucceed(0==exitVal);

            LOGGER.info("Back Up rollback conf",response.toString());
            if (!response.getSucceed()){
                throw new Exception("Fail to backup rollback nginx.conf. Response:"+response.toString());
            }
            return response;
        } catch (IOException e) {
            throw new Exception("Fail to backup rollback conf",e);
        }
    }
    public NginxResponse cleanConf(List<Long> vsid) throws IOException{
        try {
            NginxResponse response = new NginxResponse();
            String msg = "";
            List<String> confFileList = new ArrayList<>();
            for (Long vs : vsid)
            {
                confFileList.add(vs+CONF_SUFFIX);
            }
            String vhostDir = nginxConfDir + "/vhosts";
            String upstreamDir = nginxConfDir + "/upstreams";
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
            File vhostFile = new File(vhostDir);
            File [] serverFiles = vhostFile.listFiles();
            if (serverFiles != null){
                msg = "Clean Vhost File:";
                for (File file : serverFiles){
                    if (file.getName().contains(".bak.")){
                        continue;
                    }
                    if (!confFileList.contains(file.getName())){
                        File renameTo = new File(file.getAbsolutePath()+".bak."+sdf.format(new Date()));
                        file.renameTo(renameTo);
                        msg += " " + file.getName();
                    }
                }
            }

            File upstreamFile = new File(upstreamDir);

            File [] upstreamFiles = upstreamFile.listFiles();
            if (upstreamFiles != null){
                msg += "\nClean Upstream File:";
                for (File file : upstreamFiles){
                    if (file.getName().contains(".bak.")){
                        continue;
                    }
                    if (!confFileList.contains(file.getName())){
                        File renameTo = new File(file.getAbsolutePath()+".bak."+sdf.format(new Date()));
                        file.renameTo(renameTo);
                        msg += " " + file.getName();
                    }
                }
            }

            response.setSucceed(true);
            response.setOutMsg(msg);
            LOGGER.info("Clean Conf Response:"+response.toString());
            return response;
        } catch (Exception e) {
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


