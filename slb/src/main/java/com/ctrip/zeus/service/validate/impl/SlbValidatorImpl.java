package com.ctrip.zeus.service.validate.impl;

import com.ctrip.zeus.client.ValidateClient;
import com.ctrip.zeus.model.model.Slb;
import com.ctrip.zeus.model.model.SlbServer;
import com.ctrip.zeus.model.model.SlbValidateResponse;
import com.ctrip.zeus.service.validate.SlbValidator;
import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;
import org.springframework.stereotype.Service;

/**
 * Created by fanqq on 2015/6/25.
 */
@Service("slbValidator")
public class SlbValidatorImpl implements SlbValidator {
    private static DynamicIntProperty adminServerPort = DynamicPropertyFactory.getInstance().getIntProperty("server.port", 8099);

    @Override
    public SlbValidateResponse validate(Slb slb) throws Exception {
        SlbValidateResponse response = new SlbValidateResponse();
        SlbValidateResponse tmpRes = null;
        response.setSlbId(slb.getId());
        if (slb.getSlbServers() == null || slb.getSlbServers().size() == 0) {
            response.setSucceed(false).setMsg("Not found Slb Server by slbId!");
            return response;
        }
        for (SlbServer slbServer : slb.getSlbServers()) {
            ValidateClient validateClient = ValidateClient.getClient("http://" + slbServer.getIp() + ":" + adminServerPort.get());
            tmpRes = validateClient.slbValidate(slb.getId());
            if (!tmpRes.getSucceed()) {
                return response.setSucceed(false).setMsg(tmpRes.getMsg()).setIp(slbServer.getIp()).setSlbId(slb.getId());
            }
        }
        return response.setSucceed(true);
    }
}
