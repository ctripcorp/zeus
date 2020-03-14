package com.ctrip.zeus.service.approval;

import com.ctrip.zeus.auth.entity.User;
import com.ctrip.zeus.model.approval.Approval;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface ApprovalService {
    /**
    * Save new Approval
    * **/
    Approval addApproval(Approval approval) throws Exception;

    /**
     * Get All Approvals
     * **/
    List<Approval> getAllApprovals();

    /**
     * Get Approvals by apply-by, apply-type, apply-targets and apply-ops
     * **/
    List<Approval> getApprovals(String applyBy, String applyType, boolean approved, Long[] targets, String[] ops);
    /**
     * Get All Approvals
     * **/
    Approval getApprovalById(long id);

    /**
     * Update an existing approval
     * **/
    Approval updateApproval(Approval approval) throws Exception;

    /**
     * Update an existing approval
     * **/
    boolean needApprove(User user, String type, List<Long> targetIds, String ops, HttpServletRequest request) throws Exception;

}
