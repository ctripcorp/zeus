package com.ctrip.zeus.dao.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SlbBuildCommitExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    protected Integer offset;

    protected Integer rows;

    public SlbBuildCommitExample() {
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

    public SlbBuildCommitExample orderBy(String orderByClause) {
        this.setOrderByClause(orderByClause);
        return this;
    }

    public SlbBuildCommitExample orderBy(String ... orderByClauses) {
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

    public SlbBuildCommitExample limit(Integer rows) {
        this.rows = rows;
        return this;
    }

    public SlbBuildCommitExample limit(Integer offset, Integer rows) {
        this.offset = offset;
        this.rows = rows;
        return this;
    }

    public SlbBuildCommitExample page(Integer page, Integer pageSize) {
        this.offset = page * pageSize;
        this.rows = pageSize;
        return this;
    }

    public static Criteria newAndCreateCriteria() {
        SlbBuildCommitExample example = new SlbBuildCommitExample();
        return example.createCriteria();
    }

    public SlbBuildCommitExample when(boolean condition, IExampleWhen then) {
        if (condition) {
            then.example(this);
        }
        return this;
    }

    public SlbBuildCommitExample when(boolean condition, IExampleWhen then, IExampleWhen otherwise) {
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

        public Criteria andIdEqualToColumn(SlbBuildCommit.Column column) {
            addCriterion(new StringBuilder("id = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andIdNotEqualTo(Long value) {
            addCriterion("id <>", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotEqualToColumn(SlbBuildCommit.Column column) {
            addCriterion(new StringBuilder("id <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andIdGreaterThan(Long value) {
            addCriterion("id >", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThanColumn(SlbBuildCommit.Column column) {
            addCriterion(new StringBuilder("id > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andIdGreaterThanOrEqualTo(Long value) {
            addCriterion("id >=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThanOrEqualToColumn(SlbBuildCommit.Column column) {
            addCriterion(new StringBuilder("id >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andIdLessThan(Long value) {
            addCriterion("id <", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThanColumn(SlbBuildCommit.Column column) {
            addCriterion(new StringBuilder("id < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andIdLessThanOrEqualTo(Long value) {
            addCriterion("id <=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThanOrEqualToColumn(SlbBuildCommit.Column column) {
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

        public Criteria andVersionIsNull() {
            addCriterion("version is null");
            return (Criteria) this;
        }

        public Criteria andVersionIsNotNull() {
            addCriterion("version is not null");
            return (Criteria) this;
        }

        public Criteria andVersionEqualTo(Long value) {
            addCriterion("version =", value, "version");
            return (Criteria) this;
        }

        public Criteria andVersionEqualToColumn(SlbBuildCommit.Column column) {
            addCriterion(new StringBuilder("version = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andVersionNotEqualTo(Long value) {
            addCriterion("version <>", value, "version");
            return (Criteria) this;
        }

        public Criteria andVersionNotEqualToColumn(SlbBuildCommit.Column column) {
            addCriterion(new StringBuilder("version <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andVersionGreaterThan(Long value) {
            addCriterion("version >", value, "version");
            return (Criteria) this;
        }

        public Criteria andVersionGreaterThanColumn(SlbBuildCommit.Column column) {
            addCriterion(new StringBuilder("version > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andVersionGreaterThanOrEqualTo(Long value) {
            addCriterion("version >=", value, "version");
            return (Criteria) this;
        }

        public Criteria andVersionGreaterThanOrEqualToColumn(SlbBuildCommit.Column column) {
            addCriterion(new StringBuilder("version >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andVersionLessThan(Long value) {
            addCriterion("version <", value, "version");
            return (Criteria) this;
        }

        public Criteria andVersionLessThanColumn(SlbBuildCommit.Column column) {
            addCriterion(new StringBuilder("version < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andVersionLessThanOrEqualTo(Long value) {
            addCriterion("version <=", value, "version");
            return (Criteria) this;
        }

        public Criteria andVersionLessThanOrEqualToColumn(SlbBuildCommit.Column column) {
            addCriterion(new StringBuilder("version <= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andVersionIn(List<Long> values) {
            addCriterion("version in", values, "version");
            return (Criteria) this;
        }

        public Criteria andVersionNotIn(List<Long> values) {
            addCriterion("version not in", values, "version");
            return (Criteria) this;
        }

        public Criteria andVersionBetween(Long value1, Long value2) {
            addCriterion("version between", value1, value2, "version");
            return (Criteria) this;
        }

        public Criteria andVersionNotBetween(Long value1, Long value2) {
            addCriterion("version not between", value1, value2, "version");
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

        public Criteria andSlbIdEqualToColumn(SlbBuildCommit.Column column) {
            addCriterion(new StringBuilder("slb_id = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andSlbIdNotEqualTo(Long value) {
            addCriterion("slb_id <>", value, "slbId");
            return (Criteria) this;
        }

        public Criteria andSlbIdNotEqualToColumn(SlbBuildCommit.Column column) {
            addCriterion(new StringBuilder("slb_id <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andSlbIdGreaterThan(Long value) {
            addCriterion("slb_id >", value, "slbId");
            return (Criteria) this;
        }

        public Criteria andSlbIdGreaterThanColumn(SlbBuildCommit.Column column) {
            addCriterion(new StringBuilder("slb_id > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andSlbIdGreaterThanOrEqualTo(Long value) {
            addCriterion("slb_id >=", value, "slbId");
            return (Criteria) this;
        }

        public Criteria andSlbIdGreaterThanOrEqualToColumn(SlbBuildCommit.Column column) {
            addCriterion(new StringBuilder("slb_id >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andSlbIdLessThan(Long value) {
            addCriterion("slb_id <", value, "slbId");
            return (Criteria) this;
        }

        public Criteria andSlbIdLessThanColumn(SlbBuildCommit.Column column) {
            addCriterion(new StringBuilder("slb_id < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andSlbIdLessThanOrEqualTo(Long value) {
            addCriterion("slb_id <=", value, "slbId");
            return (Criteria) this;
        }

        public Criteria andSlbIdLessThanOrEqualToColumn(SlbBuildCommit.Column column) {
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

        public Criteria andVsIdsIsNull() {
            addCriterion("vs_ids is null");
            return (Criteria) this;
        }

        public Criteria andVsIdsIsNotNull() {
            addCriterion("vs_ids is not null");
            return (Criteria) this;
        }

        public Criteria andVsIdsEqualTo(String value) {
            addCriterion("vs_ids =", value, "vsIds");
            return (Criteria) this;
        }

        public Criteria andVsIdsEqualToColumn(SlbBuildCommit.Column column) {
            addCriterion(new StringBuilder("vs_ids = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andVsIdsNotEqualTo(String value) {
            addCriterion("vs_ids <>", value, "vsIds");
            return (Criteria) this;
        }

        public Criteria andVsIdsNotEqualToColumn(SlbBuildCommit.Column column) {
            addCriterion(new StringBuilder("vs_ids <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andVsIdsGreaterThan(String value) {
            addCriterion("vs_ids >", value, "vsIds");
            return (Criteria) this;
        }

        public Criteria andVsIdsGreaterThanColumn(SlbBuildCommit.Column column) {
            addCriterion(new StringBuilder("vs_ids > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andVsIdsGreaterThanOrEqualTo(String value) {
            addCriterion("vs_ids >=", value, "vsIds");
            return (Criteria) this;
        }

        public Criteria andVsIdsGreaterThanOrEqualToColumn(SlbBuildCommit.Column column) {
            addCriterion(new StringBuilder("vs_ids >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andVsIdsLessThan(String value) {
            addCriterion("vs_ids <", value, "vsIds");
            return (Criteria) this;
        }

        public Criteria andVsIdsLessThanColumn(SlbBuildCommit.Column column) {
            addCriterion(new StringBuilder("vs_ids < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andVsIdsLessThanOrEqualTo(String value) {
            addCriterion("vs_ids <=", value, "vsIds");
            return (Criteria) this;
        }

        public Criteria andVsIdsLessThanOrEqualToColumn(SlbBuildCommit.Column column) {
            addCriterion(new StringBuilder("vs_ids <= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andVsIdsLike(String value) {
            addCriterion("vs_ids like", value, "vsIds");
            return (Criteria) this;
        }

        public Criteria andVsIdsNotLike(String value) {
            addCriterion("vs_ids not like", value, "vsIds");
            return (Criteria) this;
        }

        public Criteria andVsIdsIn(List<String> values) {
            addCriterion("vs_ids in", values, "vsIds");
            return (Criteria) this;
        }

        public Criteria andVsIdsNotIn(List<String> values) {
            addCriterion("vs_ids not in", values, "vsIds");
            return (Criteria) this;
        }

        public Criteria andVsIdsBetween(String value1, String value2) {
            addCriterion("vs_ids between", value1, value2, "vsIds");
            return (Criteria) this;
        }

        public Criteria andVsIdsNotBetween(String value1, String value2) {
            addCriterion("vs_ids not between", value1, value2, "vsIds");
            return (Criteria) this;
        }

        public Criteria andGroupIdsIsNull() {
            addCriterion("group_ids is null");
            return (Criteria) this;
        }

        public Criteria andGroupIdsIsNotNull() {
            addCriterion("group_ids is not null");
            return (Criteria) this;
        }

        public Criteria andGroupIdsEqualTo(String value) {
            addCriterion("group_ids =", value, "groupIds");
            return (Criteria) this;
        }

        public Criteria andGroupIdsEqualToColumn(SlbBuildCommit.Column column) {
            addCriterion(new StringBuilder("group_ids = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andGroupIdsNotEqualTo(String value) {
            addCriterion("group_ids <>", value, "groupIds");
            return (Criteria) this;
        }

        public Criteria andGroupIdsNotEqualToColumn(SlbBuildCommit.Column column) {
            addCriterion(new StringBuilder("group_ids <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andGroupIdsGreaterThan(String value) {
            addCriterion("group_ids >", value, "groupIds");
            return (Criteria) this;
        }

        public Criteria andGroupIdsGreaterThanColumn(SlbBuildCommit.Column column) {
            addCriterion(new StringBuilder("group_ids > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andGroupIdsGreaterThanOrEqualTo(String value) {
            addCriterion("group_ids >=", value, "groupIds");
            return (Criteria) this;
        }

        public Criteria andGroupIdsGreaterThanOrEqualToColumn(SlbBuildCommit.Column column) {
            addCriterion(new StringBuilder("group_ids >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andGroupIdsLessThan(String value) {
            addCriterion("group_ids <", value, "groupIds");
            return (Criteria) this;
        }

        public Criteria andGroupIdsLessThanColumn(SlbBuildCommit.Column column) {
            addCriterion(new StringBuilder("group_ids < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andGroupIdsLessThanOrEqualTo(String value) {
            addCriterion("group_ids <=", value, "groupIds");
            return (Criteria) this;
        }

        public Criteria andGroupIdsLessThanOrEqualToColumn(SlbBuildCommit.Column column) {
            addCriterion(new StringBuilder("group_ids <= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andGroupIdsLike(String value) {
            addCriterion("group_ids like", value, "groupIds");
            return (Criteria) this;
        }

        public Criteria andGroupIdsNotLike(String value) {
            addCriterion("group_ids not like", value, "groupIds");
            return (Criteria) this;
        }

        public Criteria andGroupIdsIn(List<String> values) {
            addCriterion("group_ids in", values, "groupIds");
            return (Criteria) this;
        }

        public Criteria andGroupIdsNotIn(List<String> values) {
            addCriterion("group_ids not in", values, "groupIds");
            return (Criteria) this;
        }

        public Criteria andGroupIdsBetween(String value1, String value2) {
            addCriterion("group_ids between", value1, value2, "groupIds");
            return (Criteria) this;
        }

        public Criteria andGroupIdsNotBetween(String value1, String value2) {
            addCriterion("group_ids not between", value1, value2, "groupIds");
            return (Criteria) this;
        }

        public Criteria andTaskIdsIsNull() {
            addCriterion("task_ids is null");
            return (Criteria) this;
        }

        public Criteria andTaskIdsIsNotNull() {
            addCriterion("task_ids is not null");
            return (Criteria) this;
        }

        public Criteria andTaskIdsEqualTo(String value) {
            addCriterion("task_ids =", value, "taskIds");
            return (Criteria) this;
        }

        public Criteria andTaskIdsEqualToColumn(SlbBuildCommit.Column column) {
            addCriterion(new StringBuilder("task_ids = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andTaskIdsNotEqualTo(String value) {
            addCriterion("task_ids <>", value, "taskIds");
            return (Criteria) this;
        }

        public Criteria andTaskIdsNotEqualToColumn(SlbBuildCommit.Column column) {
            addCriterion(new StringBuilder("task_ids <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andTaskIdsGreaterThan(String value) {
            addCriterion("task_ids >", value, "taskIds");
            return (Criteria) this;
        }

        public Criteria andTaskIdsGreaterThanColumn(SlbBuildCommit.Column column) {
            addCriterion(new StringBuilder("task_ids > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andTaskIdsGreaterThanOrEqualTo(String value) {
            addCriterion("task_ids >=", value, "taskIds");
            return (Criteria) this;
        }

        public Criteria andTaskIdsGreaterThanOrEqualToColumn(SlbBuildCommit.Column column) {
            addCriterion(new StringBuilder("task_ids >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andTaskIdsLessThan(String value) {
            addCriterion("task_ids <", value, "taskIds");
            return (Criteria) this;
        }

        public Criteria andTaskIdsLessThanColumn(SlbBuildCommit.Column column) {
            addCriterion(new StringBuilder("task_ids < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andTaskIdsLessThanOrEqualTo(String value) {
            addCriterion("task_ids <=", value, "taskIds");
            return (Criteria) this;
        }

        public Criteria andTaskIdsLessThanOrEqualToColumn(SlbBuildCommit.Column column) {
            addCriterion(new StringBuilder("task_ids <= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andTaskIdsLike(String value) {
            addCriterion("task_ids like", value, "taskIds");
            return (Criteria) this;
        }

        public Criteria andTaskIdsNotLike(String value) {
            addCriterion("task_ids not like", value, "taskIds");
            return (Criteria) this;
        }

        public Criteria andTaskIdsIn(List<String> values) {
            addCriterion("task_ids in", values, "taskIds");
            return (Criteria) this;
        }

        public Criteria andTaskIdsNotIn(List<String> values) {
            addCriterion("task_ids not in", values, "taskIds");
            return (Criteria) this;
        }

        public Criteria andTaskIdsBetween(String value1, String value2) {
            addCriterion("task_ids between", value1, value2, "taskIds");
            return (Criteria) this;
        }

        public Criteria andTaskIdsNotBetween(String value1, String value2) {
            addCriterion("task_ids not between", value1, value2, "taskIds");
            return (Criteria) this;
        }

        public Criteria andCleanvsIdsIsNull() {
            addCriterion("cleanvs_ids is null");
            return (Criteria) this;
        }

        public Criteria andCleanvsIdsIsNotNull() {
            addCriterion("cleanvs_ids is not null");
            return (Criteria) this;
        }

        public Criteria andCleanvsIdsEqualTo(String value) {
            addCriterion("cleanvs_ids =", value, "cleanvsIds");
            return (Criteria) this;
        }

        public Criteria andCleanvsIdsEqualToColumn(SlbBuildCommit.Column column) {
            addCriterion(new StringBuilder("cleanvs_ids = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andCleanvsIdsNotEqualTo(String value) {
            addCriterion("cleanvs_ids <>", value, "cleanvsIds");
            return (Criteria) this;
        }

        public Criteria andCleanvsIdsNotEqualToColumn(SlbBuildCommit.Column column) {
            addCriterion(new StringBuilder("cleanvs_ids <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andCleanvsIdsGreaterThan(String value) {
            addCriterion("cleanvs_ids >", value, "cleanvsIds");
            return (Criteria) this;
        }

        public Criteria andCleanvsIdsGreaterThanColumn(SlbBuildCommit.Column column) {
            addCriterion(new StringBuilder("cleanvs_ids > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andCleanvsIdsGreaterThanOrEqualTo(String value) {
            addCriterion("cleanvs_ids >=", value, "cleanvsIds");
            return (Criteria) this;
        }

        public Criteria andCleanvsIdsGreaterThanOrEqualToColumn(SlbBuildCommit.Column column) {
            addCriterion(new StringBuilder("cleanvs_ids >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andCleanvsIdsLessThan(String value) {
            addCriterion("cleanvs_ids <", value, "cleanvsIds");
            return (Criteria) this;
        }

        public Criteria andCleanvsIdsLessThanColumn(SlbBuildCommit.Column column) {
            addCriterion(new StringBuilder("cleanvs_ids < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andCleanvsIdsLessThanOrEqualTo(String value) {
            addCriterion("cleanvs_ids <=", value, "cleanvsIds");
            return (Criteria) this;
        }

        public Criteria andCleanvsIdsLessThanOrEqualToColumn(SlbBuildCommit.Column column) {
            addCriterion(new StringBuilder("cleanvs_ids <= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andCleanvsIdsLike(String value) {
            addCriterion("cleanvs_ids like", value, "cleanvsIds");
            return (Criteria) this;
        }

        public Criteria andCleanvsIdsNotLike(String value) {
            addCriterion("cleanvs_ids not like", value, "cleanvsIds");
            return (Criteria) this;
        }

        public Criteria andCleanvsIdsIn(List<String> values) {
            addCriterion("cleanvs_ids in", values, "cleanvsIds");
            return (Criteria) this;
        }

        public Criteria andCleanvsIdsNotIn(List<String> values) {
            addCriterion("cleanvs_ids not in", values, "cleanvsIds");
            return (Criteria) this;
        }

        public Criteria andCleanvsIdsBetween(String value1, String value2) {
            addCriterion("cleanvs_ids between", value1, value2, "cleanvsIds");
            return (Criteria) this;
        }

        public Criteria andCleanvsIdsNotBetween(String value1, String value2) {
            addCriterion("cleanvs_ids not between", value1, value2, "cleanvsIds");
            return (Criteria) this;
        }

        public Criteria andTypeIsNull() {
            addCriterion("type is null");
            return (Criteria) this;
        }

        public Criteria andTypeIsNotNull() {
            addCriterion("type is not null");
            return (Criteria) this;
        }

        public Criteria andTypeEqualTo(String value) {
            addCriterion("type =", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeEqualToColumn(SlbBuildCommit.Column column) {
            addCriterion(new StringBuilder("type = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andTypeNotEqualTo(String value) {
            addCriterion("type <>", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeNotEqualToColumn(SlbBuildCommit.Column column) {
            addCriterion(new StringBuilder("type <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andTypeGreaterThan(String value) {
            addCriterion("type >", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeGreaterThanColumn(SlbBuildCommit.Column column) {
            addCriterion(new StringBuilder("type > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andTypeGreaterThanOrEqualTo(String value) {
            addCriterion("type >=", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeGreaterThanOrEqualToColumn(SlbBuildCommit.Column column) {
            addCriterion(new StringBuilder("type >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andTypeLessThan(String value) {
            addCriterion("type <", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeLessThanColumn(SlbBuildCommit.Column column) {
            addCriterion(new StringBuilder("type < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andTypeLessThanOrEqualTo(String value) {
            addCriterion("type <=", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeLessThanOrEqualToColumn(SlbBuildCommit.Column column) {
            addCriterion(new StringBuilder("type <= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andTypeLike(String value) {
            addCriterion("type like", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeNotLike(String value) {
            addCriterion("type not like", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeIn(List<String> values) {
            addCriterion("type in", values, "type");
            return (Criteria) this;
        }

        public Criteria andTypeNotIn(List<String> values) {
            addCriterion("type not in", values, "type");
            return (Criteria) this;
        }

        public Criteria andTypeBetween(String value1, String value2) {
            addCriterion("type between", value1, value2, "type");
            return (Criteria) this;
        }

        public Criteria andTypeNotBetween(String value1, String value2) {
            addCriterion("type not between", value1, value2, "type");
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

        public Criteria andDatachangeLasttimeEqualToColumn(SlbBuildCommit.Column column) {
            addCriterion(new StringBuilder("DataChange_LastTime = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeNotEqualTo(Date value) {
            addCriterion("DataChange_LastTime <>", value, "datachangeLasttime");
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeNotEqualToColumn(SlbBuildCommit.Column column) {
            addCriterion(new StringBuilder("DataChange_LastTime <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeGreaterThan(Date value) {
            addCriterion("DataChange_LastTime >", value, "datachangeLasttime");
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeGreaterThanColumn(SlbBuildCommit.Column column) {
            addCriterion(new StringBuilder("DataChange_LastTime > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeGreaterThanOrEqualTo(Date value) {
            addCriterion("DataChange_LastTime >=", value, "datachangeLasttime");
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeGreaterThanOrEqualToColumn(SlbBuildCommit.Column column) {
            addCriterion(new StringBuilder("DataChange_LastTime >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeLessThan(Date value) {
            addCriterion("DataChange_LastTime <", value, "datachangeLasttime");
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeLessThanColumn(SlbBuildCommit.Column column) {
            addCriterion(new StringBuilder("DataChange_LastTime < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeLessThanOrEqualTo(Date value) {
            addCriterion("DataChange_LastTime <=", value, "datachangeLasttime");
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeLessThanOrEqualToColumn(SlbBuildCommit.Column column) {
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
        private SlbBuildCommitExample example;

        protected Criteria(SlbBuildCommitExample example) {
            super();
            this.example = example;
        }

        public SlbBuildCommitExample example() {
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
        void example(com.ctrip.zeus.dao.entity.SlbBuildCommitExample example);
    }
}