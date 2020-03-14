package com.ctrip.zeus.service.verify.verifier;

import com.ctrip.zeus.service.verify.VerifyContext;
import com.ctrip.zeus.service.verify.VerifyResult;

import java.util.List;

public interface IllegalDataVerifier {
    List<VerifyResult> verify() throws Exception;

    void setContext(VerifyContext context);

    VerifyContext getContext();

    /*
     * @Description
     * @return: tagName or propertyKey used to mark illegal item in db
     **/
    String getMarkName();

    /*
     * @Description
     * @return: return IllegalMarkTypes.TAG or IllegalMarkTypes.PROPERTY
     **/
    String getMarkType();

    String getDisplayName();

    String getTargetItemType();
}
