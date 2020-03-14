package com.ctrip.zeus.service.nginx.handler.impl;

import com.ctrip.zeus.exceptions.NginxProcessingException;
import com.ctrip.zeus.model.nginx.ConfFile;
import com.ctrip.zeus.model.nginx.NginxConfEntry;
import com.ctrip.zeus.model.nginx.NginxResponse;
import com.ctrip.zeus.service.build.ConfigHandler;
import com.ctrip.zeus.service.nginx.handler.NginxConfOpsService;
import com.ctrip.zeus.service.nginx.impl.FileOpRecord;
import com.google.common.base.Joiner;
import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by fanqq on 2016/3/14.
 */
@Service("nginxConfOpsService")
public class NginxConfOpsServiceImpl implements NginxConfOpsService {

    @Resource
    private ConfigHandler configHandler;

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private final static String DEFAULT_NGINX_CONF_DIR = "/opt/app/nginx/conf";
    private final static String CONF_SUFFIX = ".conf";
    private final static String NGINX_CONF_FILE = "nginx.conf";
    private final static String BACKUP_TYPE_REFRESH = "Refresh";
    private final static String BACKUP_TYPE_ROLLBACK = "Rollback";
    private static final DynamicIntProperty rollbackFilesMaxDay = DynamicPropertyFactory.getInstance().getIntProperty("rollback.files.max.day", 7);
    private static final DynamicStringProperty nginxConfDir = DynamicPropertyFactory.getInstance().getStringProperty("nginx.conf.dir", DEFAULT_NGINX_CONF_DIR);

    @Override
    public Long updateAll(NginxConfEntry entry) throws Exception {
        String vhostDir = DEFAULT_NGINX_CONF_DIR + File.separator + "vhosts";
        String upstreamDir = DEFAULT_NGINX_CONF_DIR + File.separator + "upstreams";
        File vhostFile = makeSurePathExist(vhostDir);
        File upstreamFile = makeSurePathExist(upstreamDir);

        Date now = new Date();
        backupAll(now, BACKUP_TYPE_REFRESH);
        logCurrentConfFileOnDisk();

        logger.info("[NginxConfUpdateAll] Cleanse and backup files under dir upstreams and vhosts on disk.");
        List<String> cleansingFilenames = new ArrayList<>();
        List<String> trashFilenames = new ArrayList<>();
        for (String name : getFileList(vhostFile)) {
            if (name.endsWith(CONF_SUFFIX)) {
                cleansingFilenames.add(vhostDir + File.separator + name);
            } else {
                trashFilenames.add(vhostDir + File.separator + name);
            }
        }
        for (String name : getFileList(upstreamFile)) {
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
        logCurrentConfFileOnDisk();

        logger.info("[NginxConfUpdateAll] Start to overwrite complete confs under dir upstreams and vhosts.");
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
            logger.error("Updating upstream/vhost conf failed. Proceeding files stops at \n"
                    + Joiner.on(",").join(writingVhostFiles) + "\n"
                    + Joiner.on(",").join(writingUpstreamFiles) + ".", e);
            try {
                deleteFile(writingVhostFiles);
                deleteFile(writingUpstreamFiles);
                undoCleanFile(cleansingFilenames, now);
            } catch (Exception e1) {
                String err = "[NginxConfUpdateAll] Delete or revert conf files failed. Broken confs on disk. Refreshing is required.";
                logger.error(err);
                throw new NginxProcessingException(err, e1);
            }
            throw e;
        }
        logCurrentConfFileOnDisk();
        logger.info("[NginxConfUpdateAll] Successfuly updated conf files: \n"
                + Joiner.on(",").join(writingVhostFiles) + "\n"
                + Joiner.on(",").join(writingUpstreamFiles) + ".");
        return now.getTime();
    }

    @Override
    public void undoUpdateAll(Long flag) throws Exception {
        String vhostDir = DEFAULT_NGINX_CONF_DIR + File.separator + "vhosts";
        String upstreamDir = DEFAULT_NGINX_CONF_DIR + File.separator + "upstreams";
        File vhostFile = makeSurePathExist(vhostDir);
        String[] vhostFileNames = getFileList(vhostFile);
        File upstreamFile = makeSurePathExist(upstreamDir);
        String[] upstreamFileNames = getFileList(upstreamFile);
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
            logger.error("[UndoUpdateAll] Failed to delete and revert files on disk.", e);
            throw new NginxProcessingException("[UndoUpdateAll] Failed to delete and revert files on disk.", e);
        }
    }

    private void deleteFile(List<String> deleteFile) {
        File file;
        for (String name : deleteFile) {
            file = new File(name);
            if (file.exists() && file.isFile()) {
                if (!file.delete()) {
                    logger.error("[DeleteConfFile] Delete conf file failed. Filename:" + name);
                }
            }
        }
    }

    private String[] getFileList(File file) {
        if (file == null) {
            return new String[0];
        }
        String[] res = file.list();
        if (res != null) {
            return res;
        } else {
            return new String[0];
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
            String path = configHandler.getStringValue("nginx.conf.path", DEFAULT_NGINX_CONF_DIR);
            String file = configHandler.getStringValue("nginx.conf.file.name", NGINX_CONF_FILE);
            writeFile(path, file, nginxConf);
        } catch (Exception e) {
            undoCopyFile(files, null);
            throw e;
        }
    }

    @Override
    public void undoUpdateNginxConf() throws Exception {
        try {
            logger.info("[UndoUpdate] Start undo update nginx conf.");
            String fileName = DEFAULT_NGINX_CONF_DIR + File.separator + NGINX_CONF_FILE;
            List<String> files = new ArrayList<>();
            files.add(fileName);
            undoCopyFile(files, null);
        } catch (Exception e) {
            logger.error("[UndoUpdate]Exception undo update nginx conf exception.", e);
            throw new NginxProcessingException("[UndoUpdate]Nginx conf");
        } finally {
            logger.info("[UndoUpdate] Finish undo update nginx conf.");
        }

    }

    @Override
    public FileOpRecord cleanAndUpdateConf(Set<Long> cleanVsIds, Set<Long> updateVsIds, NginxConfEntry entry) throws Exception {
        FileOpRecord record = new FileOpRecord();
        String vhostDir = DEFAULT_NGINX_CONF_DIR + File.separator + "vhosts";
        String upstreamDir = DEFAULT_NGINX_CONF_DIR + File.separator + "upstreams";
        File vhostFile = makeSurePathExist(vhostDir);
        String[] vhostFileNames = getFileList(vhostFile);
        File upstreamFile = makeSurePathExist(upstreamDir);
        String[] upstreamFileNames = getFileList(upstreamFile);
        logger.info("[NginxConfPartialUpdate] Cleanse trash files under dir upstreams and vhosts.");
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
            logCurrentConfFileOnDisk();
        } catch (Exception e) {
            logger.error("[NginxConfPartialUpdate] Cleansing trash files failed.", e);
        }

        Set<String> nextRelatedUpstreams = new HashSet<>();
        for (ConfFile cf : entry.getUpstreams().getFiles()) {
            nextRelatedUpstreams.add(cf.getName());
        }
        Set<String> currentRelatedUpstreams = new HashSet<>();
        for (String upfn : getFileList(upstreamFile)) {
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
                    logger.warn("[NginxConfPartialUpdate] Unable to extract vs id information from upstream file: " + upfn + ".");
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
        logger.info("[NginxConfPartialUpdate] Backup related files under dir upstreams and vhosts on disk.");
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
            logCurrentConfFileOnDisk();
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
        writeConfs(entry, vhostDir, upstreamDir, record, cleansingFilenames, backingupFilenames);
        logCurrentConfFileOnDisk();
        record.setCleansedFilename(cleansingFilenames);
        record.setCopiedFilename(backingupFilenames);
        return record;
    }

    private void writeConfs(NginxConfEntry entry, String vhostDir, String upstreamDir,
                            FileOpRecord record, List<String> cleansingFilenames,
                            List<String> backingupFilenames) throws IOException, NginxProcessingException {
        logger.info("[NginxConfPartialUpdate] Start to write confs of related vses under dir upstreams and vhosts.");
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
            logger.error("Updating upstream/vhost conf failed. Proceeding files stops at \n" + Joiner.on(",").join(writingVhostFiles) + "\n" + Joiner.on(",").join(writingUpstreamFiles) + ".", e);
            try {
                deleteFile(writingVhostFiles);
                deleteFile(writingUpstreamFiles);
                undoCleanFile(cleansingFilenames, record.getTimeStamp());
                undoCopyFile(backingupFilenames, record.getTimeStamp());
            } catch (Exception e1) {
                String err = "[NginxConfPartialUpdate] Delete or revert conf files failed. Broken confs on disk. Refreshing is required. Maker - " + record.getTimeStamp().toString();
                logger.error(err);
                throw new NginxProcessingException(err, e1);
            }
            throw e;
        }
        logger.info("Successfuly updated conf files: \n" + Joiner.on(",").join(writingVhostFiles).replaceAll(DEFAULT_NGINX_CONF_DIR, "")
                + "\n" + Joiner.on(",").join(writingUpstreamFiles).replaceAll(DEFAULT_NGINX_CONF_DIR, "") + ".");
    }

    @Override
    public void undoCleanAndUpdateConf(Set<Long> cleanVsIds, NginxConfEntry entry, FileOpRecord record) throws Exception {
        try {
            logger.info("[UndoNginxConfPartialUpdate] Start to revert related conf files to the previous version. Maker - " + record.getTimeStamp().toString());
            backupAll(record.getTimeStamp(), BACKUP_TYPE_ROLLBACK);
            deleteFile(record.getWrittenFilename());
            undoCopyFile(record.getCopiedFilename(), record.getTimeStamp());
            undoCleanFile(record.getCleansedFilename(), record.getTimeStamp());
        } catch (Exception e) {
            String err = "[UndoNginxConfPartialUpdate] Failed to revert related conf files to the previous version. Maker - " + record.getTimeStamp().toString();
            logger.error(err, e);
            throw new NginxProcessingException(err, e);
        } finally {
            logger.info("[UndoNginxConfPartialUpdate] Successfully reverted related conf files to the previous version.");
        }

    }

    @Override
    public synchronized void cleanRollbackFiles() {
        if (!configHandler.getEnable("clean.rollback.files", true)) {
            return;
        }
        String refresh = nginxConfDir.get() + "/" + BACKUP_TYPE_REFRESH + "Backup/";
        String rollback = nginxConfDir.get() + "/" + BACKUP_TYPE_ROLLBACK + "Backup/";
        cleanFiles(refresh);
        cleanFiles(rollback);
    }

    private void cleanFiles(String dir) {
        File dirFile = new File(dir);
        if (!dirFile.exists()) return;
        File[] children = dirFile.listFiles();
        if (children == null) return;
        // Last Modify.
        long now = System.currentTimeMillis();
        for (File c : children) {
            if (now - c.lastModified() > 60 * 60 * 1000 * 24 * rollbackFilesMaxDay.get()) {
                removePath(c.getAbsolutePath());
            }
        }
        // Count
        children = dirFile.listFiles();
        DynamicIntProperty maxCount = DynamicPropertyFactory.getInstance().getIntProperty("clean.rollback.files.total.count", 100);
        if (children == null || children.length <= maxCount.get()) {
            return;
        }
        List<String> pathList = new ArrayList<>();
        for (File c : children) {
            pathList.add(c.getAbsolutePath());
        }
        Collections.sort(pathList);
        for (int i = 0; i < pathList.size() - maxCount.get(); i++) {
            removePath(pathList.get(i));
        }
    }

    private void removePath(String path) {
        String removeCommand = " rm -r " + path;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
        CommandLine commandline = CommandLine.parse(removeCommand);
        DefaultExecutor exec = new DefaultExecutor();
        exec.setExitValues(null);
        PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream, errorStream);
        exec.setStreamHandler(streamHandler);

        int exitVal = 0;
        try {
            exitVal = exec.execute(commandline);
            String out = outputStream.toString("UTF-8");
            String error = errorStream.toString("UTF-8");
            if (0 != exitVal) {
                logger.error("Remove Path Failed." + out + error);
            }
            logger.info("Success removed path. Path:" + path);
        } catch (IOException e) {
            logger.error("Remove Path Failed.");
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
            logger.info("[CleanFile] Clean Conf Suc.Clean Conf:" + sucList.toString().replaceAll(DEFAULT_NGINX_CONF_DIR, "")
                    + " Not Exist: " + notExist.toString().replaceAll(DEFAULT_NGINX_CONF_DIR, ""));
            return true;
        } catch (Exception e) {
            logger.error("[CleanFile] Fail to clean conf. Files: " + fileNames.toString(), e);
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
                            logger.error("[UndoCleanFile] delete file failed. fileName: " + file.getAbsolutePath());
                        }
                    }
                    File renameTo = new File(file.getAbsolutePath());
                    cleanFile.renameTo(renameTo);
                    sucList.add(cleanFile.getAbsolutePath());
                } else {
                    logger.error("[UndoCleanFile] file bak.clean not exist. fileName: " + cleanFile.getAbsolutePath());
                    notExist.add(cleanFile.getAbsolutePath());
                }
            }
            return true;
        } catch (Exception e) {
            logger.error("[UndoCleanFile] Fail to undo clean conf. Files: " + fileNames.toString(), e);
            throw new NginxProcessingException("[UndoCleanFile] Fail to undo clean conf. Files: " + fileNames.toString().replaceAll(DEFAULT_NGINX_CONF_DIR, ""), e);
        } finally {
            logger.info("[UndoCleanFile] Undo Clean Conf Suc.Clean Conf:" + sucList.toString().replaceAll(DEFAULT_NGINX_CONF_DIR, "")
                    + " Not Exist: " + notExist.toString().replaceAll(DEFAULT_NGINX_CONF_DIR, ""));
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
            logger.error("[CopyFile] Fail to copy conf. Files: " + fileNames.toString().replaceAll(DEFAULT_NGINX_CONF_DIR, "")
                    + " Suc Copy:" + sucList.toString().replaceAll(DEFAULT_NGINX_CONF_DIR, ""), e);
            throw e;
        } finally {
            logger.info("[CopyFile] Copy Conf Suc.Copy Conf:" + sucList.toString().replaceAll(DEFAULT_NGINX_CONF_DIR, "")
                    + " Not Exist: " + notExist.toString().replaceAll(DEFAULT_NGINX_CONF_DIR, ""));
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
                            logger.error("[UndoCopyFile] delete file failed. fileName: " + file.getAbsolutePath());
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
                    logger.error("[UndoCopyFile] bak.copy file not exist. fileName: " + copyFile.getAbsolutePath());
                    notExist.add(copyFile.getAbsolutePath());
                }
            }
            return true;
        } catch (Exception e) {
            logger.error("[UndoCopyFile] Fail to undo copy conf. Files: " + fileNames.toString() + " Suc Copy:" + sucList.toString(), e);
            throw new NginxProcessingException("[UndoCopyFile] Fail to undo copy conf. Files: " + fileNames.toString() + " Suc Copy:" + sucList.toString(), e);
        } finally {
            logger.info("[UndoCopyFile] Undo Copy Conf Finish.Copy Conf:" + sucList.toString() + " Not Exist: " + notExist.toString());
        }
    }

    private synchronized void writeFile(String confPath, String confFile, String confContent) throws IOException {
        makeSurePathExist(confPath);
        Writer writer = null;
        String path = confPath + File.separator + confFile;
        int contentLength = confContent.getBytes().length;
        File file = new File(path);
        try {
            writer = new FileWriter(file);
            writer.write(confContent);
            writer.flush();
        } catch (Exception e) {
            logger.error("[WriteFile] Write File Failed! Path:" + path);
            throw e;
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
        logger.info("Finish write file.Path:" + path + " ; DataSize:" + contentLength + ";FileSize:" + file.length());
        if (file.length() != contentLength) {
            logger.warn("File Size Not Equals Content Size;Path:" + path);
        }
    }

    private File makeSurePathExist(String confDir) {
        logger.info("Check conf dir exist.Dir:" + confDir);
        File f = new File(confDir);
        if (!f.exists() || !f.isDirectory()) {
            logger.info("Check conf dir not exist,create dir instead.Dir:" + confDir);
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

            logger.info("Back Up rollback conf", response.toString());
            if (!response.getSucceed()) {
                logger.error("[NginxConfOpsServiceImpl-BackUpAll] back up vhosts Failed.Response: " + response);
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

            logger.info("Back Up rollback conf", response.toString());
            if (!response.getSucceed()) {
                logger.error("[NginxConfOpsServiceImpl-BackUpAll] back up upstreams Failed.Response: " + response);
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

            logger.info("Back Up rollback conf", response.toString());
            if (!response.getSucceed()) {
                logger.error("[NginxConfOpsServiceImpl-BackUpAll] back up nginx.conf Failed.Response: " + response);
                throw new Exception("Fail to backup nginx.conf. Response:" + response.toString());
            }
            return response;
        } catch (IOException e) {
            logger.error("[NginxConfOpsServiceImpl-BackUpAll] Fail to backup conf. Date:" + date.toString(), e);
            throw new Exception("[NginxConfOpsServiceImpl-BackUpAll] Fail to backup conf. Date:" + date.toString(), e);
        }
    }

    private void logCurrentConfFileOnDisk() {
        String vhostDir = DEFAULT_NGINX_CONF_DIR + File.separator + "vhosts";

        File vhosts = new File(vhostDir);
        File[] vhostChildren = vhosts.listFiles();
        StringBuilder sb = new StringBuilder(128);
        if (vhostChildren != null) {
            for (File f : vhostChildren) {
                sb.append(f.getName());
                sb.append(",");
            }
        }
        logger.info("Current Vhosts Conf Files:" + sb.toString());

        String upstreamDir = DEFAULT_NGINX_CONF_DIR + File.separator + "upstreams";

        File upstream = new File(upstreamDir);
        File[] upstreamChildren = upstream.listFiles();
        StringBuilder sb2 = new StringBuilder(128);
        if (upstreamChildren != null) {
            for (File f : upstreamChildren) {
                sb2.append(f.getName());
                sb2.append(",");
            }
        }
        logger.info("Current Upstream Conf Files:" + sb2.toString());

    }
}
