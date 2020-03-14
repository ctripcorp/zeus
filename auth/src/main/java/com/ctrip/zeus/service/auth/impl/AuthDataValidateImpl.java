package com.ctrip.zeus.service.auth.impl;

import com.ctrip.zeus.auth.entity.DataResource;
import com.ctrip.zeus.auth.entity.Operation;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.service.auth.AuthDataValidate;
import com.ctrip.zeus.service.auth.AuthDefaultValues;
import com.ctrip.zeus.service.auth.ResourceDataType;
import com.ctrip.zeus.service.auth.ResourceOperationType;
import org.apache.commons.net.util.SubnetUtils;
import org.springframework.stereotype.Service;

/**
 * Created by fanqq on 2016/8/5.
 */
@Service("authDataValidate")
public class AuthDataValidateImpl implements AuthDataValidate {
    @Override
    public void validateDataResource(DataResource dataResource) throws Exception {
        try {
            switch (ResourceDataType.valueOf(dataResource.getResourceType())) {
                case Ip:
                    if (dataResource.getData().equals(AuthDefaultValues.ALL)){
                        break;
                    }
                    SubnetUtils.SubnetInfo subnetInfo = new SubnetUtils(dataResource.getData()).getInfo();
                    if (subnetInfo == null){
                        throw new ValidationException("Parser Ip Failed");
                    }
                    break;
                case Group:
                case Slb:
                case Vs:
                case Auth:
                case Policy:
                    if (dataResource.getData().equals(AuthDefaultValues.ALL)){
                        break;
                    }
                    Long.parseLong(dataResource.getData());
                    break;
                default:
                    if (dataResource.getData().equals(AuthDefaultValues.ALL)){
                        break;
                    }
                    Long.parseLong(dataResource.getData());
                    break;
            }
            for (Operation operation : dataResource.getOperations()){
                ResourceOperationType.valueOf(operation.getType());
            }
        } catch (Exception e) {
            throw new ValidationException("Resource Data Type is invalidate.");
        }
    }
}
