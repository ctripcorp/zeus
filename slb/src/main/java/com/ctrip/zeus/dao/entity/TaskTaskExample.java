package com.ctrip.zeus.dao.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TaskTaskExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    protected Integer offset;

    protected Integer rows;

    public TaskTaskExample() {
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

    public TaskTaskExample orderBy(String orderByClause) {
        this.setOrderByClause(orderByClause);
        return this;
    }

    public TaskTaskExample orderBy(String ... orderByClauses) {
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

    public TaskTaskExample limit(Integer rows) {
        this.rows = rows;
        return this;
    }

    public TaskTaskExample limit(Integer offset, Integer rows) {
        this.offset = offset;
        this.rows = rows;
        return this;
    }

    public TaskTaskExample page(Integer page, Integer pageSize) {
        this.offset = page * pageSize;
        this.rows = pageSize;
        return this;
    }

    public static Criteria newAndCreateCriteria() {
        TaskTaskExample example = new TaskTaskExample();
        return example.createCriteria();
    }

    public TaskTaskExample when(boolean condition, IExampleWhen then) {
        if (condition) {
            then.example(this);
        }
        return this;
    }

    public TaskTaskExample when(boolean condition, IExampleWhen then, IExampleWhen otherwise) {
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

        public Criteria andIdEqualToColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("id = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andIdNotEqualTo(Long value) {
            addCriterion("id <>", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotEqualToColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("id <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andIdGreaterThan(Long value) {
            addCriterion("id >", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThanColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("id > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andIdGreaterThanOrEqualTo(Long value) {
            addCriterion("id >=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThanOrEqualToColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("id >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andIdLessThan(Long value) {
            addCriterion("id <", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThanColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("id < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andIdLessThanOrEqualTo(Long value) {
            addCriterion("id <=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThanOrEqualToColumn(TaskTask.Column column) {
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

        public Criteria andOpsTypeIsNull() {
            addCriterion("ops_type is null");
            return (Criteria) this;
        }

        public Criteria andOpsTypeIsNotNull() {
            addCriterion("ops_type is not null");
            return (Criteria) this;
        }

        public Criteria andOpsTypeEqualTo(String value) {
            addCriterion("ops_type =", value, "opsType");
            return (Criteria) this;
        }

        public Criteria andOpsTypeEqualToColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("ops_type = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andOpsTypeNotEqualTo(String value) {
            addCriterion("ops_type <>", value, "opsType");
            return (Criteria) this;
        }

        public Criteria andOpsTypeNotEqualToColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("ops_type <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andOpsTypeGreaterThan(String value) {
            addCriterion("ops_type >", value, "opsType");
            return (Criteria) this;
        }

        public Criteria andOpsTypeGreaterThanColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("ops_type > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andOpsTypeGreaterThanOrEqualTo(String value) {
            addCriterion("ops_type >=", value, "opsType");
            return (Criteria) this;
        }

        public Criteria andOpsTypeGreaterThanOrEqualToColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("ops_type >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andOpsTypeLessThan(String value) {
            addCriterion("ops_type <", value, "opsType");
            return (Criteria) this;
        }

        public Criteria andOpsTypeLessThanColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("ops_type < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andOpsTypeLessThanOrEqualTo(String value) {
            addCriterion("ops_type <=", value, "opsType");
            return (Criteria) this;
        }

        public Criteria andOpsTypeLessThanOrEqualToColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("ops_type <= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andOpsTypeLike(String value) {
            addCriterion("ops_type like", value, "opsType");
            return (Criteria) this;
        }

        public Criteria andOpsTypeNotLike(String value) {
            addCriterion("ops_type not like", value, "opsType");
            return (Criteria) this;
        }

        public Criteria andOpsTypeIn(List<String> values) {
            addCriterion("ops_type in", values, "opsType");
            return (Criteria) this;
        }

        public Criteria andOpsTypeNotIn(List<String> values) {
            addCriterion("ops_type not in", values, "opsType");
            return (Criteria) this;
        }

        public Criteria andOpsTypeBetween(String value1, String value2) {
            addCriterion("ops_type between", value1, value2, "opsType");
            return (Criteria) this;
        }

        public Criteria andOpsTypeNotBetween(String value1, String value2) {
            addCriterion("ops_type not between", value1, value2, "opsType");
            return (Criteria) this;
        }

        public Criteria andGroupIdIsNull() {
            addCriterion("group_id is null");
            return (Criteria) this;
        }

        public Criteria andGroupIdIsNotNull() {
            addCriterion("group_id is not null");
            return (Criteria) this;
        }

        public Criteria andGroupIdEqualTo(Long value) {
            addCriterion("group_id =", value, "groupId");
            return (Criteria) this;
        }

        public Criteria andGroupIdEqualToColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("group_id = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andGroupIdNotEqualTo(Long value) {
            addCriterion("group_id <>", value, "groupId");
            return (Criteria) this;
        }

        public Criteria andGroupIdNotEqualToColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("group_id <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andGroupIdGreaterThan(Long value) {
            addCriterion("group_id >", value, "groupId");
            return (Criteria) this;
        }

        public Criteria andGroupIdGreaterThanColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("group_id > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andGroupIdGreaterThanOrEqualTo(Long value) {
            addCriterion("group_id >=", value, "groupId");
            return (Criteria) this;
        }

        public Criteria andGroupIdGreaterThanOrEqualToColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("group_id >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andGroupIdLessThan(Long value) {
            addCriterion("group_id <", value, "groupId");
            return (Criteria) this;
        }

        public Criteria andGroupIdLessThanColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("group_id < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andGroupIdLessThanOrEqualTo(Long value) {
            addCriterion("group_id <=", value, "groupId");
            return (Criteria) this;
        }

        public Criteria andGroupIdLessThanOrEqualToColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("group_id <= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andGroupIdIn(List<Long> values) {
            addCriterion("group_id in", values, "groupId");
            return (Criteria) this;
        }

        public Criteria andGroupIdNotIn(List<Long> values) {
            addCriterion("group_id not in", values, "groupId");
            return (Criteria) this;
        }

        public Criteria andGroupIdBetween(Long value1, Long value2) {
            addCriterion("group_id between", value1, value2, "groupId");
            return (Criteria) this;
        }

        public Criteria andGroupIdNotBetween(Long value1, Long value2) {
            addCriterion("group_id not between", value1, value2, "groupId");
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

        public Criteria andPolicyIdEqualToColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("policy_id = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andPolicyIdNotEqualTo(Long value) {
            addCriterion("policy_id <>", value, "policyId");
            return (Criteria) this;
        }

        public Criteria andPolicyIdNotEqualToColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("policy_id <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andPolicyIdGreaterThan(Long value) {
            addCriterion("policy_id >", value, "policyId");
            return (Criteria) this;
        }

        public Criteria andPolicyIdGreaterThanColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("policy_id > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andPolicyIdGreaterThanOrEqualTo(Long value) {
            addCriterion("policy_id >=", value, "policyId");
            return (Criteria) this;
        }

        public Criteria andPolicyIdGreaterThanOrEqualToColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("policy_id >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andPolicyIdLessThan(Long value) {
            addCriterion("policy_id <", value, "policyId");
            return (Criteria) this;
        }

        public Criteria andPolicyIdLessThanColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("policy_id < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andPolicyIdLessThanOrEqualTo(Long value) {
            addCriterion("policy_id <=", value, "policyId");
            return (Criteria) this;
        }

        public Criteria andPolicyIdLessThanOrEqualToColumn(TaskTask.Column column) {
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

        public Criteria andSlbIdIsNull() {
            addCriterion("slb_id is null");
            return (Criteria) this;
        }

        public Criteria andSlbIdIsNotNull() {
            addCriterion("slb_id is not null");
            return (Criteria) this;
        }

        public Criteria andSlbIdEqualTo(Long value) {
            addCriterion("slb_id =", value, "slbId");
            return (Criteria) this;
        }

        public Criteria andSlbIdEqualToColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("slb_id = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andSlbIdNotEqualTo(Long value) {
            addCriterion("slb_id <>", value, "slbId");
            return (Criteria) this;
        }

        public Criteria andSlbIdNotEqualToColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("slb_id <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andSlbIdGreaterThan(Long value) {
            addCriterion("slb_id >", value, "slbId");
            return (Criteria) this;
        }

        public Criteria andSlbIdGreaterThanColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("slb_id > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andSlbIdGreaterThanOrEqualTo(Long value) {
            addCriterion("slb_id >=", value, "slbId");
            return (Criteria) this;
        }

        public Criteria andSlbIdGreaterThanOrEqualToColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("slb_id >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andSlbIdLessThan(Long value) {
            addCriterion("slb_id <", value, "slbId");
            return (Criteria) this;
        }

        public Criteria andSlbIdLessThanColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("slb_id < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andSlbIdLessThanOrEqualTo(Long value) {
            addCriterion("slb_id <=", value, "slbId");
            return (Criteria) this;
        }

        public Criteria andSlbIdLessThanOrEqualToColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("slb_id <= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andSlbIdIn(List<Long> values) {
            addCriterion("slb_id in", values, "slbId");
            return (Criteria) this;
        }

        public Criteria andSlbIdNotIn(List<Long> values) {
            addCriterion("slb_id not in", values, "slbId");
            return (Criteria) this;
        }

        public Criteria andSlbIdBetween(Long value1, Long value2) {
            addCriterion("slb_id between", value1, value2, "slbId");
            return (Criteria) this;
        }

        public Criteria andSlbIdNotBetween(Long value1, Long value2) {
            addCriterion("slb_id not between", value1, value2, "slbId");
            return (Criteria) this;
        }

        public Criteria andSlbVirtualServerIdIsNull() {
            addCriterion("slb_virtual_server_id is null");
            return (Criteria) this;
        }

        public Criteria andSlbVirtualServerIdIsNotNull() {
            addCriterion("slb_virtual_server_id is not null");
            return (Criteria) this;
        }

        public Criteria andSlbVirtualServerIdEqualTo(Long value) {
            addCriterion("slb_virtual_server_id =", value, "slbVirtualServerId");
            return (Criteria) this;
        }

        public Criteria andSlbVirtualServerIdEqualToColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("slb_virtual_server_id = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andSlbVirtualServerIdNotEqualTo(Long value) {
            addCriterion("slb_virtual_server_id <>", value, "slbVirtualServerId");
            return (Criteria) this;
        }

        public Criteria andSlbVirtualServerIdNotEqualToColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("slb_virtual_server_id <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andSlbVirtualServerIdGreaterThan(Long value) {
            addCriterion("slb_virtual_server_id >", value, "slbVirtualServerId");
            return (Criteria) this;
        }

        public Criteria andSlbVirtualServerIdGreaterThanColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("slb_virtual_server_id > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andSlbVirtualServerIdGreaterThanOrEqualTo(Long value) {
            addCriterion("slb_virtual_server_id >=", value, "slbVirtualServerId");
            return (Criteria) this;
        }

        public Criteria andSlbVirtualServerIdGreaterThanOrEqualToColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("slb_virtual_server_id >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andSlbVirtualServerIdLessThan(Long value) {
            addCriterion("slb_virtual_server_id <", value, "slbVirtualServerId");
            return (Criteria) this;
        }

        public Criteria andSlbVirtualServerIdLessThanColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("slb_virtual_server_id < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andSlbVirtualServerIdLessThanOrEqualTo(Long value) {
            addCriterion("slb_virtual_server_id <=", value, "slbVirtualServerId");
            return (Criteria) this;
        }

        public Criteria andSlbVirtualServerIdLessThanOrEqualToColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("slb_virtual_server_id <= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andSlbVirtualServerIdIn(List<Long> values) {
            addCriterion("slb_virtual_server_id in", values, "slbVirtualServerId");
            return (Criteria) this;
        }

        public Criteria andSlbVirtualServerIdNotIn(List<Long> values) {
            addCriterion("slb_virtual_server_id not in", values, "slbVirtualServerId");
            return (Criteria) this;
        }

        public Criteria andSlbVirtualServerIdBetween(Long value1, Long value2) {
            addCriterion("slb_virtual_server_id between", value1, value2, "slbVirtualServerId");
            return (Criteria) this;
        }

        public Criteria andSlbVirtualServerIdNotBetween(Long value1, Long value2) {
            addCriterion("slb_virtual_server_id not between", value1, value2, "slbVirtualServerId");
            return (Criteria) this;
        }

        public Criteria andIpListIsNull() {
            addCriterion("ip_list is null");
            return (Criteria) this;
        }

        public Criteria andIpListIsNotNull() {
            addCriterion("ip_list is not null");
            return (Criteria) this;
        }

        public Criteria andIpListEqualTo(String value) {
            addCriterion("ip_list =", value, "ipList");
            return (Criteria) this;
        }

        public Criteria andIpListEqualToColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("ip_list = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andIpListNotEqualTo(String value) {
            addCriterion("ip_list <>", value, "ipList");
            return (Criteria) this;
        }

        public Criteria andIpListNotEqualToColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("ip_list <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andIpListGreaterThan(String value) {
            addCriterion("ip_list >", value, "ipList");
            return (Criteria) this;
        }

        public Criteria andIpListGreaterThanColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("ip_list > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andIpListGreaterThanOrEqualTo(String value) {
            addCriterion("ip_list >=", value, "ipList");
            return (Criteria) this;
        }

        public Criteria andIpListGreaterThanOrEqualToColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("ip_list >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andIpListLessThan(String value) {
            addCriterion("ip_list <", value, "ipList");
            return (Criteria) this;
        }

        public Criteria andIpListLessThanColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("ip_list < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andIpListLessThanOrEqualTo(String value) {
            addCriterion("ip_list <=", value, "ipList");
            return (Criteria) this;
        }

        public Criteria andIpListLessThanOrEqualToColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("ip_list <= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andIpListLike(String value) {
            addCriterion("ip_list like", value, "ipList");
            return (Criteria) this;
        }

        public Criteria andIpListNotLike(String value) {
            addCriterion("ip_list not like", value, "ipList");
            return (Criteria) this;
        }

        public Criteria andIpListIn(List<String> values) {
            addCriterion("ip_list in", values, "ipList");
            return (Criteria) this;
        }

        public Criteria andIpListNotIn(List<String> values) {
            addCriterion("ip_list not in", values, "ipList");
            return (Criteria) this;
        }

        public Criteria andIpListBetween(String value1, String value2) {
            addCriterion("ip_list between", value1, value2, "ipList");
            return (Criteria) this;
        }

        public Criteria andIpListNotBetween(String value1, String value2) {
            addCriterion("ip_list not between", value1, value2, "ipList");
            return (Criteria) this;
        }

        public Criteria andUpIsNull() {
            addCriterion("up is null");
            return (Criteria) this;
        }

        public Criteria andUpIsNotNull() {
            addCriterion("up is not null");
            return (Criteria) this;
        }

        public Criteria andUpEqualTo(Boolean value) {
            addCriterion("up =", value, "up");
            return (Criteria) this;
        }

        public Criteria andUpEqualToColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("up = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andUpNotEqualTo(Boolean value) {
            addCriterion("up <>", value, "up");
            return (Criteria) this;
        }

        public Criteria andUpNotEqualToColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("up <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andUpGreaterThan(Boolean value) {
            addCriterion("up >", value, "up");
            return (Criteria) this;
        }

        public Criteria andUpGreaterThanColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("up > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andUpGreaterThanOrEqualTo(Boolean value) {
            addCriterion("up >=", value, "up");
            return (Criteria) this;
        }

        public Criteria andUpGreaterThanOrEqualToColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("up >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andUpLessThan(Boolean value) {
            addCriterion("up <", value, "up");
            return (Criteria) this;
        }

        public Criteria andUpLessThanColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("up < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andUpLessThanOrEqualTo(Boolean value) {
            addCriterion("up <=", value, "up");
            return (Criteria) this;
        }

        public Criteria andUpLessThanOrEqualToColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("up <= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andUpIn(List<Boolean> values) {
            addCriterion("up in", values, "up");
            return (Criteria) this;
        }

        public Criteria andUpNotIn(List<Boolean> values) {
            addCriterion("up not in", values, "up");
            return (Criteria) this;
        }

        public Criteria andUpBetween(Boolean value1, Boolean value2) {
            addCriterion("up between", value1, value2, "up");
            return (Criteria) this;
        }

        public Criteria andUpNotBetween(Boolean value1, Boolean value2) {
            addCriterion("up not between", value1, value2, "up");
            return (Criteria) this;
        }

        public Criteria andStatusIsNull() {
            addCriterion("`status` is null");
            return (Criteria) this;
        }

        public Criteria andStatusIsNotNull() {
            addCriterion("`status` is not null");
            return (Criteria) this;
        }

        public Criteria andStatusEqualTo(String value) {
            addCriterion("`status` =", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusEqualToColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("`status` = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andStatusNotEqualTo(String value) {
            addCriterion("`status` <>", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotEqualToColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("`status` <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andStatusGreaterThan(String value) {
            addCriterion("`status` >", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusGreaterThanColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("`status` > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andStatusGreaterThanOrEqualTo(String value) {
            addCriterion("`status` >=", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusGreaterThanOrEqualToColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("`status` >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andStatusLessThan(String value) {
            addCriterion("`status` <", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLessThanColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("`status` < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andStatusLessThanOrEqualTo(String value) {
            addCriterion("`status` <=", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLessThanOrEqualToColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("`status` <= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andStatusLike(String value) {
            addCriterion("`status` like", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotLike(String value) {
            addCriterion("`status` not like", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusIn(List<String> values) {
            addCriterion("`status` in", values, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotIn(List<String> values) {
            addCriterion("`status` not in", values, "status");
            return (Criteria) this;
        }

        public Criteria andStatusBetween(String value1, String value2) {
            addCriterion("`status` between", value1, value2, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotBetween(String value1, String value2) {
            addCriterion("`status` not between", value1, value2, "status");
            return (Criteria) this;
        }

        public Criteria andTargetSlbIdIsNull() {
            addCriterion("target_slb_id is null");
            return (Criteria) this;
        }

        public Criteria andTargetSlbIdIsNotNull() {
            addCriterion("target_slb_id is not null");
            return (Criteria) this;
        }

        public Criteria andTargetSlbIdEqualTo(Long value) {
            addCriterion("target_slb_id =", value, "targetSlbId");
            return (Criteria) this;
        }

        public Criteria andTargetSlbIdEqualToColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("target_slb_id = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andTargetSlbIdNotEqualTo(Long value) {
            addCriterion("target_slb_id <>", value, "targetSlbId");
            return (Criteria) this;
        }

        public Criteria andTargetSlbIdNotEqualToColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("target_slb_id <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andTargetSlbIdGreaterThan(Long value) {
            addCriterion("target_slb_id >", value, "targetSlbId");
            return (Criteria) this;
        }

        public Criteria andTargetSlbIdGreaterThanColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("target_slb_id > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andTargetSlbIdGreaterThanOrEqualTo(Long value) {
            addCriterion("target_slb_id >=", value, "targetSlbId");
            return (Criteria) this;
        }

        public Criteria andTargetSlbIdGreaterThanOrEqualToColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("target_slb_id >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andTargetSlbIdLessThan(Long value) {
            addCriterion("target_slb_id <", value, "targetSlbId");
            return (Criteria) this;
        }

        public Criteria andTargetSlbIdLessThanColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("target_slb_id < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andTargetSlbIdLessThanOrEqualTo(Long value) {
            addCriterion("target_slb_id <=", value, "targetSlbId");
            return (Criteria) this;
        }

        public Criteria andTargetSlbIdLessThanOrEqualToColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("target_slb_id <= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andTargetSlbIdIn(List<Long> values) {
            addCriterion("target_slb_id in", values, "targetSlbId");
            return (Criteria) this;
        }

        public Criteria andTargetSlbIdNotIn(List<Long> values) {
            addCriterion("target_slb_id not in", values, "targetSlbId");
            return (Criteria) this;
        }

        public Criteria andTargetSlbIdBetween(Long value1, Long value2) {
            addCriterion("target_slb_id between", value1, value2, "targetSlbId");
            return (Criteria) this;
        }

        public Criteria andTargetSlbIdNotBetween(Long value1, Long value2) {
            addCriterion("target_slb_id not between", value1, value2, "targetSlbId");
            return (Criteria) this;
        }

        public Criteria andResourcesIsNull() {
            addCriterion("resources is null");
            return (Criteria) this;
        }

        public Criteria andResourcesIsNotNull() {
            addCriterion("resources is not null");
            return (Criteria) this;
        }

        public Criteria andResourcesEqualTo(String value) {
            addCriterion("resources =", value, "resources");
            return (Criteria) this;
        }

        public Criteria andResourcesEqualToColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("resources = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andResourcesNotEqualTo(String value) {
            addCriterion("resources <>", value, "resources");
            return (Criteria) this;
        }

        public Criteria andResourcesNotEqualToColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("resources <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andResourcesGreaterThan(String value) {
            addCriterion("resources >", value, "resources");
            return (Criteria) this;
        }

        public Criteria andResourcesGreaterThanColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("resources > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andResourcesGreaterThanOrEqualTo(String value) {
            addCriterion("resources >=", value, "resources");
            return (Criteria) this;
        }

        public Criteria andResourcesGreaterThanOrEqualToColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("resources >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andResourcesLessThan(String value) {
            addCriterion("resources <", value, "resources");
            return (Criteria) this;
        }

        public Criteria andResourcesLessThanColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("resources < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andResourcesLessThanOrEqualTo(String value) {
            addCriterion("resources <=", value, "resources");
            return (Criteria) this;
        }

        public Criteria andResourcesLessThanOrEqualToColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("resources <= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andResourcesLike(String value) {
            addCriterion("resources like", value, "resources");
            return (Criteria) this;
        }

        public Criteria andResourcesNotLike(String value) {
            addCriterion("resources not like", value, "resources");
            return (Criteria) this;
        }

        public Criteria andResourcesIn(List<String> values) {
            addCriterion("resources in", values, "resources");
            return (Criteria) this;
        }

        public Criteria andResourcesNotIn(List<String> values) {
            addCriterion("resources not in", values, "resources");
            return (Criteria) this;
        }

        public Criteria andResourcesBetween(String value1, String value2) {
            addCriterion("resources between", value1, value2, "resources");
            return (Criteria) this;
        }

        public Criteria andResourcesNotBetween(String value1, String value2) {
            addCriterion("resources not between", value1, value2, "resources");
            return (Criteria) this;
        }

        public Criteria andVersionIsNull() {
            addCriterion("version is null");
            return (Criteria) this;
        }

        public Criteria andVersionIsNotNull() {
            addCriterion("version is not null");
            return (Criteria) this;
        }

        public Criteria andVersionEqualTo(Integer value) {
            addCriterion("version =", value, "version");
            return (Criteria) this;
        }

        public Criteria andVersionEqualToColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("version = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andVersionNotEqualTo(Integer value) {
            addCriterion("version <>", value, "version");
            return (Criteria) this;
        }

        public Criteria andVersionNotEqualToColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("version <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andVersionGreaterThan(Integer value) {
            addCriterion("version >", value, "version");
            return (Criteria) this;
        }

        public Criteria andVersionGreaterThanColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("version > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andVersionGreaterThanOrEqualTo(Integer value) {
            addCriterion("version >=", value, "version");
            return (Criteria) this;
        }

        public Criteria andVersionGreaterThanOrEqualToColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("version >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andVersionLessThan(Integer value) {
            addCriterion("version <", value, "version");
            return (Criteria) this;
        }

        public Criteria andVersionLessThanColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("version < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andVersionLessThanOrEqualTo(Integer value) {
            addCriterion("version <=", value, "version");
            return (Criteria) this;
        }

        public Criteria andVersionLessThanOrEqualToColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("version <= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andVersionIn(List<Integer> values) {
            addCriterion("version in", values, "version");
            return (Criteria) this;
        }

        public Criteria andVersionNotIn(List<Integer> values) {
            addCriterion("version not in", values, "version");
            return (Criteria) this;
        }

        public Criteria andVersionBetween(Integer value1, Integer value2) {
            addCriterion("version between", value1, value2, "version");
            return (Criteria) this;
        }

        public Criteria andVersionNotBetween(Integer value1, Integer value2) {
            addCriterion("version not between", value1, value2, "version");
            return (Criteria) this;
        }

        public Criteria andSkipValidateIsNull() {
            addCriterion("skip_validate is null");
            return (Criteria) this;
        }

        public Criteria andSkipValidateIsNotNull() {
            addCriterion("skip_validate is not null");
            return (Criteria) this;
        }

        public Criteria andSkipValidateEqualTo(Boolean value) {
            addCriterion("skip_validate =", value, "skipValidate");
            return (Criteria) this;
        }

        public Criteria andSkipValidateEqualToColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("skip_validate = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andSkipValidateNotEqualTo(Boolean value) {
            addCriterion("skip_validate <>", value, "skipValidate");
            return (Criteria) this;
        }

        public Criteria andSkipValidateNotEqualToColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("skip_validate <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andSkipValidateGreaterThan(Boolean value) {
            addCriterion("skip_validate >", value, "skipValidate");
            return (Criteria) this;
        }

        public Criteria andSkipValidateGreaterThanColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("skip_validate > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andSkipValidateGreaterThanOrEqualTo(Boolean value) {
            addCriterion("skip_validate >=", value, "skipValidate");
            return (Criteria) this;
        }

        public Criteria andSkipValidateGreaterThanOrEqualToColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("skip_validate >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andSkipValidateLessThan(Boolean value) {
            addCriterion("skip_validate <", value, "skipValidate");
            return (Criteria) this;
        }

        public Criteria andSkipValidateLessThanColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("skip_validate < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andSkipValidateLessThanOrEqualTo(Boolean value) {
            addCriterion("skip_validate <=", value, "skipValidate");
            return (Criteria) this;
        }

        public Criteria andSkipValidateLessThanOrEqualToColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("skip_validate <= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andSkipValidateIn(List<Boolean> values) {
            addCriterion("skip_validate in", values, "skipValidate");
            return (Criteria) this;
        }

        public Criteria andSkipValidateNotIn(List<Boolean> values) {
            addCriterion("skip_validate not in", values, "skipValidate");
            return (Criteria) this;
        }

        public Criteria andSkipValidateBetween(Boolean value1, Boolean value2) {
            addCriterion("skip_validate between", value1, value2, "skipValidate");
            return (Criteria) this;
        }

        public Criteria andSkipValidateNotBetween(Boolean value1, Boolean value2) {
            addCriterion("skip_validate not between", value1, value2, "skipValidate");
            return (Criteria) this;
        }

        public Criteria andFailCauseIsNull() {
            addCriterion("fail_cause is null");
            return (Criteria) this;
        }

        public Criteria andFailCauseIsNotNull() {
            addCriterion("fail_cause is not null");
            return (Criteria) this;
        }

        public Criteria andFailCauseEqualTo(String value) {
            addCriterion("fail_cause =", value, "failCause");
            return (Criteria) this;
        }

        public Criteria andFailCauseEqualToColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("fail_cause = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andFailCauseNotEqualTo(String value) {
            addCriterion("fail_cause <>", value, "failCause");
            return (Criteria) this;
        }

        public Criteria andFailCauseNotEqualToColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("fail_cause <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andFailCauseGreaterThan(String value) {
            addCriterion("fail_cause >", value, "failCause");
            return (Criteria) this;
        }

        public Criteria andFailCauseGreaterThanColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("fail_cause > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andFailCauseGreaterThanOrEqualTo(String value) {
            addCriterion("fail_cause >=", value, "failCause");
            return (Criteria) this;
        }

        public Criteria andFailCauseGreaterThanOrEqualToColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("fail_cause >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andFailCauseLessThan(String value) {
            addCriterion("fail_cause <", value, "failCause");
            return (Criteria) this;
        }

        public Criteria andFailCauseLessThanColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("fail_cause < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andFailCauseLessThanOrEqualTo(String value) {
            addCriterion("fail_cause <=", value, "failCause");
            return (Criteria) this;
        }

        public Criteria andFailCauseLessThanOrEqualToColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("fail_cause <= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andFailCauseLike(String value) {
            addCriterion("fail_cause like", value, "failCause");
            return (Criteria) this;
        }

        public Criteria andFailCauseNotLike(String value) {
            addCriterion("fail_cause not like", value, "failCause");
            return (Criteria) this;
        }

        public Criteria andFailCauseIn(List<String> values) {
            addCriterion("fail_cause in", values, "failCause");
            return (Criteria) this;
        }

        public Criteria andFailCauseNotIn(List<String> values) {
            addCriterion("fail_cause not in", values, "failCause");
            return (Criteria) this;
        }

        public Criteria andFailCauseBetween(String value1, String value2) {
            addCriterion("fail_cause between", value1, value2, "failCause");
            return (Criteria) this;
        }

        public Criteria andFailCauseNotBetween(String value1, String value2) {
            addCriterion("fail_cause not between", value1, value2, "failCause");
            return (Criteria) this;
        }

        public Criteria andCreateTimeIsNull() {
            addCriterion("create_time is null");
            return (Criteria) this;
        }

        public Criteria andCreateTimeIsNotNull() {
            addCriterion("create_time is not null");
            return (Criteria) this;
        }

        public Criteria andCreateTimeEqualTo(Date value) {
            addCriterion("create_time =", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeEqualToColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("create_time = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andCreateTimeNotEqualTo(Date value) {
            addCriterion("create_time <>", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeNotEqualToColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("create_time <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andCreateTimeGreaterThan(Date value) {
            addCriterion("create_time >", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeGreaterThanColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("create_time > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andCreateTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("create_time >=", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeGreaterThanOrEqualToColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("create_time >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andCreateTimeLessThan(Date value) {
            addCriterion("create_time <", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeLessThanColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("create_time < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andCreateTimeLessThanOrEqualTo(Date value) {
            addCriterion("create_time <=", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeLessThanOrEqualToColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("create_time <= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andCreateTimeIn(List<Date> values) {
            addCriterion("create_time in", values, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeNotIn(List<Date> values) {
            addCriterion("create_time not in", values, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeBetween(Date value1, Date value2) {
            addCriterion("create_time between", value1, value2, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeNotBetween(Date value1, Date value2) {
            addCriterion("create_time not between", value1, value2, "createTime");
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

        public Criteria andDatachangeLasttimeEqualToColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("DataChange_LastTime = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeNotEqualTo(Date value) {
            addCriterion("DataChange_LastTime <>", value, "datachangeLasttime");
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeNotEqualToColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("DataChange_LastTime <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeGreaterThan(Date value) {
            addCriterion("DataChange_LastTime >", value, "datachangeLasttime");
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeGreaterThanColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("DataChange_LastTime > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeGreaterThanOrEqualTo(Date value) {
            addCriterion("DataChange_LastTime >=", value, "datachangeLasttime");
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeGreaterThanOrEqualToColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("DataChange_LastTime >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeLessThan(Date value) {
            addCriterion("DataChange_LastTime <", value, "datachangeLasttime");
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeLessThanColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("DataChange_LastTime < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeLessThanOrEqualTo(Date value) {
            addCriterion("DataChange_LastTime <=", value, "datachangeLasttime");
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeLessThanOrEqualToColumn(TaskTask.Column column) {
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

        public Criteria andDrIdIsNull() {
            addCriterion("dr_id is null");
            return (Criteria) this;
        }

        public Criteria andDrIdIsNotNull() {
            addCriterion("dr_id is not null");
            return (Criteria) this;
        }

        public Criteria andDrIdEqualTo(Long value) {
            addCriterion("dr_id =", value, "drId");
            return (Criteria) this;
        }

        public Criteria andDrIdEqualToColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("dr_id = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andDrIdNotEqualTo(Long value) {
            addCriterion("dr_id <>", value, "drId");
            return (Criteria) this;
        }

        public Criteria andDrIdNotEqualToColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("dr_id <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andDrIdGreaterThan(Long value) {
            addCriterion("dr_id >", value, "drId");
            return (Criteria) this;
        }

        public Criteria andDrIdGreaterThanColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("dr_id > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andDrIdGreaterThanOrEqualTo(Long value) {
            addCriterion("dr_id >=", value, "drId");
            return (Criteria) this;
        }

        public Criteria andDrIdGreaterThanOrEqualToColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("dr_id >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andDrIdLessThan(Long value) {
            addCriterion("dr_id <", value, "drId");
            return (Criteria) this;
        }

        public Criteria andDrIdLessThanColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("dr_id < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andDrIdLessThanOrEqualTo(Long value) {
            addCriterion("dr_id <=", value, "drId");
            return (Criteria) this;
        }

        public Criteria andDrIdLessThanOrEqualToColumn(TaskTask.Column column) {
            addCriterion(new StringBuilder("dr_id <= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andDrIdIn(List<Long> values) {
            addCriterion("dr_id in", values, "drId");
            return (Criteria) this;
        }

        public Criteria andDrIdNotIn(List<Long> values) {
            addCriterion("dr_id not in", values, "drId");
            return (Criteria) this;
        }

        public Criteria andDrIdBetween(Long value1, Long value2) {
            addCriterion("dr_id between", value1, value2, "drId");
            return (Criteria) this;
        }

        public Criteria andDrIdNotBetween(Long value1, Long value2) {
            addCriterion("dr_id not between", value1, value2, "drId");
            return (Criteria) this;
        }
    }

    public static class Criteria extends GeneratedCriteria {
        private TaskTaskExample example;

        protected Criteria(TaskTaskExample example) {
            super();
            this.example = example;
        }

        public TaskTaskExample example() {
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
        void example(com.ctrip.zeus.dao.entity.TaskTaskExample example);
    }
}