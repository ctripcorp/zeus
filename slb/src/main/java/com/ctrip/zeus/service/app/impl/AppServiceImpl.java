package com.ctrip.zeus.service.app.impl;

import com.ctrip.zeus.config.ConfigValueService;
import com.ctrip.zeus.dao.entity.SlbApp;
import com.ctrip.zeus.dao.entity.SlbAppExample;
import com.ctrip.zeus.dao.mapper.SlbAppMapper;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.model.App;
import com.ctrip.zeus.model.model.Domain;
import com.ctrip.zeus.model.model.Group;
import com.ctrip.zeus.model.model.VirtualServer;
import com.ctrip.zeus.service.app.AppService;
import com.ctrip.zeus.service.model.*;
import com.ctrip.zeus.service.query.GroupCriteriaQuery;
import com.ctrip.zeus.service.query.VirtualServerCriteriaQuery;
import com.ctrip.zeus.support.LanguageCheck;
import com.ctrip.zeus.support.ObjectJsonParser;
import com.ctrip.zeus.support.ObjectJsonWriter;
import com.ctrip.zeus.tag.PropertyBox;
import com.ctrip.zeus.tag.PropertyService;
import com.ctrip.zeus.tag.TagBox;
import com.ctrip.zeus.tag.TagService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * Created by fanqq on 2016/9/12.
 */
@Service("appService")
public class AppServiceImpl implements AppService {

    @Resource
    private SlbAppMapper slbAppMapper;
    @Resource
    private ArchiveRepository archiveRepository;
    @Autowired
    private GroupRepository groupRepository;
    @Resource
    private GroupCriteriaQuery groupCriteriaQuery;
    @Resource
    private VirtualServerRepository virtualServerRepository;
    @Resource
    private VirtualServerCriteriaQuery virtualServerCriteriaQuery;
    @Resource
    private PropertyBox propertyBox;
    @Resource
    private PropertyService propertyService;
    @Resource
    private TagBox tagBox;
    @Resource
    private TagService tagService;
    @Autowired
    private ConfigValueService configValueService;

    private final static String APP_STATUS = "appStatus";
    private final static String APP_STATUS_TRUE = "已接入";
    private final static String PROPERTY_TYPE = "app";

    private final static String SLB_ID = "appSlbId_";
    private final static String VS_ID = "appVsId_";
    private final static String GROUP_ID = "appGroupId_";
    private final static String DOMAIN_NAME = "appDomain_";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final static String VIRTUAL_GROUP_APP_ID = "VirtualGroup";
    private final static String DEFAULT_APP = "999999999";
    private final static String UNKNOWN = "UNKNOWN";


    @Override
    public List<App> getAllApps() throws Exception {
        return parseSlbApps(slbAppMapper.selectByExampleWithBLOBs(new SlbAppExample().createCriteria().example()));
    }

    @Override
    public List<App> getAllAppsInSlb() throws Exception {
        return getAllAppsByAppIds(getAllAppIdsInSlb());
    }

    @Override
    public Set<String> getAllAppIdsInSlb() throws Exception {
        Set<IdVersion> groupIdVersions = groupCriteriaQuery.queryAll(SelectionMode.OFFLINE_FIRST);
        List<Group> groups = groupRepository.list(groupIdVersions.toArray(new IdVersion[groupIdVersions.size()]));
        Set<String> appIds = new HashSet<>();

        List<Long> ids = propertyService.queryTargets(APP_STATUS, APP_STATUS_TRUE, PROPERTY_TYPE);
        for (Long id : ids) {
            appIds.add(id.toString());
        }

        for (Group group : groups) {
            if (group.isVirtual()) {
                continue;
            }
            appIds.add(group.getAppId());
        }
        return appIds;
    }

    @Override
    public App getAppBySbu(String sbu) throws Exception {
        return parseSlbApp(slbAppMapper.selectOneByExampleWithBLOBs(new SlbAppExample().createCriteria().andSbuEqualTo(sbu).example()));
    }

    @Override
    public Set<String> getAllAppIds() throws Exception {
        Set<String> res = new HashSet<>();
        for (SlbApp slbApp : slbAppMapper.selectByExampleWithBLOBs(new SlbAppExample().createCriteria().example())) {
            res.add(slbApp.getAppId());
        }
        return res;
    }

    @Override
    public List<App> getAllAppsByAppIds(Set<String> appIds) throws Exception {
        if (appIds == null || appIds.size() == 0) {
            return Collections.EMPTY_LIST;
        }
        return parseSlbApps(slbAppMapper.selectByExampleWithBLOBs(new SlbAppExample().createCriteria().andAppIdIn(new ArrayList<>(appIds)).example()));
    }

    @Override
    public App getAppByAppid(String appId) throws Exception {
        if (appId == null) {
            return null;
        }
        if (DEFAULT_APP.equalsIgnoreCase(appId)) {
            return getDefaultApp();
        }
        logger.info("[Model Snapshot Test]generate location Conf: Start Get APP." + appId);
        SlbApp app = slbAppMapper.selectOneByExampleWithBLOBs(new SlbAppExample().createCriteria().andAppIdEqualTo(appId).example());
        logger.info("[Model Snapshot Test]generate location Conf: End Get APP." + appId);
        if (app != null) {
            return parseSlbApp(app);
        }
        return null;
    }

    @Override
    public App getDefaultApp() throws Exception {
        App app = new App();
        app.setAppId(DEFAULT_APP);
        app.setChineseName("DEFAULT_APP");
        app.setSbu("DEFAULT");
        app.setOwner("DEFAULT");
        app.setOwnerEmail(configValueService.getAppDefaultOwnerMail());
        return app;
    }

    @Override
    public List<App> getAppsBySlbIds(Set<Long> slbIds) throws Exception {
        List<App> res = new ArrayList<>();
        if (slbIds == null || slbIds.size() == 0) {
            return res;
        }
        return getAllAppsByAppIds(getAppIds(slbIds, SLB_ID));
    }

    @Override
    public List<App> getAppsByVsIds(Set<Long> vsIds) throws Exception {
        return getAppsByTargets(vsIds, VS_ID);
    }

    @Override
    public List<App> getAppsByGroupIds(Set<Long> groupIds) throws Exception {
        return getAppsByTargets(groupIds, GROUP_ID);
    }

    @Override
    public List<App> getAppsByDomains(Set<String> domains) throws Exception {
        List<App> res = new ArrayList<>();
        if (domains == null || domains.size() == 0) {
            return res;
        }

        Set<String> appIds = getAppIds(new ArrayList<>(domains), DOMAIN_NAME);
        return getAllAppsByAppIds(appIds);
    }

    private List<App> getAppsByTargets(Set<Long> targetIds, String prefix) throws Exception {
        List<App> res = new ArrayList<>();
        if (targetIds == null || targetIds.size() == 0) {
            return res;
        }
        Set<String> appIds = getAppIds(targetIds, prefix);
        res = getAllAppsByAppIds(appIds);
        return res;
    }

    @Override
    public Set<String> getAppIdsBySlbId(Long slbId) throws Exception {
        Set<String> res = new HashSet<>();
        if (slbId == null) return res;

        res.addAll(getAppIds(Arrays.asList(slbId), SLB_ID));

        return res;
    }

    @Override
    public Set<String> getAppIdsByVsId(Long vsId) throws Exception {
        Set<String> res = new HashSet<>();
        if (vsId == null) return res;
        res.addAll(getAppIds(Arrays.asList(vsId), VS_ID));
        return res;
    }

    @Override
    public Set<String> getAppIdsByDomain(String domain) throws Exception {
        Set<String> res = new HashSet<>();
        if (domain == null) return res;
        List<String> domains = new ArrayList<>();
        domains.add(domain);
        res.addAll(getAppIds(domains, DOMAIN_NAME));

        return res;
    }

    @Override
    public String getAppIdByGroupId(Long groupId) throws Exception {
        if (groupId == null) return null;
        List<Long> appIds = tagService.query(GROUP_ID + groupId.toString(), PROPERTY_TYPE);
        if (appIds.size() == 0) return null;
        return appIds.get(0).toString();
    }

    @Override
    public Set<String> getAppIdsBySlbIds(Long[] slbIds) throws Exception {
        Set<String> res = new HashSet<>();
        if (slbIds == null) return res;
        res.addAll(getAppIds(Arrays.asList(slbIds), SLB_ID));
        return res;
    }

    @Override
    public Set<String> getAppIdsByVsIds(Long[] vsIds) throws Exception {
        Set<String> res = new HashSet<>();
        if (vsIds == null) return res;
        res.addAll(getAppIds(Arrays.asList(vsIds), VS_ID));
        return res;
    }

    @Override
    public Set<String> getAppIdsByDomains(String[] domains) throws Exception {
        Set<String> res = new HashSet<>();
        if (domains == null) return res;
        List<String> domainSet = new ArrayList<>();
        for (String domain : domains) {
            domainSet.add(domain);
        }
        res.addAll(getAppIds(domainSet, DOMAIN_NAME));
        return res;
    }

    @Override
    public Set<String> getAppIdsByGroupIds(Long[] groupIds) throws Exception {
        Set<String> res = new HashSet<>();
        if (groupIds == null) return res;
        res.addAll(getAppIds(Arrays.asList(groupIds), GROUP_ID));
        return res;
    }

    @Override
    public void refreshAllRelationTable() throws Exception {
        Set<IdVersion> groupIdVersions = groupCriteriaQuery.queryAll(SelectionMode.OFFLINE_FIRST);
        List<Group> groups = groupRepository.list(groupIdVersions.toArray(new IdVersion[groupIdVersions.size()]));
        Set<String> appIdToFresh = new HashSet<>();

        List<Long> ids = propertyService.queryTargets(APP_STATUS, APP_STATUS_TRUE, PROPERTY_TYPE);
        for (Long id : ids) {
            appIdToFresh.add(id.toString());
        }
        for (Group group : groups) {
            if (group.isVirtual()) {
                continue;
            }
            appIdToFresh.add(group.getAppId());
        }
        for (String appId : appIdToFresh) {
            refreshByAppId(appId);
        }
    }

    @Override
    public void groupChange(Long groupId) throws Exception {
        if (groupId == null) return;

        Group group = groupRepository.getById(groupId);
        if (group == null) {
            throw new ValidationException("Not Found Group By Group Id.GroupId :" + groupId);
        }
        String preAppId = getGroupAppId(groupId);

        // Refresh
        refreshByAppId(group.getAppId());
        if (preAppId != null && !group.getAppId().equals(preAppId)) {
            refreshByAppId(preAppId);
        }
    }

    @Override
    public void refreshByAppId(String appId) throws Exception {
        if (appId == null || appId.isEmpty()) return;
        if (appId.equalsIgnoreCase(VIRTUAL_GROUP_APP_ID)) return;
        Set<Long> groupIds = groupCriteriaQuery.queryByAppId(appId);
        Set<Long> slbIds = refreshApp(appId, groupIds);
        checkSlbAppRelations(appId, slbIds);
        checkAppIsInSlb(appId, groupIds);
    }

    private void checkAppIsInSlb(String appId, Set<Long> groupIds) throws Exception {
        if (isAppInSlb(appId)) {
            propertyBox.set(APP_STATUS, APP_STATUS_TRUE, PROPERTY_TYPE, Long.parseLong(appId));
        } else {
            propertyBox.clear(APP_STATUS, APP_STATUS_TRUE, PROPERTY_TYPE, Long.parseLong(appId));
        }
        try {
            App app = getAppByAppid(appId);
            if (app != null) {
                propertyBox.set("language", LanguageCheck.getLanguage(app.getContainer()), "group", groupIds.toArray(new Long[groupIds.size()]));
            }
        } catch (Exception e) {
            logger.error("Add Language Property for app failed. AppId:" + appId, e);
        }
    }

    private void checkSlbAppRelations(String appId, Set<Long> slbIds) throws Exception {
        if (slbIds == null) return;
        logger.info("[AppService]Check " + SLB_ID + " tags on app:" + appId + ", target slbids:" + slbIds.toString());

        Long longAppId = Long.parseLong(appId);
        for (String tag : tagService.getTags(PROPERTY_TYPE, longAppId)) {
            if (tag.startsWith(SLB_ID)) {
                Long slbIdLong;
                String slbId = tag.substring(SLB_ID.length(), tag.length());
                logger.info("[AppService]Found " + SLB_ID + " tag on current app:" + appId + ", tag name:" + tag + ", tag target slb id:" + slbId);
                try {
                    slbIdLong = Long.parseLong(slbId);
                    if (!slbIds.contains(slbIdLong)) {
                        tagBox.untagging(tag, PROPERTY_TYPE, new Long[]{longAppId});
                        logger.info("[AppService]Untagging tag: " + tag + " on slb:" + slbId);
                    }
                } catch (NumberFormatException ne) {
                    tagBox.untagging(tag, PROPERTY_TYPE, new Long[]{longAppId});
                }
            }
        }

        if (slbIds.size() > 0) {
            for (Long slbId : slbIds) {
                tagBox.tagging(SLB_ID + slbId, PROPERTY_TYPE, new Long[]{longAppId});
                logger.info("[AppService]Slb-App Relation tagging. AppId:" + appId + " SlbIds:" + slbIds.toString());
            }
        }
    }

    @Override
    public void groupDelete(Long groupId) throws Exception {
        if (groupId == null) return;
        Group group = archiveRepository.getGroupArchive(groupId, 0);
        if (group == null) {
            throw new ValidationException("Not Found Archive Group By Group Id.GroupId :" + groupId);
        }

        String preAppId = getGroupAppId(groupId);

        // refresh
        refreshByAppId(group.getAppId());
        if (preAppId != null && !group.getAppId().equals(preAppId)) {
            refreshByAppId(preAppId);
        }
    }

    @Override
    public void vsChange(Long vsId) throws Exception {
        VirtualServer virtualServer = virtualServerRepository.getById(vsId);
        Set<String> domains = new HashSet<>();
        for (Domain d : virtualServer.getDomains()) {
            domains.add(d.getName());
        }

        VirtualServer vs = virtualServerRepository.getById(vsId);
        if (vs == null) {
            throw new ValidationException("Not Found Archive Vs By Vs Id.VsId :" + vsId);
        }
        Set<IdVersion> idVersions = groupCriteriaQuery.queryByVsId(vsId);
        for (IdVersion idVersion : idVersions) {
            Group group = groupRepository.getByKey(idVersion);
            refreshByAppId(group.getAppId());
        }
    }

    @Override
    public void updateAllApp() throws Exception {

    }

    @Override
    public void updateApps(Set<String> appIds) throws Exception {
        if (appIds == null || appIds.size() == 0) {
            return;
        }
        for (String appId : appIds) {
            updateApp(appId);
        }
    }

    @Override
    public App updateApp(String appId) throws Exception {
        if (appId == null) {
            return null;
        }
        App target = new App().setAppId(appId).setChineseName(UNKNOWN).setOwner(UNKNOWN).setOwnerEmail(UNKNOWN).setSbu(UNKNOWN).setEnglishName(UNKNOWN).setDescription(UNKNOWN);
        slbAppMapper.insertOrUpdateByAppId(SlbApp.builder().appId(appId).sbu(UNKNOWN).content(ObjectJsonWriter.write(target)).build());
        return null;
    }

    @Override
    public boolean hasApp(String appId) throws Exception {
        boolean result = false;
        SlbApp app = slbAppMapper.selectOneByExampleSelective(new SlbAppExample().createCriteria().andAppIdEqualTo(appId).example(), SlbApp.Column.appId);
        if (app != null) {
            result = true;
        }
        return result;
    }

    @Override
    public boolean isAppInSlb(String appId) throws Exception {
        List<String> tags = tagService.getTags(PROPERTY_TYPE, Long.parseLong(appId));
        for (String tag : tags) {
            if (tag.indexOf(GROUP_ID) == 0) return true;
        }
        return false;
    }

    private void addAppOwnerTag(String appOwner, Long appId) {
        try {
            if (appOwner == null) {
                tagBox.tagging("owner_unknown", PROPERTY_TYPE, new Long[]{appId});
            } else {
                tagBox.tagging("owner_" + appOwner, PROPERTY_TYPE, new Long[]{appId});
            }
        } catch (Exception e) {
            logger.error("Parse Long Failed. AppId:" + appId, e);
        }
    }

    private void addAppOwnerTag(List<App> apps) {
        if (apps != null && apps.size() > 0) {
            for (App app : apps) {
                addAppOwnerTag(app.getOwner(), Long.parseLong(app.getAppId()));
            }
        }
    }

    // App to app do
    private List<SlbApp> toSlbApps(List<App> apps) throws Exception {
        List<SlbApp> result = new ArrayList<>();
        for (App app : apps) {
            result.add(toSlbApp(app));
        }
        return result;
    }

    private SlbApp toSlbApp(App app) throws Exception {
        app.setChineseName(app.getChineseName() == null ? "unknown" : app.getChineseName());
        app.setEnglishName(app.getEnglishName() == null ? "unknown" : app.getEnglishName());
        app.setSbu(app.getSbu() == null ? "unknown" : app.getSbu());
        app.setSbuCode(app.getSbuCode() == null ? "unknown" : app.getSbuCode());
        app.setOwnerEmail(app.getOwnerEmail() == null ? "unknown" : app.getOwnerEmail());
        app.setOwner(app.getOwner() == null ? "unknown" : app.getOwner());
        return SlbApp.builder().
                appId(app.getAppId()).
                sbu(app.getSbu() == null ? "unknown" : app.getSbu())
                .content(ObjectJsonWriter.write(app)).build();
    }

    // Appdo to app
    private List<App> parseSlbApps(List<SlbApp> apps) {
        if (apps == null) return null;
        List<App> result = new ArrayList<>();
        for (SlbApp app : apps) {
            String content = app.getContent();
            result.add(ObjectJsonParser.parse(content, App.class));
        }
        return result;
    }

    private App parseSlbApp(SlbApp app) {
        if (app == null) return null;
        String content = app.getContent();
        return ObjectJsonParser.parse(content, App.class);
    }

    // Unit methods
    private String getGroupAppId(Long groupId) throws Exception {
        List<Long> targetIds = tagService.query(GROUP_ID + groupId, PROPERTY_TYPE);
        if (targetIds.size() > 0) {
            return targetIds.get(0).toString();
        }

        return null;
    }

    private Set<String> convertSet(Set<Long> targetIds) {
        Set<String> result = new HashSet<>();
        for (Long groupId : targetIds) {
            result.add(groupId.toString());
        }
        return result;
    }

    private void resetAppTags(List<String> appIds, String appId, Collection<String> targetIds, String prefix) throws Exception {
        if (!appIds.contains(appId)) {
            for (Object targetId : targetIds) {
                if (tagService.query(prefix + targetId, PROPERTY_TYPE).size() == 0) {
                    tagBox.tagging(prefix + targetId, PROPERTY_TYPE, new Long[]{Long.parseLong(appId)});
                    logger.info("[AppService] Add New APP Tag. AppId:" + appId + " Target:" + targetId);
                }
            }
        } else {
            // clear old tag first
            // tagging again
            // because of tag value might changed. such as domain of vses
            List<String> oldTags = new ArrayList<>();
            List<String> backedTags = new ArrayList<>();
            List<String> tobeAddedTags = new ArrayList<>();
            for (String s : tagService.getTags(PROPERTY_TYPE, Long.parseLong(appId))) {
                if (s.startsWith(prefix)) {
                    backedTags.add(s);
                    oldTags.add(s);
                }
            }
            for (Object targetId : targetIds) {
                tobeAddedTags.add(prefix+targetId);
            }

            oldTags.retainAll(tobeAddedTags);
            // to be removed
            backedTags.removeIf(c->oldTags.contains(c));
            // to be added
            tobeAddedTags.removeIf(c->oldTags.contains(c));

            for (String tag : backedTags) {
                tagBox.untagging(tag, PROPERTY_TYPE, new Long[]{Long.parseLong(appId)});
                logger.info("[AppService] Untagging tag:" + tag + " for app:" + appId);
            }

            for (String tag : tobeAddedTags) {
                tagBox.tagging(tag, PROPERTY_TYPE, new Long[]{Long.parseLong(appId)});
                logger.info("[AppService] Tagging tag:" + tag + " for app:" + appId);
            }

            appIds.remove(appId);
        }
        for (String app : appIds) {
            for (Object targetId : targetIds) {
                tagBox.untagging(prefix + targetId, PROPERTY_TYPE, new Long[]{Long.parseLong(app)});
                logger.info("[AppService]Clear APP Tag. AppId:" + app + " Target:" + targetId);
            }
        }
    }

    private Set<Long> refreshApp(String appId, Set<Long> groupIds) throws Exception {
        Set<Long> slbIds = new HashSet<>();
        Long appIdLong = Long.parseLong(appId);

        if (groupIds == null || groupIds.size() == 0) {
            clearTag(appIdLong, GROUP_ID);
            clearTag(appIdLong, VS_ID);
            clearTag(appIdLong, SLB_ID);
            clearTag(appIdLong, DOMAIN_NAME);
            propertyBox.clear(APP_STATUS, APP_STATUS_TRUE, PROPERTY_TYPE, appIdLong);
            logger.info("Not Found Group By AppId. Delete All Relations By Appid:" + appId);
            return null;
        }

        // 1. Check group-app relations.
        List<App> apps = getAppsByGroupIds(groupIds);
        List<String> groupAppIds = new ArrayList<>();
        for (App app : apps) {
            groupAppIds.add(app.getAppId());
        }
        resetAppTags(groupAppIds, appId, convertSet(groupIds), GROUP_ID);
        // 2. check vs-app relations. remove changed or deleted relations. add new relations.
        Set<IdVersion> idVersions = groupCriteriaQuery.queryByIdsAndMode(groupIds.toArray(new Long[groupIds.size()]), SelectionMode.OFFLINE_FIRST);
        Set<Long> vsIds = virtualServerCriteriaQuery.queryByGroup(idVersions.toArray(new IdVersion[idVersions.size()]));
        apps = getAppsByVsIds(vsIds);
        List<String> vsAppIds = new ArrayList<>();
        for (App app : apps) {
            vsAppIds.add(app.getAppId());
        }
        resetAppTags(vsAppIds, appId, convertSet(vsIds), VS_ID);

        // 3. check domain-app relations.
        Set<IdVersion> keys = virtualServerCriteriaQuery.queryByIdsAndMode(vsIds.toArray(new Long[vsIds.size()]), SelectionMode.OFFLINE_FIRST);
        List<VirtualServer> virtualServers = virtualServerRepository.listAll(keys.toArray(new IdVersion[keys.size()]));
        Set<String> domains = new TreeSet<>();
        for (VirtualServer vs : virtualServers) {
            for (Domain d : vs.getDomains()) {
                domains.add(DOMAIN_NAME + d.getName());
            }
            logger.info("[AppService] Found SLB IDS:" + vs.getSlbIds() + " from vs ids:" + vs.getId());
            slbIds.addAll(vs.getSlbIds());
        }
        apps = getAppsByDomains(domains);
        List<String> domainAppIds = new ArrayList<>();
        for (App app : apps) {
            domainAppIds.add(app.getAppId());
        }
        resetAppTags(domainAppIds, appId, domains, DOMAIN_NAME);
        return slbIds;
    }

    private Set<String> getAppIds(List<String> tagNames, String type) throws Exception {
        Set<String> result = new TreeSet<>();
        if (tagNames == null || tagNames.size() == 0) return result;

        if (type != null) {
            for (int s = 0; s < tagNames.size(); s++) {
                tagNames.set(s, type + tagNames.get(s));
            }
        }
        for (Long appId : tagService.unionQuery(tagNames, PROPERTY_TYPE)) {
            result.add(appId.toString());
        }
        return result;
    }

    private Set<String> getAppIds(Collection<Long> ids, String type) throws Exception {
        List<String> searches = new ArrayList<>();
        for (Long id : ids) {
            searches.add(type + id.toString());
        }
        return getAppIds(searches, null);
    }

    private void clearTag(Long appId, String prefix) throws Exception {
        List<String> tags = tagService.getTags(PROPERTY_TYPE, appId);
        if (tags.size() == 0) return;

        for (String tag : tags) {
            if (tag.toLowerCase().startsWith(prefix.toLowerCase())) {
                tagBox.untagging(tag, PROPERTY_TYPE, new Long[]{appId});
            }
        }
    }
}
