package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.auth.impl.AuthorizeException;
import com.ctrip.zeus.exceptions.NotFoundException;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.executor.TaskManager;
import com.ctrip.zeus.lock.DbLockFactory;
import com.ctrip.zeus.lock.DistLock;
import com.ctrip.zeus.model.model.*;
import com.ctrip.zeus.model.task.OpsTask;
import com.ctrip.zeus.restful.message.QueryParamRender;
import com.ctrip.zeus.restful.message.ResponseHandler;
import com.ctrip.zeus.restful.message.TrimmedQueryParam;
import com.ctrip.zeus.restful.message.view.ExtendedView;
import com.ctrip.zeus.restful.message.view.RuleListView;
import com.ctrip.zeus.restful.message.view.ViewConstraints;
import com.ctrip.zeus.restful.message.view.ViewDecorator;
import com.ctrip.zeus.service.auth.AuthDefaultValues;
import com.ctrip.zeus.service.auth.AuthService;
import com.ctrip.zeus.service.auth.ResourceDataType;
import com.ctrip.zeus.service.auth.ResourceOperationType;
import com.ctrip.zeus.service.message.queue.MessageQueue;
import com.ctrip.zeus.service.model.*;
import com.ctrip.zeus.service.model.common.RuleTargetType;
import com.ctrip.zeus.service.query.CriteriaQueryFactory;
import com.ctrip.zeus.service.query.QueryEngine;
import com.ctrip.zeus.service.query.RuleCriteriaQuery;
import com.ctrip.zeus.service.query.command.PropQueryCommand;
import com.ctrip.zeus.service.query.command.RuleQueryCommand;
import com.ctrip.zeus.service.query.command.TagQueryCommand;
import com.ctrip.zeus.service.task.constant.TaskOpsType;
import com.ctrip.zeus.support.ObjectJsonParser;
import com.ctrip.zeus.support.ObjectJsonWriter;
import com.ctrip.zeus.util.MessageUtil;
import com.ctrip.zeus.util.UserUtils;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.netflix.config.DynamicLongProperty;
import com.netflix.config.DynamicPropertyFactory;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.*;

@Component
@Path("/")
public class RuleResource {

    @Resource
    private RuleRepository ruleRepository;

    @Resource
    private ResponseHandler responseHandler;

    @Resource
    private CriteriaQueryFactory criteriaQueryFactory;

    @Resource
    private RuleCriteriaQuery ruleCriteriaQuery;

    @Resource
    private ViewDecorator viewDecorator;

    @Resource
    private DbLockFactory dbLockFactory;

    @Resource
    private AuthService authService;

    @Resource
    private MessageQueue messageQueue;

    @Autowired
    private GroupRepository groupRepository;

    @Resource
    private VirtualServerRepository virtualServerRepository;

    @Resource
    private SlbRepository slbRepository;

    @Resource
    private TaskManager taskManager;

    @Resource
    private EntityFactory entityFactory;

    @Resource
    private TrafficPolicyRepository trafficPolicyRepository;

    private final int timeout = 1000;

    private static DynamicLongProperty apiTimeout = DynamicPropertyFactory.getInstance().getLongProperty("api.timeout", 30000L);

    private static Map<RuleTargetType, String> lockKeys = new HashMap<>();

    static {
        lockKeys.put(RuleTargetType.SLB, "_updateSlb");
        lockKeys.put(RuleTargetType.GROUP, "_updateGroup");
        lockKeys.put(RuleTargetType.VS, "_updateVs");
        lockKeys.put(RuleTargetType.TRAFFIC_POLICY, "_updatePolicy");
    }

    @POST
    @Path("/rule/batch/set")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "*/*"})
    public Response batchSet(@Context HttpHeaders hh,
                             @Context HttpServletRequest request,
                             String requestBody) throws Exception {
        Rules rules = ObjectJsonParser.parse(requestBody, Rules.class);
        validateRuleArray(rules.getRules());

        // group all the rules
        Map<String, Rules> sets = new HashMap<>();
        for (Rule rule : rules.getRules()) {
            String key = String.format("%s_%s", rule.getTargetId(), rule.getTargetType());
            if (sets.get(key) == null) sets.put(key, new Rules());
            sets.get(key).addRule(rule).setTargetId(rule.getTargetId()).setTargetType(rule.getTargetType());
        }

        List<Rule> result = new ArrayList<>();
        for (Rules current : sets.values()) {
            result.addAll(setRules(current, request, true));
        }
        return responseHandler.handleSerializedValue(ObjectJsonWriter.write(result), hh.getMediaType());
    }

    @POST
    @Path("/rule/set")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "*/*"})
    public Response set(@Context HttpHeaders hh,
                        @Context HttpServletRequest request,
                        String requestBody) throws Exception {


        // validate
        Rules rules = ObjectJsonParser.parse(requestBody, Rules.class);

        return responseHandler.handleSerializedValue(ObjectJsonWriter.write(setRules(rules, request, false)), hh.getMediaType());
    }

    @POST
    @Path("/rule/new")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "*/*"})
    public Response add(@Context HttpHeaders hh,
                        @Context HttpServletRequest request,
                        String requestBody) throws Exception {
        Rule addedRule;

        // validate
        Rule rule = ObjectJsonParser.parse(requestBody, Rule.class);
        if (rule == null) {
            throw new ValidationException("Invalid post entity. Fail to parse json to rule.");
        }
        String ruleTargetId = rule.getTargetId();
        String targetType = rule.getTargetType();
        validateRuleTarget(ruleTargetId, targetType);

        //  auth
        RuleTargetType ruleTargetType = RuleTargetType.getTargetType(targetType);
        authRequest(ruleTargetId, ruleTargetType, request);
        if (ruleTargetType.equals(RuleTargetType.DEFAULT)) {
            authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.NEW, ResourceDataType.Rule, AuthDefaultValues.ALL);
        }

        // save
        DistLock lock = null;
        String lockKey = getUpdateTargetLockKey(targetType, ruleTargetId);
        if (lockKey != null) {
            lock = dbLockFactory.newLock(lockKey);
            lock.lock(timeout);
        }
        try {
            addedRule = ruleRepository.add(rule);
        } finally {
            if (lock != null) {
                lock.unlock();
            }
        }

        // message
        if (ruleTargetType.isNeedTarget()) {
            // target id has been validate in new stage
            List<Rule> rules = new ArrayList<>();
            rules.add(addedRule);
            sendUpdateTargetMessage(request, ruleTargetType, RuleTargetType.parseLongTargetId(ruleTargetId), "/api/rule/new", rules);
        }
        return responseHandler.handleSerializedValue(ObjectJsonWriter.write(addedRule), hh.getMediaType());
    }

    @POST
    @Path("/rule/batch/new")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "*/*"})
    public Response batchAdd(@Context HttpHeaders hh,
                             @Context HttpServletRequest request,
                             String requestBody) throws Exception {
        List<Rule> addedRules;

        // validate
        Rules rules = ObjectJsonParser.parse(requestBody, Rules.class);
        validateRules(rules);

        // auth
        Rule rule = rules.getRules().get(0);
        String targetId = rule.getTargetId();
        String targetType = rule.getTargetType();

        authRequest(targetId, targetType, request);
        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.NEW, ResourceDataType.Rule, AuthDefaultValues.ALL);

        // save
        DistLock lock = null;
        String lockKey = getUpdateTargetLockKey(targetType, targetId);
        if (lockKey != null) {
            lock = dbLockFactory.newLock(lockKey);
            lock.lock(timeout);
        }
        try {
            addedRules = ruleRepository.add(rules.getRules());
        } finally {
            if (lock != null) {
                lock.unlock();
            }
        }


        // message
        RuleTargetType ruleTargetType = RuleTargetType.getTargetType(targetType);
        if (ruleTargetType.isNeedTarget()) {
            sendUpdateTargetMessage(request, ruleTargetType, RuleTargetType.parseLongTargetId(targetId), "/api/rule/new", addedRules);
        }

        return responseHandler.handleSerializedValue(ObjectJsonWriter.write(addedRules), hh.getMediaType());
    }

    @POST
    @Path("/rule/update")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "*/*"})
    public Response update(@Context HttpHeaders hh,
                           @Context HttpServletRequest request,
                           String requestBody) throws Exception {
        Rule updatedRule;

        // validate
        Rule rule = ObjectJsonParser.parse(requestBody, Rule.class);
        if (rule == null) {
            throw new ValidationException("Could not parse rule request body");
        }
        String targetId = rule.getTargetId();
        String targetType = rule.getTargetType();
        validateRuleTarget(targetId, targetType);

        // auth
        authRequest(targetId, targetType, request);
        RuleTargetType ruleTargetType = RuleTargetType.getTargetType(targetType);
        if (ruleTargetType.equals(RuleTargetType.DEFAULT)) {
            authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.UPDATE, ResourceDataType.Rule, rule.getId());
        }

        // update
        DistLock lock = null;
        String lockKey = getUpdateTargetLockKey(targetType, targetId);
        if (lockKey != null) {
            lock = dbLockFactory.newLock(lockKey);
            lock.lock(timeout);
        }
        try {
            updatedRule = ruleRepository.update(rule);
        } finally {
            if (lock != null) {
                lock.unlock();
            }
        }

        // message
        if (ruleTargetType.isNeedTarget()) {
            List<Rule> rules = new ArrayList<>();
            rules.add(updatedRule);
            sendUpdateTargetMessage(request, ruleTargetType, RuleTargetType.parseLongTargetId(targetId), "/api/rule/update", rules);
        }
        return responseHandler.handleSerializedValue(ObjectJsonWriter.write(updatedRule), hh.getMediaType());
    }


    @POST
    @Path("/rule/batch/update")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "*/*"})
    public Response batchUpdate(@Context HttpHeaders hh,
                                @Context HttpServletRequest request,
                                String requestBody) throws Exception {
        List<Rule> updatedRules;

        // validate
        Rules rules = ObjectJsonParser.parse(requestBody, Rules.class);
        validateRules(rules);

        // get lock
        Rule rule = rules.getRules().get(0);
        String targetType = rule.getTargetType();
        String targetId = rule.getTargetId();
        RuleTargetType ruleTargetType = RuleTargetType.getTargetType(targetType);

        // auth
        int index = 0;
        Long[] ruleIds = new Long[rules.getRules().size()];
        for (Rule r : rules.getRules()) {
            ruleIds[index] = r.getId();
            index++;
        }
        authRequest(targetId, targetType, request);
        if (ruleTargetType.equals(RuleTargetType.DEFAULT)) {
            authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.UPDATE, ResourceDataType.Rule, ruleIds);
        }

        // update
        DistLock lock = null;
        String lockKey = getUpdateTargetLockKey(targetType, targetId);
        if (lockKey != null) {
            lock = dbLockFactory.newLock(lockKey);
            lock.lock(timeout);
        }
        try {
            updatedRules = ruleRepository.update(rules.getRules());
        } finally {
            if (lock != null) {
                lock.unlock();
            }
        }

        // message
        if (ruleTargetType.isNeedTarget()) {
            sendUpdateTargetMessage(request, ruleTargetType, RuleTargetType.parseLongTargetId(targetId), "/api/rule/update", updatedRules);
        }
        return responseHandler.handleSerializedValue(ObjectJsonWriter.write(updatedRules), hh.getMediaType());
    }


    @GET
    @Path("/rules")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response list(@Context final HttpHeaders hh,
                         @Context HttpServletRequest request,
                         @TrimmedQueryParam("type") final String type,
                         @QueryParam("targetId") final String targetId,
                         @TrimmedQueryParam("targetType") final String targetType,
                         @Context UriInfo uriInfo) throws Exception {
        List<Rule> results;

        if (targetId != null && targetType == null) {
            throw new ValidationException("Target type is required");
        }

        if (targetType != null) {
            RuleTargetType ruleTargetType = RuleTargetType.getTargetType(targetType.toUpperCase());
            if (ruleTargetType == null) {
                throw new ValidationException("Target type:" + targetType + " is not supported");
            }
        }

        SelectionMode selectionMode = SelectionMode.getMode(null);
        QueryEngine queryRender = new QueryEngine(QueryParamRender.extractRawQueryParam(uriInfo), "rule", selectionMode);
        RuleQueryCommand ruleQueryCommand = new RuleQueryCommand();
        filterByPropsAndTags(queryRender, ruleQueryCommand);

        IdVersion[] searchKeys = ruleCriteriaQuery.queryByCommand(ruleQueryCommand, selectionMode);
        if (searchKeys == null) {
            Set<IdVersion> r = ruleCriteriaQuery.queryAll(null);
            searchKeys = r.toArray(new IdVersion[]{});
        }

        List<Long> ids = new ArrayList<>();
        for (IdVersion idv : searchKeys) {
            ids.add(idv.getId());
        }

        results = ruleRepository.list(ids);

        List<ExtendedView.ExtendedRule> viewArray = new ArrayList<>(results.size());
        for (Rule e : results) {
            viewArray.add(new ExtendedView.ExtendedRule(e));
        }
        if (ViewConstraints.EXTENDED.equalsIgnoreCase(type)) {
            viewDecorator.decorate(viewArray, "rule");
        }

        RuleListView listView = new RuleListView(results.size());
        for (ExtendedView.ExtendedRule e : viewArray) {
            listView.add(e);
        }

        return responseHandler.handleSerializedValue(ObjectJsonWriter.write(listView), hh.getMediaType());
    }

    @GET
    @Path("/rule")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response get(@Context final HttpHeaders hh,
                        @Context HttpServletRequest request,
                        @TrimmedQueryParam("type") final String type,
                        @Context UriInfo uriInfo) throws Exception {

        List<Rule> results;
        SelectionMode selectionMode = SelectionMode.getMode(null);
        QueryEngine queryRender = new QueryEngine(QueryParamRender.extractRawQueryParam(uriInfo), "rule", selectionMode);
        RuleQueryCommand ruleQueryCommand = new RuleQueryCommand();
        filterByPropsAndTags(queryRender, ruleQueryCommand);

        IdVersion[] searchKeys = ruleCriteriaQuery.queryByCommand(ruleQueryCommand, selectionMode);
        if (searchKeys == null) {
            Set<IdVersion> r = ruleCriteriaQuery.queryAll(null);
            searchKeys = r.toArray(new IdVersion[]{});
        }

        if (searchKeys.length > 1)
            throw new ValidationException("Too many matches have been found after querying.");

        List<Long> ids = new ArrayList<>();
        for (IdVersion idv : searchKeys) {
            ids.add(idv.getId());
        }

        results = ruleRepository.list(ids);

        List<ExtendedView.ExtendedRule> viewArray = new ArrayList<>(results.size());
        for (Rule e : results) {
            viewArray.add(new ExtendedView.ExtendedRule(e));
        }
        if (ViewConstraints.EXTENDED.equalsIgnoreCase(type)) {
            viewDecorator.decorate(viewArray, "rule");
        }

        RuleListView listView = new RuleListView(results.size());
        for (ExtendedView.ExtendedRule e : viewArray) {
            listView.add(e);
        }

        if (listView.getTotal() == 0) throw new ValidationException("no rule found.");
        if (listView.getTotal() == 1) {
            return responseHandler.handleSerializedValue(ObjectJsonWriter.write(listView.getList().get(0), type), hh.getMediaType());
        }
        return responseHandler.handleSerializedValue(ObjectJsonWriter.write(listView, type), hh.getMediaType());
    }


    @GET
    @Path("/rule/delete")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response delete(@Context final HttpHeaders hh,
                           @Context HttpServletRequest request,
                           @QueryParam("ruleId") final List<Long> id,
                           @QueryParam("activate") Boolean activate,
                           @Context UriInfo uriInfo) throws Exception {
        if (activate == null) activate = false;
        List<Rule> removedRules = new ArrayList<>();
        Map<String, List<Rule>> rulesMapByTarget = new HashMap<>();

        // validate
        if (id == null || id.size() == 0) {
            throw new ValidationException("Rule id parameter is required");
        }

        // auth
        List<Rule> rules = ruleRepository.list(id);
        if (rules == null || rules.size() != id.size()) {
            throw new ValidationException("Not found rule with ids: " + StringUtils.join(id, ','));
        }

        for (Rule rule : rules) {
            String targetType = rule.getTargetType();
            String targetId = rule.getTargetId();
            String key = targetType + "_" + targetId;

            authRequest(targetId, targetType, request);
            if (!rulesMapByTarget.containsKey(key)) {
                rulesMapByTarget.put(key, new ArrayList<Rule>());
            }
            rulesMapByTarget.get(key).add(rule);
        }

        // delete
        for (Map.Entry<String, List<Rule>> entry : rulesMapByTarget.entrySet()) {
            String rTargetType = entry.getValue().get(0).getTargetType();
            String rTargetId = entry.getValue().get(0).getTargetId();
            List<Long> ruleIds = new ArrayList<>();

            for (Rule rule : entry.getValue()) {
                Long ruleId = rule.getId();
                ruleIds.add(ruleId);
            }

            DistLock lock = null;
            String lockKey = getUpdateTargetLockKey(rTargetType, rTargetId);
            if (lockKey != null) {
                lock = dbLockFactory.newLock(lockKey);
                lock.lock(timeout);
            }
            try {
                removedRules.addAll(ruleRepository.removeRuleByIds(ruleIds));
            } finally {
                if (lock != null) {
                    lock.unlock();
                }
            }

            if (activate && RuleTargetType.GROUP.name().equalsIgnoreCase(rTargetType)) {
                activateTarget(Long.parseLong(rTargetId));
            }

            RuleTargetType targetType = RuleTargetType.getTargetType(rTargetType);
            if (targetType.isNeedTarget()) {
                sendUpdateTargetMessage(request, targetType, RuleTargetType.parseLongTargetId(rTargetId), "/api/rule/delete", removedRules);
            }
        }

        return responseHandler.handleSerializedValue(ObjectJsonWriter.write(removedRules), hh.getMediaType());
    }

    @GET
    @Path("/rule/clear")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response clear(@Context final HttpHeaders hh,
                          @Context HttpServletRequest request,
                          @QueryParam("targetId") final String targetId,
                          @QueryParam("targetType") final String targetType,
                          @QueryParam("activate") Boolean activate,
                          @Context UriInfo uriInfo) throws Exception {

        List<Rule> removedRules;
        if (activate == null) activate = false;

        // validation
        validateRuleTarget(targetId, targetType);

        //  auth
        List<Long> ids = new ArrayList<>();
        List<Rule> rules = ruleRepository.getRulesByTarget(targetId, targetType);
        for (Rule rule : rules) {
            ids.add(rule.getId());
        }

        if (ids.size() == 0) {
            throw new NotFoundException("Could not found any rules related to " + targetType + ", target id:" + targetId);
        }
        authRequest(targetId, targetType, request);

        // clear
        DistLock lock = null;
        String lockKey = getUpdateTargetLockKey(targetType, targetId);
        if (lockKey != null) {
            lock = dbLockFactory.newLock(lockKey);
            lock.lock(timeout);
        }
        try {
            removedRules = ruleRepository.removeRuleByIds(ids);
        } finally {
            if (lock != null) {
                lock.unlock();
            }
        }

        if (activate && RuleTargetType.GROUP.name().equalsIgnoreCase(targetType)) {
            activateTarget(Long.parseLong(targetId));
        }
        // message
        RuleTargetType target = RuleTargetType.getTargetType(targetType);
        if (target.isNeedTarget()) {
            sendUpdateTargetMessage(request, target, RuleTargetType.parseLongTargetId(targetId), "/api/rule/clear", removedRules);
        }

        return responseHandler.handleSerializedValue(ObjectJsonWriter.write(removedRules), hh.getMediaType());
    }

    /*
    * Return null if add rule not need to update target
    * */
    private String getUpdateTargetLockKey(String targetType, String targetId) throws ValidationException {
        RuleTargetType type = RuleTargetType.getTargetType(targetType);
        String lockKey = lockKeys.get(type);
        if (lockKey == null) {
            return null;
        }
        return targetId + lockKey;
    }

    private void authRequest(String ruleTargetId, String targetType, HttpServletRequest request) throws ValidationException, AuthorizeException {
        authRequest(ruleTargetId, RuleTargetType.getTargetType(targetType), request);
    }

    private void authRequest(String ruleTargetId, RuleTargetType ruleTargetType, HttpServletRequest request) throws ValidationException, AuthorizeException {
        if (!ruleTargetType.isNeedTarget()) return;

        ResourceDataType resourceDataType;
        switch (ruleTargetType) {
            case GROUP:
                resourceDataType = ResourceDataType.Group;
                break;
            case SLB:
                resourceDataType = ResourceDataType.Slb;
                break;
            case VS:
                resourceDataType = ResourceDataType.Vs;
                break;
            default:
                throw new ValidationException("Only Slb, Vs, Group, Policy types are valid target type");
        }

        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.UPDATE, resourceDataType, ruleTargetId);
    }

    private void filterByPropsAndTags(QueryEngine queryRender, RuleQueryCommand ruleQueryCommand) throws Exception {
        TagQueryCommand tcmd = new TagQueryCommand();
        PropQueryCommand pcmd = new PropQueryCommand();

        queryRender.readToCommand(Lists.newArrayList(ruleQueryCommand, tcmd, pcmd));

        Set<Long> preFilteredId = queryRender.preFilter(criteriaQueryFactory, tcmd, pcmd, "rule");
        if (preFilteredId == null) return;

        if (ruleQueryCommand.hasValue(0)) {
            for (String s : ruleQueryCommand.getValue(0)) {
                try {
                    preFilteredId.add(Long.parseLong(s));
                } catch (NumberFormatException e) {
                    throw new ValidationException("Invalid query value from parameter `id`.");
                }
            }
        }
        ruleQueryCommand.addAtIndex(0, preFilteredId.size() == 0 ? "-1" : Joiner.on(",").join(preFilteredId));
    }

    private void sendUpdateTargetMessage(HttpServletRequest request, RuleTargetType targetType, Long targetId, String messageType, List<Rule> rules) throws Exception {
        String slbMessageData;

        String targetUrl;

        if (rules == null || rules.contains(null)) return;

        switch (targetType) {
            case GROUP: {
                Group g = groupRepository.getById(targetId);
                targetUrl = "/api/group/update";
                slbMessageData = MessageUtil.getMessageBuilder(request, true).bindRules(rules.toArray(new Rule[]{})).bindGroups(new Group[]{g}).bindUri(targetUrl).bindType(messageType).build();
                break;
            }
            case VS: {
                VirtualServer v = virtualServerRepository.getById(targetId);
                targetUrl = "/api/vs/update";
                slbMessageData = MessageUtil.getMessageBuilder(request, true).bindUri(targetUrl).bindRules(rules.toArray(new Rule[]{})).bindVses(new VirtualServer[]{v}).bindType(messageType).build();
                break;
            }
            case SLB: {
                Slb s = slbRepository.getById(targetId);
                targetUrl = "/api/slb/update";
                request.setAttribute("URI", targetUrl);
                slbMessageData = MessageUtil.getMessageBuilder(request, true).bindUri(targetUrl).bindRules(rules.toArray(new Rule[]{})).bindSlbs(new Slb[]{s}).bindType(messageType).build();
                break;
            }
            case TRAFFIC_POLICY: {
                TrafficPolicy t = trafficPolicyRepository.getById(targetId);
                targetUrl = "/api/policy/update";
                request.setAttribute("URI", targetUrl);
                slbMessageData = MessageUtil.getMessageBuilder(request, true).bindUri(targetUrl).bindRules(rules.toArray(new Rule[]{})).bindPolicies(new TrafficPolicy[]{t}).bindType(messageType).build();
                break;
            }
            default: {
                throw new ValidationException("[Rule][Target Message] Target type " + targetType + ", is not supported");
            }
        }
        messageQueue.produceMessage(targetUrl,
                targetId,
                slbMessageData);
    }

    /*
    * Rules in batch shall be for same target. or all for default
    * */
    private void validateRules(Rules rules) throws Exception {
        // Rules not null
        if (rules == null) {
            throw new ValidationException("Rules for set operation shall not be null");
        }
        List<Rule> ruleList = rules.getRules();
        if (ruleList == null || ruleList.size() == 0) {
            throw new ValidationException("Invalid post entity. No rules post");
        }

        // rules target validate
        String ruleTargetType = rules.getTargetType();
        String ruleTargetId = rules.getTargetId();
        validateRuleTarget(ruleTargetId, ruleTargetType);

        // rules list validate
        for (Rule rule : ruleList) {
            String tempRuleTargetType = rule.getTargetType();
            String tempRuleTargetId = rule.getTargetId();
            validateRuleTarget(tempRuleTargetId, tempRuleTargetType);

            // target id and target type are same?
            if (!tempRuleTargetType.equalsIgnoreCase(ruleTargetType) ||
                    !ruleTargetId.equalsIgnoreCase(tempRuleTargetId)) {
                throw new ValidationException("Invalid post entity. rules are not for same target");
            }
        }
    }

    private void validateRuleArray(List<Rule> rules) throws Exception {
        if (rules == null || rules.size() == 0) throw new ValidationException("Rules for set shall not be empty");
        for (Rule rule : rules) {
            validateRuleTarget(rule.getTargetId(), rule.getTargetType());
        }
    }

    private void validateRuleTarget(String targetId, String targetType) throws ValidationException {
        if (StringUtils.isEmpty(targetId) || StringUtils.isEmpty(targetType)) {
            throw new ValidationException("Invalid post entity. Rule target id and target type shall not be empty");
        }
        if (RuleTargetType.getTargetType(targetType) == null)
            throw new ValidationException("Invalid post entity. Rule target type: " + targetType + ", is not supported");
    }

    private List<Rule> setRules(Rules rules, HttpServletRequest request, boolean needActivate) throws Exception {
        List<Rule> addedRules;

        validateRules(rules);

        String targetType = rules.getTargetType();
        String targetId = rules.getTargetId();
        RuleTargetType ruleTargetType = RuleTargetType.getTargetType(targetType);

        //  auth
        authRequest(targetId, ruleTargetType, request);
        List<Long> updatedRuleIds = new ArrayList<>();
        int newRuleCount = 0;
        for (Rule rule : rules.getRules()) {
            if (rule.getId() != null) {
                updatedRuleIds.add(rule.getId());
            } else {
                newRuleCount++;
            }
        }

        if (ruleTargetType.equals(RuleTargetType.DEFAULT)) {
            if (updatedRuleIds.size() > 0) {
                authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.UPDATE, ResourceDataType.Rule, updatedRuleIds.toArray(new Long[]{}));
            }
            if (newRuleCount > 0) {
                authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.NEW, ResourceDataType.Rule, AuthDefaultValues.ALL);
            }
        }


        // save
        DistLock lock = null;
        String lockKey = getUpdateTargetLockKey(targetType, targetId);
        if (lockKey != null) {
            lock = dbLockFactory.newLock(lockKey);
            lock.lock(timeout);
        }
        try {
            // Group validation
            if (needActivate && RuleTargetType.GROUP.name().equalsIgnoreCase(targetType)) {
                Long groupId = Long.parseLong(targetId);
                ModelStatusMapping<Group> groupMap = entityFactory.getGroupsByIds(new Long[]{groupId});
                if (groupMap.getOnlineMapping().get(groupId) == null) {
                    throw new ValidationException("Group only has offline version . GroupId:" + groupId);
                }

                if (!groupMap.getOnlineMapping().get(groupId).getVersion().equals(groupMap.getOfflineMapping().get(groupId).getVersion())) {
                    throw new ValidationException("Online/Offline group version is expected to be same. Please activate group first.GroupId:"
                            + groupId + ";OnlineVersion:" + groupMap.getOnlineMapping().get(groupId).getVersion()
                            + ";OfflineVersion:" + groupMap.getOfflineMapping().get(groupId).getVersion());
                }
            }
            addedRules = ruleRepository.set(rules.getRules());
        } finally {
            if (lock != null) {
                lock.unlock();
            }
        }
        if (ruleTargetType.isNeedTarget()) {
            sendUpdateTargetMessage(request, ruleTargetType, RuleTargetType.parseLongTargetId(targetId), "/api/rule/set", addedRules);
        }

        if (needActivate && RuleTargetType.GROUP.name().equalsIgnoreCase(targetType)) {
            activateTarget(Long.parseLong(targetId));
        }

        return addedRules;
    }

    private void activateTarget(Long groupId) throws Exception {
        if (groupId == null) return;

        Group offGroup = null;
        Group onGroup = null;

        ModelStatusMapping<VirtualServer> vsMapping = null;
        ModelStatusMapping<Group> mapping = entityFactory.getGroupsByIds(new Long[]{groupId});
        offGroup = mapping.getOfflineMapping().get(groupId);
        onGroup = mapping.getOnlineMapping().get(groupId);

        if (offGroup != null && onGroup != null) {
            // VS validation
            Set<Long> vsIds = new HashSet<>();
            for (GroupVirtualServer gvs : offGroup.getGroupVirtualServers()) {
                vsIds.add(gvs.getVirtualServer().getId());
            }
            vsMapping = entityFactory.getVsesByIds(vsIds.toArray(new Long[]{}));
            if (vsMapping.getOnlineMapping().size() == 0) {
                throw new ValidationException("Related vs is not activated.VsIds: " + vsIds);
            }

            List<OpsTask> tasks = new ArrayList<>();
            for (VirtualServer vs : vsMapping.getOnlineMapping().values()) {
                for (Long slbId : vs.getSlbIds()) {
                    OpsTask task = new OpsTask();
                    task.setCreateTime(new Date())
                            .setGroupId(groupId)
                            .setTargetSlbId(slbId)
                            .setOpsType(TaskOpsType.ACTIVATE_GROUP)
                            .setSkipValidate(true)
                            .setVersion(offGroup.getVersion());
                    tasks.add(task);
                }
            }

            String slbMessageData = MessageUtil.getMessageBuilder("slb", "/api/activate/group",
                    "rule set", true).bindGroups(new Group[]{offGroup}).build();
            messageQueue.produceMessage("/api/activate/group", offGroup.getId(), slbMessageData);

            List<Long> taskIds = taskManager.addTask(tasks);
            taskManager.getResult(taskIds, apiTimeout.get());
        }
    }
}
