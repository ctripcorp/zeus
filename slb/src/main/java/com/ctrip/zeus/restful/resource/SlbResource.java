package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.lock.DbLockFactory;
import com.ctrip.zeus.lock.DistLock;
import com.ctrip.zeus.model.Property;
import com.ctrip.zeus.model.model.Slb;
import com.ctrip.zeus.model.model.SlbServer;
import com.ctrip.zeus.restful.message.QueryParamRender;
import com.ctrip.zeus.restful.message.ResponseHandler;
import com.ctrip.zeus.restful.message.TrimmedQueryParam;
import com.ctrip.zeus.restful.message.view.ExtendedView;
import com.ctrip.zeus.restful.message.view.SlbListView;
import com.ctrip.zeus.restful.message.view.ViewConstraints;
import com.ctrip.zeus.restful.message.view.ViewDecorator;
import com.ctrip.zeus.service.auth.AuthDefaultValues;
import com.ctrip.zeus.service.auth.AuthService;
import com.ctrip.zeus.service.auth.ResourceDataType;
import com.ctrip.zeus.service.auth.ResourceOperationType;
import com.ctrip.zeus.service.message.queue.MessageQueue;
import com.ctrip.zeus.service.model.ArchiveRepository;
import com.ctrip.zeus.service.model.IdVersion;
import com.ctrip.zeus.service.model.SelectionMode;
import com.ctrip.zeus.service.model.SlbRepository;
import com.ctrip.zeus.service.query.CriteriaQueryFactory;
import com.ctrip.zeus.service.query.QueryEngine;
import com.ctrip.zeus.service.query.SlbCriteriaQuery;
import com.ctrip.zeus.service.query.sort.SortEngine;
import com.ctrip.zeus.support.ObjectJsonParser;
import com.ctrip.zeus.support.ObjectJsonWriter;
import com.ctrip.zeus.tag.PropertyBox;
import com.ctrip.zeus.tag.TagBox;
import com.ctrip.zeus.util.MessageUtil;
import com.ctrip.zeus.util.UserUtils;
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
 * @author:xingchaowang
 * @date: 3/4/2015.
 */
@Component
@Path("/")
public class SlbResource {
    @Resource
    private SlbRepository slbRepository;
    @Resource
    private ArchiveRepository archiveRepository;
    @Resource
    private ResponseHandler responseHandler;
    @Resource
    private DbLockFactory dbLockFactory;
    @Resource
    private CriteriaQueryFactory criteriaQueryFactory;
    @Resource
    private SlbCriteriaQuery slbCriteriaQuery;
    @Resource
    private PropertyBox propertyBox;
    @Resource
    private TagBox tagBox;
    @Resource
    private ViewDecorator viewDecorator;
    @Resource
    private MessageQueue messageQueue;

    @Resource
    private AuthService authService;
    private SortEngine sortEngine = new SortEngine();
    private final static int TIMEOUT = 1000;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * @api {get} /api/slbs: [Read] Batch fetch slb data
     * @apiName ListSlbs
     * @apiGroup Slb
     * @apiParam {long[]} slbId         1,2,3
     * @apiParam {string[]} slbName     slb,dev
     * @apiParam {string[]} fuzzyName   sl,d
     * @apiParam {string[]} ip          127.0.0.1,127.0.0.1
     * @apiParam {string[]} vip         not supported yet
     * @apiParam {string} mode          get {online/offline/redundant} (redundant=online&offline) version
     * @apiParam {string} type          get slbs with {info/normal/detail/extended} information
     * @apiParam {string[]} anyTag      union search slbs by tags e.g. anyTag=group1,group2
     * @apiParam {string[]} tags        join search slbs by tags e.g. tags=group1,group2
     * @apiParam {string[]} anyProp     union search slbs by properties(key:value) e.g. anyProp=dc:oy,dc:jq
     * @apiParam {string[]} props       join search slbs by properties(key:value) e.g. props=department:hotel,dc:jq
     * @apiParam {any} vs               supported vs property queries, ref /api/vses
     * @apiParam {any} group            supported group property queries, ref /api/groups
     * @apiSuccess {Slb[]} slbs         slb list json object
     */
    @GET
    @Path("/slbs")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response list(@Context final HttpHeaders hh,
                         @Context HttpServletRequest request,
                         @TrimmedQueryParam("mode") final String mode,
                         @TrimmedQueryParam("type") final String type,
                         @Context UriInfo uriInfo) throws Exception {
        QueryEngine queryRender = new QueryEngine(QueryParamRender.extractRawQueryParam(uriInfo), "slb", SelectionMode.getMode(mode));
        queryRender.init(true);
        IdVersion[] searchKeys = queryRender.run(criteriaQueryFactory);

        Long[] slbIdsArray = new Long[searchKeys.length];
        for (int i = 0; i < searchKeys.length; i++) {
            slbIdsArray[i] = searchKeys[i].getId();
        }
        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.READ, ResourceDataType.Slb, slbIdsArray);

        List<Slb> result = slbRepository.list(searchKeys);
        ExtendedView.ExtendedSlb[] viewArray = new ExtendedView.ExtendedSlb[result.size()];

        for (int i = 0; i < result.size(); i++) {
            viewArray[i] = new ExtendedView.ExtendedSlb(result.get(i));
        }
        if (ViewConstraints.EXTENDED.equalsIgnoreCase(type)) {
            viewDecorator.decorate(viewArray, "slb");
        }

        if (queryRender.sortRequired()) {
            sortEngine.sort(queryRender.getSortProperty(), viewArray, queryRender.isAsc());
        }

        SlbListView listView = new SlbListView(result.size());
        for (int i = queryRender.getOffset(); i < queryRender.getOffset() + queryRender.getLimit(viewArray.length); i++) {
            listView.add(viewArray[i]);
        }

        return responseHandler.handleSerializedValue(ObjectJsonWriter.write(listView, type), hh.getMediaType());
    }

    @GET
    @Path("/slb")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response get(@Context HttpHeaders hh, @Context HttpServletRequest request,
                        @TrimmedQueryParam("type") final String type,
                        @TrimmedQueryParam("mode") final String mode,
                        @Context UriInfo uriInfo) throws Exception {
        SelectionMode selectionMode = SelectionMode.getMode(mode);
        QueryEngine queryRender = new QueryEngine(QueryParamRender.extractRawQueryParam(uriInfo), "slb", selectionMode);
        queryRender.init(true);
        IdVersion[] searchKeys = queryRender.run(criteriaQueryFactory);

        Long[] slbIdsArray = new Long[searchKeys.length];
        for (int i = 0; i < searchKeys.length; i++) {
            slbIdsArray[i] = searchKeys[i].getId();
        }
        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.READ, ResourceDataType.Slb, slbIdsArray);

        if (SelectionMode.REDUNDANT == selectionMode) {
            if (searchKeys.length > 2)
                throw new ValidationException("Too many matches have been found after querying.");
        } else {
            if (searchKeys.length > 1)
                throw new ValidationException("Too many matches have been found after querying.");
        }

        SlbListView listView = new SlbListView();
        for (Slb slb : slbRepository.list(searchKeys)) {
            listView.add(new ExtendedView.ExtendedSlb(slb));
        }
        if (ViewConstraints.EXTENDED.equalsIgnoreCase(type)) {
            viewDecorator.decorate(listView.getList(), "slb");
        }

        if (listView.getTotal() == 0) throw new ValidationException("Slb cannot be found.");
        if (listView.getTotal() == 1) {
            return responseHandler.handleSerializedValue(ObjectJsonWriter.write(listView.getList().get(0), type), hh.getMediaType());
        }

        return responseHandler.handleSerializedValue(ObjectJsonWriter.write(listView, type), hh.getMediaType());
    }

    @POST
    @Path("/slb/new")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "*/*"})
    public Response add(@Context HttpHeaders hh, @Context HttpServletRequest request, String requestBody) throws Exception {
        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.UPDATE, ResourceDataType.Slb, AuthDefaultValues.ALL);

        ExtendedView.ExtendedSlb extendedView = ObjectJsonParser.parse(requestBody, ExtendedView.ExtendedSlb.class);
        Slb s = ObjectJsonParser.parse(requestBody, Slb.class);
        if (s == null) {
            throw new ValidationException("Invalid post entity. Fail to parse json to slb.");
        }
        if (s.getName() == null) {
            throw new ValidationException("Field `name` is not allowed empty.");
        }
        trim(s);
        Long checkId = slbCriteriaQuery.queryByName(s.getName());
        if (checkId > 0L) {
            throw new ValidationException("Slb name " + s.getName() + " has been taken by " + checkId + ".");
        }

        s = slbRepository.add(s);

        try {
            propertyBox.set("status", "deactivated", "slb", s.getId());
        } catch (Exception ex) {
        }

        if (extendedView.getProperties() != null) {
            setProperties(s.getId(), extendedView.getProperties());
        }

        if (extendedView.getTags() != null) {
            addTag(s.getId(), extendedView.getTags());
        }

        String slbMessageData = MessageUtil.getMessageData(request, null, null, null, new Slb[]{s}, null, true);
        messageQueue.produceMessage(request.getRequestURI(), s.getId(), slbMessageData);

        return responseHandler.handleSerializedValue(ObjectJsonWriter.write(new ExtendedView.ExtendedSlb(s), ViewConstraints.DETAIL), hh.getMediaType());
    }

    @POST
    @Path("/slb/update")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "*/*"})
    public Response update(@Context HttpHeaders hh, @Context HttpServletRequest request, String requestBody) throws Exception {
        ExtendedView.ExtendedSlb extendedView = ObjectJsonParser.parse(requestBody, ExtendedView.ExtendedSlb.class);
        Slb s = ObjectJsonParser.parse(requestBody, Slb.class);
        if (s == null) {
            throw new ValidationException("Invalid post entity. Fail to parse json to slb.");
        }
        if (s.getName() == null) {
            throw new ValidationException("Field `name` is not allowed empty.");
        }
        trim(s);
        Long checkId = slbCriteriaQuery.queryByName(s.getName());
        if (checkId > 0L && !checkId.equals(s.getId())) {
            throw new ValidationException("Slb name " + s.getName() + " has been taken by " + checkId + ".");
        }

        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.UPDATE, ResourceDataType.Slb, s.getId());


        IdVersion[] check = slbCriteriaQuery.queryByIdAndMode(s.getId(), SelectionMode.OFFLINE_FIRST);
        if (check.length == 0) throw new ValidationException("Slb " + s.getId() + " cannot be found.");
        DistLock lock = dbLockFactory.newLock(s.getId() + "_updateSlb");
        lock.lock(TIMEOUT);
        try {
            s = slbRepository.update(s);
        } finally {
            lock.unlock();
        }

        try {
            if (slbCriteriaQuery.queryByIdAndMode(s.getId(), SelectionMode.ONLINE_EXCLUSIVE).length == 1) {
                propertyBox.set("status", "toBeActivated", "slb", s.getId());
            }
        } catch (Exception ex) {
        }
        if (extendedView.getProperties() != null) {
            setProperties(s.getId(), extendedView.getProperties());
        }

        if (extendedView.getTags() != null) {
            addTag(s.getId(), extendedView.getTags());
        }

        String slbMessageData = MessageUtil.getMessageData(request, null, null, null, new Slb[]{s}, null, true);
        messageQueue.produceMessage(request.getRequestURI(), s.getId(), slbMessageData);

        return responseHandler.handleSerializedValue(ObjectJsonWriter.write(new ExtendedView.ExtendedSlb(s), ViewConstraints.DETAIL), hh.getMediaType());
    }

    @GET
    @Path("/slb/delete")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response delete(@Context HttpHeaders hh, @Context HttpServletRequest request, @QueryParam("slbId") Long slbId) throws Exception {
        if (slbId == null) throw new ValidationException("Field slb-id is required.");
        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.DELETE, ResourceDataType.Slb, slbId);


        Slb archive = slbRepository.getById(slbId);
        if (archive == null) throw new ValidationException("Slb cannot be found with id " + slbId + ".");

        int count = slbRepository.delete(slbId);
        try {
            archiveRepository.archiveSlb(archive);
        } catch (Exception ex) {
            logger.warn("Try archive deleted slb-" + slbId + " failed.", ex);
        }

        try {
            propertyBox.clear("slb", slbId);
        } catch (Exception ex) {
        }
        try {
            tagBox.clear("slb", slbId);
        } catch (Exception ex) {
        }

        String message = count == 1 ? "Delete slb successfully." : "No deletion is needed.";

        String slbMessageData = MessageUtil.getMessageData(request, null, null, null, new Slb[]{archive}, null, true);
        messageQueue.produceMessage(request.getRequestURI(), archive.getId(), slbMessageData);

        return responseHandler.handle(message, hh.getMediaType());
    }

    @POST
    @Path("/slb/addServer")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "*/*"})
    public Response addServer(@Context HttpHeaders hh, @Context HttpServletRequest request, String requestBody) throws Exception {
        Slb serverList = ObjectJsonParser.parse(requestBody, Slb.class);
        if (serverList == null) throw new ValidationException("Fail to parse request.");

        Long slbId = serverList.getId();
        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.UPDATE, ResourceDataType.Slb, slbId);

        IdVersion[] searchKey = slbCriteriaQuery.queryByIdAndMode(slbId, SelectionMode.OFFLINE_FIRST);
        if (searchKey.length == 0) throw new ValidationException("Oops, slb " + slbId + " does not exists.");

        DistLock lock = dbLockFactory.newLock(slbId + "_updateSlb");
        lock.lock(TIMEOUT);

        Slb slb;
        try {
            slb = slbRepository.getByKey(searchKey[0]);


            for (SlbServer server : serverList.getSlbServers()) {
                slb.addSlbServer(server);
            }
            slb = slbRepository.update(slb);

        } finally {
            lock.unlock();
        }

        try {
            if (slbCriteriaQuery.queryByIdAndMode(slbId, SelectionMode.ONLINE_EXCLUSIVE).length == 1) {
                propertyBox.set("status", "toBeActivated", "slb", slbId);
            }
        } catch (Exception ex) {
        }

        String[] ips = new String[serverList.getSlbServers().size()];
        for (int i = 0; i < ips.length; i++) {
            ips[i] = serverList.getSlbServers().get(i).getIp();
        }
        String slbMessageData = MessageUtil.getMessageData(request, null, null, null, new Slb[]{slb}, ips, true);
        messageQueue.produceMessage(request.getRequestURI(), slb.getId(), slbMessageData);

        return responseHandler.handleSerializedValue(ObjectJsonWriter.write(new ExtendedView.ExtendedSlb(slb), ViewConstraints.DETAIL), hh.getMediaType());
    }

    @GET
    @Path("/slb/removeServer")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "*/*"})
    public Response removeServer(@Context HttpHeaders hh, @Context HttpServletRequest request,
                                 @QueryParam("slbId") Long slbId,
                                 @TrimmedQueryParam("ip") String ip) throws Exception {
        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.UPDATE, ResourceDataType.Slb, slbId);

        IdVersion[] searchKey = slbCriteriaQuery.queryByIdAndMode(slbId, SelectionMode.OFFLINE_FIRST);
        if (searchKey.length == 0) throw new ValidationException("Oops, slb " + slbId + " does not exists.");

        DistLock lock = dbLockFactory.newLock(slbId + "_updateSlb");
        lock.lock(TIMEOUT);

        Slb slb;
        Set<String> servers = new HashSet<>();
        try {
            for (String s : ip.split(",")) {
                servers.add(s);
            }

            slb = slbRepository.getByKey(searchKey[0]);
            Iterator<SlbServer> iter = slb.getSlbServers().iterator();

            while (iter.hasNext()) {
                SlbServer server = iter.next();
                if (servers.contains(server.getIp())) {
                    iter.remove();
                }
            }
            slb = slbRepository.update(slb);
        } finally {
            lock.unlock();
        }

        try {
            if (slbCriteriaQuery.queryByIdAndMode(slbId, SelectionMode.ONLINE_EXCLUSIVE).length == 1) {
                propertyBox.set("status", "toBeActivated", "slb", slbId);
            }
        } catch (Exception ex) {
        }

        String slbMessageData = MessageUtil.getMessageData(request, null, null, null, new Slb[]{slb}, servers.toArray(new String[servers.size()]), true);
        messageQueue.produceMessage(request.getRequestURI(), slb.getId(), slbMessageData);

        return responseHandler.handleSerializedValue(ObjectJsonWriter.write(new ExtendedView.ExtendedSlb(slb), ViewConstraints.DETAIL), hh.getMediaType());
    }

    private void setProperties(Long slbId, List<Property> properties) {
        for (Property p : properties) {
            try {
                propertyBox.set(p.getName(), p.getValue(), "slb", slbId);
            } catch (Exception e) {
                logger.warn("Fail to set property " + p.getName() + "/" + p.getValue() + " on slb " + slbId + ".");
            }
        }
    }

    private void addTag(Long slbId, List<String> tags) {
        for (String tag : tags) {
            try {
                tagBox.tagging(tag, "slb", new Long[]{slbId});
            } catch (Exception e) {
                logger.warn("Fail to tagging " + tag + " on slb " + slbId + ".");
            }
        }
    }

    private void trim(Slb s) throws Exception {
        s.setName(trimIfNotNull(s.getName()));
        for (SlbServer slbServer : s.getSlbServers()) {
            slbServer.setIp(trimIfNotNull(slbServer.getIp()));
            slbServer.setHostName(trimIfNotNull(slbServer.getHostName()));
        }
    }

    private String trimIfNotNull(String value) {
        return value != null ? value.trim() : value;
    }
}
