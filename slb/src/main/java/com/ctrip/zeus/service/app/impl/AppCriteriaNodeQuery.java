package com.ctrip.zeus.service.app.impl;

import com.ctrip.zeus.model.Property;
import com.ctrip.zeus.service.app.AppService;
import com.ctrip.zeus.service.app.CriteriaNodeQuery;
import com.ctrip.zeus.service.app.QueryNode;
import com.ctrip.zeus.service.query.GroupCriteriaQuery;
import com.ctrip.zeus.service.query.SlbCriteriaQuery;
import com.ctrip.zeus.service.query.VirtualServerCriteriaQuery;
import com.ctrip.zeus.tag.PropertyService;
import com.ctrip.zeus.tag.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

;

/**
 * Created by fanqq on 2016/9/18.
 */
@Service("appCriteriaNodeQuery")
public class AppCriteriaNodeQuery implements CriteriaNodeQuery<String> {
    @Autowired
    AppService appService;
    @Resource
    GroupCriteriaQuery groupCriteriaQuery;
    @Resource
    VirtualServerCriteriaQuery virtualServerCriteriaQuery;
    @Resource
    SlbCriteriaQuery slbCriteriaQuery;
    @Resource
    TagService tagService;
    @Resource
    PropertyService propertyService;

    private final static String RESOURCE_TYPE = "app";

    @Override
    public boolean shouldSkip(QueryNode queryNode) {
        for (String key : queryNode.getQueryParams().keySet())
            switch (key) {
                case "groupId":
                case "groupName":
                case "appId":
                case "fuzzyName":
                case "vsId":
                case "vsName":
                case "domain":
                case "slbId":
                case "slbName":
                case "anyTag":
                case "unionTag":
                case "anyProp":
                case "unionProp":
                case "prop":
                case "joinProp":
                case "tags":
                case "joinTag":
                case "targetId":
                case "targetType":
                case "queryAll":
                    return false;
                default:
                    return true;
            }
        return true;
    }

    @Override
    public Set<String> query(Map<String, List<String>> query) throws Exception {
        Set<String> res = new HashSet<>();
        if (query.size() == 0) {
            return appService.getAllAppIds();
        }
        String firstKey = query.keySet().iterator().next();
        Long[] gids, vids, sids;
        List<String> tagNames;
        switch (firstKey) {
            case "groupId":
                gids = new Long[query.get(firstKey).size()];
                for (int i = 0; i < query.get(firstKey).size(); i++) {
                    gids[i] = Long.parseLong(query.get(firstKey).get(i));
                }
                return appService.getAppIdsByGroupIds(gids);
            case "groupName":
                gids = new Long[query.get(firstKey).size()];
                for (int i = 0; i < query.get(firstKey).size(); i++) {
                    gids[i] = groupCriteriaQuery.queryByName(query.get(firstKey).get(i));
                    i++;
                }
                return appService.getAppIdsByGroupIds(gids);
            case "appId":
                res.addAll(query.get(firstKey));
                return res;
            case "vsId":
                vids = new Long[query.get(firstKey).size()];
                for (int i = 0; i < query.get(firstKey).size(); i++) {
                    vids[i] = Long.parseLong(query.get(firstKey).get(i));
                }
                return appService.getAppIdsByVsIds(vids);
            case "vsName":
                vids = new Long[query.get(firstKey).size()];
                for (int i = 0; i < query.get(firstKey).size(); i++) {
                    vids[i] = virtualServerCriteriaQuery.queryByName(query.get(firstKey).get(i));
                }
                return appService.getAppIdsByVsIds(vids);
            case "domain":
                return appService.getAppIdsByDomains(query.get(firstKey).toArray(new String[]{}));
            case "slbId":
                sids = new Long[query.get(firstKey).size()];
                for (int i = 0; i < query.get(firstKey).size(); i++) {
                    sids[i] = Long.parseLong(query.get(firstKey).get(i));
                }
                return appService.getAppIdsBySlbIds(sids);
            case "slbName":
                sids = new Long[query.get(firstKey).size()];
                for (int i = 0; i < query.get(firstKey).size(); i++) {
                    sids[i] = slbCriteriaQuery.queryByName(query.get(firstKey).get(i));
                }
                return appService.getAppIdsBySlbIds(sids);
            case "anyTag":
            case "unionTag":
                tagNames = new ArrayList<>();
                for (String tag : query.get(firstKey)) {
                    tagNames.addAll(Arrays.asList(tag.split(",")));
                }
                Set<Long> tmp = tagService.unionQuery(tagNames, "app");
                Set<String> unionTagsApp = new HashSet<>();
                for (Long l : tmp) {
                    unionTagsApp.add(l.toString());
                }
                return unionTagsApp;
            case "anyProp":
            case "unionProp":
                List<Property> unionProps = getProperties(query.get(firstKey));
                Set<Long> t = propertyService.unionQuery(unionProps, RESOURCE_TYPE);
                Set<String> unionPropsApp = new HashSet<>();
                for (Long l : t) {
                    unionPropsApp.add(l.toString());
                }
                return unionPropsApp;
            case "prop":
            case "joinProp":
                List<Property> joinProps = getProperties(query.get(firstKey));
                Set<Long> jp = propertyService.joinQuery(joinProps, RESOURCE_TYPE);
                Set<String> joinPropsApp = new HashSet<>();
                for (Long l : jp) {
                    joinPropsApp.add(l.toString());
                }
                return joinPropsApp;
            case "tags":
            case "joinTag":
                tagNames = new ArrayList<>();
                for (String tag : query.get(firstKey)) {
                    tagNames.addAll(Arrays.asList(tag.split(",")));
                }
                Set<Long> tt = tagService.joinQuery(tagNames, "app");
                Set<String> joinTagsApp = new HashSet<>();
                for (Long l : tt) {
                    joinTagsApp.add(l.toString());
                }
                return joinTagsApp;
            case "queryAll":
                return appService.getAllAppIdsInSlb();
            case "fuzzyName":
            default:
                return res;
        }
    }

    public List<Property> getProperties(List<String> prop) {
        List<Property> properties = new ArrayList<>();
        for (String s : prop) {
            String[] tmp = s.split(",");
            for (String ss : tmp) {
                int ps = ss.trim().indexOf(':');
                if (ps == -1) continue;
                properties.add(new Property().setName(ss.substring(0, ps)).setValue(ss.substring(ps + 1)));
            }
        }
        return properties;
    }
}
