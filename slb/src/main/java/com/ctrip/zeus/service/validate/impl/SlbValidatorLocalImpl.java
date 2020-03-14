package com.ctrip.zeus.service.validate.impl;

import com.ctrip.zeus.model.model.Slb;
import com.ctrip.zeus.model.model.SlbValidateResponse;
import com.ctrip.zeus.model.model.VirtualServer;
import com.ctrip.zeus.model.nginx.NginxResponse;
import com.ctrip.zeus.nginx.LocalValidate;
import com.ctrip.zeus.service.build.ConfigHandler;
import com.ctrip.zeus.service.build.conf.ServerConf;
import com.ctrip.zeus.service.file.FileSysService;
import com.ctrip.zeus.service.file.SessionTicketService;
import com.ctrip.zeus.service.lua.LuaService;
import com.ctrip.zeus.service.model.EntityFactory;
import com.ctrip.zeus.service.model.ModelStatusMapping;
import com.ctrip.zeus.service.validate.SlbValidatorLocal;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Created by fanqq on 2015/6/25.
 */
@Component("slbValidatorLocal")
public class SlbValidatorLocalImpl implements SlbValidatorLocal {
    @Resource
    private EntityFactory entityFactory;
    @Resource
    private LocalValidate localValidate;
    @Resource
    private FileSysService fileSysService;
    @Resource
    ConfigHandler configHandler;
    @Resource
    private LuaService luaService;
    @Resource
    private SessionTicketService sessionTicketService;

    @Override
    public SlbValidateResponse validate(Long slbId) throws Exception {
        SlbValidateResponse response = new SlbValidateResponse();
        response.setSlbId(slbId);

        ModelStatusMapping<Slb> slbMap = entityFactory.getSlbsByIds(new Long[]{slbId});
        if (slbMap == null || slbMap.getOfflineMapping() == null || slbMap.getOfflineMapping().get(slbId) == null) {
            response.setSucceed(false).setMsg("Not found Slb by slbId!");
            return response;
        }

        Slb slb = slbMap.getOfflineMapping().get(slbId);
        if (!validateNginxBinAndConf(slb)) {
            response.setSucceed(false).setMsg("slb conf path or bin path is not exist!");
            return response;
        }

        NginxResponse res = localValidate.nginxIsUp(slb.getNginxBin());
        if (!res.getSucceed()) {
            response.setSucceed(false).setMsg(res.getOutMsg());
            return response;
        }

        ModelStatusMapping<VirtualServer> vsMap = entityFactory.getVsesBySlbIds(slbId);
        for (VirtualServer vs : vsMap.getOfflineMapping().values()) {
            if (vs.getSsl() != null && vs.getSsl()) {
                if (!(localValidate.pathExistValidate(ServerConf.SSL_PATH + vs.getId() + "/ssl.crt", false)
                        && localValidate.pathExistValidate(ServerConf.SSL_PATH + vs.getId() + "/ssl.key", false))) {
                    response.setSucceed(false).setMsg("Not found ssl.crt and ssl.key for ssl virtual server! vsId="
                            + vs.getId() + ";vsName=" + vs.getName());
                    return response;
                }
            }
        }
        fileSysService.fileInit();
        luaService.luaStartInit();
        sessionTicketService.sessionTicketFileStartInit();
        return response.setSucceed(true);
    }

    private boolean validateNginxBinAndConf(Slb slb) throws Exception {
        return localValidate.pathExistValidate(slb.getNginxBin(), true) &&
                localValidate.pathExistValidate(slb.getNginxConf(), true);
    }
}
