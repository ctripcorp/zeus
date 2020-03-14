package com.ctrip.zeus.dao.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SlbTrafficPolicyVsRExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    protected Integer offset;

    protected Integer rows;

    public SlbTrafficPolicyVsRExample() {
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

    public SlbTrafficPolicyVsRExample orderBy(String orderByClause) {
        this.setOrderByClause(orderByClause);
        return this;
    }

    public SlbTrafficPolicyVsRExample orderBy(String ... orderByClauses) {
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

    public SlbTrafficPolicyVsRExample limit(Integer rows) {
        this.rows = rows;
        return this;
    }

    public SlbTrafficPolicyVsRExample limit(Integer offset, Integer rows) {
        this.offset = offset;
        this.rows = rows;
        return this;
    }

    public SlbTrafficPolicyVsRExample page(Integer page, Integer pageSize) {
        this.offset = page * pageSize;
        this.rows = pageSize;
        return this;
    }

    public static Criteria newAndCreateCriteria() {
        SlbTrafficPolicyVsRExample example = new SlbTrafficPolicyVsRExample();
        return example.createCriteria();
    }

    public SlbTrafficPolicyVsRExample when(boolean condition, IExampleWhen then) {
        if (condition) {
            then.example(this);
        }
        return this;
    }

    public SlbTrafficPolicyVsRExample when(boolean condition, IExampleWhen then, IExampleWhen otherwise) {
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

        public Criteria andIdEqualToColumn(SlbTrafficPolicyVsR.Column column) {
            addCriterion(new StringBuilder("id = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andIdNotEqualTo(Long value) {
            addCriterion("id <>", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotEqualToColumn(SlbTrafficPolicyVsR.Column column) {
            addCriterion(new StringBuilder("id <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andIdGreaterThan(Long value) {
            addCriterion("id >", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThanColumn(SlbTrafficPolicyVsR.Column column) {
            addCriterion(new StringBuilder("id > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andIdGreaterThanOrEqualTo(Long value) {
            addCriterion("id >=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThanOrEqualToColumn(SlbTrafficPolicyVsR.Column column) {
            addCriterion(new StringBuilder("id >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andIdLessThan(Long value) {
            addCriterion("id <", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThanColumn(SlbTrafficPolicyVsR.Column column) {
            addCriterion(new StringBuilder("id < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andIdLessThanOrEqualTo(Long value) {
            addCriterion("id <=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThanOrEqualToColumn(SlbTrafficPolicyVsR.Column column) {
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

        public Criteria andVsIdIsNull() {
            addCriterion("vs_id is null");
            return (Criteria) this;
        }

        public Criteria andVsIdIsNotNull() {
            addCriterion("vs_id is not null");
            return (Criteria) this;
        }

        public Criteria andVsIdEqualTo(Long value) {
            addCriterion("vs_id =", value, "vsId");
            return (Criteria) this;
        }

        public Criteria andVsIdEqualToColumn(SlbTrafficPolicyVsR.Column column) {
            addCriterion(new StringBuilder("vs_id = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andVsIdNotEqualTo(Long value) {
            addCriterion("vs_id <>", value, "vsId");
            return (Criteria) this;
        }

        public Criteria andVsIdNotEqualToColumn(SlbTrafficPolicyVsR.Column column) {
            addCriterion(new StringBuilder("vs_id <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andVsIdGreaterThan(Long value) {
            addCriterion("vs_id >", value, "vsId");
            return (Criteria) this;
        }

        public Criteria andVsIdGreaterThanColumn(SlbTrafficPolicyVsR.Column column) {
            addCriterion(new StringBuilder("vs_id > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andVsIdGreaterThanOrEqualTo(Long value) {
            addCriterion("vs_id >=", value, "vsId");
            return (Criteria) this;
        }

        public Criteria andVsIdGreaterThanOrEqualToColumn(SlbTrafficPolicyVsR.Column column) {
            addCriterion(new StringBuilder("vs_id >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andVsIdLessThan(Long value) {
            addCriterion("vs_id <", value, "vsId");
            return (Criteria) this;
        }

        public Criteria andVsIdLessThanColumn(SlbTrafficPolicyVsR.Column column) {
            addCriterion(new StringBuilder("vs_id < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andVsIdLessThanOrEqualTo(Long value) {
            addCriterion("vs_id <=", value, "vsId");
            return (Criteria) this;
        }

        public Criteria andVsIdLessThanOrEqualToColumn(SlbTrafficPolicyVsR.Column column) {
            addCriterion(new StringBuilder("vs_id <= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andVsIdIn(List<Long> values) {
            addCriterion("vs_id in", values, "vsId");
            return (Criteria) this;
        }

        public Criteria andVsIdNotIn(List<Long> values) {
            addCriterion("vs_id not in", values, "vsId");
            return (Criteria) this;
        }

        public Criteria andVsIdBetween(Long value1, Long value2) {
            addCriterion("vs_id between", value1, value2, "vsId");
            return (Criteria) this;
        }

        public Criteria andVsIdNotBetween(Long value1, Long value2) {
            addCriterion("vs_id not between", value1, value2, "vsId");
            return (Criteria) this;
        }

        public Criteria andPolicyIdIsNull() {
            addCriterion("policy_id is null");
            return (Criteria) this;
        }

        public Criteria andPolicyIdIsNotNull() {
            addCriterion("policy_id is not null");
            return (Criteria) this;
        }

        public Criteria andPolicyIdEqualTo(Long value) {
            addCriterion("policy_id =", value, "policyId");
            return (Criteria) this;
        }

        public Criteria andPolicyIdEqualToColumn(SlbTrafficPolicyVsR.Column column) {
            addCriterion(new StringBuilder("policy_id = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andPolicyIdNotEqualTo(Long value) {
            addCriterion("policy_id <>", value, "policyId");
            return (Criteria) this;
        }

        public Criteria andPolicyIdNotEqualToColumn(SlbTrafficPolicyVsR.Column column) {
            addCriterion(new StringBuilder("policy_id <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andPolicyIdGreaterThan(Long value) {
            addCriterion("policy_id >", value, "policyId");
            return (Criteria) this;
        }

        public Criteria andPolicyIdGreaterThanColumn(SlbTrafficPolicyVsR.Column column) {
            addCriterion(new StringBuilder("policy_id > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andPolicyIdGreaterThanOrEqualTo(Long value) {
            addCriterion("policy_id >=", value, "policyId");
            return (Criteria) this;
        }

        public Criteria andPolicyIdGreaterThanOrEqualToColumn(SlbTrafficPolicyVsR.Column column) {
            addCriterion(new StringBuilder("policy_id >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andPolicyIdLessThan(Long value) {
            addCriterion("policy_id <", value, "policyId");
            return (Criteria) this;
        }

        public Criteria andPolicyIdLessThanColumn(SlbTrafficPolicyVsR.Column column) {
            addCriterion(new StringBuilder("policy_id < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andPolicyIdLessThanOrEqualTo(Long value) {
            addCriterion("policy_id <=", value, "policyId");
            return (Criteria) this;
        }

        public Criteria andPolicyIdLessThanOrEqualToColumn(SlbTrafficPolicyVsR.Column column) {
            addCriterion(new StringBuilder("policy_id <= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andPolicyIdIn(List<Long> values) {
            addCriterion("policy_id in", values, "policyId");
            return (Criteria) this;
        }

        public Criteria andPolicyIdNotIn(List<Long> values) {
            addCriterion("policy_id not in", values, "policyId");
            return (Criteria) this;
        }

        public Criteria andPolicyIdBetween(Long value1, Long value2) {
            addCriterion("policy_id between", value1, value2, "policyId");
            return (Criteria) this;
        }

        public Criteria andPolicyIdNotBetween(Long value1, Long value2) {
            addCriterion("policy_id not between", value1, value2, "policyId");
            return (Criteria) this;
        }

        public Criteria andPolicyVersionIsNull() {
            addCriterion("policy_version is null");
            return (Criteria) this;
        }

        public Criteria andPolicyVersionIsNotNull() {
            addCriterion("policy_version is not null");
            return (Criteria) this;
        }

        public Criteria andPolicyVersionEqualTo(Integer value) {
            addCriterion("policy_version =", value, "policyVersion");
            return (Criteria) this;
        }

        public Criteria andPolicyVersionEqualToColumn(SlbTrafficPolicyVsR.Column column) {
            addCriterion(new StringBuilder("policy_version = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andPolicyVersionNotEqualTo(Integer value) {
            addCriterion("policy_version <>", value, "policyVersion");
            return (Criteria) this;
        }

        public Criteria andPolicyVersionNotEqualToColumn(SlbTrafficPolicyVsR.Column column) {
            addCriterion(new StringBuilder("policy_version <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andPolicyVersionGreaterThan(Integer value) {
            addCriterion("policy_version >", value, "policyVersion");
            return (Criteria) this;
        }

        public Criteria andPolicyVersionGreaterThanColumn(SlbTrafficPolicyVsR.Column column) {
            addCriterion(new StringBuilder("policy_version > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andPolicyVersionGreaterThanOrEqualTo(Integer value) {
            addCriterion("policy_version >=", value, "policyVersion");
            return (Criteria) this;
        }

        public Criteria andPolicyVersionGreaterThanOrEqualToColumn(SlbTrafficPolicyVsR.Column column) {
            addCriterion(new StringBuilder("policy_version >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andPolicyVersionLessThan(Integer value) {
            addCriterion("policy_version <", value, "policyVersion");
            return (Criteria) this;
        }

        public Criteria andPolicyVersionLessThanColumn(SlbTrafficPolicyVsR.Column column) {
            addCriterion(new StringBuilder("policy_version < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andPolicyVersionLessThanOrEqualTo(Integer value) {
            addCriterion("policy_version <=", value, "policyVersion");
            return (Criteria) this;
        }

        public Criteria andPolicyVersionLessThanOrEqualToColumn(SlbTrafficPolicyVsR.Column column) {
            addCriterion(new StringBuilder("policy_version <= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andPolicyVersionIn(List<Integer> values) {
            addCriterion("policy_version in", values, "policyVersion");
            return (Criteria) this;
        }

        public Criteria andPolicyVersionNotIn(List<Integer> values) {
            addCriterion("policy_version not in", values, "policyVersion");
            return (Criteria) this;
        }

        public Criteria andPolicyVersionBetween(Integer value1, Integer value2) {
            addCriterion("policy_version between", value1, value2, "policyVersion");
            return (Criteria) this;
        }

        public Criteria andPolicyVersionNotBetween(Integer value1, Integer value2) {
            addCriterion("policy_version not between", value1, value2, "policyVersion");
            return (Criteria) this;
        }

        public Criteria andPathIsNull() {
            addCriterion("path is null");
            return (Criteria) this;
        }

        public Criteria andPathIsNotNull() {
            addCriterion("path is not null");
            return (Criteria) this;
        }

        public Criteria andPathEqualTo(String value) {
            addCriterion("path =", value, "path");
            return (Criteria) this;
        }

        public Criteria andPathEqualToColumn(SlbTrafficPolicyVsR.Column column) {
            addCriterion(new StringBuilder("path = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andPathNotEqualTo(String value) {
            addCriterion("path <>", value, "path");
            return (Criteria) this;
        }

        public Criteria andPathNotEqualToColumn(SlbTrafficPolicyVsR.Column column) {
            addCriterion(new StringBuilder("path <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andPathGreaterThan(String value) {
            addCriterion("path >", value, "path");
            return (Criteria) this;
        }

        public Criteria andPathGreaterThanColumn(SlbTrafficPolicyVsR.Column column) {
            addCriterion(new StringBuilder("path > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andPathGreaterThanOrEqualTo(String value) {
            addCriterion("path >=", value, "path");
            return (Criteria) this;
        }

        public Criteria andPathGreaterThanOrEqualToColumn(SlbTrafficPolicyVsR.Column column) {
            addCriterion(new StringBuilder("path >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andPathLessThan(String value) {
            addCriterion("path <", value, "path");
            return (Criteria) this;
        }

        public Criteria andPathLessThanColumn(SlbTrafficPolicyVsR.Column column) {
            addCriterion(new StringBuilder("path < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andPathLessThanOrEqualTo(String value) {
            addCriterion("path <=", value, "path");
            return (Criteria) this;
        }

        public Criteria andPathLessThanOrEqualToColumn(SlbTrafficPolicyVsR.Column column) {
            addCriterion(new StringBuilder("path <= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andPathLike(String value) {
            addCriterion("path like", value, "path");
            return (Criteria) this;
        }

        public Criteria andPathNotLike(String value) {
            addCriterion("path not like", value, "path");
            return (Criteria) this;
        }

        public Criteria andPathIn(List<String> values) {
            addCriterion("path in", values, "path");
            return (Criteria) this;
        }

        public Criteria andPathNotIn(List<String> values) {
            addCriterion("path not in", values, "path");
            return (Criteria) this;
        }

        public Criteria andPathBetween(String value1, String value2) {
            addCriterion("path between", value1, value2, "path");
            return (Criteria) this;
        }

        public Criteria andPathNotBetween(String value1, String value2) {
            addCriterion("path not between", value1, value2, "path");
            return (Criteria) this;
        }

        public Criteria andPriorityIsNull() {
            addCriterion("priority is null");
            return (Criteria) this;
        }

        public Criteria andPriorityIsNotNull() {
            addCriterion("priority is not null");
            return (Criteria) this;
        }

        public Criteria andPriorityEqualTo(Integer value) {
            addCriterion("priority =", value, "priority");
            return (Criteria) this;
        }

        public Criteria andPriorityEqualToColumn(SlbTrafficPolicyVsR.Column column) {
            addCriterion(new StringBuilder("priority = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andPriorityNotEqualTo(Integer value) {
            addCriterion("priority <>", value, "priority");
            return (Criteria) this;
        }

        public Criteria andPriorityNotEqualToColumn(SlbTrafficPolicyVsR.Column column) {
            addCriterion(new StringBuilder("priority <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andPriorityGreaterThan(Integer value) {
            addCriterion("priority >", value, "priority");
            return (Criteria) this;
        }

        public Criteria andPriorityGreaterThanColumn(SlbTrafficPolicyVsR.Column column) {
            addCriterion(new StringBuilder("priority > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andPriorityGreaterThanOrEqualTo(Integer value) {
            addCriterion("priority >=", value, "priority");
            return (Criteria) this;
        }

        public Criteria andPriorityGreaterThanOrEqualToColumn(SlbTrafficPolicyVsR.Column column) {
            addCriterion(new StringBuilder("priority >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andPriorityLessThan(Integer value) {
            addCriterion("priority <", value, "priority");
            return (Criteria) this;
        }

        public Criteria andPriorityLessThanColumn(SlbTrafficPolicyVsR.Column column) {
            addCriterion(new StringBuilder("priority < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andPriorityLessThanOrEqualTo(Integer value) {
            addCriterion("priority <=", value, "priority");
            return (Criteria) this;
        }

        public Criteria andPriorityLessThanOrEqualToColumn(SlbTrafficPolicyVsR.Column column) {
            addCriterion(new StringBuilder("priority <= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andPriorityIn(List<Integer> values) {
            addCriterion("priority in", values, "priority");
            return (Criteria) this;
        }

        public Criteria andPriorityNotIn(List<Integer> values) {
            addCriterion("priority not in", values, "priority");
            return (Criteria) this;
        }

        public Criteria andPriorityBetween(Integer value1, Integer value2) {
            addCriterion("priority between", value1, value2, "priority");
            return (Criteria) this;
        }

        public Criteria andPriorityNotBetween(Integer value1, Integer value2) {
            addCriterion("priority not between", value1, value2, "priority");
            return (Criteria) this;
        }

        public Criteria andHashIsNull() {
            addCriterion("hash is null");
            return (Criteria) this;
        }

        public Criteria andHashIsNotNull() {
            addCriterion("hash is not null");
            return (Criteria) this;
        }

        public Criteria andHashEqualTo(Integer value) {
            addCriterion("hash =", value, "hash");
            return (Criteria) this;
        }

        public Criteria andHashEqualToColumn(SlbTrafficPolicyVsR.Column column) {
            addCriterion(new StringBuilder("hash = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andHashNotEqualTo(Integer value) {
            addCriterion("hash <>", value, "hash");
            return (Criteria) this;
        }

        public Criteria andHashNotEqualToColumn(SlbTrafficPolicyVsR.Column column) {
            addCriterion(new StringBuilder("hash <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andHashGreaterThan(Integer value) {
            addCriterion("hash >", value, "hash");
            return (Criteria) this;
        }

        public Criteria andHashGreaterThanColumn(SlbTrafficPolicyVsR.Column column) {
            addCriterion(new StringBuilder("hash > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andHashGreaterThanOrEqualTo(Integer value) {
            addCriterion("hash >=", value, "hash");
            return (Criteria) this;
        }

        public Criteria andHashGreaterThanOrEqualToColumn(SlbTrafficPolicyVsR.Column column) {
            addCriterion(new StringBuilder("hash >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andHashLessThan(Integer value) {
            addCriterion("hash <", value, "hash");
            return (Criteria) this;
        }

        public Criteria andHashLessThanColumn(SlbTrafficPolicyVsR.Column column) {
            addCriterion(new StringBuilder("hash < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andHashLessThanOrEqualTo(Integer value) {
            addCriterion("hash <=", value, "hash");
            return (Criteria) this;
        }

        public Criteria andHashLessThanOrEqualToColumn(SlbTrafficPolicyVsR.Column column) {
            addCriterion(new StringBuilder("hash <= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andHashIn(List<Integer> values) {
            addCriterion("hash in", values, "hash");
            return (Criteria) this;
        }

        public Criteria andHashNotIn(List<Integer> values) {
            addCriterion("hash not in", values, "hash");
            return (Criteria) this;
        }

        public Criteria andHashBetween(Integer value1, Integer value2) {
            addCriterion("hash between", value1, value2, "hash");
            return (Criteria) this;
        }

        public Criteria andHashNotBetween(Integer value1, Integer value2) {
            addCriterion("hash not between", value1, value2, "hash");
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

        public Criteria andDatachangeLasttimeEqualToColumn(SlbTrafficPolicyVsR.Column column) {
            addCriterion(new StringBuilder("DataChange_LastTime = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeNotEqualTo(Date value) {
            addCriterion("DataChange_LastTime <>", value, "datachangeLasttime");
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeNotEqualToColumn(SlbTrafficPolicyVsR.Column column) {
            addCriterion(new StringBuilder("DataChange_LastTime <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeGreaterThan(Date value) {
            addCriterion("DataChange_LastTime >", value, "datachangeLasttime");
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeGreaterThanColumn(SlbTrafficPolicyVsR.Column column) {
            addCriterion(new StringBuilder("DataChange_LastTime > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeGreaterThanOrEqualTo(Date value) {
            addCriterion("DataChange_LastTime >=", value, "datachangeLasttime");
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeGreaterThanOrEqualToColumn(SlbTrafficPolicyVsR.Column column) {
            addCriterion(new StringBuilder("DataChange_LastTime >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeLessThan(Date value) {
            addCriterion("DataChange_LastTime <", value, "datachangeLasttime");
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeLessThanColumn(SlbTrafficPolicyVsR.Column column) {
            addCriterion(new StringBuilder("DataChange_LastTime < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeLessThanOrEqualTo(Date value) {
            addCriterion("DataChange_LastTime <=", value, "datachangeLasttime");
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeLessThanOrEqualToColumn(SlbTrafficPolicyVsR.Column column) {
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
        private SlbTrafficPolicyVsRExample example;

        protected Criteria(SlbTrafficPolicyVsRExample example) {
            super();
            this.example = example;
        }

        public SlbTrafficPolicyVsRExample example() {
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
        void example(com.ctrip.zeus.dao.entity.SlbTrafficPolicyVsRExample example);
    }
}