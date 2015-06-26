package com.ctrip.zeus.service.validate.impl;

import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.SlbValidateResponse;
import com.ctrip.zeus.nginx.LocalValidate;
import com.ctrip.zeus.service.model.SlbRepository;
import com.ctrip.zeus.service.validate.SlbValidateLocal;

import javax.annotation.Resource;

/**
 * Created by fanqq on 2015/6/25.
 */
public class SlbValidateLocalImpl implements SlbValidateLocal {
    @Resource
    SlbRepository slbRepository;
    @Resource
    LocalValidate localValidate;

    @Override
    public SlbValidateResponse validate(Long slbId) throws Exception {
        SlbValidateResponse response = new SlbValidateResponse();

        Slb slb = slbRepository.getById(slbId);
        if (slb == null)
        {
            response.setSlbId(slbId).setSucceed(false).setMsg("Not found Slb by slbId!");
            return response;
        }
        

        return null;
    }
}
