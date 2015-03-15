package com.ctrip.zeus.service.op;

import com.ctrip.zeus.model.entity.MemberAction;
import com.ctrip.zeus.model.entity.ServerAction;

/**
 * @author:xingchaowang
 * @date: 3/15/2015.
 */
public interface OperationService {

    void upMember(MemberAction action);
    void downMember(MemberAction action);
    void upServer(ServerAction action);
    void downServer(ServerAction action);
}
