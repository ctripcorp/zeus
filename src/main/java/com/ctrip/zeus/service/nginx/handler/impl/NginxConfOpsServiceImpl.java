package com.ctrip.zeus.service.nginx.handler.impl;

import com.ctrip.zeus.exceptions.NginxProcessingException;
import com.ctrip.zeus.nginx.entity.NginxResponse;
import com.ctrip.zeus.nginx.entity.VsConfData;
import com.ctrip.zeus.service.nginx.handler.NginxConfOpsService;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by fanqq on 2016/3/14.
 */
@Service("nginxConfOpsService")
public class NginxConfOpsServiceImpl implements NginxConfOpsService {

    private Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    private final String DEFAULT_NGINX_CONF_DIR = "/opt/app/nginx/conf";
    private final String CONF_SUFFIX = ".conf";
    private final String NGINX_CONF_FILE = "nginx.conf";
    private final String BACKUP_TYPE_REFRESH = "Refresh";
    private final String BACKUP_TYPE_ROLLBACK = "Rollback";


    @Override
    public Long updateAll(Map<Long, VsConfData> vsConfs) throws Exception {
        String vhostDir = DEFAULT_NGINX_CONF_DIR + File.separator + "vhosts";
        String upstreamDir = DEFAULT_NGINX_CONF_DIR + File.separator + "upstreams";
        File vhostFile = new File(vhostDir);
        String[] vhostFileNames = vhostFile.list();
        File upstreamFile = new File(upstreamDir);
        String[] upstreamFileNames = upstreamFile.list();
        Date now = new Date();

        backupAll(now, BACKUP_TYPE_REFRESH);

        List<String> cleanFileNames = new ArrayList<>();
        List<String> deleteFileNames = new ArrayList<>();
        for (String name : vhostFileNames) {
            if (name.endsWith(CONF_SUFFIX)) {
                cleanFileNames.add(vhostDir + File.separator + name);
            } else {
                deleteFileNames.add(vhostDir + File.separator + name);
            }
        }
        for (String name : upstreamFileNames) {
            if (name.endsWith(CONF_SUFFIX)) {
                cleanFileNames.add(upstreamDir + File.separator + name);
            } else {
                deleteFileNames.add(upstreamDir + File.separator + name);
            }
        }
        try {
            cleanFile(cleanFileNames, now);
            deleteFile(deleteFileNames);
        } catch (Exception e) {
            undoCleanFile(cleanFileNames, now);
            throw e;
        }

        List<Long> sucIds = new ArrayList<>();
        try {
            for (Long vsId : vsConfs.keySet()) {
                writeFile(vhostDir, vsId + CONF_SUFFIX, vsConfs.get(vsId).getVhostConf());
                writeFile(upstreamDir, vsId + CONF_SUFFIX, vsConfs.get(vsId).getUpstreamConf());
                sucIds.add(vsId);
            }
        } catch (Exception e) {
            List<String> deleteFile = new ArrayList<>();
            for (Long vsId : vsConfs.keySet()) {
                deleteFile.add(vhostDir + File.separator + vsId + CONF_SUFFIX);
                deleteFile.add(upstreamDir + File.separator + vsId + CONF_SUFFIX);
            }
            try {
                deleteFile(deleteFile);
                undoCleanFile(cleanFileNames, now);
            } catch (Exception e1) {
                LOGGER.error("[NginxConfOpsUpdateAll]delete file or undo clean file failed. Nginx Config on disk is uncontrollable. Need refresh config files.");
                throw new NginxProcessingException("[NginxConfOpsUpdateAll]delete file or undo clean file failed.", e1);
            }
            LOGGER.error("Vs config updated. But some file write failed. Success updated vsIds: " + sucIds.toString(), e);
            throw e;
        }
        LOGGER.info("Vs config updated. vsId: " + sucIds.toString());
        return now.getTime();
    }

    @Override
    public void undoUpdateAll(Long flag) throws Exception {
        String vhostDir = DEFAULT_NGINX_CONF_DIR + File.separator + "vhosts";
        String upstreamDir = DEFAULT_NGINX_CONF_DIR + File.separator + "upstreams";
        File vhostFile = new File(vhostDir);
        String[] vhostFileNames = vhostFile.list();
        File upstreamFile = new File(upstreamDir);
        String[] upstreamFileNames = upstreamFile.list();
        Date time = new Date(flag);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String endWith = ".bak.clean." + sdf.format(time);
        List<String> undoFileNames = new ArrayList<>();
        List<String> deleteFileNames = new ArrayList<>();
        for (String name : vhostFileNames) {
            if (name.endsWith(CONF_SUFFIX)) {
                deleteFileNames.add(vhostDir + File.separator + name);
            } else if (name.endsWith(endWith)) {
                undoFileNames.add(vhostDir + File.separator + name.replace(endWith, ""));
            }
        }
        for (String name : upstreamFileNames) {
            if (name.endsWith(CONF_SUFFIX)) {
                deleteFileNames.add(upstreamDir + File.separator + name);
            } else if (name.endsWith(endWith)) {
                undoFileNames.add(upstreamDir + File.separator + name.replace(endWith, ""));
            }
        }
        try {
            deleteFile(deleteFileNames);
            undoCleanFile(undoFileNames, time);
        } catch (Exception e) {
            LOGGER.error("[UndoUpdateAll]Failed to delete and undo file.", e);
            throw new NginxProcessingException("[UndoUpdateAll]Failed to delete and undo file.", e);
        }
    }

    private void deleteFile(List<String> deleteFile) {
        File file;
        for (String name : deleteFile) {
            file = new File(name);
            if (file.exists()) {
                if (!file.delete()) {
                    LOGGER.error("[DeleteConfFile] Delete conf file failed. File name:" + name);
                }
            }
        }
    }

    @Override
    public void updateNginxConf(String nginxConf) throws Exception {

        String fileName = DEFAULT_NGINX_CONF_DIR + File.separator + NGINX_CONF_FILE;
        List<String> files = new ArrayList<>();
        files.add(fileName);
        try {
            copyFile(files, null);
        } catch (Exception e) {
            undoCopyFile(files, null);
            throw e;
        }
        try {
            writeFile(DEFAULT_NGINX_CONF_DIR, NGINX_CONF_FILE, nginxConf);
        } catch (Exception e) {
            undoCopyFile(files, null);
            throw e;
        }
    }

    @Override
    public void undoUpdateNginxConf() throws Exception {
        try {
            LOGGER.info("[UndoUpdate] Start undo update nginx conf.");
            String fileName = DEFAULT_NGINX_CONF_DIR + File.separator + NGINX_CONF_FILE;
            List<String> files = new ArrayList<>();
            files.add(fileName);
            undoCopyFile(files, null);
        } catch (Exception e) {
            LOGGER.error("[UndoUpdate]Exception undo update nginx conf exception.", e);
            throw new NginxProcessingException("[UndoUpdate]Nginx conf");
        } finally {
            LOGGER.info("[UndoUpdate] Finish undo update nginx conf.");
        }

    }

    @Override
    public Long cleanAndUpdateConf(Set<Long> cleanVsIds, Map<Long, VsConfData> vsConfs) throws Exception {
        List<String> cleanFileNames = new ArrayList<>();
        Date now = new Date();
        String vhostDir = DEFAULT_NGINX_CONF_DIR + File.separator + "vhosts";
        String upstreamDir = DEFAULT_NGINX_CONF_DIR + File.separator + "upstreams";
        for (Long vsId : cleanVsIds) {
            cleanFileNames.add(vhostDir + File.separator + vsId + CONF_SUFFIX);
            cleanFileNames.add(upstreamDir + File.separator + vsId + CONF_SUFFIX);
        }
        try {
            cleanFile(cleanFileNames, now);
        } catch (Exception e) {
            undoCleanFile(cleanFileNames, now);
            throw e;
        }
        List<Long> sucIds = new ArrayList<>();
        List<String> copyFiles = new ArrayList<>();
        try {
            for (Long vsId : vsConfs.keySet()) {
                copyFiles.add(vhostDir + File.separator + vsId + CONF_SUFFIX);
                copyFiles.add(upstreamDir + File.separator + vsId + CONF_SUFFIX);
            }
            copyFile(copyFiles, now);

            for (Long vsId : vsConfs.keySet()) {
                writeFile(vhostDir, vsId + CONF_SUFFIX, vsConfs.get(vsId).getVhostConf());
                writeFile(upstreamDir, vsId + CONF_SUFFIX, vsConfs.get(vsId).getUpstreamConf());
                sucIds.add(vsId);
            }
        } catch (Exception e) {
            LOGGER.error("[CleanAndUpdateConf]Vs config updated. But some file write failed.Going to undo ops. Suc Ids:" + sucIds.toString(), e);
            try {
                undoCleanFile(cleanFileNames, now);
                undoCopyFile(copyFiles, now);
            } catch (Exception e1) {
                LOGGER.error("[CleanAndUpdateConf] undo clean file or undo copy file failed. config is Uncontrollable. Need refresh configs.Date:" + now.toString());
                throw new NginxProcessingException("[CleanAndUpdateConf] undo clean file or undo copy file failed. config is Uncontrollable. Need refresh configs.Date:" + now.toString(), e);
            }
            throw e;
        }
        LOGGER.info("[CleanAndUpdateConf]Vs config updated. vsId: " + sucIds.toString());
        return now.getTime();
    }

    @Override
    public void undoCleanAndUpdateConf(Set<Long> cleanVsIds, Map<Long, VsConfData> vsConfs, Long timeFlag) throws Exception {
        Date flag = new Date(timeFlag);
        try {
            LOGGER.info("[UndoCleanAndUpdateConf] Start undo clean and update conf.Time flag:" + flag.toString());
            List<String> copyFiles = new ArrayList<>();
            String vhostDir = DEFAULT_NGINX_CONF_DIR + File.separator + "vhosts";
            String upstreamDir = DEFAULT_NGINX_CONF_DIR + File.separator + "upstreams";
            List<String> cleanFileNames = new ArrayList<>();

            backupAll(flag, BACKUP_TYPE_ROLLBACK);

            for (Long vsId : vsConfs.keySet()) {
                copyFiles.add(vhostDir + File.separator + vsId + CONF_SUFFIX);
                copyFiles.add(upstreamDir + File.separator + vsId + CONF_SUFFIX);
            }
            undoCopyFile(copyFiles, flag);

            for (Long vsId : cleanVsIds) {
                cleanFileNames.add(vhostDir + File.separator + vsId + CONF_SUFFIX);
                cleanFileNames.add(upstreamDir + File.separator + vsId + CONF_SUFFIX);
            }
            undoCleanFile(cleanFileNames, flag);
        } catch (Exception e) {
            LOGGER.info("[UndoCleanAndUpdateConf] Failed to undo clean and update conf.Time flag:" + flag.toString(), e);
            throw new NginxProcessingException("[UndoCleanAndUpdateConf] Failed to undo clean and update conf.Time flag:" + flag.toString(), e);
        } finally {
            LOGGER.info("[UndoCleanAndUpdateConf] End undo clean and update conf.Time flag:" + flag.toString());
        }

    }

    private boolean cleanFile(List<String> fileNames, Date date) throws Exception {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
            List<String> sucList = new ArrayList<>();
            List<String> notExist = new ArrayList<>();
            for (String fileName : fileNames) {
                File file = new File(fileName);
                if (file.exists()) {
                    File renameTo = new File(file.getAbsolutePath() + ".bak.clean." + sdf.format(date));
                    file.renameTo(renameTo);
                    sucList.add(fileName);
                } else {
                    notExist.add(fileName);
                }
            }
            LOGGER.info("[CleanFile] Clean Conf Suc.Clean Conf:" + sucList.toString() + " Not Exist: " + notExist.toString());
            return true;
        } catch (Exception e) {
            LOGGER.error("[CleanFile] Fail to clean conf. Files: " + fileNames.toString(), e);
            throw e;
        }
    }

    private boolean undoCleanFile(List<String> fileNames, Date date) throws Exception {
        List<String> sucList = new ArrayList<>();
        List<String> notExist = new ArrayList<>();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");

            for (String fileName : fileNames) {
                File file = new File(fileName);
                File cleanFile = new File(file.getAbsolutePath() + ".bak.clean." + sdf.format(date));
                if (cleanFile.exists()) {
                    if (file.exists()) {
                        if (!file.delete()) {
                            LOGGER.error("[UndoCleanFile] delete file failed. fileName: " + file.getAbsolutePath());
                        }
                    }
                    File renameTo = new File(file.getAbsolutePath());
                    cleanFile.renameTo(renameTo);
                    sucList.add(cleanFile.getAbsolutePath());
                } else {
                    LOGGER.error("[UndoCleanFile] file bak.clean not exist. fileName: " + cleanFile.getAbsolutePath());
                    notExist.add(cleanFile.getAbsolutePath());
                }
            }
            return true;
        } catch (Exception e) {
            LOGGER.error("[UndoCleanFile] Fail to undo clean conf. Files: " + fileNames.toString(), e);
            throw new NginxProcessingException("[UndoCleanFile] Fail to undo clean conf. Files: " + fileNames.toString(), e);
        } finally {
            LOGGER.info("[UndoCleanFile] Undo Clean Conf Suc.Clean Conf:" + sucList.toString() + " Not Exist: " + notExist.toString());
        }
    }

    private boolean copyFile(List<String> fileNames, Date date) throws Exception {
        List<String> sucList = new ArrayList<>();
        List<String> notExist = new ArrayList<>();
        try {
            String dateStr;
            if (date != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
                dateStr = sdf.format(date);
            } else {
                dateStr = "Default";
            }
            String suffix = ".bak.copy." + dateStr;

            for (String fileName : fileNames) {
                File file = new File(fileName);
                if (file.exists()) {
                    File copyTo = new File(file.getAbsolutePath() + suffix);
                    InputStream inStream = new FileInputStream(file);
                    FileOutputStream fs = new FileOutputStream(copyTo);
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = inStream.read(buffer)) != -1) {
                        fs.write(buffer, 0, length);
                    }
                    inStream.close();
                    fs.flush();
                    fs.close();
                    sucList.add(fileName);
                } else {
                    notExist.add(fileName);
                }
            }
            return true;
        } catch (Exception e) {
            LOGGER.error("[CopyFile] Fail to copy conf. Files: " + fileNames.toString() + " Suc Copy:" + sucList.toString(), e);
            throw e;
        } finally {
            LOGGER.info("[CopyFile] Copy Conf Suc.Copy Conf:" + sucList.toString() + " Not Exist: " + notExist.toString());
        }
    }

    private boolean undoCopyFile(List<String> fileNames, Date date) throws Exception {
        List<String> sucList = new ArrayList<>();
        List<String> notExist = new ArrayList<>();
        try {
            String dateStr;
            if (date != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
                dateStr = sdf.format(date);
            } else {
                dateStr = "Default";
            }
            String suffix = ".bak.copy." + dateStr;

            for (String fileName : fileNames) {
                File file = new File(fileName);
                File copyFile = new File(file.getAbsolutePath() + suffix);

                if (copyFile.exists()) {
                    if (file.exists()) {
                        if (!file.delete()) {
                            LOGGER.error("[UndoCopyFile] delete file failed. fileName: " + file.getAbsolutePath());
                        }
                    }
                    File copyTo = new File(file.getAbsolutePath());
                    InputStream inStream = new FileInputStream(copyFile);
                    FileOutputStream fs = new FileOutputStream(copyTo);
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = inStream.read(buffer)) != -1) {
                        fs.write(buffer, 0, length);
                    }
                    inStream.close();
                    fs.flush();
                    fs.close();
                    sucList.add(copyFile.getAbsolutePath());
                } else {
                    LOGGER.error("[UndoCopyFile] bak.copy file not exist. fileName: " + copyFile.getAbsolutePath());
                    notExist.add(copyFile.getAbsolutePath());
                }
            }
            return true;
        } catch (Exception e) {
            LOGGER.error("[UndoCopyFile] Fail to undo copy conf. Files: " + fileNames.toString() + " Suc Copy:" + sucList.toString(), e);
            throw new NginxProcessingException("[UndoCopyFile] Fail to undo copy conf. Files: " + fileNames.toString() + " Suc Copy:" + sucList.toString(), e);
        } finally {
            LOGGER.info("[UndoCopyFile] Undo Copy Conf Finish.Copy Conf:" + sucList.toString() + " Not Exist: " + notExist.toString());
        }
    }

    private synchronized void writeFile(String confPath, String confFile, String confContent) throws IOException {
        makeSurePathExist(confPath);
        Writer writer = null;
        try {
            writer = new FileWriter(new File(confPath + File.separator + confFile));
            writer.write(confContent);
        } catch (Exception e) {
            LOGGER.error("[WriteFile] Write File Failed! Path:" + confPath + File.separator + confFile);
            throw e;
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    private void makeSurePathExist(String confDir) {
        File f = new File(confDir);
        if (!f.exists() || !f.isDirectory()) {
            f.mkdirs();
        }
    }

    private NginxResponse backupAll(Date date, String type) throws Exception {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
            String backupConfDir = DEFAULT_NGINX_CONF_DIR + "/" + type + "Backup/" + sdf.format(date);
            makeSurePathExist(backupConfDir);
            String mvVhostCommand = " cp -r " + DEFAULT_NGINX_CONF_DIR + "/vhosts " + backupConfDir + "/ ";
            String mvUpstreamCommand = " cp -r " + DEFAULT_NGINX_CONF_DIR + "/upstreams " + backupConfDir + "/ ";
            String mvNginxConfCommand = " cp " + DEFAULT_NGINX_CONF_DIR + "/nginx.conf " + backupConfDir + "/ ";
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
            CommandLine commandline = CommandLine.parse(mvVhostCommand);
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

            LOGGER.info("Back Up rollback conf", response.toString());
            if (!response.getSucceed()) {
                LOGGER.error("[NginxConfOpsServiceImpl-BackUpAll] back up vhosts Failed.Response: " + response);
                throw new Exception("Fail to backup server confs. Response:" + response.toString());
            }

            commandline = CommandLine.parse(mvUpstreamCommand);
            exitVal = exec.execute(commandline);
            out = outputStream.toString("UTF-8");
            error = errorStream.toString("UTF-8");
            response = new NginxResponse();
            response.setOutMsg(out);
            response.setErrMsg(error);
            response.setSucceed(0 == exitVal);

            LOGGER.info("Back Up rollback conf", response.toString());
            if (!response.getSucceed()) {
                LOGGER.error("[NginxConfOpsServiceImpl-BackUpAll] back up upstreams Failed.Response: " + response);
                throw new Exception("Fail to backup upstream confs. Response:" + response.toString());
            }

            commandline = CommandLine.parse(mvNginxConfCommand);
            exitVal = exec.execute(commandline);
            out = outputStream.toString("UTF-8");
            error = errorStream.toString("UTF-8");
            response = new NginxResponse();
            response.setOutMsg(out);
            response.setErrMsg(error);
            response.setSucceed(0 == exitVal);

            LOGGER.info("Back Up rollback conf", response.toString());
            if (!response.getSucceed()) {
                LOGGER.error("[NginxConfOpsServiceImpl-BackUpAll] back up nginx.conf Failed.Response: " + response);
                throw new Exception("Fail to backup nginx.conf. Response:" + response.toString());
            }
            return response;
        } catch (IOException e) {
            LOGGER.error("[NginxConfOpsServiceImpl-BackUpAll] Fail to backup conf. Date:" + date.toString(), e);
            throw new Exception("[NginxConfOpsServiceImpl-BackUpAll] Fail to backup conf. Date:" + date.toString(), e);
        }
    }
}
