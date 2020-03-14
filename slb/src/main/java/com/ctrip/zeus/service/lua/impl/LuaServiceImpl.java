package com.ctrip.zeus.service.lua.impl;

import com.ctrip.zeus.client.InstallDefaultPageClient;
import com.ctrip.zeus.config.ConfigValueService;
import com.ctrip.zeus.dao.entity.FileData;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.model.Slb;
import com.ctrip.zeus.model.model.SlbServer;
import com.ctrip.zeus.model.page.DefaultFile;
import com.ctrip.zeus.server.LocalInfoPack;
import com.ctrip.zeus.service.build.ConfigHandler;
import com.ctrip.zeus.service.build.conf.ConfWriter;
import com.ctrip.zeus.service.file.Constants;
import com.ctrip.zeus.service.file.FilesInDbService;
import com.ctrip.zeus.service.lua.LuaService;
import com.ctrip.zeus.service.model.SlbRepository;
import com.ctrip.zeus.service.tools.local.LocalInfoService;
import com.ctrip.zeus.util.AssertUtils;
import com.ctrip.zeus.util.EnvHelper;
import com.ctrip.zeus.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by fanqq on 2017/4/28.
 */
@Service("luaService")
public class LuaServiceImpl implements LuaService {
    @Resource
    FilesInDbService filesInDbService;
    @Resource
    SlbRepository slbRepository;
    @Resource
    ConfigHandler configHandler;
    @Resource
    LocalInfoService localInfoService;
    @Autowired
    private ConfigValueService configValueService;

    private final static String FILE_PREFIX = "lua_";
    private final static String FILE_TYPE = "LUA";
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void addLuaFile(String name, byte[] content) throws Exception {
        validateName(name);
        filesInDbService.addFile(FILE_PREFIX + name, content);
    }

    @Override
    public Long getMaxVersion(String name, Long slbId) throws Exception {
        validateName(name);
        return filesInDbService.getMaxIndexPageVersion(FILE_PREFIX + name);
    }

    @Override
    public boolean installLuaFile(String name, Long slbId, Long version) throws Exception {
        validateName(name);
        Slb slb = slbRepository.getById(slbId);
        if (slb == null || slb.getSlbServers().size() == 0) {
            throw new ValidationException("Not Found slb by id. id:" + slbId);
        }
        byte[] data = getFile(name, version);
        if (data == null) {
            throw new ValidationException("Not Found file by slbId. id:" + slbId + "and version:" + version);
        }

        boolean succeed = true;
        for (SlbServer slbServer : slb.getSlbServers()) {
            if (!InstallDefaultPageClient.getClientByServerIp(slbServer.getIp()).luaFile(name, data)) {
                succeed = false;
                break;
            }
        }
        if (succeed) {
            filesInDbService.updateFileStatus(FILE_PREFIX + name, slbId, FILE_TYPE, version);
            for (SlbServer slbServer : slb.getSlbServers()) {
                filesInDbService.updateFileStatus(FILE_PREFIX + name, slbServer.getIp(), version);
            }
        }

        return succeed;
    }

    @Override
    public boolean installLocalLuaFile(String name, byte[] content) throws Exception {
        if (content == null || content.length == 0)
            throw new ValidationException("Install file content can not be null.");
        installFile(name, content);
        return true;
    }

    @Override
    public void installLocalLuaFile(String name, Long version) throws Exception {
        validateName(name);
        byte[] content = getFile(name, version);
        if (content == null) {
            throw new ValidationException("Not found Lua File.File:" + name + " ; version: " + version);
        }
        String path = configHandler.getStringValue("lua.root.path", "/opt/app/nginx/conf/lua");

        doWrite(path, name, content);
        install(name, path);
        filesInDbService.updateFileStatus(FILE_PREFIX + name, LocalInfoPack.INSTANCE.getIp(), version);
    }


    @Override
    public FileData getCurrentLuaFile(String name, Long slbId) throws Exception {
        validateName(name);
        FileData file = filesInDbService.getCurrentFile(FILE_PREFIX + name, FILE_TYPE, slbId);
        return file;
    }

    @Override
    public List<FileData> getCurrentLuaFiles(Long slbId) throws Exception {
        List<FileData> res = filesInDbService.getCurrentFiles(FILE_TYPE, slbId);
        if (res == null || res.size() == 0) return null;
        for (FileData file : res) {
            if (file != null && file.getKey().startsWith(FILE_PREFIX)) {
                file.setKey(file.getKey().replaceFirst(FILE_PREFIX, ""));
            }
        }
        return res;
    }

    @Override
    public FileData getCurrentLuaFile(String name, String ip) throws Exception {
        validateName(name);
        FileData file = filesInDbService.getCurrentFile(FILE_PREFIX + name, ip);
        return file;
    }

    @Override
    public void updateServerFileStatus(String name, String ip, Long version) throws Exception {
        AssertUtils.assertNotNull(name, "File Name is null");
        AssertUtils.assertNotNull(ip, "ip is null");
        AssertUtils.assertNotNull(version, "version is null");
        filesInDbService.updateFileStatus(FILE_PREFIX + name, ip, version);
    }

    @Override
    public void updateSlbFileStatus(String name, Long slbId, Long version) throws Exception {
        AssertUtils.assertNotNull(name, "File Name is null");
        AssertUtils.assertNotNull(slbId, "slbId is null");
        AssertUtils.assertNotNull(version, "version is null");
        filesInDbService.updateFileStatus(FILE_PREFIX + name, slbId, FILE_TYPE, version);
    }

    @Override
    public boolean installLuaConfFile(Long slbId) throws Exception {
        Slb slb = slbRepository.getById(slbId);
        if (slb == null || slb.getSlbServers().size() == 0) {
            throw new ValidationException("Not Found slb by id. id:" + slbId);
        }

        for (SlbServer slbServer : slb.getSlbServers()) {
            if (!Constants.SETSTATUSSUCCESSMSG.equalsIgnoreCase(InstallDefaultPageClient.getClientByServerIp(slbServer.getIp()).luaConfFile(slbId))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void installLocalLuaConfFile() throws Exception {
        ConfWriter confWriter = new ConfWriter(1024, true);
        String path = configHandler.getStringValue("lua.root.path", "/opt/app/nginx/conf/lua");
        buildLuaConf(confWriter, path);
        doWrite(path, "lua.conf", confWriter.getValue().getBytes());
    }

    /*Startup lua */
    @Override
    public void luaStartInit() throws Exception {
        ConfWriter confWriter = new ConfWriter(1024, true);
        Long slbId = localInfoService.getLocalSlbId();
        String ip = localInfoService.getLocalIp();
        String path = configHandler.getStringValue("lua.root.path", "/opt/app/nginx/conf/lua");
        File lua = new File(path + "/lua.conf");

        if (configHandler.getEnable("always.install.lua.conf", true) || !lua.exists()) {
            buildLuaConf(confWriter, path);
            doWrite(path, "lua.conf", confWriter.getValue().getBytes());
        }

        if (EnvHelper.portal()) {
            List<FileData> slbLuaFiles = getCurrentLuaFiles(slbId);
            if (slbLuaFiles != null) {
                for (FileData file : slbLuaFiles) {
                    FileData ipFile = getCurrentLuaFile(file.getKey(), ip);
                    if (ipFile == null || !ipFile.getVersion().equals(file.getVersion())) {
                        installLocalLuaFile(file.getKey(), file.getVersion());
                    }
                }
            }

            return;
        }
        if (slbId == null || ip == null) return;

        List<DefaultFile> slbLuaFiles = InstallDefaultPageClient.getClientByHost(configHandler.getStringValue("agent.api.host", configValueService.getAgentApi())).luaDefaultFilesBySlbId(slbId);
        if (slbLuaFiles == null) return;
        for (DefaultFile file : slbLuaFiles) {
            DefaultFile ipFile = InstallDefaultPageClient.getClientByHost(configHandler.getStringValue("agent.api.host", configValueService.getAgentApi())).luaDefaultFileByIp(file.getName(), ip);
            if (ipFile == null || !ipFile.getVersion().equals(file.getVersion())) {
                ByteArrayOutputStream fileData = InstallDefaultPageClient.getClientByHost(configHandler.getStringValue("agent.api.host", configValueService.getAgentApi())).getLuaFile(file.getName(), file.getVersion());
                if (fileData != null) {
                    boolean succeed = installLocalLuaFile(file.getName(), fileData.toByteArray());
                    if (succeed) {
                        InstallDefaultPageClient.getClientByHost(configHandler.getStringValue("agent.api.host", configValueService.getAgentApi())).luaSetStatus(ip, file.getVersion(), file.getName());
                    }
                }
            }
        }
    }

    @Override
    public boolean isLuaModuleExist(String module) throws Exception {
        String path = configHandler.getStringValue("lua.root.path", "/opt/app/nginx/conf/lua");
        if (module.contains(".")) {
            module = module.replace(".", "/");
        }
        File lua = new File(path + "/" + module + ".lua");
        return lua.exists();
    }

    private void buildLuaConf(ConfWriter confWriter, String path) throws Exception {
        String packagePath = path + "/?.lua;";

        String wafInstallDir = configHandler.getStringValue("waf.install.dir", null, null, null, "/opt/app/nginx/conf");
        packagePath += wafInstallDir + "/waf/?.lua;";

        confWriter.writeCommand("lua_package_path", "\"" + packagePath + "\"");
        confWriter.writeCommand("lua_shared_dict", "limit " + configHandler.getStringValue("lua.shared.dict.limit", "10m"));
        confWriter.writeCommand("lua_shared_dict", "global " + configHandler.getStringValue("lua.shared.dict.global", "1m"));
        confWriter.writeCommand("lua_shared_dict", "dynrule " + configHandler.getStringValue("lua.shared.dict.dynrule", "10m"));
//        if (wafEnable) {
//            confWriter.writeCommand("access_by_lua_file", wafInstallDir + "/waf/core/waf.lua");
//            confWriter.writeCommand("init_worker_by_lua_file", wafInstallDir + "/waf/core/worker.lua");
//        }
    }

    @Override
    public byte[] getFile(String name, Long version) throws Exception {
        validateName(name);
        return filesInDbService.getFile(FILE_PREFIX + name, version);
    }

    private void doWrite(String path, String fileName, byte[] content) throws IOException {
        File f = new File(path);
        if (!f.exists()) {
            f.mkdirs();
        }
        FileOutputStream writer = null;
        try {
            File file = new File(path + File.separator + fileName);
            writer = new FileOutputStream(file);
            writer.write(content);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    private void validateName(String name) throws Exception {
        if (!name.endsWith(".lua") && !name.endsWith(".zip")) {
            throw new ValidationException("File Name is invalidate.");
        }
    }


    private void installFile(String name, byte[] content) throws Exception {
        String path = configHandler.getStringValue("lua.root.path", "/opt/app/nginx/conf/lua");
        doWrite(path, name, content);
        install(name, path);
    }

    private void install(String name, String path) {
        if (name.endsWith(".zip")) {
            String tmp = name.substring(0, name.length() - 4);
            //1. Back Up Files If Need.
            File file = new File(path + "/" + tmp);
            if (file.exists()) {
                String commandline = "cp -r " + path + "/" + tmp + " " + path + "/bk_" + tmp;
                try {
                    Process p = Runtime.getRuntime().exec(commandline);
                    p.waitFor();
                    logger.info(IOUtils.inputStreamStringify(p.getInputStream()));
                    logger.error(IOUtils.inputStreamStringify(p.getErrorStream()));
                } catch (Exception e) {
                    logger.error("Fail to execute command {}.", commandline, e);
                    return;
                }
            }

            //2. Unzip
            String commandline = "unzip -o " + path + "/" + name + " -d " + path;
            try {
                Process p = Runtime.getRuntime().exec(commandline);
                p.waitFor();
                logger.info(IOUtils.inputStreamStringify(p.getInputStream()));
                logger.error(IOUtils.inputStreamStringify(p.getErrorStream()));
            } catch (Exception e) {
                logger.error("Fail to execute command {}.", commandline, e);
                return;
            }
        }
    }
}
