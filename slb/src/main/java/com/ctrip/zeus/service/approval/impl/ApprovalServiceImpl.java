package com.ctrip.zeus.service.approval.impl;

import com.ctrip.zeus.auth.entity.User;
import com.ctrip.zeus.dao.entity.AuthApprove;
import com.ctrip.zeus.dao.entity.AuthApproveExample;
import com.ctrip.zeus.dao.mapper.AuthApproveMapper;
import com.ctrip.zeus.model.approval.Approval;
import com.ctrip.zeus.service.approval.ApprovalService;
import com.ctrip.zeus.support.C;
import com.ctrip.zeus.support.ObjectJsonParser;
import com.ctrip.zeus.support.ObjectJsonWriter;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service("approvalService")
public class ApprovalServiceImpl implements ApprovalService {
    @Resource
    private AuthApproveMapper authApproveMapper;

    @Override
    public Approval addApproval(Approval approval) throws Exception {
        return addApprovalMybatis(approval);
    }

    private Approval addApprovalMybatis(Approval approval) throws JsonProcessingException {
        AuthApprove authApprove = new AuthApprove();
        authApprove.setApplyBy(approval.getApplyBy());
        authApprove.setApplyType(approval.getApplyType());
        authApprove.setApproved(approval.getApproved());
        authApprove.setApplyTime(approval.getApplyTime());
        authApprove.setApprovedBy(approval.getApprovedBy());
        authApprove.setApprovedTime(approval.getApprovedTime());
        authApprove.setContext(ObjectJsonWriter.write(approval).getBytes());
        authApproveMapper.insert(authApprove);
        return approval;
    }


    @Override
    public Approval getApprovalById(long id) {
        AuthApprove approve = authApproveMapper.selectByPrimaryKey(id);
        if (approve.getContext() != null) {
            Approval res = ObjectJsonParser.parse(new String(approve.getContext()), Approval.class);
            if (res == null) return null;
            return res.setId(approve.getId());
        } else {
            return C.toApproval(approve);
        }
    }

    @Override
    public List<Approval> getAllApprovals() {
        List<Approval> results = new ArrayList<>();

        List<AuthApprove> approves = authApproveMapper.selectByExampleWithBLOBs(new AuthApproveExample().createCriteria().example());
        for (AuthApprove item : approves) {
            if (item.getContext() != null) {
                Approval res = ObjectJsonParser.parse(new String(item.getContext()), Approval.class);
                if (res == null) {
                    results.add(C.toApproval(item));
                } else {
                    results.add(res.setId(item.getId()));
                }
            } else {
                results.add(C.toApproval(item));
            }
        }
        return results;
    }

    @Override
    public List<Approval> getApprovals(String applyBy, String applyType, boolean approved, Long[] targets, String[] ops) {

        List<Long> targetIds = null;
        if (targets != null) {
            targetIds = Arrays.asList(targets);
        }
        List<String> opsList = null;
        if (ops != null) {
            opsList = Arrays.asList(ops);
        }
        AuthApproveExample example = new AuthApproveExample();
        AuthApproveExample.Criteria criteria = example.createCriteria();
        if (applyBy != null) {
            criteria.andApplyByEqualTo(applyBy);
        }
        if (applyType != null) {
            criteria.andApplyTypeEqualTo(applyType);
        }
        criteria.andApprovedEqualTo(approved);
        List<AuthApprove> approves = authApproveMapper.selectByExampleWithBLOBs(example);
        List<Approval> result = new ArrayList<>();
        for (AuthApprove item : approves) {
            Approval res = null;
            if (item.getContext() != null) {
                res = ObjectJsonParser.parse(new String(item.getContext()), Approval.class);
                if (res == null) {
                    res = C.toApproval(item);
                } else {
                    res.setId(item.getId());
                }
            } else {
                res = C.toApproval(item);
            }
            if (res != null && (targetIds == null || hasIntersection(res.getApplyTargets(), targetIds))
                    && (opsList == null || hasIntersection(res.getApplyOps(), opsList))) {
                result.add(res);
            }
        }
        return result;
    }

    @Override
    public Approval updateApproval(Approval approval) throws Exception {

        AuthApprove authApprove = new AuthApprove();
        authApprove.setId(approval.getId());
        authApprove.setApplyBy(approval.getApplyBy());
        authApprove.setApplyType(approval.getApplyType());
        authApprove.setApproved(approval.getApproved());
        authApprove.setApplyTime(approval.getApplyTime());
        authApprove.setApprovedBy(approval.getApprovedBy());
        authApprove.setApprovedTime(approval.getApprovedTime());
        authApprove.setContext(ObjectJsonWriter.write(approval).getBytes());
        authApproveMapper.updateByPrimaryKeyWithBLOBs(authApprove);
        return approval;
    }


    @Override
    public boolean needApprove(User user, String type, List<Long> targetIds, String ops, HttpServletRequest request) throws Exception {
        return true;
    }

    private boolean hasIntersection(List list, List list2) {
        for (Object o1 : list) {
            if (list2.contains(o1)) {
                return true;
            }
        }
        return false;
    }
}
