package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.executor.TaskManager;
import com.ctrip.zeus.lock.DbLockFactory;
import com.ctrip.zeus.lock.DistLock;
import com.ctrip.zeus.model.Property;
import com.ctrip.zeus.model.model.Domain;
import com.ctrip.zeus.model.model.Group;
import com.ctrip.zeus.model.model.GroupVirtualServer;
import com.ctrip.zeus.model.model.VirtualServer;
import com.ctrip.zeus.model.task.OpsTask;
import com.ctrip.zeus.model.task.TaskResult;
import com.ctrip.zeus.model.task.TaskResultList;
import com.ctrip.zeus.restful.message.QueryParamRender;
import com.ctrip.zeus.restful.message.ResponseHandler;
import com.ctrip.zeus.restful.message.TrimmedQueryParam;
import com.ctrip.zeus.restful.message.view.ExtendedView;
import com.ctrip.zeus.restful.message.view.ViewConstraints;
import com.ctrip.zeus.restful.message.view.ViewDecorator;
import com.ctrip.zeus.restful.message.view.VsListView;
import com.ctrip.zeus.service.auth.AuthService;
import com.ctrip.zeus.service.auth.ResourceDataType;
import com.ctrip.zeus.service.auth.ResourceOperationType;
import com.ctrip.zeus.service.build.ConfigHandler;
import com.ctrip.zeus.service.message.queue.MessageQueue;
import com.ctrip.zeus.service.model.*;
import com.ctrip.zeus.service.query.CriteriaQueryFactory;
import com.ctrip.zeus.service.query.QueryEngine;
import com.ctrip.zeus.service.query.VirtualServerCriteriaQuery;
import com.ctrip.zeus.service.query.sort.SortEngine;
import com.ctrip.zeus.service.task.constant.TaskOpsType;
import com.ctrip.zeus.support.ObjectJsonParser;
import com.ctrip.zeus.support.ObjectJsonWriter;
import com.ctrip.zeus.tag.PropertyBox;
import com.ctrip.zeus.tag.TagBox;
import com.ctrip.zeus.util.MessageUtil;
import com.ctrip.zeus.util.UserUtils;
import com.netflix.config.DynamicLongProperty;
import com.netflix.config.DynamicPropertyFactory;
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
 * Created by zhoumy on 2015/8/5.
 */
@Component
@Path("/")
public class VirtualServerResource {
    @Resource
    private VirtualServerRepository virtualServerRepository;
    @Resource
    private ArchiveRepository archiveRepository;
    @Resource
    private CriteriaQueryFactory criteriaQueryFactory;
    @Resource
    private VirtualServerCriteriaQuery virtualServerCriteriaQuery;
    @Resource
    private ResponseHandler responseHandler;
    @Resource
    private DbLockFactory dbLockFactory;
    @Resource
    private PropertyBox propertyBox;
    @Resource
    private TagBox tagBox;
    @Resource
    private ViewDecorator viewDecorator;
    @Resource
    private MessageQueue messageQueue;
    @Resource
    private ConfigHandler configHandler;
    @Resource
    private TaskManager taskManager;
    @Resource
    private EntityFactory entityFactory;
    @Autowired
    private GroupRepository groupRepository;
    @Resource
    private AuthService authService;

    private static DynamicLongProperty apiTimeout = DynamicPropertyFactory.getInstance().getLongProperty("api.timeout", 30000L);

    private SortEngine sortEngine = new SortEngine();

    private final static int TIMEOUT = 1000;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * @api {get} /api/vses: [Read] Batch fetch vs data
     * @apiName ListVSes
     * @apiGroup VS
     * @apiParam {long[]} vsId          1,2,3
     * @apiParam {string[]} vsName      localhost,80
     * @apiParam {string[]} fuzzyName   local,8
     * @apiParam {string[]} ip          reserved
     * @apiParam {string[]} domain      a.ctrip.com,b.ctrip.com
     * @apiParam {boolean} ssl          true/false
     * @apiParam {string} mode          get {online/offline/redundant} (redundant=online&offline) version
     * @apiParam {string} type          get vses with {info/normal/detail/extended} information
     * @apiParam {string[]} anyTag      union search vses by tags e.g. anyTag=group1,group2
     * @apiParam {string[]} tags        join search vses by tags e.g. tags=group1,group2
     * @apiParam {string[]} anyProp     union search vses by properties(key:value) e.g. anyProp=dc:oy,dc:jq
     * @apiParam {string[]} props       join search vses by properties(key:value) e.g. props=department:hotel,dc:jq
     * @apiParam {any} group            supported group property queries, ref /api/groups
     * @apiParam {any} slb              supported slb property queries, ref /api/slbs
     * @apiSuccess {VirtualServer[]} vses   vs list json object
     */
    @GET
    @Path("/vses")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response list(@Context HttpHeaders hh,
                         @Context HttpServletRequest request,
                         @TrimmedQueryParam("mode") final String mode,
                         @TrimmedQueryParam("type") final String type,
                         @Context UriInfo uriInfo) throws Exception {
        QueryEngine queryRender = new QueryEngine(QueryParamRender.extractRawQueryParam(uriInfo), "vs", SelectionMode.getMode(mode));
        queryRender.init(true);
        IdVersion[] searchKeys = queryRender.run(criteriaQueryFactory);

        Long[] vsIdsArray = new Long[searchKeys.length];
        for (int i = 0; i < searchKeys.length; i++) {
            vsIdsArray[i] = searchKeys[i].getId();
        }
        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.READ, ResourceDataType.Vs, vsIdsArray);

        List<VirtualServer> result = virtualServerRepository.listAll(searchKeys);
        ExtendedView.ExtendedVs[] viewArray = new ExtendedView.ExtendedVs[result.size()];

        for (int i = 0; i < result.size(); i++) {
            viewArray[i] = new ExtendedView.ExtendedVs(result.get(i));
        }
        if (ViewConstraints.EXTENDED.equalsIgnoreCase(type)) {
            viewDecorator.decorate(viewArray, "vs");
        }

        if (queryRender.sortRequired()) {
            sortEngine.sort(queryRender.getSortProperty(), viewArray, queryRender.isAsc());
        }

        VsListView listView = new VsListView(result.size());
        for (int i = queryRender.getOffset(); i < queryRender.getOffset() + queryRender.getLimit(viewArray.length); i++) {
            listView.add(viewArray[i]);
        }

        return responseHandler.handleSerializedValue(ObjectJsonWriter.write(listView, type), hh.getMediaType());
    }

    @GET
    @Path("/vs")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getVirtualServer(@Context HttpHeaders hh,
                                     @Context HttpServletRequest request,
                                     @TrimmedQueryParam("mode") final String mode,
                                     @TrimmedQueryParam("type") final String type,
                                     @Context UriInfo uriInfo) throws Exception {
        SelectionMode selectionMode = SelectionMode.getMode(mode);
        QueryEngine queryRender = new QueryEngine(QueryParamRender.extractRawQueryParam(uriInfo), "vs", SelectionMode.getMode(mode));
        queryRender.init(true);
        IdVersion[] searchKeys = queryRender.run(criteriaQueryFactory);

        Long[] vsIdsArray = new Long[searchKeys.length];
        for (int i = 0; i < searchKeys.length; i++) {
            vsIdsArray[i] = searchKeys[i].getId();
        }

        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.READ, ResourceDataType.Vs, vsIdsArray);

        if (SelectionMode.REDUNDANT == selectionMode) {
            if (searchKeys.length > 2)
                throw new ValidationException("Too many matches have been found after querying.");
        } else {
            if (searchKeys.length > 1)
                throw new ValidationException("Too many matches have been found after querying.");
        }

        VsListView listView = new VsListView();
        for (VirtualServer vs : virtualServerRepository.listAll(searchKeys)) {
            listView.add(new ExtendedView.ExtendedVs(vs));
        }
        if (ViewConstraints.EXTENDED.equalsIgnoreCase(type)) {
            viewDecorator.decorate(listView.getList(), "vs");
        }

        if (listView.getTotal() == 0) throw new ValidationException("Virtual server cannot be found.");
        if (listView.getTotal() == 1) {
            return responseHandler.handleSerializedValue(ObjectJsonWriter.write(listView.getList().get(0), type), hh.getMediaType());
        }

        return responseHandler.handleSerializedValue(ObjectJsonWriter.write(listView, type), hh.getMediaType());
    }

    /**
     * @api {post} /api/vs/new: [Write] Create new virtual server
     * @apiName CreateVS
     * @apiGroup VS
     * @apiDescription See [Update vs content](#api-VS-FullUpdateVS) for object description
     * @apiParam {boolean} [force]             skip all validations and forcibly create a group
     * @apiSuccess (Success 200) {VirtualServer} vs    newly created vs object
     **/
    @POST
    @Path("/vs/new")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response addVirtualServer(@Context HttpHeaders hh,
                                     @Context HttpServletRequest request, String requestBody) throws Exception {
        ExtendedView.ExtendedVs extendedView = ObjectJsonParser.parse(requestBody, ExtendedView.ExtendedVs.class);
        VirtualServer vs = ObjectJsonParser.parse(requestBody, VirtualServer.class);
        if (vs == null) {
            throw new ValidationException("Invalid post entity. Fail to parse json to virtual server.");
        }
        trim(vs);

        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.UPDATE, ResourceDataType.Slb, vs.getSlbIds().toArray(new Long[vs.getSlbIds().size()]));

        vs = virtualServerRepository.add(vs);

        try {
            propertyBox.set("status", "deactivated", "vs", vs.getId());
        } catch (Exception ex) {
        }

        if (extendedView.getProperties() != null) {
            setProperties(vs.getId(), extendedView.getProperties());
        }

        if (extendedView.getTags() != null) {
            addTag(vs.getId(), extendedView.getTags());
        }
        String slbMessageData = MessageUtil.getMessageData(request, null, null, new VirtualServer[]{vs}, null, null, true);
        messageQueue.produceMessage(request.getRequestURI(), vs.getId(), slbMessageData);

        return responseHandler.handleSerializedValue(ObjectJsonWriter.write(vs, ViewConstraints.DETAIL), hh.getMediaType());

    }

    /**
     * @api {post} /api/vs/update: [Write] Update vs content
     * @apiName FullUpdateVS
     * @apiGroup VS
     * @apiSuccess {VirtualServer} vs json object
     * @apiParam {boolean} [force]  skip all validations and forcibly create a vs
     * @apiParam (VirtualServer) {Long} id                        id
     * @apiParam (VirtualServer) {String} name                    name
     * @apiParam (VirtualServer) {Integer} version                version
     * @apiParam (VirtualServer) {Boolean} ssl                    https vs
     * @apiParam (VirtualServer) {String} port                    vs port
     * @apiParam (VirtualServer) {Long[]} slb-ids                 slb dependencies
     * @apiParam (VirtualServer) {Domain[]} domains               vs domains
     * @apiParam (VirtualServer) {String[]} [tags]                add tags to group
     * @apiParam (VirtualServer) {Object[]} [properties]          add/update properties of group
     * @apiParam (Domain) {String} name                           domain name
     * @apiExample {json} Usage:
     *  {
     *    "version" : 1,
     *    "name" : "localhost_80",
     *    "id" : 3,
     *    "port" : "80",
     *    "domains" : [ {
     *      "name" : "localhost_80"
     *    } ],
     *    "ssl" : false,
     *    "slb-ids" : [ 3 ]
     *  }
     */
    @POST
    @Path("/vs/update")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response updateVirtualServer(@Context HttpHeaders hh,
                                        @Context HttpServletRequest request, String requestBody) throws Exception {
        ExtendedView.ExtendedVs extendedView = ObjectJsonParser.parse(requestBody, ExtendedView.ExtendedVs.class);
        VirtualServer vs = ObjectJsonParser.parse(requestBody, VirtualServer.class);
        if (vs == null) {
            throw new ValidationException("Invalid post entity. Fail to parse json to virtual server.");
        }
        trim(vs);
        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.UPDATE, ResourceDataType.Vs, vs.getId());

        IdVersion[] check = virtualServerCriteriaQuery.queryByIdAndMode(vs.getId(), SelectionMode.OFFLINE_FIRST);
        if (check.length == 0) throw new ValidationException("Virtual server " + vs.getId() + " cannot be found.");

        DistLock lock = dbLockFactory.newLock(vs.getId() + "_updateVs");
        lock.lock(TIMEOUT);
        try {
            virtualServerRepository.update(vs);
        } finally {
            lock.unlock();
        }

        try {
            if (virtualServerCriteriaQuery.queryByIdAndMode(vs.getId(), SelectionMode.ONLINE_EXCLUSIVE).length == 1) {
                propertyBox.set("status", "toBeActivated", "vs", vs.getId());
            }
        } catch (Exception ex) {
        }

        if (extendedView.getProperties() != null) {
            setProperties(vs.getId(), extendedView.getProperties());
        }

        if (extendedView.getTags() != null) {
            addTag(vs.getId(), extendedView.getTags());
        }

        String slbMessageData = MessageUtil.getMessageData(request, null, null, new VirtualServer[]{vs}, null, null, true);
        messageQueue.produceMessage(request.getRequestURI(), vs.getId(), slbMessageData);

        return responseHandler.handleSerializedValue(ObjectJsonWriter.write(vs, ViewConstraints.DETAIL), hh.getMediaType());
    }

    @GET
    @Path("/vs/addDomain")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response addDomain(@Context HttpHeaders hh,
                              @Context HttpServletRequest request,
                              @QueryParam("vsId") Long vsId,
                              @TrimmedQueryParam("domain") String domain) throws Exception {
        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.UPDATE, ResourceDataType.Vs, vsId);

        VirtualServer vs = virtualServerRepository.getById(vsId);
        if (vs == null) throw new ValidationException("Virtual server " + vsId + " cannot be found.");

        for (String d : domain.split(",")) {
            vs.addDomain(new Domain().setName(d));
        }

        DistLock lock = dbLockFactory.newLock(vs.getId() + "_updateVs");
        lock.lock(TIMEOUT);
        try {
            virtualServerRepository.update(vs);
        } finally {
            lock.unlock();
        }

        try {
            if (virtualServerCriteriaQuery.queryByIdAndMode(vs.getId(), SelectionMode.ONLINE_EXCLUSIVE).length == 1) {
                propertyBox.set("status", "toBeActivated", "vs", vs.getId());
            }
        } catch (Exception ex) {
        }

        String slbMessageData = MessageUtil.getMessageData(request, null, null, new VirtualServer[]{vs}, null, null, true);
        messageQueue.produceMessage(request.getRequestURI(), vs.getId(), slbMessageData);

        return responseHandler.handleSerializedValue(ObjectJsonWriter.write(vs, ViewConstraints.DETAIL), hh.getMediaType());
    }

    @GET
    @Path("vs/removeDomain")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response removeDomain(@Context HttpHeaders hh,
                                 @Context HttpServletRequest request,
                                 @QueryParam("vsId") Long vsId,
                                 @TrimmedQueryParam("domain") String domain) throws Exception {
        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.UPDATE, ResourceDataType.Vs, vsId);

        VirtualServer vs = virtualServerRepository.getById(vsId);
        if (vs == null) throw new ValidationException("Virtual server " + vsId + " cannot be found.");

        Set<String> domains = new HashSet<>();
        for (String d : domain.split(",")) {
            domains.add(d);
        }
        Iterator<Domain> iter = vs.getDomains().iterator();
        while (iter.hasNext()) {
            Domain d = iter.next();
            if (domains.contains(d.getName())) {
                iter.remove();
            }
        }

        DistLock lock = dbLockFactory.newLock(vs.getId() + "_updateVs");
        lock.lock(TIMEOUT);
        try {
            virtualServerRepository.update(vs);
        } finally {
            lock.unlock();
        }

        try {
            if (virtualServerCriteriaQuery.queryByIdAndMode(vs.getId(), SelectionMode.ONLINE_EXCLUSIVE).length == 1) {
                propertyBox.set("status", "toBeActivated", "vs", vs.getId());
            }
        } catch (Exception ex) {
        }

        String slbMessageData = MessageUtil.getMessageData(request, null, null, new VirtualServer[]{vs}, null, null, true);
        messageQueue.produceMessage(request.getRequestURI(), vs.getId(), slbMessageData);

        return responseHandler.handleSerializedValue(ObjectJsonWriter.write(vs, ViewConstraints.DETAIL), hh.getMediaType());
    }

    @GET
    @Path("/vs/delete")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response deleteVirtualServer(@Context HttpHeaders hh,
                                        @Context HttpServletRequest request,
                                        @QueryParam("vsId") Long vsId) throws Exception {
        if (vsId == null)
            throw new ValidationException("Query parameter - vsId is required.");
        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.DELETE, ResourceDataType.Vs, vsId);

        VirtualServer archive = virtualServerRepository.getById(vsId);
        if (archive == null) throw new ValidationException("Virtual server cannot be found with id " + vsId + ".");

        virtualServerRepository.delete(vsId);
        try {
            archiveRepository.archiveVs(archive);
        } catch (Exception ex) {
            logger.warn("Try archive deleted vs-" + vsId + " failed.", ex);
        }

        try {
            propertyBox.clear("vs", vsId);
        } catch (Exception ex) {
        }
        try {
            tagBox.clear("vs", vsId);
        } catch (Exception ex) {
        }

        String slbMessageData = MessageUtil.getMessageData(request, null, null, null, null, null, true);
        messageQueue.produceMessage(request.getRequestURI(), vsId, slbMessageData);

        return responseHandler.handle("Successfully deleted virtual server with id " + vsId + ".", hh.getMediaType());
    }

    @GET
    @Path("/vs/split")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response split(@Context HttpHeaders hh, @Context HttpServletRequest request, @QueryParam("vsId") Long vsId,
                          @QueryParam("force") Boolean force, @TrimmedQueryParam("domain") String domain) throws Exception {
        if (configHandler.getEnable("auth.with.force", false) && force != null && force) {
            authService.authValidateWithForce(UserUtils.getUserName(request), ResourceOperationType.UPDATE, ResourceDataType.Vs, vsId);
        } else {
            authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.UPDATE, ResourceDataType.Vs, vsId);
        }
        VirtualServer vs = virtualServerRepository.getById(vsId);
        if (vs == null) throw new ValidationException("Virtual server " + vsId + " cannot be found.");
        if (domain == null) throw new ValidationException("Miss Parameter [domain].");
        ModelStatusMapping<Group> groupMapping = entityFactory.getGroupsByVsIds(new Long[]{vsId});
        List<DistLock> groupLock = new ArrayList<>();
        DistLock lock = null;
        try {
            for (Long gid : groupMapping.getOfflineMapping().keySet()) {
                if (groupMapping.getOnlineMapping().get(gid) != null
                        && groupMapping.getOfflineMapping().get(gid).getVersion()
                        .equals(groupMapping.getOnlineMapping().get(gid).getVersion())) {
                    DistLock tmp = dbLockFactory.newLock(gid + "_updateGroup");
                    tmp.lock(TIMEOUT);
                    groupLock.add(tmp);
                } else {
                    throw new ValidationException("Group have different online/offline version. GroupId:" + gid);
                }
            }
            lock = dbLockFactory.newLock(vs.getId() + "_updateVs");
            lock.lock(TIMEOUT);
            VirtualServer virtualServer = updateVsForSplit(domain, vs);
            List<Group> updatedGroups = updateGroupsForSplit(groupMapping, virtualServer, vsId, force);

            List<OpsTask> tasks = new ArrayList<>();
            for (Long slbId : vs.getSlbIds()) {
                OpsTask task = new OpsTask();
                task.setSlbVirtualServerId(vsId)
                        .setTargetSlbId(slbId)
                        .setVersion(vs.getVersion())
                        .setOpsType(TaskOpsType.ACTIVATE_VS)
                        .setSkipValidate(force == null ? false : force)
                        .setCreateTime(new Date());
                tasks.add(task);
                task = new OpsTask();
                task.setSlbVirtualServerId(virtualServer.getId())
                        .setTargetSlbId(slbId)
                        .setVersion(virtualServer.getVersion())
                        .setOpsType(TaskOpsType.ACTIVATE_VS)
                        .setSkipValidate(force == null ? false : force)
                        .setCreateTime(new Date());
                tasks.add(task);
                for (Group g : updatedGroups) {
                    task = new OpsTask();
                    task.setGroupId(g.getId())
                            .setTargetSlbId(slbId)
                            .setVersion(g.getVersion())
                            .setOpsType(TaskOpsType.ACTIVATE_GROUP)
                            .setSkipValidate(force == null ? false : force)
                            .setCreateTime(new Date());
                    tasks.add(task);
                }
            }
            List<Long> taskIds = taskManager.addAggTask(tasks);
            List<TaskResult> results = taskManager.getAggResult(taskIds, apiTimeout.get());
            TaskResultList resultList = new TaskResultList();
            for (TaskResult t : results) {
                resultList.addTaskResult(t);
            }
            resultList.setTotal(results.size());
            String slbMessageData = MessageUtil.getMessageData(request, null, null, new VirtualServer[]{vs}, null, null, true);
            messageQueue.produceMessage(request.getRequestURI(), vs.getId(), slbMessageData);
            return responseHandler.handle(resultList, hh.getMediaType());
        } finally {
            if (lock != null) {
                lock.unlock();
            }
            for (DistLock distLock : groupLock) {
                distLock.unlock();
            }
        }
    }

    private List<Group> updateGroupsForSplit(ModelStatusMapping<Group> groupMapping, VirtualServer virtualServer, Long vsId, Boolean force) throws Exception {
        List<Group> result = new ArrayList<>();
        for (Group group : groupMapping.getOfflineMapping().values()) {
            for (GroupVirtualServer gvs : group.getGroupVirtualServers()) {
                if (gvs.getVirtualServer().getId().equals(vsId)) {
                    GroupVirtualServer mockGvs = new GroupVirtualServer();
                    mockGvs.setPriority(gvs.getPriority());
                    mockGvs.setPath(gvs.getPath());
                    mockGvs.setName(gvs.getName());
                    mockGvs.setRewrite(gvs.getRewrite());
                    mockGvs.setRedirect(gvs.getRedirect());
                    mockGvs.setVirtualServer(virtualServer);
                    group.addGroupVirtualServer(mockGvs);
                    result.add(groupRepository.add(group, force == null ? false : force));
                }
            }
        }
        return result;
    }

    private VirtualServer updateVsForSplit(String domain, VirtualServer vs) throws Exception {
        String[] domains = domain.split(",");
        VirtualServer virtualServer = new VirtualServer();
        virtualServer.setSlbId(vs.getSlbId());
        virtualServer.getSlbIds().addAll(vs.getSlbIds());
        virtualServer.setSsl(vs.getSsl());
        virtualServer.setPort(vs.getPort());
        virtualServer.setName(domains[0]);
        for (String d : domains) {
            Domain tmp = new Domain().setName(d);
            if (vs.getDomains().contains(tmp)) {
                vs.getDomains().remove(tmp);
                virtualServer.addDomain(tmp);
            } else {
                throw new ValidationException("Not found domain:" + d + " in vs.VsId:" + vs.getId());
            }
        }
        virtualServerRepository.update(vs);
        virtualServer = virtualServerRepository.add(virtualServer);
        if (virtualServerCriteriaQuery.queryByIdAndMode(vs.getId(), SelectionMode.ONLINE_EXCLUSIVE).length == 1) {
            setProperty(vs.getId(), new Property().setName("status").setValue("toBeActivated"));
        }
        setProperty(virtualServer.getId(), new Property().setName("status").setValue("deactivated"));
        return virtualServer;
    }


    private void setProperties(Long vsId, List<Property> properties) {
        for (Property p : properties) {
            try {
                propertyBox.set(p.getName(), p.getValue(), "vs", vsId);
            } catch (Exception e) {
                logger.warn("Fail to set property " + p.getName() + "/" + p.getValue() + " on vs " + vsId + ".");
            }
        }
    }

    private void setProperty(Long vsId, Property property) {
        try {
            propertyBox.set(property.getName(), property.getValue(), "vs", vsId);
        } catch (Exception e) {
            logger.warn("Fail to set property " + property.getName() + "/" + property.getValue() + " on vs " + vsId + ".");
        }
    }

    private void addTag(Long vsId, List<String> tags) {
        for (String tag : tags) {
            try {
                tagBox.tagging(tag, "vs", new Long[]{vsId});
            } catch (Exception e) {
                logger.warn("Fail to tagging " + tag + " on vs " + vsId + ".");
            }
        }
    }

    private void trim(VirtualServer vs) throws Exception {
        vs.setName(trimIfNotNull(vs.getName()));
        vs.setPort(trimIfNotNull(vs.getPort()));
        for (Domain domain : vs.getDomains()) {
            domain.setName(trimIfNotNull(domain.getName()));
        }
    }

    private String trimIfNotNull(String value) {
        return value != null ? value.trim() : value;
    }
}
