package com.ctrip.zeus.service.lua;

import com.ctrip.zeus.dao.entity.FileData;
import com.ctrip.zeus.model.page.DefaultFile;

import java.util.List;

/**
 * Created by fanqq on 2017/4/28.
 */
public interface LuaService {
    void addLuaFile(String name, byte[] content) throws Exception;

    Long getMaxVersion(String name, Long slbId) throws Exception;

    /*
   * Portal: Call slb server to install lua files
   * */
    boolean installLuaFile(String name, Long slbId, Long version) throws Exception;


    /*
    * Agent
    * Install Lua file on the agent machine
    * */
    boolean installLocalLuaFile(String name, byte[] content) throws Exception;

    void installLocalLuaFile(String name, Long version) throws Exception;

    FileData getCurrentLuaFile(String name, Long slbId) throws Exception;


    List<FileData> getCurrentLuaFiles(Long slbId) throws Exception;

    FileData getCurrentLuaFile(String name, String ip) throws Exception;

    void updateServerFileStatus(String name, String ip, Long version) throws Exception;

    void updateSlbFileStatus(String name, Long slbId, Long version) throws Exception;

    /*
    * Portal
    * Call Slb Server to install conf files
    * */
    boolean installLuaConfFile(Long slbId) throws Exception;

    /*
    * Agent
    * Install ua file on the agent machine
    * */
    void installLocalLuaConfFile() throws Exception;

    void luaStartInit() throws Exception;

    byte[] getFile(String name, Long version) throws Exception;

    boolean isLuaModuleExist(String module) throws Exception;
}
