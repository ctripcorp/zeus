package com.ctrip.zeus.service.verify;

import com.ctrip.zeus.auth.entity.User;
import com.ctrip.zeus.auth.entity.UserList;
import com.ctrip.zeus.model.Property;
import com.ctrip.zeus.model.model.*;
import com.ctrip.zeus.restful.message.view.*;
import com.ctrip.zeus.service.app.AppQueryEngine;
import com.ctrip.zeus.service.app.AppService;
import com.ctrip.zeus.service.app.impl.AppCriteriaNodeQuery;
import com.ctrip.zeus.service.auth.UserService;
import com.ctrip.zeus.service.model.*;
import com.ctrip.zeus.service.model.impl.RepositoryContext;
import com.ctrip.zeus.service.query.CriteriaQueryFactory;
import com.ctrip.zeus.service.query.QueryEngine;
import com.ctrip.zeus.service.query.TrafficPolicyQuery;
import com.ctrip.zeus.service.query.command.TrafficPolicyCommand;
import com.ctrip.zeus.service.verify.verifier.IllegalDataVerifier;
import com.ctrip.zeus.service.verify.verifier.PropertyValueUtils;
import com.ctrip.zeus.tag.PropertyService;
import com.ctrip.zeus.tag.TagService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

/**
 * @Discription
 **/
@Component("verifyManager")
public class VerifyManager {

    @Resource
    private VerifyResultHandler verifyResultHandler;
    @Resource
    private AppCriteriaNodeQuery appCriteriaNodeQuery;
    @Resource
    private AppQueryEngine appQueryEngine;
    @Autowired
    private AppService appService;
    @Resource
    private TrafficPolicyQuery trafficPolicyQuery;
    @Resource
    private ViewDecorator viewDecorator;
    @Resource
    private TrafficPolicyRepository trafficPolicyRepository;
    @Resource
    private CriteriaQueryFactory criteriaQueryFactory;
    @Resource
    private SlbRepository slbRepository;
    @Resource
    private VirtualServerRepository virtualServerRepository;
    @Autowired
    private GroupRepository groupRepository;
    @Resource
    private UserService userService;
    @Resource
    private TagService tagService;
    @Resource
    private PropertyService propertyService;

    private final Logger logger = LoggerFactory.getLogger(VerifyManager.class);

    public static final Map<String, String> MARK_NAME_ITEM_TYPE_MAP = new HashMap<>();

    public static final Map<String, String> ILLEGAL_TYPE_MARK_NAME_MAP = new HashMap<>();

    private final static Map<String, IllegalDataVerifier> MARK_NAME_VERIFIER_MAP = new HashMap<>();

    public void run() throws Exception {
        if (MARK_NAME_VERIFIER_MAP.size() <= 0) {
            return;
        }
        VerifyContext context = loadData();
        List<VerifyResult> results = new ArrayList<>();
        for (IllegalDataVerifier verifier : MARK_NAME_VERIFIER_MAP.values()) {
            verifier.setContext(context);
            results.addAll(verifier.verify());
        }
        if (results.size() > 0) {
            verifyResultHandler.handle(results);
        }
    }

    public VerifyContext loadData() {
        DefaultVerifyContext context = new DefaultVerifyContext();
        context.setAppList(loadApps());
        context.setUserList(loadUsers());
        context.setPolicyList(loadPolicies());
        context.setSlbListView(loadSlb());
        context.setVsListView(loadVses());
        context.setGroupListView(loadGroups());
        return context;
    }

    public static void addVerifier(IllegalDataVerifier verifier) {
        MARK_NAME_VERIFIER_MAP.put(verifier.getMarkName(), verifier);
    }

    public Map<String, List<IllegalDataUnit>> getIllegalData(List<String> types)
            throws Exception {
        Map<String, List<IllegalDataUnit>> result = new HashMap<>();
        if (types == null) {
            types = new ArrayList<>();
        }
        if (types.size() == 0) {
            types.addAll(ILLEGAL_TYPE_MARK_NAME_MAP.keySet());
        }

        for (String type : types) {
            if (ILLEGAL_TYPE_MARK_NAME_MAP.containsKey(type)) {
                String markName = ILLEGAL_TYPE_MARK_NAME_MAP.get(type);
                IllegalDataVerifier verifier = MARK_NAME_VERIFIER_MAP.get(markName);
                if (verifier != null) {
                    if (verifier.getMarkType().equals(IllegalMarkTypes.TAG)) {
                        result.put(type, getDataViaTagName(markName));
                    } else if (verifier.getMarkType().equals(IllegalMarkTypes.PROPERTY)) {
                        result.put(type, getDataViaPropertyKey(markName));
                    }
                }
            }
        }
        return result;
    }

    /*
     * @Description Get all item ids that have tag with named tagName.
     * @return: see getDataViaPropertyKey method.
     **/
    private List<IllegalDataUnit> getDataViaTagName(String tagName) throws Exception {
        List<IllegalDataUnit> data = new LinkedList<>();
        if (MARK_NAME_VERIFIER_MAP.containsKey(tagName)) {
            String itemType = MARK_NAME_ITEM_TYPE_MAP.get(tagName);
            List<Long> itemIds = tagService.query(tagName, itemType);
            for (Long itemId : itemIds) {
                IllegalDataUnit dataUnit = new IllegalDataUnit(Arrays.asList(new IdItemType(itemId, itemType)), null);
                data.add(dataUnit);
            }
        }
        return data;
    }

    /*
     * @Description: Get all item ids that have property named propertyKey
     * and related info. Example includes: dr group whose configuration is different,
     * or vs who share part of domains.
     * @return
     **/
    public List<IllegalDataUnit> getDataViaPropertyKey(String propertyKey)
            throws Exception {
        List<IllegalDataUnit> data = new LinkedList<>();

        if (MARK_NAME_VERIFIER_MAP.containsKey(propertyKey)) {
            IllegalDataVerifier verifier = MARK_NAME_VERIFIER_MAP.get(propertyKey);
            String itemType = verifier.getTargetItemType();

            Set<Long> itemIds = propertyService.queryTargets(propertyKey, itemType);
            Map<Long, Property> itemPropertyMap = propertyService.getProperties(
                    propertyKey, itemType, itemIds.toArray(new Long[itemIds.size()]));

            for (Long itemId : itemPropertyMap.keySet()) {
                List<IdItemType> idItemTypes = new ArrayList<>();
                idItemTypes.add(new IdItemType(itemId, itemType));
                idItemTypes.addAll(PropertyValueUtils.read(itemPropertyMap.get(itemId).getValue()));
                data.add(new IllegalDataUnit(idItemTypes, null));
            }
        }
        return data;
    }

    private GroupListView loadGroups() {
        try {
            Queue<String[]> renderedQueries = new LinkedList<>();

            QueryEngine queryRender = new QueryEngine(renderedQueries, "group", SelectionMode.getMode(null));// todo load offline first?
            queryRender.init(true);
            IdVersion[] searchKeys = queryRender.run(criteriaQueryFactory);

            Long[] groupIds = new Long[searchKeys.length];
            for (int i = 0; i < searchKeys.length; i++) {
                groupIds[i] = searchKeys[i].getId();
            }

            List<Group> result = groupRepository.list(searchKeys, new RepositoryContext(ViewConstraints.INFO.equalsIgnoreCase("extended"), SelectionMode.getMode(null)));
            ExtendedView.ExtendedGroup[] viewArray = new ExtendedView.ExtendedGroup[result.size()];

            for (int i = 0; i < result.size(); i++) {
                viewArray[i] = new ExtendedView.ExtendedGroup(result.get(i));
            }

            viewDecorator.decorate(viewArray, "group");

            GroupListView listView = new GroupListView(result.size());
            for (int i = queryRender.getOffset(); i < queryRender.getOffset() + queryRender.getLimit(viewArray.length); i++) {
                listView.add(viewArray[i]);
            }

            return listView;
        } catch (Exception e) {
            logger.warn("Exception happen when load group data. Message is " + e.getMessage());
        }
        return new GroupListView();
    }


    private VsListView loadVses() {
        try {
            QueryEngine queryRender = new QueryEngine(new ArrayDeque<>(), "vs", SelectionMode.getMode(null));
            queryRender.init(true);
            IdVersion[] searchKeys = queryRender.run(criteriaQueryFactory);

            Long[] vsIdsArray = new Long[searchKeys.length];
            for (int i = 0; i < searchKeys.length; i++) {
                vsIdsArray[i] = searchKeys[i].getId();
            }

            List<VirtualServer> result = virtualServerRepository.listAll(searchKeys);
            ExtendedView.ExtendedVs[] viewArray = new ExtendedView.ExtendedVs[result.size()];

            for (int i = 0; i < result.size(); i++) {
                viewArray[i] = new ExtendedView.ExtendedVs(result.get(i));
            }

            viewDecorator.decorate(viewArray, "vs");

            VsListView listView = new VsListView(result.size());
            for (int i = queryRender.getOffset(); i < queryRender.getOffset() + queryRender.getLimit(viewArray.length); i++) {
                listView.add(viewArray[i]);
            }

            return listView;
        } catch (Exception e) {
            logger.warn("Exception happen when load vs data. Message is " + e.getMessage());
        }
        return new VsListView();
    }

    private SlbListView loadSlb() {
        try {
            QueryEngine queryRender = new QueryEngine(new ArrayDeque<>(), "slb", SelectionMode.getMode(null));
            queryRender.init(true);
            IdVersion[] searchKeys = queryRender.run(criteriaQueryFactory);

            Long[] slbIdsArray = new Long[searchKeys.length];
            for (int i = 0; i < searchKeys.length; i++) {
                slbIdsArray[i] = searchKeys[i].getId();
            }

            List<Slb> result = slbRepository.list(searchKeys);
            ExtendedView.ExtendedSlb[] viewArray = new ExtendedView.ExtendedSlb[result.size()];

            for (int i = 0; i < result.size(); i++) {
                viewArray[i] = new ExtendedView.ExtendedSlb(result.get(i));
            }

            viewDecorator.decorate(viewArray, "slb");

            SlbListView listView = new SlbListView(result.size());
            for (int i = queryRender.getOffset(); i < queryRender.getOffset() + queryRender.getLimit(viewArray.length); i++) {
                listView.add(viewArray[i]);
            }
            return listView;
        } catch (Exception e) {
            logger.warn("Exception happen when load slb data. Message is " + e.getMessage());
        }
        return new SlbListView();
    }

    private TrafficPolicyListView loadPolicies() {
        try {
            TrafficPolicyCommand command = new TrafficPolicyCommand();

            SelectionMode selectionMode = SelectionMode.getMode(null);
            IdVersion[] searchKeys = trafficPolicyQuery.queryByCommand(command, selectionMode);
            if (searchKeys == null) {
                searchKeys = trafficPolicyQuery.queryAll(selectionMode).toArray(new IdVersion[]{});
            }

            List<Long> ids = new ArrayList<>();
            for (IdVersion idv : searchKeys) {
                ids.add(idv.getId());
            }

            List<TrafficPolicy> result = trafficPolicyRepository.list(searchKeys);

            List<ExtendedView.ExtendedTrafficPolicy> viewArray = new ArrayList<>(result.size());
            for (TrafficPolicy e : result) {
                viewArray.add(new ExtendedView.ExtendedTrafficPolicy(e));
            }

            viewDecorator.decorate(viewArray, "policy");

            TrafficPolicyListView listView = new TrafficPolicyListView(result.size());
            for (ExtendedView.ExtendedTrafficPolicy e : viewArray) {
                listView.add(e);
            }

            return listView;
        } catch (Exception e) {
            logger.warn("Exception happen when load user data. Message is " + e.getMessage());
        }
        return new TrafficPolicyListView();
    }

    private UserList loadUsers() {
        try {
            List<User> users = userService.getUsersSimpleInfo();
            UserList userList = new UserList();
            userList.getUsers().addAll(users);
            userList.setTotal(users.size());
            return userList;
        } catch (Exception e) {
            logger.warn("Exception happen when load user data. Message is " + e.getMessage());

        }
        return new UserList();
    }

    private AppList loadApps() {
        try {
            Set<String> apps = appQueryEngine.executeQuery(appCriteriaNodeQuery, appQueryEngine.parseQueryNode(null));
            AppList result = new AppList();
            result.getApps().addAll(appService.getAllAppsByAppIds(apps));
            result.setTotal(result.getApps().size());
            return result;
        } catch (Exception e) {
            logger.warn("Exception happen when load app data. Message is " + e.getMessage());
        }
        return new AppList();
    }

    private static class DefaultVerifyContext implements VerifyContext {

        private AppList appList;

        private com.ctrip.zeus.auth.entity.UserList userList;

        private TrafficPolicyListView policyList;

        private SlbListView slbListView;

        private VsListView vsListView;

        private GroupListView groupListView;

        public void setAppList(AppList appList) {
            this.appList = appList;
        }

        public void setUserList(UserList userList) {
            this.userList = userList;
        }

        public void setPolicyList(TrafficPolicyListView policyList) {
            this.policyList = policyList;
        }

        public void setSlbListView(SlbListView slbListView) {
            this.slbListView = slbListView;
        }

        public void setVsListView(VsListView vsListView) {
            this.vsListView = vsListView;
        }

        public void setGroupListView(GroupListView groupListView) {
            this.groupListView = groupListView;
        }

        @Override
        public AppList getApps() {
            return appList;
        }

        @Override
        public UserList getUsers() {
            return userList;
        }

        @Override
        public TrafficPolicyListView getPolicies() {
            return policyList;
        }

        @Override
        public SlbListView getSlbs() {
            return slbListView;
        }

        @Override
        public VsListView getVses() {
            return vsListView;
        }

        @Override
        public GroupListView getGroups() {
            return groupListView;
        }
    }
}
