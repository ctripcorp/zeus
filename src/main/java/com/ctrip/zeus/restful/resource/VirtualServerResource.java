package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.auth.Authorize;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.lock.DbLockFactory;
import com.ctrip.zeus.lock.DistLock;
import com.ctrip.zeus.model.entity.Domain;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.restful.message.QueryParamRender;
import com.ctrip.zeus.restful.message.view.ExtendedView;
import com.ctrip.zeus.restful.message.view.ViewConstraints;
import com.ctrip.zeus.restful.message.view.ViewDecorator;
import com.ctrip.zeus.restful.message.view.VsListView;
import com.ctrip.zeus.service.build.ConfigHandler;
import com.ctrip.zeus.service.message.queue.MessageQueue;
import com.ctrip.zeus.service.message.queue.MessageType;
import com.ctrip.zeus.service.query.CriteriaQueryFactory;
import com.ctrip.zeus.service.query.QueryEngine;
import com.ctrip.zeus.restful.message.ResponseHandler;
import com.ctrip.zeus.restful.message.TrimmedQueryParam;
import com.ctrip.zeus.service.model.ArchiveRepository;
import com.ctrip.zeus.service.model.SelectionMode;
import com.ctrip.zeus.service.model.VirtualServerRepository;
import com.ctrip.zeus.service.model.IdVersion;
import com.ctrip.zeus.service.query.VirtualServerCriteriaQuery;
import com.ctrip.zeus.service.query.sort.SortEngine;
import com.ctrip.zeus.support.ObjectJsonParser;
import com.ctrip.zeus.support.ObjectJsonWriter;
import com.ctrip.zeus.util.MessageUtil;
import com.ctrip.zeus.tag.PropertyBox;
import com.ctrip.zeus.tag.TagBox;
import com.ctrip.zeus.tag.entity.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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

    private SortEngine sortEngine = new SortEngine();

    private final int TIMEOUT = 1000;

    Logger logger = LoggerFactory.getLogger(this.getClass());

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
    @Authorize(name = "getAllVses")
    public Response list(@Context HttpHeaders hh,
                         @Context HttpServletRequest request,
                         @TrimmedQueryParam("mode") final String mode,
                         @TrimmedQueryParam("type") final String type,
                         @Context UriInfo uriInfo) throws Exception {
        QueryEngine queryRender = new QueryEngine(QueryParamRender.extractRawQueryParam(uriInfo), "vs", SelectionMode.getMode(mode));
        queryRender.init(true);
        IdVersion[] searchKeys = queryRender.run(criteriaQueryFactory);

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
    @Authorize(name = "getVs")
    public Response getVirtualServer(@Context HttpHeaders hh,
                                     @Context HttpServletRequest request,
                                     @TrimmedQueryParam("mode") final String mode,
                                     @TrimmedQueryParam("type") final String type,
                                     @Context UriInfo uriInfo) throws Exception {
        SelectionMode selectionMode = SelectionMode.getMode(mode);
        QueryEngine queryRender = new QueryEngine(QueryParamRender.extractRawQueryParam(uriInfo), "vs", SelectionMode.getMode(mode));
        queryRender.init(true);
        IdVersion[] searchKeys = queryRender.run(criteriaQueryFactory);

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
    @Authorize(name = "addVs")
    public Response addVirtualServer(@Context HttpHeaders hh,
                                     @Context HttpServletRequest request, String requestBody) throws Exception {
        ExtendedView.ExtendedVs extendedView = ObjectJsonParser.parse(requestBody, ExtendedView.ExtendedVs.class);
        VirtualServer vs = ObjectJsonParser.parse(requestBody, VirtualServer.class);
        if (vs == null) {
            throw new ValidationException("Invalid post entity. Fail to parse json to virtual server.");
        }
        trim(vs);

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
        String slbMessageData = MessageUtil.getMessageData(request, null, new VirtualServer[]{vs}, null, null, true);
        if (configHandler.getEnable("use.new,message.queue.producer", false)) {
            messageQueue.produceMessage(request.getRequestURI(), vs.getId(), slbMessageData);
        } else {
            messageQueue.produceMessage(MessageType.NewVs, vs.getId(), slbMessageData);
        }

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
    @Authorize(name = "updateVs")
    public Response updateVirtualServer(@Context HttpHeaders hh,
                                        @Context HttpServletRequest request, String requestBody) throws Exception {
        ExtendedView.ExtendedVs extendedView = ObjectJsonParser.parse(requestBody, ExtendedView.ExtendedVs.class);
        VirtualServer vs = ObjectJsonParser.parse(requestBody, VirtualServer.class);
        if (vs == null) {
            throw new ValidationException("Invalid post entity. Fail to parse json to virtual server.");
        }
        trim(vs);

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

        String slbMessageData = MessageUtil.getMessageData(request, null, new VirtualServer[]{vs}, null, null, true);
        if (configHandler.getEnable("use.new,message.queue.producer", false)) {
            messageQueue.produceMessage(request.getRequestURI(), vs.getId(), slbMessageData);
        } else {
            messageQueue.produceMessage(MessageType.UpdateVs, vs.getId(), slbMessageData);
        }

        return responseHandler.handleSerializedValue(ObjectJsonWriter.write(vs, ViewConstraints.DETAIL), hh.getMediaType());
    }

    @GET
    @Path("/vs/addDomain")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Authorize(name = "updateVs")
    public Response addDomain(@Context HttpHeaders hh,
                              @Context HttpServletRequest request,
                              @QueryParam("vsId") Long vsId,
                              @TrimmedQueryParam("domain") String domain) throws Exception {
        VirtualServer vs = virtualServerRepository.getById(vsId);
        if (vs == null) throw new ValidationException("Virtual server " + vs.getId() + " cannot be found.");

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

        String slbMessageData = MessageUtil.getMessageData(request, null, new VirtualServer[]{vs}, null, null, true);
        if (configHandler.getEnable("use.new,message.queue.producer", false)) {
            messageQueue.produceMessage(request.getRequestURI(), vs.getId(), slbMessageData);
        } else {
            messageQueue.produceMessage(MessageType.UpdateVs, vs.getId(), slbMessageData);
        }

        return responseHandler.handleSerializedValue(ObjectJsonWriter.write(vs, ViewConstraints.DETAIL), hh.getMediaType());
    }

    @GET
    @Path("vs/removeDomain")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Authorize(name = "updateVs")
    public Response removeDomain(@Context HttpHeaders hh,
                                 @Context HttpServletRequest request,
                                 @QueryParam("vsId") Long vsId,
                                 @TrimmedQueryParam("domain") String domain) throws Exception {
        VirtualServer vs = virtualServerRepository.getById(vsId);
        if (vs == null) throw new ValidationException("Virtual server " + vs.getId() + " cannot be found.");

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

        String slbMessageData = MessageUtil.getMessageData(request, null, new VirtualServer[]{vs}, null, null, true);
        if (configHandler.getEnable("use.new,message.queue.producer", false)) {
            messageQueue.produceMessage(request.getRequestURI(), vs.getId(), slbMessageData);
        } else {
            messageQueue.produceMessage(MessageType.UpdateVs, vs.getId(), slbMessageData);
        }
        return responseHandler.handleSerializedValue(ObjectJsonWriter.write(vs, ViewConstraints.DETAIL), hh.getMediaType());
    }

    @GET
    @Path("/vs/delete")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Authorize(name = "deleteVs")
    public Response deleteVirtualServer(@Context HttpHeaders hh,
                                        @Context HttpServletRequest request,
                                        @QueryParam("vsId") Long vsId) throws Exception {
        if (vsId == null)
            throw new ValidationException("Query parameter - vsId is required.");
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

        String slbMessageData = MessageUtil.getMessageData(request, null, null, null, null, true);
        if (configHandler.getEnable("use.new,message.queue.producer", false)) {
            messageQueue.produceMessage(request.getRequestURI(), vsId, slbMessageData);
        } else {
            messageQueue.produceMessage(MessageType.DeleteVs, vsId, slbMessageData);
        }
        return responseHandler.handle("Successfully deleted virtual server with id " + vsId + ".", hh.getMediaType());
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
