package com.ctrip.zeus.dao.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AuthApproveExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    protected Integer offset;

    protected Integer rows;

    public AuthApproveExample() {
        oredCriteria = new ArrayList<Criteria>();
    }

    public void setOrderByClause(String orderByClause) {
        this.orderByClause = orderByClause;
    }

    public String getOrderByClause() {
        return orderByClause;
    }

    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    public boolean isDistinct() {
        return distinct;
    }

    public List<Criteria> getOredCriteria() {
        return oredCriteria;
    }

    public void or(Criteria criteria) {
        oredCriteria.add(criteria);
    }

    public Criteria or() {
        Criteria criteria = createCriteriaInternal();
        oredCriteria.add(criteria);
        return criteria;
    }

    public AuthApproveExample orderBy(String orderByClause) {
        this.setOrderByClause(orderByClause);
        return this;
    }

    public AuthApproveExample orderBy(String ... orderByClauses) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < orderByClauses.length; i++) {
            sb.append(orderByClauses[i]);
            if (i < orderByClauses.length - 1) {
                sb.append(" , ");
            }
        }
        this.setOrderByClause(sb.toString());
        return this;
    }

    public Criteria createCriteria() {
        Criteria criteria = createCriteriaInternal();
        if (oredCriteria.size() == 0) {
            oredCriteria.add(criteria);
        }
        return criteria;
    }

    protected Criteria createCriteriaInternal() {
        Criteria criteria = new Criteria(this);
        return criteria;
    }

    public void clear() {
        oredCriteria.clear();
        orderByClause = null;
        distinct = false;
        rows = null;
        offset = null;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    public Integer getOffset() {
        return this.offset;
    }

    public void setRows(Integer rows) {
        this.rows = rows;
    }

    public Integer getRows() {
        return this.rows;
    }

    public AuthApproveExample limit(Integer rows) {
        this.rows = rows;
        return this;
    }

    public AuthApproveExample limit(Integer offset, Integer rows) {
        this.offset = offset;
        this.rows = rows;
        return this;
    }

    public AuthApproveExample page(Integer page, Integer pageSize) {
        this.offset = page * pageSize;
        this.rows = pageSize;
        return this;
    }

    public static Criteria newAndCreateCriteria() {
        AuthApproveExample example = new AuthApproveExample();
        return example.createCriteria();
    }

    public AuthApproveExample when(boolean condition, IExampleWhen then) {
        if (condition) {
            then.example(this);
        }
        return this;
    }

    public AuthApproveExample when(boolean condition, IExampleWhen then, IExampleWhen otherwise) {
        if (condition) {
            then.example(this);
        } else {
            otherwise.example(this);
        }
        return this;
    }

    protected abstract static class GeneratedCriteria {
        protected List<Criterion> criteria;

        protected GeneratedCriteria() {
            super();
            criteria = new ArrayList<Criterion>();
        }

        public boolean isValid() {
            return criteria.size() > 0;
        }

        public List<Criterion> getAllCriteria() {
            return criteria;
        }

        public List<Criterion> getCriteria() {
            return criteria;
        }

        protected void addCriterion(String condition) {
            if (condition == null) {
                throw new RuntimeException("Value for condition cannot be null");
            }
            criteria.add(new Criterion(condition));
        }

        protected void addCriterion(String condition, Object value, String property) {
            if (value == null) {
                throw new RuntimeException("Value for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value));
        }

        protected void addCriterion(String condition, Object value1, Object value2, String property) {
            if (value1 == null || value2 == null) {
                throw new RuntimeException("Between values for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value1, value2));
        }

        public Criteria andIdIsNull() {
            addCriterion("id is null");
            return (Criteria) this;
        }

        public Criteria andIdIsNotNull() {
            addCriterion("id is not null");
            return (Criteria) this;
        }

        public Criteria andIdEqualTo(Long value) {
            addCriterion("id =", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdEqualToColumn(AuthApprove.Column column) {
            addCriterion(new StringBuilder("id = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andIdNotEqualTo(Long value) {
            addCriterion("id <>", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotEqualToColumn(AuthApprove.Column column) {
            addCriterion(new StringBuilder("id <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andIdGreaterThan(Long value) {
            addCriterion("id >", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThanColumn(AuthApprove.Column column) {
            addCriterion(new StringBuilder("id > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andIdGreaterThanOrEqualTo(Long value) {
            addCriterion("id >=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThanOrEqualToColumn(AuthApprove.Column column) {
            addCriterion(new StringBuilder("id >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andIdLessThan(Long value) {
            addCriterion("id <", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThanColumn(AuthApprove.Column column) {
            addCriterion(new StringBuilder("id < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andIdLessThanOrEqualTo(Long value) {
            addCriterion("id <=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThanOrEqualToColumn(AuthApprove.Column column) {
            addCriterion(new StringBuilder("id <= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andIdIn(List<Long> values) {
            addCriterion("id in", values, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotIn(List<Long> values) {
            addCriterion("id not in", values, "id");
            return (Criteria) this;
        }

        public Criteria andIdBetween(Long value1, Long value2) {
            addCriterion("id between", value1, value2, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotBetween(Long value1, Long value2) {
            addCriterion("id not between", value1, value2, "id");
            return (Criteria) this;
        }

        public Criteria andApplyByIsNull() {
            addCriterion("apply_by is null");
            return (Criteria) this;
        }

        public Criteria andApplyByIsNotNull() {
            addCriterion("apply_by is not null");
            return (Criteria) this;
        }

        public Criteria andApplyByEqualTo(String value) {
            addCriterion("apply_by =", value, "applyBy");
            return (Criteria) this;
        }

        public Criteria andApplyByEqualToColumn(AuthApprove.Column column) {
            addCriterion(new StringBuilder("apply_by = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andApplyByNotEqualTo(String value) {
            addCriterion("apply_by <>", value, "applyBy");
            return (Criteria) this;
        }

        public Criteria andApplyByNotEqualToColumn(AuthApprove.Column column) {
            addCriterion(new StringBuilder("apply_by <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andApplyByGreaterThan(String value) {
            addCriterion("apply_by >", value, "applyBy");
            return (Criteria) this;
        }

        public Criteria andApplyByGreaterThanColumn(AuthApprove.Column column) {
            addCriterion(new StringBuilder("apply_by > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andApplyByGreaterThanOrEqualTo(String value) {
            addCriterion("apply_by >=", value, "applyBy");
            return (Criteria) this;
        }

        public Criteria andApplyByGreaterThanOrEqualToColumn(AuthApprove.Column column) {
            addCriterion(new StringBuilder("apply_by >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andApplyByLessThan(String value) {
            addCriterion("apply_by <", value, "applyBy");
            return (Criteria) this;
        }

        public Criteria andApplyByLessThanColumn(AuthApprove.Column column) {
            addCriterion(new StringBuilder("apply_by < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andApplyByLessThanOrEqualTo(String value) {
            addCriterion("apply_by <=", value, "applyBy");
            return (Criteria) this;
        }

        public Criteria andApplyByLessThanOrEqualToColumn(AuthApprove.Column column) {
            addCriterion(new StringBuilder("apply_by <= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andApplyByLike(String value) {
            addCriterion("apply_by like", value, "applyBy");
            return (Criteria) this;
        }

        public Criteria andApplyByNotLike(String value) {
            addCriterion("apply_by not like", value, "applyBy");
            return (Criteria) this;
        }

        public Criteria andApplyByIn(List<String> values) {
            addCriterion("apply_by in", values, "applyBy");
            return (Criteria) this;
        }

        public Criteria andApplyByNotIn(List<String> values) {
            addCriterion("apply_by not in", values, "applyBy");
            return (Criteria) this;
        }

        public Criteria andApplyByBetween(String value1, String value2) {
            addCriterion("apply_by between", value1, value2, "applyBy");
            return (Criteria) this;
        }

        public Criteria andApplyByNotBetween(String value1, String value2) {
            addCriterion("apply_by not between", value1, value2, "applyBy");
            return (Criteria) this;
        }

        public Criteria andApplyTimeIsNull() {
            addCriterion("apply_time is null");
            return (Criteria) this;
        }

        public Criteria andApplyTimeIsNotNull() {
            addCriterion("apply_time is not null");
            return (Criteria) this;
        }

        public Criteria andApplyTimeEqualTo(Date value) {
            addCriterion("apply_time =", value, "applyTime");
            return (Criteria) this;
        }

        public Criteria andApplyTimeEqualToColumn(AuthApprove.Column column) {
            addCriterion(new StringBuilder("apply_time = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andApplyTimeNotEqualTo(Date value) {
            addCriterion("apply_time <>", value, "applyTime");
            return (Criteria) this;
        }

        public Criteria andApplyTimeNotEqualToColumn(AuthApprove.Column column) {
            addCriterion(new StringBuilder("apply_time <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andApplyTimeGreaterThan(Date value) {
            addCriterion("apply_time >", value, "applyTime");
            return (Criteria) this;
        }

        public Criteria andApplyTimeGreaterThanColumn(AuthApprove.Column column) {
            addCriterion(new StringBuilder("apply_time > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andApplyTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("apply_time >=", value, "applyTime");
            return (Criteria) this;
        }

        public Criteria andApplyTimeGreaterThanOrEqualToColumn(AuthApprove.Column column) {
            addCriterion(new StringBuilder("apply_time >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andApplyTimeLessThan(Date value) {
            addCriterion("apply_time <", value, "applyTime");
            return (Criteria) this;
        }

        public Criteria andApplyTimeLessThanColumn(AuthApprove.Column column) {
            addCriterion(new StringBuilder("apply_time < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andApplyTimeLessThanOrEqualTo(Date value) {
            addCriterion("apply_time <=", value, "applyTime");
            return (Criteria) this;
        }

        public Criteria andApplyTimeLessThanOrEqualToColumn(AuthApprove.Column column) {
            addCriterion(new StringBuilder("apply_time <= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andApplyTimeIn(List<Date> values) {
            addCriterion("apply_time in", values, "applyTime");
            return (Criteria) this;
        }

        public Criteria andApplyTimeNotIn(List<Date> values) {
            addCriterion("apply_time not in", values, "applyTime");
            return (Criteria) this;
        }

        public Criteria andApplyTimeBetween(Date value1, Date value2) {
            addCriterion("apply_time between", value1, value2, "applyTime");
            return (Criteria) this;
        }

        public Criteria andApplyTimeNotBetween(Date value1, Date value2) {
            addCriterion("apply_time not between", value1, value2, "applyTime");
            return (Criteria) this;
        }

        public Criteria andApplyTypeIsNull() {
            addCriterion("apply_type is null");
            return (Criteria) this;
        }

        public Criteria andApplyTypeIsNotNull() {
            addCriterion("apply_type is not null");
            return (Criteria) this;
        }

        public Criteria andApplyTypeEqualTo(String value) {
            addCriterion("apply_type =", value, "applyType");
            return (Criteria) this;
        }

        public Criteria andApplyTypeEqualToColumn(AuthApprove.Column column) {
            addCriterion(new StringBuilder("apply_type = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andApplyTypeNotEqualTo(String value) {
            addCriterion("apply_type <>", value, "applyType");
            return (Criteria) this;
        }

        public Criteria andApplyTypeNotEqualToColumn(AuthApprove.Column column) {
            addCriterion(new StringBuilder("apply_type <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andApplyTypeGreaterThan(String value) {
            addCriterion("apply_type >", value, "applyType");
            return (Criteria) this;
        }

        public Criteria andApplyTypeGreaterThanColumn(AuthApprove.Column column) {
            addCriterion(new StringBuilder("apply_type > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andApplyTypeGreaterThanOrEqualTo(String value) {
            addCriterion("apply_type >=", value, "applyType");
            return (Criteria) this;
        }

        public Criteria andApplyTypeGreaterThanOrEqualToColumn(AuthApprove.Column column) {
            addCriterion(new StringBuilder("apply_type >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andApplyTypeLessThan(String value) {
            addCriterion("apply_type <", value, "applyType");
            return (Criteria) this;
        }

        public Criteria andApplyTypeLessThanColumn(AuthApprove.Column column) {
            addCriterion(new StringBuilder("apply_type < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andApplyTypeLessThanOrEqualTo(String value) {
            addCriterion("apply_type <=", value, "applyType");
            return (Criteria) this;
        }

        public Criteria andApplyTypeLessThanOrEqualToColumn(AuthApprove.Column column) {
            addCriterion(new StringBuilder("apply_type <= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andApplyTypeLike(String value) {
            addCriterion("apply_type like", value, "applyType");
            return (Criteria) this;
        }

        public Criteria andApplyTypeNotLike(String value) {
            addCriterion("apply_type not like", value, "applyType");
            return (Criteria) this;
        }

        public Criteria andApplyTypeIn(List<String> values) {
            addCriterion("apply_type in", values, "applyType");
            return (Criteria) this;
        }

        public Criteria andApplyTypeNotIn(List<String> values) {
            addCriterion("apply_type not in", values, "applyType");
            return (Criteria) this;
        }

        public Criteria andApplyTypeBetween(String value1, String value2) {
            addCriterion("apply_type between", value1, value2, "applyType");
            return (Criteria) this;
        }

        public Criteria andApplyTypeNotBetween(String value1, String value2) {
            addCriterion("apply_type not between", value1, value2, "applyType");
            return (Criteria) this;
        }

        public Criteria andApplyOpsIsNull() {
            addCriterion("apply_ops is null");
            return (Criteria) this;
        }

        public Criteria andApplyOpsIsNotNull() {
            addCriterion("apply_ops is not null");
            return (Criteria) this;
        }

        public Criteria andApplyOpsEqualTo(String value) {
            addCriterion("apply_ops =", value, "applyOps");
            return (Criteria) this;
        }

        public Criteria andApplyOpsEqualToColumn(AuthApprove.Column column) {
            addCriterion(new StringBuilder("apply_ops = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andApplyOpsNotEqualTo(String value) {
            addCriterion("apply_ops <>", value, "applyOps");
            return (Criteria) this;
        }

        public Criteria andApplyOpsNotEqualToColumn(AuthApprove.Column column) {
            addCriterion(new StringBuilder("apply_ops <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andApplyOpsGreaterThan(String value) {
            addCriterion("apply_ops >", value, "applyOps");
            return (Criteria) this;
        }

        public Criteria andApplyOpsGreaterThanColumn(AuthApprove.Column column) {
            addCriterion(new StringBuilder("apply_ops > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andApplyOpsGreaterThanOrEqualTo(String value) {
            addCriterion("apply_ops >=", value, "applyOps");
            return (Criteria) this;
        }

        public Criteria andApplyOpsGreaterThanOrEqualToColumn(AuthApprove.Column column) {
            addCriterion(new StringBuilder("apply_ops >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andApplyOpsLessThan(String value) {
            addCriterion("apply_ops <", value, "applyOps");
            return (Criteria) this;
        }

        public Criteria andApplyOpsLessThanColumn(AuthApprove.Column column) {
            addCriterion(new StringBuilder("apply_ops < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andApplyOpsLessThanOrEqualTo(String value) {
            addCriterion("apply_ops <=", value, "applyOps");
            return (Criteria) this;
        }

        public Criteria andApplyOpsLessThanOrEqualToColumn(AuthApprove.Column column) {
            addCriterion(new StringBuilder("apply_ops <= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andApplyOpsLike(String value) {
            addCriterion("apply_ops like", value, "applyOps");
            return (Criteria) this;
        }

        public Criteria andApplyOpsNotLike(String value) {
            addCriterion("apply_ops not like", value, "applyOps");
            return (Criteria) this;
        }

        public Criteria andApplyOpsIn(List<String> values) {
            addCriterion("apply_ops in", values, "applyOps");
            return (Criteria) this;
        }

        public Criteria andApplyOpsNotIn(List<String> values) {
            addCriterion("apply_ops not in", values, "applyOps");
            return (Criteria) this;
        }

        public Criteria andApplyOpsBetween(String value1, String value2) {
            addCriterion("apply_ops between", value1, value2, "applyOps");
            return (Criteria) this;
        }

        public Criteria andApplyOpsNotBetween(String value1, String value2) {
            addCriterion("apply_ops not between", value1, value2, "applyOps");
            return (Criteria) this;
        }

        public Criteria andApplyTargetsIsNull() {
            addCriterion("apply_targets is null");
            return (Criteria) this;
        }

        public Criteria andApplyTargetsIsNotNull() {
            addCriterion("apply_targets is not null");
            return (Criteria) this;
        }

        public Criteria andApplyTargetsEqualTo(String value) {
            addCriterion("apply_targets =", value, "applyTargets");
            return (Criteria) this;
        }

        public Criteria andApplyTargetsEqualToColumn(AuthApprove.Column column) {
            addCriterion(new StringBuilder("apply_targets = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andApplyTargetsNotEqualTo(String value) {
            addCriterion("apply_targets <>", value, "applyTargets");
            return (Criteria) this;
        }

        public Criteria andApplyTargetsNotEqualToColumn(AuthApprove.Column column) {
            addCriterion(new StringBuilder("apply_targets <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andApplyTargetsGreaterThan(String value) {
            addCriterion("apply_targets >", value, "applyTargets");
            return (Criteria) this;
        }

        public Criteria andApplyTargetsGreaterThanColumn(AuthApprove.Column column) {
            addCriterion(new StringBuilder("apply_targets > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andApplyTargetsGreaterThanOrEqualTo(String value) {
            addCriterion("apply_targets >=", value, "applyTargets");
            return (Criteria) this;
        }

        public Criteria andApplyTargetsGreaterThanOrEqualToColumn(AuthApprove.Column column) {
            addCriterion(new StringBuilder("apply_targets >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andApplyTargetsLessThan(String value) {
            addCriterion("apply_targets <", value, "applyTargets");
            return (Criteria) this;
        }

        public Criteria andApplyTargetsLessThanColumn(AuthApprove.Column column) {
            addCriterion(new StringBuilder("apply_targets < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andApplyTargetsLessThanOrEqualTo(String value) {
            addCriterion("apply_targets <=", value, "applyTargets");
            return (Criteria) this;
        }

        public Criteria andApplyTargetsLessThanOrEqualToColumn(AuthApprove.Column column) {
            addCriterion(new StringBuilder("apply_targets <= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andApplyTargetsLike(String value) {
            addCriterion("apply_targets like", value, "applyTargets");
            return (Criteria) this;
        }

        public Criteria andApplyTargetsNotLike(String value) {
            addCriterion("apply_targets not like", value, "applyTargets");
            return (Criteria) this;
        }

        public Criteria andApplyTargetsIn(List<String> values) {
            addCriterion("apply_targets in", values, "applyTargets");
            return (Criteria) this;
        }

        public Criteria andApplyTargetsNotIn(List<String> values) {
            addCriterion("apply_targets not in", values, "applyTargets");
            return (Criteria) this;
        }

        public Criteria andApplyTargetsBetween(String value1, String value2) {
            addCriterion("apply_targets between", value1, value2, "applyTargets");
            return (Criteria) this;
        }

        public Criteria andApplyTargetsNotBetween(String value1, String value2) {
            addCriterion("apply_targets not between", value1, value2, "applyTargets");
            return (Criteria) this;
        }

        public Criteria andApprovedByIsNull() {
            addCriterion("approved_by is null");
            return (Criteria) this;
        }

        public Criteria andApprovedByIsNotNull() {
            addCriterion("approved_by is not null");
            return (Criteria) this;
        }

        public Criteria andApprovedByEqualTo(String value) {
            addCriterion("approved_by =", value, "approvedBy");
            return (Criteria) this;
        }

        public Criteria andApprovedByEqualToColumn(AuthApprove.Column column) {
            addCriterion(new StringBuilder("approved_by = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andApprovedByNotEqualTo(String value) {
            addCriterion("approved_by <>", value, "approvedBy");
            return (Criteria) this;
        }

        public Criteria andApprovedByNotEqualToColumn(AuthApprove.Column column) {
            addCriterion(new StringBuilder("approved_by <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andApprovedByGreaterThan(String value) {
            addCriterion("approved_by >", value, "approvedBy");
            return (Criteria) this;
        }

        public Criteria andApprovedByGreaterThanColumn(AuthApprove.Column column) {
            addCriterion(new StringBuilder("approved_by > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andApprovedByGreaterThanOrEqualTo(String value) {
            addCriterion("approved_by >=", value, "approvedBy");
            return (Criteria) this;
        }

        public Criteria andApprovedByGreaterThanOrEqualToColumn(AuthApprove.Column column) {
            addCriterion(new StringBuilder("approved_by >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andApprovedByLessThan(String value) {
            addCriterion("approved_by <", value, "approvedBy");
            return (Criteria) this;
        }

        public Criteria andApprovedByLessThanColumn(AuthApprove.Column column) {
            addCriterion(new StringBuilder("approved_by < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andApprovedByLessThanOrEqualTo(String value) {
            addCriterion("approved_by <=", value, "approvedBy");
            return (Criteria) this;
        }

        public Criteria andApprovedByLessThanOrEqualToColumn(AuthApprove.Column column) {
            addCriterion(new StringBuilder("approved_by <= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andApprovedByLike(String value) {
            addCriterion("approved_by like", value, "approvedBy");
            return (Criteria) this;
        }

        public Criteria andApprovedByNotLike(String value) {
            addCriterion("approved_by not like", value, "approvedBy");
            return (Criteria) this;
        }

        public Criteria andApprovedByIn(List<String> values) {
            addCriterion("approved_by in", values, "approvedBy");
            return (Criteria) this;
        }

        public Criteria andApprovedByNotIn(List<String> values) {
            addCriterion("approved_by not in", values, "approvedBy");
            return (Criteria) this;
        }

        public Criteria andApprovedByBetween(String value1, String value2) {
            addCriterion("approved_by between", value1, value2, "approvedBy");
            return (Criteria) this;
        }

        public Criteria andApprovedByNotBetween(String value1, String value2) {
            addCriterion("approved_by not between", value1, value2, "approvedBy");
            return (Criteria) this;
        }

        public Criteria andApprovedTimeIsNull() {
            addCriterion("approved_time is null");
            return (Criteria) this;
        }

        public Criteria andApprovedTimeIsNotNull() {
            addCriterion("approved_time is not null");
            return (Criteria) this;
        }

        public Criteria andApprovedTimeEqualTo(Date value) {
            addCriterion("approved_time =", value, "approvedTime");
            return (Criteria) this;
        }

        public Criteria andApprovedTimeEqualToColumn(AuthApprove.Column column) {
            addCriterion(new StringBuilder("approved_time = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andApprovedTimeNotEqualTo(Date value) {
            addCriterion("approved_time <>", value, "approvedTime");
            return (Criteria) this;
        }

        public Criteria andApprovedTimeNotEqualToColumn(AuthApprove.Column column) {
            addCriterion(new StringBuilder("approved_time <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andApprovedTimeGreaterThan(Date value) {
            addCriterion("approved_time >", value, "approvedTime");
            return (Criteria) this;
        }

        public Criteria andApprovedTimeGreaterThanColumn(AuthApprove.Column column) {
            addCriterion(new StringBuilder("approved_time > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andApprovedTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("approved_time >=", value, "approvedTime");
            return (Criteria) this;
        }

        public Criteria andApprovedTimeGreaterThanOrEqualToColumn(AuthApprove.Column column) {
            addCriterion(new StringBuilder("approved_time >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andApprovedTimeLessThan(Date value) {
            addCriterion("approved_time <", value, "approvedTime");
            return (Criteria) this;
        }

        public Criteria andApprovedTimeLessThanColumn(AuthApprove.Column column) {
            addCriterion(new StringBuilder("approved_time < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andApprovedTimeLessThanOrEqualTo(Date value) {
            addCriterion("approved_time <=", value, "approvedTime");
            return (Criteria) this;
        }

        public Criteria andApprovedTimeLessThanOrEqualToColumn(AuthApprove.Column column) {
            addCriterion(new StringBuilder("approved_time <= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andApprovedTimeIn(List<Date> values) {
            addCriterion("approved_time in", values, "approvedTime");
            return (Criteria) this;
        }

        public Criteria andApprovedTimeNotIn(List<Date> values) {
            addCriterion("approved_time not in", values, "approvedTime");
            return (Criteria) this;
        }

        public Criteria andApprovedTimeBetween(Date value1, Date value2) {
            addCriterion("approved_time between", value1, value2, "approvedTime");
            return (Criteria) this;
        }

        public Criteria andApprovedTimeNotBetween(Date value1, Date value2) {
            addCriterion("approved_time not between", value1, value2, "approvedTime");
            return (Criteria) this;
        }

        public Criteria andApprovedIsNull() {
            addCriterion("approved is null");
            return (Criteria) this;
        }

        public Criteria andApprovedIsNotNull() {
            addCriterion("approved is not null");
            return (Criteria) this;
        }

        public Criteria andApprovedEqualTo(Boolean value) {
            addCriterion("approved =", value, "approved");
            return (Criteria) this;
        }

        public Criteria andApprovedEqualToColumn(AuthApprove.Column column) {
            addCriterion(new StringBuilder("approved = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andApprovedNotEqualTo(Boolean value) {
            addCriterion("approved <>", value, "approved");
            return (Criteria) this;
        }

        public Criteria andApprovedNotEqualToColumn(AuthApprove.Column column) {
            addCriterion(new StringBuilder("approved <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andApprovedGreaterThan(Boolean value) {
            addCriterion("approved >", value, "approved");
            return (Criteria) this;
        }

        public Criteria andApprovedGreaterThanColumn(AuthApprove.Column column) {
            addCriterion(new StringBuilder("approved > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andApprovedGreaterThanOrEqualTo(Boolean value) {
            addCriterion("approved >=", value, "approved");
            return (Criteria) this;
        }

        public Criteria andApprovedGreaterThanOrEqualToColumn(AuthApprove.Column column) {
            addCriterion(new StringBuilder("approved >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andApprovedLessThan(Boolean value) {
            addCriterion("approved <", value, "approved");
            return (Criteria) this;
        }

        public Criteria andApprovedLessThanColumn(AuthApprove.Column column) {
            addCriterion(new StringBuilder("approved < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andApprovedLessThanOrEqualTo(Boolean value) {
            addCriterion("approved <=", value, "approved");
            return (Criteria) this;
        }

        public Criteria andApprovedLessThanOrEqualToColumn(AuthApprove.Column column) {
            addCriterion(new StringBuilder("approved <= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andApprovedIn(List<Boolean> values) {
            addCriterion("approved in", values, "approved");
            return (Criteria) this;
        }

        public Criteria andApprovedNotIn(List<Boolean> values) {
            addCriterion("approved not in", values, "approved");
            return (Criteria) this;
        }

        public Criteria andApprovedBetween(Boolean value1, Boolean value2) {
            addCriterion("approved between", value1, value2, "approved");
            return (Criteria) this;
        }

        public Criteria andApprovedNotBetween(Boolean value1, Boolean value2) {
            addCriterion("approved not between", value1, value2, "approved");
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeIsNull() {
            addCriterion("DataChange_LastTime is null");
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeIsNotNull() {
            addCriterion("DataChange_LastTime is not null");
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeEqualTo(Date value) {
            addCriterion("DataChange_LastTime =", value, "datachangeLasttime");
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeEqualToColumn(AuthApprove.Column column) {
            addCriterion(new StringBuilder("DataChange_LastTime = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeNotEqualTo(Date value) {
            addCriterion("DataChange_LastTime <>", value, "datachangeLasttime");
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeNotEqualToColumn(AuthApprove.Column column) {
            addCriterion(new StringBuilder("DataChange_LastTime <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeGreaterThan(Date value) {
            addCriterion("DataChange_LastTime >", value, "datachangeLasttime");
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeGreaterThanColumn(AuthApprove.Column column) {
            addCriterion(new StringBuilder("DataChange_LastTime > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeGreaterThanOrEqualTo(Date value) {
            addCriterion("DataChange_LastTime >=", value, "datachangeLasttime");
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeGreaterThanOrEqualToColumn(AuthApprove.Column column) {
            addCriterion(new StringBuilder("DataChange_LastTime >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeLessThan(Date value) {
            addCriterion("DataChange_LastTime <", value, "datachangeLasttime");
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeLessThanColumn(AuthApprove.Column column) {
            addCriterion(new StringBuilder("DataChange_LastTime < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeLessThanOrEqualTo(Date value) {
            addCriterion("DataChange_LastTime <=", value, "datachangeLasttime");
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeLessThanOrEqualToColumn(AuthApprove.Column column) {
            addCriterion(new StringBuilder("DataChange_LastTime <= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeIn(List<Date> values) {
            addCriterion("DataChange_LastTime in", values, "datachangeLasttime");
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeNotIn(List<Date> values) {
            addCriterion("DataChange_LastTime not in", values, "datachangeLasttime");
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeBetween(Date value1, Date value2) {
            addCriterion("DataChange_LastTime between", value1, value2, "datachangeLasttime");
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeNotBetween(Date value1, Date value2) {
            addCriterion("DataChange_LastTime not between", value1, value2, "datachangeLasttime");
            return (Criteria) this;
        }
    }

    public static class Criteria extends GeneratedCriteria {
        private AuthApproveExample example;

        protected Criteria(AuthApproveExample example) {
            super();
            this.example = example;
        }

        public AuthApproveExample example() {
            return this.example;
        }

        @Deprecated
        public Criteria andIf(boolean ifAdd, ICriteriaAdd add) {
            if (ifAdd) {
                add.add(this);
            }
            return this;
        }

        public Criteria when(boolean condition, ICriteriaWhen then) {
            if (condition) {
                then.criteria(this);
            }
            return this;
        }

        public Criteria when(boolean condition, ICriteriaWhen then, ICriteriaWhen otherwise) {
            if (condition) {
                then.criteria(this);
            } else {
                otherwise.criteria(this);
            }
            return this;
        }

        @Deprecated
        public interface ICriteriaAdd {
            Criteria add(Criteria add);
        }
    }

    public static class Criterion {
        private String condition;

        private Object value;

        private Object secondValue;

        private boolean noValue;

        private boolean singleValue;

        private boolean betweenValue;

        private boolean listValue;

        private String typeHandler;

        public String getCondition() {
            return condition;
        }

        public Object getValue() {
            return value;
        }

        public Object getSecondValue() {
            return secondValue;
        }

        public boolean isNoValue() {
            return noValue;
        }

        public boolean isSingleValue() {
            return singleValue;
        }

        public boolean isBetweenValue() {
            return betweenValue;
        }

        public boolean isListValue() {
            return listValue;
        }

        public String getTypeHandler() {
            return typeHandler;
        }

        protected Criterion(String condition) {
            super();
            this.condition = condition;
            this.typeHandler = null;
            this.noValue = true;
        }

        protected Criterion(String condition, Object value, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.typeHandler = typeHandler;
            if (value instanceof List<?>) {
                this.listValue = true;
            } else {
                this.singleValue = true;
            }
        }

        protected Criterion(String condition, Object value) {
            this(condition, value, null);
        }

        protected Criterion(String condition, Object value, Object secondValue, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.secondValue = secondValue;
            this.typeHandler = typeHandler;
            this.betweenValue = true;
        }

        protected Criterion(String condition, Object value, Object secondValue) {
            this(condition, value, secondValue, null);
        }
    }

    public interface ICriteriaWhen {
        void criteria(Criteria criteria);
    }

    public interface IExampleWhen {
        void example(com.ctrip.zeus.dao.entity.AuthApproveExample example);
    }
}