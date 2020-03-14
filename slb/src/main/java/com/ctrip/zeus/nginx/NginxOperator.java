package com.ctrip.zeus.nginx;

import com.ctrip.zeus.model.nginx.NginxResponse;
import com.netflix.config.DynamicBooleanProperty;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.*;

@Component("nginxOperator")
public class NginxOperator {
    private static final Logger LOGGER = LoggerFactory.getLogger(NginxOperator.class);

    private static DynamicStringProperty wafRoleDir = DynamicPropertyFactory.getInstance().getStringProperty("waf.role.dir", "/opt/app/nginx/conf/waf/conf");
    private static DynamicBooleanProperty wafDeleteInstallDirEnable = DynamicPropertyFactory.getInstance().getBooleanProperty("waf.delete.install.dir.enable", true);


    public void writeWafRuleFile(String fileName, String content) throws Exception {
        File dir = new File(wafRoleDir.get());
        File dirBackup = new File(wafRoleDir.get() + "_bk");

        if (!dir.exists()) {
            dir.mkdir();
        }
        if (!dirBackup.exists()) {
            dirBackup.mkdir();
        }

        File file = new File(wafRoleDir.get() + "/" + fileName);
        if (file.exists()) {
            File fileBk = new File(wafRoleDir.get() + "_bk/" + fileName);
            copyFile(file, fileBk);
        }
        writeFile(file, content);
    }

    public NginxResponse writeWafMainFile(String installDir, byte[] content) throws Exception {
        try {
            String fileName = installDir + "/waf.tar";
            File file = new File(fileName);
            File fileBackup = new File(fileName + "_bk");
            if (file.exists()) {
                copyFile(file, fileBackup);
            }
            writeFile(file, content);

            File dir = new File(installDir);
            if (wafDeleteInstallDirEnable.get() && dir.exists() && dir.isDirectory()) {
                try {
                    String command = "sudo mv " + installDir + "/waf " + installDir + "/waf_bk";
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
                } catch (Exception e) {
                    LOGGER.warn("Try to back up waf install dir failed.", e);
                }
            }

            String command = "tar xf " + fileName + " -C " + installDir;
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
            LOGGER.error("Fail update waf main file.", e);
            throw e;
        }

    }

    private void writeFile(File file, String content) throws Exception {
        Writer writer = null;
        try {
            writer = new FileWriter(file);
            writer.write(content);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    private void writeFile(File file, byte[] content) throws Exception {
        FileOutputStream writer = null;
        try {
            writer = new FileOutputStream(file);
            writer.write(content);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    private void copyFile(File f1, File f2) throws Exception {
        int length = 1024;
        FileInputStream in = new FileInputStream(f1);
        FileOutputStream out = new FileOutputStream(f2);
        byte[] buffer = new byte[length];
        while (true) {
            int ins = in.read(buffer);
            if (ins == -1) {
                in.close();
                out.flush();
                out.close();
                return;
            } else {
                out.write(buffer, 0, ins);
            }
        }
    }
}


