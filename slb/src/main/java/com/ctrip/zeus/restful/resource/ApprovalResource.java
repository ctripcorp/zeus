package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.auth.entity.DataResource;
import com.ctrip.zeus.auth.entity.Operation;
import com.ctrip.zeus.auth.entity.User;
import com.ctrip.zeus.exceptions.BadRequestException;
import com.ctrip.zeus.model.approval.Approval;
import com.ctrip.zeus.restful.message.ResponseHandler;
import com.ctrip.zeus.service.approval.ApprovalService;
import com.ctrip.zeus.service.auth.*;
import com.ctrip.zeus.service.mail.MailService;
import com.ctrip.zeus.service.mail.model.SlbMail;
import com.ctrip.zeus.service.mail.templet.AuthApplyMailTemple;
import com.ctrip.zeus.service.message.queue.MessageQueue;
import com.ctrip.zeus.service.model.IdVersion;
import com.ctrip.zeus.service.model.SelectionMode;
import com.ctrip.zeus.service.query.*;
import com.ctrip.zeus.support.ObjectJsonParser;
import com.ctrip.zeus.util.AssertUtils;
import com.ctrip.zeus.util.MessageUtil;
import com.ctrip.zeus.util.UserUtils;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.ValidationException;
import java.util.*;

@Component
@Path("/")
public class ApprovalResource {

    @Autowired
    private ApprovalService approvalService;

    @Resource
    private ResponseHandler responseHandler;

    @Resource
    private AuthService authService;

    @Resource
    private DrCriteriaQuery drCriteriaQuery;

    @Resource
    private TrafficPolicyQuery trafficPolicyQuery;

    @Resource
    private GroupCriteriaQuery groupCriteriaQuery;

    @Resource
    private VirtualServerCriteriaQuery virtualServerCriteriaQuery;

    @Resource
    private SlbCriteriaQuery slbCriteriaQuery;

    @Resource
    private UserService userService;

    @Resource
    private MessageQueue messageQueue;

    @Autowired
    private MailService mailService;

    final Logger logger = LoggerFactory.getLogger(this.getClass());

    DynamicStringProperty applyUrl = DynamicPropertyFactory.getInstance().getStringProperty("auth.apply.url", "");
    DynamicStringProperty applyRecipients = DynamicPropertyFactory.getInstance().getStringProperty("auth.apply.apply.recipients", "alias@mail.com");


    @GET
    @Path("/auth/apply")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response applyAuth(@Context HttpServletRequest request, @Context HttpHeaders hh,
                              @QueryParam("userName") String name,
                              @QueryParam("type") String type,
                              @QueryParam("targetId") List<Long> targetIds,
                              @QueryParam("op") String ops) throws Exception {
        String currentUser = UserUtils.getUserName(request);

        authService.authValidate(currentUser, ResourceOperationType.AUTH, ResourceDataType.Auth, AuthDefaultValues.ALL);
        AssertUtils.assertNotNull(name, "Parameter is missing.");
        AssertUtils.assertNotNull(ops, "Parameter is missing.");
        AssertUtils.assertNotNull(targetIds, "Parameter is missing.");
        AssertUtils.assertNotNull(type, "Parameter is missing.");

        if (targetIds.size() == 0) {
            throw new ValidationException("Parameter is missing.");
        }

        Collections.sort(targetIds);

        if (!ResourceDataType.contain(type)) {
            throw new ValidationException("Type is invalidate. Type Enum:" + ResourceOperationType.getNames());
        }
        ResourceDataType opType = ResourceDataType.valueOf(type);
        Set<IdVersion> idvs = null;
        switch (opType) {
            case Dr:
                idvs = drCriteriaQuery.queryByIdsAndMode(targetIds.toArray(new Long[targetIds.size()]), SelectionMode.OFFLINE_FIRST);
                break;
            case Policy:
                idvs = trafficPolicyQuery.queryByIdsAndMode(targetIds.toArray(new Long[targetIds.size()]), SelectionMode.OFFLINE_FIRST);
                break;
            case Group:
                idvs = groupCriteriaQuery.queryByIdsAndMode(targetIds.toArray(new Long[targetIds.size()]), SelectionMode.OFFLINE_FIRST);
                break;
            case Vs:
                idvs = virtualServerCriteriaQuery.queryByIdsAndMode(targetIds.toArray(new Long[targetIds.size()]), SelectionMode.OFFLINE_FIRST);
                break;
            case Slb:
                idvs = slbCriteriaQuery.queryByIdsAndMode(targetIds.toArray(new Long[targetIds.size()]), SelectionMode.OFFLINE_FIRST);
                break;
            default:
                break;
        }
        if (idvs == null || idvs.size() != targetIds.size()) {
            throw new ValidationException("Invalidate target id list.Not found target by target ids.");
        }
        List<String> target = new ArrayList<>();
        Long[] longTarget = new Long[targetIds.size()];
        for (int index = 0; index < targetIds.size(); index++) {
            Long current = targetIds.get(index);
            target.add(String.valueOf(current));
            longTarget[index] = current;
        }
        List<Operation> operations = new ArrayList<>();
        String[] opsArray = ops.split(";");
        for (String op : opsArray) {
            if (ResourceOperationType.contain(op)) {
                operations.add(new Operation().setType(op));
            } else {
                throw new ValidationException("Invalidate op. Op:" + op);
            }
        }
        User user = userService.getUser(name);
        if (user == null) {
            throw new ValidationException("Not Found User. UserName:" + name);
        }
        for (DataResource ds : user.getDataResources()) {
            if (ds.getResourceType().equalsIgnoreCase(type) && target.contains(ds.getData())) {
                for (Operation op : operations) {
                    if (!ds.getOperations().contains(op)) {
                        ds.addOperation(op);
                    }
                }
                target.remove(ds.getData());
            }
        }
        for (String t : target) {
            DataResource ds = new DataResource();
            ds.setData(t);
            ds.setResourceType(ResourceDataType.valueOf(type).getType());
            ds.getOperations().addAll(operations);
            user.addDataResource(ds);
        }
        userService.updateUser(user);
        String slbMessageData = MessageUtil.getMessageData(request, null, null, null, null, null, true);
        messageQueue.produceMessage(request.getRequestURI(), -1L, slbMessageData);
        List<String> r = new ArrayList<>();
        Collections.addAll(r, applyRecipients.get().split(";"));

        // update approve
        String apply_by = name;
        List<Approval> approvals = approvalService.getApprovals(apply_by, type, false, longTarget, opsArray);
        for (Approval approval : approvals) {
            approval.setApproved(true);
            approval.setApprovedTime(new Date());
            approval.setApprovedBy(currentUser);
            approvalService.updateApproval(approval);
        }

        if (user.getEmail() != null) {
            r.add(user.getEmail());
            String mailBody = AuthApplyMailTemple.getInstance().notifyApplyResult(user.getUserName() + "(" + user.getChineseName() + ")",
                    type, targetIds, ops, currentUser);

            mailService.sendEmail(new SlbMail().setSubject("【SLB权限申请通过】用户：" + user.getChineseName()).setRecipients(r).setBody(mailBody));
        }
        return responseHandler.handle("success", hh.getMediaType());
    }

    @GET
    @Path("/auth/apply/mail")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response sendApply(@Context HttpServletRequest request, @Context HttpHeaders hh,
                              @QueryParam("userName") String name,
                              @QueryParam("type") String type,
                              @QueryParam("targetId") List<Long> targetIds,
                              @QueryParam("op") String ops) throws Exception {
        AssertUtils.assertNotNull(name, "Parameter is missing.");
        AssertUtils.assertNotNull(ops, "Parameter is missing.");
        AssertUtils.assertNotNull(targetIds, "Parameter is missing.");
        AssertUtils.assertNotNull(type, "Parameter is missing.");

        if (targetIds.size() == 0) {
            throw new ValidationException("Parameter is missing.");
        }

        Collections.sort(targetIds);

        if (!ResourceDataType.contain(type)) {
            throw new ValidationException("Type is invalidate. Type Enum:" + ResourceOperationType.getNames());
        }
        User user = userService.getUserSimpleInfo(name);
        if (user == null) {
            throw new ValidationException("Not Found User. UserName:" + name);
        }

        List<String> r = new ArrayList<>();
        Collections.addAll(r, applyRecipients.get().split(";"));
        String url = applyUrl.get() + "?" + request.getQueryString();

        // save the apply
        Approval approval = new Approval();
        approval.setApplyBy(name).
                setApplyTime(new Date()).
                setApplyType(type);
        // approved targets and ops
        for (Long targetId : targetIds) {
            approval.addApplyTarget(targetId);
        }
        String[] opsArray = ops.split(";");
        for (String op : opsArray) {
            if (!op.isEmpty()) {
                approval.addApplyOp(op);
            }
        }
        approvalService.addApproval(approval);

        String mailBody = AuthApplyMailTemple.getInstance().buildApply(name,
                type, targetIds, ops, url);

        if (!approvalService.needApprove(user, type, targetIds, ops, request)) {
            mailService.sendEmail(new SlbMail().setSubject("【SLB 权限申请 - 需人工审批】用户：" + user.getChineseName()).setRecipients(r).setBody(mailBody));
        }

        return responseHandler.handle("success", hh.getMediaType());
    }

    @GET
    @Path("/approvals")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response allApprovals(@Context HttpServletRequest request, @Context HttpHeaders hh,
                                 @QueryParam("approved") Boolean approved,
                                 @QueryParam("applyBy") String applyBy,
                                 @QueryParam("applyType") String applyType,
                                 @QueryParam("applyTarget") List<Long> applyTargets
    ) throws Exception {
        List<Approval> approvals = new ArrayList<>();

        if (approved == null) approved = false;

        Long[] targetQueryArray = null;

        if (applyTargets != null && applyTargets.size() > 0) {
            Collections.sort(applyTargets);
            int size = applyTargets.size();
            targetQueryArray = new Long[size];
            for (int id = 0; id < applyTargets.size(); id++) {
                targetQueryArray[id] = applyTargets.get(id);
            }
        }


        approvals = approvalService.getApprovals(applyBy, applyType, approved, targetQueryArray, null);

        return responseHandler.handle(approvals, hh.getMediaType());
    }

    @POST
    @Path("/approval/new")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response newApproval(@Context HttpServletRequest request, @Context HttpHeaders hh, String approvalText) throws Exception {
        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.AUTH, ResourceDataType.Auth, AuthDefaultValues.ALL);

        Approval approval = ObjectJsonParser.parse(approvalText, Approval.class);

        Approval approvalSaved = approvalService.addApproval(approval);

        return responseHandler.handle(approvalSaved, hh.getMediaType());
    }

    @GET
    @Path("/approval/approve")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response deleteApproval(@Context HttpServletRequest request, @Context HttpHeaders hh, @QueryParam("approveId") Long approveId) throws Exception {
        String currentUser = UserUtils.getUserName(request);

        authService.authValidate(currentUser, ResourceOperationType.AUTH, ResourceDataType.Auth, AuthDefaultValues.ALL);
        if (approveId == null) {
            throw new BadRequestException("Bad Request Query Param.At least one param is needed.");
        }
        Approval approval = approvalService.getApprovalById(approveId);
        if (approval == null) throw new BadRequestException("Bad Request: Could not get approval with id=" + approveId);

        approval.setApproved(true);
        approval.setApprovedTime(new Date());
        approval.setApprovedBy(currentUser);

        Approval result = approvalService.updateApproval(approval);

        String message;

        if (result != null) {
            message = "Successfully deleted approval with id: " + approveId;
        } else {
            message = "Failed to deleted approval with id: " + approveId;
        }

        return responseHandler.handle(message, hh.getMediaType());
    }

}
