package com.ctrip.zeus.service.validate.impl;

import com.ctrip.zeus.client.ValidateClient;
import com.ctrip.zeus.dal.core.GroupSlbDo;
import com.ctrip.zeus.dal.core.GroupSlbEntity;
import com.ctrip.zeus.dal.core.SlbDao;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.service.model.SlbRepository;
import com.ctrip.zeus.service.model.handler.SlbQuery;
import com.ctrip.zeus.service.validate.SlbValidator;
import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by fanqq on 2015/6/25.
 */
@Service("slbValidator")
public class SlbValidatorImpl implements SlbValidator {
    @Resource
    private SlbRepository slbRepository;
    private static DynamicIntProperty adminServerPort = DynamicPropertyFactory.getInstance().getIntProperty("server.port", 8099);

    @Override
    public SlbValidateResponse validate(Long slbId) throws Exception{
        SlbValidateResponse response = new SlbValidateResponse();
        SlbValidateResponse tmpRes = null;
        response.setSlbId(slbId);
        Slb slb = slbRepository.getById(slbId);
        if (slb == null)
        {
            response.setSucceed(false).setMsg("Not found Slb by slbId!");
            return response;
        }
        for (SlbServer slbServer : slb.getSlbServers()){
            ValidateClient validateClient = ValidateClient.getClient("http://" + slbServer.getIp() + ":" + adminServerPort.get());
            tmpRes=validateClient.slbValidate(slbId);
            if (!tmpRes.getSucceed()){
                return response.setSucceed(false).setMsg(tmpRes.getMsg()).setIp(slbServer.getIp()).setSlbId(slbId);
            }
        }
        return response.setSucceed(true);
    }
}
