package com.ctrip.zeus.service.query;

import com.ctrip.zeus.service.query.impl.PropertyCommandService;
import com.ctrip.zeus.service.query.impl.TagCommandService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Created by zhoumy on 2016/7/19.
 */
@Component("criteriaQueryFactory")
public class CriteriaQueryFactory {
    @Resource
    private CriteriaQuery groupCriteriaQuery;
    @Resource
    private CriteriaQuery slbCriteriaQuery;
    @Resource
    private CriteriaQuery virtualServerCriteriaQuery;
    @Resource
    private CriteriaQuery trafficPolicyQuery;
    @Resource
    private CriteriaQuery drCriteriaQuery;
    @Resource
    private TagCommandService tagCommandService;
    @Resource
    private PropertyCommandService propertyCommandService;

    public CriteriaQuery getCriteriaQuery(String type) {
        switch (type) {
            case "group":
                return groupCriteriaQuery;
            case "vs":
                return virtualServerCriteriaQuery;
            case "slb":
                return slbCriteriaQuery;
            case "policy":
                return trafficPolicyQuery;
            case "dr":
                return drCriteriaQuery;
            default:
                return null;
        }
    }

    public TagCommandService getTagService() {
        return tagCommandService;
    }

    public PropertyCommandService getPropertyService() {
        return propertyCommandService;
    }
}
