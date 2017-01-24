package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.TrafficPolicy;
import com.ctrip.zeus.restful.message.QueryParamRender;
import com.ctrip.zeus.restful.message.ResponseHandler;
import com.ctrip.zeus.restful.message.TrimmedQueryParam;
import com.ctrip.zeus.restful.message.view.ExtendedView;
import com.ctrip.zeus.restful.message.view.TrafficPolicyListView;
import com.ctrip.zeus.restful.message.view.ViewConstraints;
import com.ctrip.zeus.restful.message.view.ViewDecorator;
import com.ctrip.zeus.service.model.ArchiveRepository;
import com.ctrip.zeus.service.model.IdVersion;
import com.ctrip.zeus.service.model.SelectionMode;
import com.ctrip.zeus.service.model.TrafficPolicyRepository;
import com.ctrip.zeus.service.query.QueryEngine;
import com.ctrip.zeus.service.query.TrafficPolicyQuery;
import com.ctrip.zeus.service.query.command.QueryCommand;
import com.ctrip.zeus.service.query.command.TrafficPolicyCommand;
import com.ctrip.zeus.support.ObjectJsonParser;
import com.ctrip.zeus.support.ObjectJsonWriter;
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
import java.util.ArrayList;
import java.util.List;

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

    private static Logger logger = LoggerFactory.getLogger(TrafficPolicyResource.class);

    @GET
    @Path("/policies")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response list(@Context HttpHeaders hh,
                         @Context final HttpServletRequest request,
                         @TrimmedQueryParam("mode") final String mode,
                         @TrimmedQueryParam("type") final String type,
                         @Context UriInfo uriInfo) throws Exception {

        QueryEngine queryRender = new QueryEngine(QueryParamRender.extractRawQueryParam(uriInfo), "policy", SelectionMode.getMode(mode));
        QueryCommand cmd = new TrafficPolicyCommand();
        queryRender.readToCommand(cmd);

        IdVersion[] searchKeys = trafficPolicyQuery.queryByCommand(cmd, SelectionMode.getMode(mode));
        if (searchKeys == null) {
            searchKeys = trafficPolicyQuery.queryAll(SelectionMode.getMode(mode)).toArray(new IdVersion[]{});
        }
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
        trim(p);
        p = trafficPolicyRepository.add(p, force != null && force);

        try {
            propertyBox.set("status", "deactivated", "policy", p.getId());
        } catch (Exception ex) {
        }
        if (extendedView.getProperties() != null) {
            setProperties(p.getId(), extendedView.getProperties());
        }
        if (extendedView.getTags() != null) {
            addTag(p.getId(), extendedView.getTags());
        }
        return responseHandler.handle(p, hh.getMediaType());
    }

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
        trim(p);
        p = trafficPolicyRepository.update(p, force != null && force);
        if (extendedView.getProperties() != null) {
            setProperties(p.getId(), extendedView.getProperties());
        }
        if (extendedView.getTags() != null) {
            addTag(p.getId(), extendedView.getTags());
        }
        try {
            if (trafficPolicyQuery.queryByIdAndMode(p.getId(), SelectionMode.ONLINE_EXCLUSIVE).length == 1) {
                propertyBox.set("status", "toBeActivated", "policy", p.getId());
            }
        } catch (Exception ex) {
        }

        return responseHandler.handle(p, hh.getMediaType());
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
        TrafficPolicy result = trafficPolicyRepository.getById(policyId);
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
        return responseHandler.handle("Traffic policy " + policyId + " is deleted.", hh.getMediaType());
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
