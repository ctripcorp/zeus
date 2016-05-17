package com.ctrip.zeus.service.nginx.handler.impl;

import com.ctrip.zeus.exceptions.NginxProcessingException;
import com.ctrip.zeus.nginx.entity.ConfFile;
import com.ctrip.zeus.nginx.entity.NginxConfEntry;
import com.ctrip.zeus.nginx.entity.NginxResponse;
import com.ctrip.zeus.service.nginx.handler.NginxConfOpsService;
import com.ctrip.zeus.service.nginx.impl.FileOpRecord;
import com.google.common.base.Joiner;
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
    public Long updateAll(NginxConfEntry entry) throws Exception {
        String vhostDir = DEFAULT_NGINX_CONF_DIR + File.separator + "vhosts";
        String upstreamDir = DEFAULT_NGINX_CONF_DIR + File.separator + "upstreams";
        File vhostFile = makeSurePathExist(vhostDir);
        File upstreamFile = makeSurePathExist(upstreamDir);

        Date now = new Date();
        backupAll(now, BACKUP_TYPE_REFRESH);

        LOGGER.info("[NginxConfUpdateAll] Cleanse and backup files under dir upstreams and vhosts on disk.");
        List<String> cleansingFilenames = new ArrayList<>();
        List<String> trashFilenames = new ArrayList<>();
        for (String name : vhostFile.list()) {
            if (name.endsWith(CONF_SUFFIX)) {
                cleansingFilenames.add(vhostDir + File.separator + name);
            } else {
                trashFilenames.add(vhostDir + File.separator + name);
            }
        }
        for (String name : upstreamFile.list()) {
            if (name.endsWith(CONF_SUFFIX)) {
                cleansingFilenames.add(upstreamDir + File.separator + name);
            } else {
                trashFilenames.add(upstreamDir + File.separator + name);
            }
        }
        try {
            cleanFile(cleansingFilenames, now);
            deleteFile(trashFilenames);
        } catch (Exception e) {
            undoCleanFile(cleansingFilenames, now);
            throw e;
        }

        LOGGER.info("[NginxConfUpdateAll] Start to overwrite complete confs under dir upstreams and vhosts.");
        List<String> writingVhostFiles = new ArrayList<>();
        List<String> writingUpstreamFiles = new ArrayList<>();
        try {
            for (ConfFile cf : entry.getVhosts().getFiles()) {
                writeFile(vhostDir, cf.getName() + CONF_SUFFIX, cf.getContent());
                writingVhostFiles.add(vhostDir + File.separator + cf.getName() + CONF_SUFFIX);
            }
            for (ConfFile cf : entry.getUpstreams().getFiles()) {
                writeFile(upstreamDir, cf.getName() + CONF_SUFFIX, cf.getContent());
                writingUpstreamFiles.add(upstreamDir + File.separator + cf.getName() + CONF_SUFFIX);
            }
        } catch (Exception e) {
            LOGGER.error("Updating upstream/vhost conf failed. Proceeding files stops at \n"
                    + Joiner.on(",").join(writingVhostFiles) + "\n"
                    + Joiner.on(",").join(writingUpstreamFiles) + ".", e);
            try {
                deleteFile(writingVhostFiles);
                deleteFile(writingUpstreamFiles);
                undoCleanFile(cleansingFilenames, now);
            } catch (Exception e1) {
                String err = "[NginxConfUpdateAll] Delete or revert conf files failed. Broken confs on disk. Refreshing is required.";
                LOGGER.error(err);
                throw new NginxProcessingException(err, e1);
            }
            throw e;
        }

        LOGGER.info("[NginxConfUpdateAll] Successfuly updated conf files: \n"
                + Joiner.on(",").join(writingVhostFiles) + "\n"
                + Joiner.on(",").join(writingUpstreamFiles) + ".");
        return now.getTime();
    }

    @Override
    public void undoUpdateAll(Long flag) throws Exception {
        String vhostDir = DEFAULT_NGINX_CONF_DIR + File.separator + "vhosts";
        String upstreamDir = DEFAULT_NGINX_CONF_DIR + File.separator + "upstreams";
        File vhostFile = makeSurePathExist(vhostDir);
        String[] vhostFileNames = vhostFile.list();
        File upstreamFile = makeSurePathExist(upstreamDir);
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
            LOGGER.error("[UndoUpdateAll] Failed to delete and revert files on disk.", e);
            throw new NginxProcessingException("[UndoUpdateAll] Failed to delete and revert files on disk.", e);
        }
    }

    private void deleteFile(List<String> deleteFile) {
        File file;
        for (String name : deleteFile) {
            file = new File(name);
            if (file.exists() && file.isFile()) {
                if (!file.delete()) {
                    LOGGER.error("[DeleteConfFile] Delete conf file failed. Filename:" + name);
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
    public FileOpRecord cleanAndUpdateConf(Set<Long> cleanVsIds, Set<Long> updateVsIds, NginxConfEntry entry) throws Exception {
        FileOpRecord record = new FileOpRecord();
        String vhostDir = DEFAULT_NGINX_CONF_DIR + File.separator + "vhosts";
        String upstreamDir = DEFAULT_NGINX_CONF_DIR + File.separator + "upstreams";

        File vhostFile = makeSurePathExist(vhostDir);
        String[] vhostFileNames = vhostFile.list();
        File upstreamFile = makeSurePathExist(upstreamDir);
        String[] upstreamFileNames = upstreamFile.list();

        LOGGER.info("[NginxConfPartialUpdate] Cleanse trash files under dir upstreams and vhosts.");
        List<String> trashFilenames = new ArrayList<>();
        for (String name : vhostFileNames) {
            if (!name.endsWith(CONF_SUFFIX)) {
                trashFilenames.add(vhostDir + File.separator + name);
            }
        }
        for (String name : upstreamFileNames) {
            if (!name.endsWith(CONF_SUFFIX)) {
                trashFilenames.add(upstreamDir + File.separator + name);
            }
        }
        try {
            deleteFile(trashFilenames);
        } catch (Exception e) {
            LOGGER.error("[NginxConfPartialUpdate] Cleansing trash files failed.", e);
        }

        // mark files to be cleansed(.clean/.bak)
        // extract filename if contains related vs ids
        Set<String> nextRelatedUpstreams = new HashSet<>();
        for (ConfFile cf : entry.getUpstreams().getFiles()) {
            nextRelatedUpstreams.add(cf.getName());
        }
        Set<String> currentRelatedUpstreams = new HashSet<>();
        for (String upfn : upstreamFile.list()) {
            int idx = upfn.indexOf(CONF_SUFFIX);
            if (idx == -1) continue;

            upfn = upfn.substring(0, idx);
            String[] fn = upfn.split("_");
            boolean add = false;
            for (String relatedVsId : fn) {
                if (relatedVsId.isEmpty()) continue;

                Long vsId = 0L;
                try {
                    vsId = Long.parseLong(relatedVsId);
                } catch (NumberFormatException ex) {
                    LOGGER.warn("[NginxConfPartialUpdate] Unable to extract vs id information from upstream file: " + upfn + ".");
                    break;
                }
                if (updateVsIds.contains(vsId) || cleanVsIds.contains(vsId)) {
                    if (!add) add = true;
                }
            }
            if (add) {
                currentRelatedUpstreams.add(upfn);
            }
        }

        LOGGER.info("[NginxConfPartialUpdate] Backup related files under dir upstreams and vhosts on disk.");
        Set<String> cleansingUpstreams = new HashSet<>(currentRelatedUpstreams);
        cleansingUpstreams.removeAll(nextRelatedUpstreams);

        Set<String> backupUpstreams = new HashSet<>(currentRelatedUpstreams);
        backupUpstreams.retainAll(nextRelatedUpstreams);

        List<String> cleansingFilenames = new ArrayList<>();
        List<String> backingupFilenames = new ArrayList<>();
        for (Long vsId : cleanVsIds) {
            cleansingFilenames.add(vhostDir + File.separator + vsId + CONF_SUFFIX);
        }
        for (String fn : cleansingUpstreams) {
            cleansingFilenames.add(upstreamDir + File.separator + fn + CONF_SUFFIX);
        }

        try {
            cleanFile(cleansingFilenames, record.getTimeStamp());
        } catch (Exception e) {
            undoCleanFile(cleansingFilenames, record.getTimeStamp());
            throw e;
        }

        for (ConfFile cf : entry.getVhosts().getFiles()) {
            backingupFilenames.add(vhostDir + File.separator + cf.getName() + CONF_SUFFIX);
        }
        for (String fn : backupUpstreams) {
            backingupFilenames.add(upstreamDir + File.separator + fn + CONF_SUFFIX);
        }

        try {
            copyFile(backingupFilenames, record.getTimeStamp());
        } catch (Exception e) {
            undoCopyFile(backingupFilenames, record.getTimeStamp());
            throw e;
        }

        LOGGER.info("[NginxConfPartialUpdate] Start to write confs of related vses under dir upstreams and vhosts.");
        List<String> writingVhostFiles = new ArrayList<>();
        List<String> writingUpstreamFiles = new ArrayList<>();
        try {
            for (ConfFile cf : entry.getVhosts().getFiles()) {
                writeFile(vhostDir, cf.getName() + CONF_SUFFIX, cf.getContent());
                writingVhostFiles.add(vhostDir + File.separator + cf.getName() + CONF_SUFFIX);
            }
            for (ConfFile cf : entry.getUpstreams().getFiles()) {
                writeFile(upstreamDir, cf.getName() + CONF_SUFFIX, cf.getContent());
                writingUpstreamFiles.add(upstreamDir + File.separator + cf.getName() + CONF_SUFFIX);
            }
            record.setWrittenFilename(new ArrayList<String>());
            record.getWrittenFilename().addAll(writingVhostFiles);
            record.getWrittenFilename().addAll(writingUpstreamFiles);
        } catch (Exception e) {
            LOGGER.error("Updating upstream/vhost conf failed. Proceeding files stops at \n"
                    + Joiner.on(",").join(writingVhostFiles) + "\n"
                    + Joiner.on(",").join(writingUpstreamFiles) + ".", e);
            try {
                deleteFile(writingVhostFiles);
                deleteFile(writingUpstreamFiles);
                undoCleanFile(cleansingFilenames, record.getTimeStamp());
                undoCopyFile(backingupFilenames, record.getTimeStamp());
            } catch (Exception e1) {
                String err = "[NginxConfPartialUpdate] Delete or revert conf files failed. Broken confs on disk. Refreshing is required. Maker - " + record.getTimeStamp().toString();
                LOGGER.error(err);
                throw new NginxProcessingException(err, e1);
            }
            throw e;
        }
        LOGGER.info("Successfuly updated conf files: \n"
                + Joiner.on(",").join(writingVhostFiles) + "\n"
                + Joiner.on(",").join(writingUpstreamFiles) + ".");

        record.setCleansedFilename(cleansingFilenames);
        record.setCopiedFilename(backingupFilenames);
        return record;
    }

    @Override
    public void undoCleanAndUpdateConf(Set<Long> cleanVsIds, NginxConfEntry entry, FileOpRecord record) throws Exception {
        try {
            LOGGER.info("[UndoNginxConfPartialUpdate] Start to revert related conf files to the previous version. Maker - " + record.getTimeStamp().toString());
            backupAll(record.getTimeStamp(), BACKUP_TYPE_ROLLBACK);
            deleteFile(record.getWrittenFilename());
            undoCopyFile(record.getCopiedFilename(), record.getTimeStamp());
            undoCleanFile(record.getCleansedFilename(), record.getTimeStamp());
        } catch (Exception e) {
            String err = "[UndoNginxConfPartialUpdate] Failed to revert related conf files to the previous version. Maker - " + record.getTimeStamp().toString();
            LOGGER.error(err, e);
            throw new NginxProcessingException(err, e);
        } finally {
            LOGGER.info("[UndoNginxConfPartialUpdate] Successfully reverted related conf files to the previous version.");
        }

    }

    private boolean cleanFile(List<String> fileNames, Date date) throws Exception {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
            List<String> sucList = new ArrayList<>();
            List<String> notExist = new ArrayList<>();
            for (String fileName : fileNames) {
                File file = new File(fileName);
                if (file.exists() && file.isFile()) {
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
                if (cleanFile.exists() && cleanFile.isFile()) {
                    if (file.exists() && file.isFile()) {
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
                if (file.exists() && file.isFile()) {
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

                if (copyFile.exists() && copyFile.isFile()) {
                    if (file.exists() && file.isFile()) {
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

    private File makeSurePathExist(String confDir) {
        File f = new File(confDir);
        if (!f.exists() || !f.isDirectory()) {
            f.mkdirs();
        }
        return f;
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
