package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.flow.mergevs.MergeVsFlowService;
import com.ctrip.zeus.flow.mergevs.model.MergeVsFlowEntity;
import com.ctrip.zeus.restful.message.ResponseHandler;
import com.ctrip.zeus.service.auth.AuthDefaultValues;
import com.ctrip.zeus.service.auth.AuthService;
import com.ctrip.zeus.service.auth.ResourceDataType;
import com.ctrip.zeus.service.auth.ResourceOperationType;
import com.ctrip.zeus.service.message.queue.MessageQueue;
import com.ctrip.zeus.service.model.ValidationFacade;
import com.ctrip.zeus.support.ObjectJsonParser;
import com.ctrip.zeus.util.MessageUtil;
import com.ctrip.zeus.util.UserUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Component
@Path("/flow/vs/merge")
public class FlowVsMergeResource {

    @Resource
    private MergeVsFlowService mergeVsFlowService;
    @Resource
    private ResponseHandler responseHandler;
    @Resource
    private AuthService authService;
    @Resource
    private MessageQueue messageQueue;
    @Resource
    private ValidationFacade validationFacade;

    @POST
    @Path("/new")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response add(@Context HttpHeaders hh,
                        @Context final HttpServletRequest request,
                        String data) throws Exception {
        if (data == null) throw new ValidationException("Post Data Can Not Be Null.");
        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.NEW, ResourceDataType.Flow, AuthDefaultValues.ALL);

        MergeVsFlowEntity mergeVsFlowEntity = ObjectJsonParser.parse(data, MergeVsFlowEntity.class);
        if (mergeVsFlowEntity == null) throw new ValidationException("Invalidate Json Data.");
        MergeVsFlowEntity newEntity = mergeVsFlowService.add(mergeVsFlowEntity);
        String slbMessageData = MessageUtil.getMessageBuilder(request, true).flow(newEntity.getName(), newEntity.getId()).build();
        messageQueue.produceMessage(request.getRequestURI(), newEntity.getId(), slbMessageData);
        return responseHandler.handle(newEntity, hh.getMediaType());
    }

    @POST
    @Path("/update")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response update(@Context HttpHeaders hh,
                           @Context final HttpServletRequest request,
                           @QueryParam("force") Boolean force,
                           String data) throws Exception {
        if (data == null) throw new ValidationException("Post Data Can Not Be Null.");
        MergeVsFlowEntity mergeVsFlowEntity = ObjectJsonParser.parse(data, MergeVsFlowEntity.class);
        if (mergeVsFlowEntity == null) throw new ValidationException("Invalidate Json Data.");
        if (force != null && force) {
            authService.authValidateWithForce(UserUtils.getUserName(request), ResourceOperationType.UPDATE, ResourceDataType.Flow, mergeVsFlowEntity.getId());
        } else {
            authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.UPDATE, ResourceDataType.Flow, mergeVsFlowEntity.getId());
        }

        MergeVsFlowEntity newEntity = mergeVsFlowService.update(mergeVsFlowEntity, force == null ? false : force);

        String slbMessageData = MessageUtil.getMessageBuilder(request, true).flow(newEntity.getName(), newEntity.getId()).build();
        messageQueue.produceMessage(request.getRequestURI(), newEntity.getId(), slbMessageData);

        return responseHandler.handle(newEntity, hh.getMediaType());
    }


    @GET
    @Path("/get")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response get(@Context HttpHeaders hh,
                        @Context final HttpServletRequest request,
                        @QueryParam("id") Long id) throws Exception {
        if (id == null) throw new ValidationException("id Can Not Be Null.");
        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.READ, ResourceDataType.Flow, id);
        MergeVsFlowEntity newEntity = mergeVsFlowService.get(id);
        return responseHandler.handle(newEntity, hh.getMediaType());
    }

    @GET
    @Path("/delete")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response delete(@Context HttpHeaders hh,
                           @Context final HttpServletRequest request,
                           @QueryParam("id") Long id) throws Exception {
        if (id == null) throw new ValidationException("id Can Not Be Null.");
        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.DELETE, ResourceDataType.Flow, id);
        MergeVsFlowEntity entity = mergeVsFlowService.delete(id);
        return responseHandler.handle(entity, hh.getMediaType());
    }

    @GET
    @Path("/list")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response list(@Context HttpHeaders hh,
                         @Context final HttpServletRequest request) throws Exception {
        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.READ, ResourceDataType.Flow, AuthDefaultValues.ALL);
        List<MergeVsFlowEntity> newEntity = mergeVsFlowService.queryAll();
        return responseHandler.handle(newEntity, hh.getMediaType());
    }


    @GET
    @Path("/bind/new/vs")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response bindNewVs(@Context HttpHeaders hh,
                              @Context final HttpServletRequest request,
                              @QueryParam("id") Long id) throws Exception {
        if (id == null) throw new ValidationException("id Can Not Be Null.");
        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.READ, ResourceDataType.Flow, id);
        MergeVsFlowEntity newEntity = mergeVsFlowService.createAndBindNewVs(id);

        String slbMessageData = MessageUtil.getMessageBuilder(request, true).flow(newEntity.getName(), newEntity.getId()).build();
        messageQueue.produceMessage(request.getRequestURI(), newEntity.getId(), slbMessageData);

        return responseHandler.handle(newEntity, hh.getMediaType());
    }

    @GET
    @Path("/merge")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response activate(@Context HttpHeaders hh,
                             @Context final HttpServletRequest request,
                             @QueryParam("id") Long id) throws Exception {
        if (id == null) throw new ValidationException("id Can Not Be Null.");
        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.READ, ResourceDataType.Flow, id);
        MergeVsFlowEntity newEntity = mergeVsFlowService.mergeVs(id);

        String slbMessageData = MessageUtil.getMessageBuilder(request, true).flow(newEntity.getName(), newEntity.getId()).build();
        messageQueue.produceMessage(request.getRequestURI(), newEntity.getId(), slbMessageData);

        return responseHandler.handle(newEntity, hh.getMediaType());
    }

    @GET
    @Path("/rollback")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response rollback(@Context HttpHeaders hh,
                             @Context final HttpServletRequest request,
                             @QueryParam("id") Long id) throws Exception {
        if (id == null) throw new ValidationException("id Can Not Be Null.");
        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.READ, ResourceDataType.Flow, id);
        MergeVsFlowEntity newEntity = mergeVsFlowService.rollback(id);

        String slbMessageData = MessageUtil.getMessageBuilder(request, true).flow(newEntity.getName(), newEntity.getId()).build();
        messageQueue.produceMessage(request.getRequestURI(), newEntity.getId(), slbMessageData);

        return responseHandler.handle(newEntity, hh.getMediaType());
    }

    @GET
    @Path("/validate")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response validate(@Context HttpHeaders hh,
                             @Context final HttpServletRequest request,
                             @QueryParam("vsId") List<Long> vsIds) throws Exception {
        if (vsIds == null || vsIds.size() == 0) throw new ValidationException("id Can Not Be Null.");
        validationFacade.validateForMergeVs(vsIds, null);
        return responseHandler.handle("success", hh.getMediaType());
    }

    @GET
    @Path("/clean")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response clean(@Context HttpHeaders hh,
                          @Context final HttpServletRequest request,
                          @QueryParam("id") Long id) throws Exception {
        if (id == null) throw new ValidationException("id Can Not Be Null.");
        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.READ, ResourceDataType.Flow, id);
        MergeVsFlowEntity newEntity = mergeVsFlowService.clean(id);

        String slbMessageData = MessageUtil.getMessageBuilder(request, true).flow(newEntity.getName(), newEntity.getId()).build();
        messageQueue.produceMessage(request.getRequestURI(), newEntity.getId(), slbMessageData);

        return responseHandler.handle(newEntity, hh.getMediaType());
    }

    @GET
    @Path("/disable")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response disable(@Context HttpHeaders hh,
                            @Context final HttpServletRequest request,
                            @QueryParam("id") Long id) throws Exception {
        if (id == null) throw new ValidationException("id Can Not Be Null.");
        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.UPDATE, ResourceDataType.Flow, id);
        MergeVsFlowEntity newEntity = mergeVsFlowService.disable(id);

        String slbMessageData = MessageUtil.getMessageBuilder(request, true).flow(newEntity.getName(), newEntity.getId()).build();
        messageQueue.produceMessage(request.getRequestURI(), newEntity.getId(), slbMessageData);
        return responseHandler.handle(newEntity, hh.getMediaType());
    }


}
