package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.exceptions.NotFoundException;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.Property;
import com.ctrip.zeus.model.model.*;
import com.ctrip.zeus.restful.message.QueryParamRender;
import com.ctrip.zeus.restful.message.ResponseHandler;
import com.ctrip.zeus.restful.message.TrimmedQueryParam;
import com.ctrip.zeus.restful.message.view.ExtendedView;
import com.ctrip.zeus.restful.message.view.TrafficPolicyListView;
import com.ctrip.zeus.restful.message.view.ViewConstraints;
import com.ctrip.zeus.restful.message.view.ViewDecorator;
import com.ctrip.zeus.service.auth.AuthService;
import com.ctrip.zeus.service.auth.ResourceDataType;
import com.ctrip.zeus.service.auth.ResourceOperationType;
import com.ctrip.zeus.service.message.queue.MessageQueue;
import com.ctrip.zeus.service.model.*;
import com.ctrip.zeus.service.query.CriteriaQueryFactory;
import com.ctrip.zeus.service.query.GroupCriteriaQuery;
import com.ctrip.zeus.service.query.QueryEngine;
import com.ctrip.zeus.service.query.TrafficPolicyQuery;
import com.ctrip.zeus.service.query.command.PropQueryCommand;
import com.ctrip.zeus.service.query.command.TagQueryCommand;
import com.ctrip.zeus.service.query.command.TrafficPolicyCommand;
import com.ctrip.zeus.support.ObjectJsonParser;
import com.ctrip.zeus.support.ObjectJsonWriter;
import com.ctrip.zeus.tag.PropertyBox;
import com.ctrip.zeus.tag.PropertyService;
import com.ctrip.zeus.tag.TagBox;
import com.ctrip.zeus.util.MessageUtil;
import com.ctrip.zeus.util.UserUtils;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.*;

;

/**
 * Created by zhoumy on 2017/1/18.
 */
@Component
@Path("/")
public class TrafficPolicyResource {
    @Resource
    private TrafficPolicyQuery trafficPolicyQuery;
    @Resource
    private TrafficPolicyRepository trafficPolicyRepository;
    @Resource
    private ArchiveRepository archiveRepository;
    @Resource
    private PropertyBox propertyBox;
    @Resource
    private TagBox tagBox;
    @Resource
    private ViewDecorator viewDecorator;
    @Resource
    private ResponseHandler responseHandler;
    @Resource
    private CriteriaQueryFactory criteriaQueryFactory;
    @Resource
    private AuthService authService;
    @Resource
    private MessageQueue messageQueue;
    @Resource
    private PropertyService propertyService;
    @Autowired
    private GroupRepository groupRepository;
    @Resource
    private GroupCriteriaQuery groupCriteriaQuery;

    private static Logger logger = LoggerFactory.getLogger(TrafficPolicyResource.class);

    @GET
    @Path("/policy")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getPolicy(@Context HttpHeaders hh,
                              @Context final HttpServletRequest request,
                              @TrimmedQueryParam("mode") final String mode,
                              @TrimmedQueryParam("type") final String type,
                              @Context UriInfo uriInfo) throws Exception {
        QueryEngine queryRender = new QueryEngine(QueryParamRender.extractRawQueryParam(uriInfo), "policy", SelectionMode.getMode(mode));
        TrafficPolicyCommand tpcmd = new TrafficPolicyCommand();
        filterByPropsAndTags(queryRender, tpcmd);

        SelectionMode selectionMode = SelectionMode.getMode(mode);
        IdVersion[] searchKeys = trafficPolicyQuery.queryByCommand(tpcmd, selectionMode);
        if (searchKeys == null) {
            searchKeys = trafficPolicyQuery.queryAll(SelectionMode.getMode(mode)).toArray(new IdVersion[]{});
        }
        if (SelectionMode.REDUNDANT == selectionMode) {
            if (searchKeys.length > 2)
                throw new ValidationException("Too many matches have been found after querying.");
        } else {
            if (searchKeys.length > 1)
                throw new ValidationException("Too many matches have been found after querying.");
        }

        List<Long> ids = new ArrayList<>();
        for (IdVersion idv : searchKeys) {
            ids.add(idv.getId());
        }

        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.READ, ResourceDataType.Policy, ids.toArray(new Long[ids.size()]));

        List<TrafficPolicy> result = trafficPolicyRepository.list(searchKeys);

        List<ExtendedView.ExtendedTrafficPolicy> viewArray = new ArrayList<>(result.size());
        for (TrafficPolicy e : result) {
            viewArray.add(new ExtendedView.ExtendedTrafficPolicy(e));
        }
        if (ViewConstraints.EXTENDED.equalsIgnoreCase(type)) {
            viewDecorator.decorate(viewArray, "policy");
        }

        TrafficPolicyListView listView = new TrafficPolicyListView(result.size());
        for (ExtendedView.ExtendedTrafficPolicy e : viewArray) {
            listView.add(e);
        }
        if (listView.getTotal() == 0) throw new ValidationException("Traffic policy cannot be found.");
        if (listView.getTotal() == 1) {
            return responseHandler.handleSerializedValue(ObjectJsonWriter.write(listView.getList().get(0), type), hh.getMediaType());
        }

        return responseHandler.handleSerializedValue(ObjectJsonWriter.write(listView, type), hh.getMediaType());
    }

    private void filterByPropsAndTags(QueryEngine queryRender, TrafficPolicyCommand tpcmd) throws Exception {
        TagQueryCommand tcmd = new TagQueryCommand();
        PropQueryCommand pcmd = new PropQueryCommand();

        queryRender.readToCommand(Lists.newArrayList(tpcmd, tcmd, pcmd));

        Set<Long> preFilteredId = queryRender.preFilter(criteriaQueryFactory, tcmd, pcmd, "policy");
        if (preFilteredId == null) return;

        if (tpcmd.hasValue(0)) {
            for (String s : tpcmd.getValue(0)) {
                try {
                    preFilteredId.add(Long.parseLong(s));
                } catch (NumberFormatException e) {
                    throw new ValidationException("Invalid query value from parameter `id`.");
                }
            }
        }
        tpcmd.addAtIndex(0, preFilteredId.size() == 0 ? "-1" : Joiner.on(",").join(preFilteredId));
    }

    /**
     * @api {get} /api/policies: [Read] Batch fetch policy data
     * @apiName ListPolicies
     * @apiGroup Policy
     * @apiDescription See [Update Policy content](#api-Policy-FullUpdatePolicy) for object description
     * @apiSuccess (Success 200) {PolicyObject[]} policies     policy list result after query
     * @apiSuccess (Success 200) {Integer[]} total          total number of policy entities in the policy list, it may be useful when `limit` parameter is specified
     * @apiParam {long[]} [policyId]            1,2,3
     * @apiParam {string[]} [policyName]        dev,localhost,test
     * @apiParam {string[]} [fuzzyName]         de,local,te
     * @apiParam {long[]} [vsId]                1001,1101,1100
     * @apiParam {long[]} [groupId]             1001,1101,1100
     * @apiParam {string=online,offline,redundant(online&offline)} [mode]   query snapshot versions by mode
     * @apiParam {string[]} [anyTag]      union search policy by tags e.g. anyTag=policy1,policy2
     * @apiParam {string[]} [tags]        join search policy by tags e.g. tags=policy1,policy2
     * @apiParam {string[]} [anyProp]     union search policy by properties(key:value) e.g. anyProp=dc:oy,dc:jq
     * @apiParam {string[]} [props]       join search policy by properties(key:value) e.g. props=department:hotel,dc:jq
     * @apiSuccessExample {json} JSON format:
     * {
     *   "id": 1,
     *   "name": "policy1",
     *   "version": 3,
     *   "policy-virtual-servers": [
     *     {
     *       "priority": 1000,
     *       "path": "~*  ^/path",
     *       "virtual-server": {
     *         "id": 1
     *       }
     *     }
     *   ],
     *   "controls": [
     *     {
     *       "group": {
     *         "id": 1
     *       },
     *       "weight": 3
     *     },
     *     {
     *       "group": {
     *         "id": 2
     *       },
     *       "weight": 5
     *     }
     *   ]
     * }
     */
    @GET
    @Path("/policies")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response list(@Context HttpHeaders hh,
                         @Context final HttpServletRequest request,
                         @TrimmedQueryParam("mode") final String mode,
                         @TrimmedQueryParam("type") final String type,
                         @Context UriInfo uriInfo) throws Exception {
        QueryEngine queryRender = new QueryEngine(QueryParamRender.extractRawQueryParam(uriInfo), "policy", SelectionMode.getMode(mode));
        TrafficPolicyCommand tpcmd = new TrafficPolicyCommand();
        filterByPropsAndTags(queryRender, tpcmd);

        SelectionMode selectionMode = SelectionMode.getMode(mode);
        IdVersion[] searchKeys = trafficPolicyQuery.queryByCommand(tpcmd, selectionMode);
        if (searchKeys == null) {
            searchKeys = trafficPolicyQuery.queryAll(selectionMode).toArray(new IdVersion[]{});
        }

        List<Long> ids = new ArrayList<>();
        for (IdVersion idv : searchKeys) {
            ids.add(idv.getId());
        }

        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.READ, ResourceDataType.Policy,
                ids.toArray(new Long[ids.size()]));

        List<TrafficPolicy> result = trafficPolicyRepository.list(searchKeys);

        List<ExtendedView.ExtendedTrafficPolicy> viewArray = new ArrayList<>(result.size());
        for (TrafficPolicy e : result) {
            viewArray.add(new ExtendedView.ExtendedTrafficPolicy(e));
        }
        if (ViewConstraints.EXTENDED.equalsIgnoreCase(type)) {
            viewDecorator.decorate(viewArray, "policy");
        }

        TrafficPolicyListView listView = new TrafficPolicyListView(result.size());
        for (ExtendedView.ExtendedTrafficPolicy e : viewArray) {
            listView.add(e);
        }

        return responseHandler.handleSerializedValue(ObjectJsonWriter.write(listView, type), hh.getMediaType());
    }

    /**
     * @api {get} /api/policy: [Read] Get single policy data
     * @apiName GetSinglePolicy
     * @apiGroup Policy
     * @apiDescription See [Batch fetch policy data](#api-Policy-ListPolicies) for more information
     * @apiSuccess (Success 200) {PolicyObject} policy    policy entity
     */
    @POST
    @Path("/policy/new")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "*/*"})
    public Response add(@Context HttpHeaders hh, @Context HttpServletRequest request, String requestBody,
                        @QueryParam("force") Boolean force) throws Exception {
        ExtendedView.ExtendedTrafficPolicy extendedView = ObjectJsonParser.parse(requestBody, ExtendedView.ExtendedTrafficPolicy.class);
        TrafficPolicy p = ObjectJsonParser.parse(requestBody, TrafficPolicy.class);
        if (p == null) {
            throw new ValidationException("Invalid post entity. Fail to parse json to traffic-policy.");
        }
        if (p.getName() == null) {
            throw new ValidationException("Field `name` is not allowed empty.");
        }
        trim(p);
        Long checkId = trafficPolicyQuery.queryByName(p.getName());
        if (checkId > 0L) {
            throw new ValidationException("Traffic policy name has been taken by " + checkId + ".");
        }
        if (p.getControls() == null || p.getControls().size() == 0) {
            throw new ValidationException("Need at least one control element.");
        }
        Long[] gids = new Long[p.getControls().size()];
        for (int i = 0; i < p.getControls().size(); i++) {
            gids[i] = p.getControls().get(i).getGroup().getId();
        }

        if (force != null && force) {
            authService.authValidateWithForce(UserUtils.getUserName(request), ResourceOperationType.UPDATE, ResourceDataType.Group, gids);
        } else {
            authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.UPDATE, ResourceDataType.Group, gids);
        }

        p = trafficPolicyRepository.add(p, force != null && force);

        try {
            propertyBox.set("status", "deactivated", "policy", p.getId());
            Long[] ids = new Long[p.getControls().size()];
            int index = 0;
            for (TrafficControl tc : p.getControls()) {
                ids[index] = tc.getGroup().getId();
            }
            propertyBox.set("migrationStatus", "start", "group", ids);

            if (extendedView != null && extendedView.getProperties() != null) {
                for (Property property : extendedView.getProperties()) {
                    if (property.getName().equalsIgnoreCase("target")) {
                        propertyBox.set("migrationType", property.getValue(), "group", ids);
                        break;
                    }
                }
            }
        } catch (Exception ex) {
        }
        if (extendedView != null && extendedView.getProperties() != null) {
            setProperties(p.getId(), extendedView.getProperties());
        }
        if (extendedView != null && extendedView.getTags() != null) {
            addTag(p.getId(), extendedView.getTags());
        }

        String slbMessageData = MessageUtil.getMessageData(request, null, new TrafficPolicy[]{p}, null, null, null, true);

        messageQueue.produceMessage(request.getRequestURI(), p.getId(), slbMessageData);

        return responseHandler.handleSerializedValue(ObjectJsonWriter.write(new ExtendedView.ExtendedTrafficPolicy(p), ViewConstraints.DETAIL), hh.getMediaType());
    }

    /**
     * @api {post} /api/policy/update: [Write] Update policy content
     * @apiName FullUpdatePolicy
     * @apiGroup Policy
     * @apiSuccess (Success 200) {PolicyObject} policy    updated policy entity
     * @apiParam (PolicyObject) {Long} id                          id
     * @apiParam (PolicyObject) {String} name                      name
     * @apiParam (PolicyObject) {Integer} version                  version
     * @apiParam (PolicyObject) {PolicyVirtualServer[]} policy-virtual-servers   configuration on specified virtual server
     * @apiParam (PolicyObject) {PolicyControl[]} controls         policy control list
     * @apiParam (PolicyObject) {String[]} [tags]                  add tags to policy
     * @apiParam (PolicyObject) {Object[]} [properties]            add/update properties of policy
     * @apiParam (PolicyVirtualServer) {String} path                 location entry on the specified virtual server
     * @apiParam (PolicyVirtualServer) {Integer} priority            must be explicitly set and higher than its controls' priority
     * @apiParam (PolicyVirtualServer) {Object} virtual-server       combined virtual server [id only]
     * @apiParam (PolicyControl) {Group} group                a/b testing group
     * @apiParam (PolicyControl) {Integer} weight             proxying weight of this group
     * @apiParamExample {json} Sample Request:
     * {
     *   "id": 1,
     *   "name": "policy1",
     *   "version": 3,
     *   "policy-virtual-servers": [
     *     {
     *       "priority": 1000,
     *       "path": "~*  ^/path",
     *       "virtual-server": {
     *         "id": 1
     *       }
     *     }
     *   ],
     *   "controls": [
     *     {
     *       "group": {
     *         "id": 1
     *       },
     *       "weight": 3
     *     },
     *     {
     *       "group": {
     *         "id": 2
     *       },
     *       "weight": 5
     *     }
     *   ]
     * }
     */
    @POST
    @Path("/policy/update")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "*/*"})
    public Response update(@Context HttpHeaders hh, @Context HttpServletRequest request, String requestBody,
                           @QueryParam("force") Boolean force) throws Exception {
        ExtendedView.ExtendedTrafficPolicy extendedView = ObjectJsonParser.parse(requestBody, ExtendedView.ExtendedTrafficPolicy.class);
        TrafficPolicy p = ObjectJsonParser.parse(requestBody, TrafficPolicy.class);
        if (p == null) {
            throw new ValidationException("Invalid post entity. Fail to parse json to traffic-policy.");
        }
        if (p.getName() == null) {
            throw new ValidationException("Field `name` is not allowed empty.");
        }
        trim(p);
        Long checkId = trafficPolicyQuery.queryByName(p.getName());
        if (checkId > 0L && !checkId.equals(p.getId())) {
            throw new ValidationException("Traffic policy name has been taken by " + checkId + ".");
        }
        if (force != null && force) {
            authService.authValidateWithForce(UserUtils.getUserName(request), ResourceOperationType.UPDATE, ResourceDataType.Policy, p.getId());
        } else {
            authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.UPDATE, ResourceDataType.Policy, p.getId());
        }

        p = trafficPolicyRepository.update(p, force != null && force);
        if (extendedView != null && extendedView.getProperties() != null) {
            setProperties(p.getId(), extendedView.getProperties());
        }
        if (extendedView != null && extendedView.getTags() != null) {
            addTag(p.getId(), extendedView.getTags());
        }
        try {
            if (trafficPolicyQuery.queryByIdAndMode(p.getId(), SelectionMode.ONLINE_EXCLUSIVE).length == 1) {
                propertyBox.set("status", "toBeActivated", "policy", p.getId());
            }
        } catch (Exception ex) {
        }

        String slbMessageData = MessageUtil.getMessageData(request, null, new TrafficPolicy[]{p}, null, null, null, true);

        messageQueue.produceMessage(request.getRequestURI(), p.getId(), slbMessageData);

        return responseHandler.handleSerializedValue(ObjectJsonWriter.write(new ExtendedView.ExtendedTrafficPolicy(p), ViewConstraints.DETAIL), hh.getMediaType());
    }

    @GET
    @Path("/policy/delete")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response delete(@Context HttpHeaders hh, @Context HttpServletRequest request,
                           @QueryParam("policyId") Long policyId,
                           @QueryParam("policyName") String policyName) throws Exception {
        if (policyId == null) {
            if (policyName != null && !policyName.isEmpty())
                policyId = trafficPolicyQuery.queryByName(policyName);
        }
        if (policyId == null) {
            throw new ValidationException("Query parameter - policyId is not provided or could not be found by query.");
        }
        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.DELETE, ResourceDataType.Policy, policyId);

        TrafficPolicy result = trafficPolicyRepository.getById(policyId);
        if (result == null) throw new ValidationException("Not Found Policy By Id,ID:" + policyId);

        trafficPolicyRepository.delete(policyId);
        try {
            propertyBox.clear("policy", policyId);
        } catch (Exception ex) {
        }
        try {
            tagBox.clear("policy", policyId);
        } catch (Exception ex) {
        }
        archiveRepository.archivePolicy(result);

        String slbMessageData = MessageUtil.getMessageData(request, null, null, null, null, null, true);

        messageQueue.produceMessage(request.getRequestURI(), policyId, slbMessageData);

        for (TrafficControl tc : result.getControls()) {
            try {
                IdVersion[] onlineIdv = groupCriteriaQuery.queryByIdAndMode(tc.getGroup().getId(), SelectionMode.ONLINE_EXCLUSIVE);
                if (onlineIdv == null || onlineIdv.length == 0) {
                    Long gid = tc.getGroup().getId();
                    groupRepository.delete(gid);
                    String messageData = MessageUtil.getMessageBuilder(request, true).bindUri(request.getRequestURI())
                            .bindGroups(new Group[]{new Group().setId(gid)}).bindType("/api/group/delete").build();
                    messageQueue.produceMessage("/api/group/delete", gid, messageData);
                }
            } catch (Exception e) {
                logger.error("Clean Deactivated Traffic Control Group Failed.TrafficControl:" + tc);
            }
        }

        return responseHandler.handle("Traffic policy " + policyId + " is deleted.", hh.getMediaType());
    }

    @GET
    @Path("/policy/related/view")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getPolicy(@Context HttpHeaders hh,
                              @Context final HttpServletRequest request,
                              @TrimmedQueryParam("appId") final String appId) throws Exception {
        if (appId == null) {
            throw new ValidationException("Need Parameter [appId]");
        }
        List<Long> relatedGroupIds = propertyService.queryTargets("relatedAppId", appId, "group");
        Set<Long> groupIds = groupCriteriaQuery.queryByAppId(appId);
        if (relatedGroupIds == null || relatedGroupIds.size() == 0) {
            throw new NotFoundException("Not found related policy.");
        }
        List<Group> relatedGroups = groupRepository.list(relatedGroupIds.toArray(new Long[relatedGroupIds.size()]));
        List<Group> groups = groupRepository.list(groupIds.toArray(new Long[groupIds.size()]));
        Map<Long, Group> groupMap = new HashMap<>();
        for (Group g : groups) {
            groupMap.put(g.getId(), g);
        }
        for (Group g : relatedGroups) {
            groupMap.put(g.getId(), g);
        }

        Set<Long> pids = new HashSet<>();
        for (Long gid : relatedGroupIds) {
            Set<IdVersion> tmp = trafficPolicyQuery.queryByGroupId(gid);
            for (IdVersion idv : tmp) {
                pids.add(idv.getId());
            }
        }
        Set<IdVersion> idvs = trafficPolicyQuery.queryByIdsAndMode(pids.toArray(new Long[pids.size()]), SelectionMode.ONLINE_FIRST);
        List<TrafficPolicy> result = trafficPolicyRepository.list(idvs.toArray(new IdVersion[idvs.size()]));

        PolicyViewList res = new PolicyViewList();
        for (TrafficPolicy trafficPolicy : result) {
            PolicyView policyView = new PolicyView();
            for (TrafficControl control : trafficPolicy.getControls()) {
                Control control1 = new Control();
                control1.setGroupId(control.getGroup().getId());
                control1.setWeight(control.getWeight());
                control1.setAppId(groupMap.get(control1.getGroupId()).getAppId());
                control1.setGroupName(groupMap.get(control1.getGroupId()).getName());
                policyView.addControl(control1);
            }
            res.addPolicyView(policyView);
        }
        res.setTotal(res.getPolicyViews().size());
        return responseHandler.handle(res, hh.getMediaType());

    }

    private void trim(TrafficPolicy p) {
        p.setName(p.getName().trim());
    }

    private void setProperties(Long policyId, List<Property> properties) {
        for (Property p : properties) {
            try {
                propertyBox.set(p.getName(), p.getValue(), "policy", policyId);
            } catch (Exception e) {
                logger.warn("Fail to set property " + p.getName() + "/" + p.getValue() + " on policy " + policyId + ".");
            }
        }
    }

    private void addTag(Long policyId, List<String> tags) {
        for (String tag : tags) {
            try {
                tagBox.tagging(tag, "policy", new Long[]{policyId});
            } catch (Exception e) {
                logger.warn("Fail to tagging " + tag + " on policy " + policyId + ".");
            }
        }
    }
}
