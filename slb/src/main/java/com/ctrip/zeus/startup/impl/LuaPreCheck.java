package com.ctrip.zeus.startup.impl;

import com.ctrip.zeus.service.lua.LuaService;
import com.ctrip.zeus.startup.PreCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Created by fanqq on 2017/4/28.
 */
@Service("luaPreCheck")
public class LuaPreCheck implements PreCheck {
    private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
    @Resource
    private LuaService luaService;

    @Override
    public boolean ready() {
        try {
            luaService.luaStartInit();
            return true;
        } catch (Exception e) {
            logger.error("Initialize Lua environment failed.", e);
            return false;
        }
    }
}
