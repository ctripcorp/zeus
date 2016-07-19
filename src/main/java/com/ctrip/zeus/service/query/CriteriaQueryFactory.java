package com.ctrip.zeus.service.query;

import com.ctrip.zeus.tag.PropertyService;
import com.ctrip.zeus.tag.TagService;
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
    private TagService tagService;
    @Resource
    private PropertyService propertyService;

    public CriteriaQuery getCriteriaQuery(String type) {
        switch (type) {
            case "group":
                return groupCriteriaQuery;
            case "vs":
                return virtualServerCriteriaQuery;
            case "slb":
                return slbCriteriaQuery;
            default:
                return null;
        }
    }

    public TagService getTagService() {
        return tagService;
    }

    public PropertyService getPropertyService() {
        return propertyService;
    }
}
