package com.ctrip.zeus.service.validate.impl;

import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.SlbValidateResponse;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.nginx.LocalValidate;
import com.ctrip.zeus.nginx.entity.NginxResponse;
import com.ctrip.zeus.service.build.conf.ServerConf;
import com.ctrip.zeus.service.model.SlbRepository;
import com.ctrip.zeus.service.validate.SlbValidatorLocal;

import javax.annotation.Resource;

/**
 * Created by fanqq on 2015/6/25.
 */
public class SlbValidatorLocalImpl implements SlbValidatorLocal {
    @Resource
    SlbRepository slbRepository;
    @Resource
    LocalValidate localValidate;

    @Override
    public SlbValidateResponse validate(Long slbId) throws Exception {
        SlbValidateResponse response = new SlbValidateResponse();
        response.setSlbId(slbId);
        Slb slb = slbRepository.getById(slbId);
        if (slb == null)
        {
            response.setSucceed(false).setMsg("Not found Slb by slbId!");
            return response;
        }
        if (!validateNginxBinAndConf(slb)){
            response.setSucceed(false).setMsg("slb conf path or bin path is not exist!");
            return response;
        }
        NginxResponse res = localValidate.nginxIsUp(slb.getNginxBin());
        if (!res.getSucceed()){
            response.setSucceed(false).setMsg(res.getOutMsg());
            return response;
        }
        for (VirtualServer vs : slb.getVirtualServers())
        {
            if (vs.getSsl())
            {
                if(localValidate.pathExistValidate(ServerConf.SSL_PATH+vs.getId()+"/ssl.crt",false)
                &&localValidate.pathExistValidate(ServerConf.SSL_PATH+vs.getId()+"/ssl.key",false)){
                    response.setSucceed(false).setMsg("Not found ssl.crt and ssl.key for ssl virtual server! vsId="
                            +vs.getId()+";vsName="+vs.getName());
                    return response;
                }
            }
        }
        return response.setSucceed(true);
    }

    private boolean validateNginxBinAndConf(Slb slb) throws Exception {
        return localValidate.pathExistValidate(slb.getNginxBin(),true)&&
                localValidate.pathExistValidate(slb.getNginxConf(),true);
    }
}
