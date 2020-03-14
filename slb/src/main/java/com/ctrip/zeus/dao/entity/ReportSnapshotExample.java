package com.ctrip.zeus.dao.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ReportSnapshotExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    protected Integer offset;

    protected Integer rows;

    public ReportSnapshotExample() {
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

    public ReportSnapshotExample orderBy(String orderByClause) {
        this.setOrderByClause(orderByClause);
        return this;
    }

    public ReportSnapshotExample orderBy(String ... orderByClauses) {
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

    public ReportSnapshotExample limit(Integer rows) {
        this.rows = rows;
        return this;
    }

    public ReportSnapshotExample limit(Integer offset, Integer rows) {
        this.offset = offset;
        this.rows = rows;
        return this;
    }

    public ReportSnapshotExample page(Integer page, Integer pageSize) {
        this.offset = page * pageSize;
        this.rows = pageSize;
        return this;
    }

    public static Criteria newAndCreateCriteria() {
        ReportSnapshotExample example = new ReportSnapshotExample();
        return example.createCriteria();
    }

    public ReportSnapshotExample when(boolean condition, IExampleWhen then) {
        if (condition) {
            then.example(this);
        }
        return this;
    }

    public ReportSnapshotExample when(boolean condition, IExampleWhen then, IExampleWhen otherwise) {
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

        public Criteria andIdEqualToColumn(ReportSnapshot.Column column) {
            addCriterion(new StringBuilder("id = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andIdNotEqualTo(Long value) {
            addCriterion("id <>", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotEqualToColumn(ReportSnapshot.Column column) {
            addCriterion(new StringBuilder("id <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andIdGreaterThan(Long value) {
            addCriterion("id >", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThanColumn(ReportSnapshot.Column column) {
            addCriterion(new StringBuilder("id > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andIdGreaterThanOrEqualTo(Long value) {
            addCriterion("id >=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThanOrEqualToColumn(ReportSnapshot.Column column) {
            addCriterion(new StringBuilder("id >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andIdLessThan(Long value) {
            addCriterion("id <", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThanColumn(ReportSnapshot.Column column) {
            addCriterion(new StringBuilder("id < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andIdLessThanOrEqualTo(Long value) {
            addCriterion("id <=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThanOrEqualToColumn(ReportSnapshot.Column column) {
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

        public Criteria andTargetTypeIsNull() {
            addCriterion("target_type is null");
            return (Criteria) this;
        }

        public Criteria andTargetTypeIsNotNull() {
            addCriterion("target_type is not null");
            return (Criteria) this;
        }

        public Criteria andTargetTypeEqualTo(String value) {
            addCriterion("target_type =", value, "targetType");
            return (Criteria) this;
        }

        public Criteria andTargetTypeEqualToColumn(ReportSnapshot.Column column) {
            addCriterion(new StringBuilder("target_type = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andTargetTypeNotEqualTo(String value) {
            addCriterion("target_type <>", value, "targetType");
            return (Criteria) this;
        }

        public Criteria andTargetTypeNotEqualToColumn(ReportSnapshot.Column column) {
            addCriterion(new StringBuilder("target_type <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andTargetTypeGreaterThan(String value) {
            addCriterion("target_type >", value, "targetType");
            return (Criteria) this;
        }

        public Criteria andTargetTypeGreaterThanColumn(ReportSnapshot.Column column) {
            addCriterion(new StringBuilder("target_type > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andTargetTypeGreaterThanOrEqualTo(String value) {
            addCriterion("target_type >=", value, "targetType");
            return (Criteria) this;
        }

        public Criteria andTargetTypeGreaterThanOrEqualToColumn(ReportSnapshot.Column column) {
            addCriterion(new StringBuilder("target_type >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andTargetTypeLessThan(String value) {
            addCriterion("target_type <", value, "targetType");
            return (Criteria) this;
        }

        public Criteria andTargetTypeLessThanColumn(ReportSnapshot.Column column) {
            addCriterion(new StringBuilder("target_type < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andTargetTypeLessThanOrEqualTo(String value) {
            addCriterion("target_type <=", value, "targetType");
            return (Criteria) this;
        }

        public Criteria andTargetTypeLessThanOrEqualToColumn(ReportSnapshot.Column column) {
            addCriterion(new StringBuilder("target_type <= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andTargetTypeLike(String value) {
            addCriterion("target_type like", value, "targetType");
            return (Criteria) this;
        }

        public Criteria andTargetTypeNotLike(String value) {
            addCriterion("target_type not like", value, "targetType");
            return (Criteria) this;
        }

        public Criteria andTargetTypeIn(List<String> values) {
            addCriterion("target_type in", values, "targetType");
            return (Criteria) this;
        }

        public Criteria andTargetTypeNotIn(List<String> values) {
            addCriterion("target_type not in", values, "targetType");
            return (Criteria) this;
        }

        public Criteria andTargetTypeBetween(String value1, String value2) {
            addCriterion("target_type between", value1, value2, "targetType");
            return (Criteria) this;
        }

        public Criteria andTargetTypeNotBetween(String value1, String value2) {
            addCriterion("target_type not between", value1, value2, "targetType");
            return (Criteria) this;
        }

        public Criteria andAggKeyIsNull() {
            addCriterion("agg_key is null");
            return (Criteria) this;
        }

        public Criteria andAggKeyIsNotNull() {
            addCriterion("agg_key is not null");
            return (Criteria) this;
        }

        public Criteria andAggKeyEqualTo(String value) {
            addCriterion("agg_key =", value, "aggKey");
            return (Criteria) this;
        }

        public Criteria andAggKeyEqualToColumn(ReportSnapshot.Column column) {
            addCriterion(new StringBuilder("agg_key = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andAggKeyNotEqualTo(String value) {
            addCriterion("agg_key <>", value, "aggKey");
            return (Criteria) this;
        }

        public Criteria andAggKeyNotEqualToColumn(ReportSnapshot.Column column) {
            addCriterion(new StringBuilder("agg_key <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andAggKeyGreaterThan(String value) {
            addCriterion("agg_key >", value, "aggKey");
            return (Criteria) this;
        }

        public Criteria andAggKeyGreaterThanColumn(ReportSnapshot.Column column) {
            addCriterion(new StringBuilder("agg_key > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andAggKeyGreaterThanOrEqualTo(String value) {
            addCriterion("agg_key >=", value, "aggKey");
            return (Criteria) this;
        }

        public Criteria andAggKeyGreaterThanOrEqualToColumn(ReportSnapshot.Column column) {
            addCriterion(new StringBuilder("agg_key >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andAggKeyLessThan(String value) {
            addCriterion("agg_key <", value, "aggKey");
            return (Criteria) this;
        }

        public Criteria andAggKeyLessThanColumn(ReportSnapshot.Column column) {
            addCriterion(new StringBuilder("agg_key < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andAggKeyLessThanOrEqualTo(String value) {
            addCriterion("agg_key <=", value, "aggKey");
            return (Criteria) this;
        }

        public Criteria andAggKeyLessThanOrEqualToColumn(ReportSnapshot.Column column) {
            addCriterion(new StringBuilder("agg_key <= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andAggKeyLike(String value) {
            addCriterion("agg_key like", value, "aggKey");
            return (Criteria) this;
        }

        public Criteria andAggKeyNotLike(String value) {
            addCriterion("agg_key not like", value, "aggKey");
            return (Criteria) this;
        }

        public Criteria andAggKeyIn(List<String> values) {
            addCriterion("agg_key in", values, "aggKey");
            return (Criteria) this;
        }

        public Criteria andAggKeyNotIn(List<String> values) {
            addCriterion("agg_key not in", values, "aggKey");
            return (Criteria) this;
        }

        public Criteria andAggKeyBetween(String value1, String value2) {
            addCriterion("agg_key between", value1, value2, "aggKey");
            return (Criteria) this;
        }

        public Criteria andAggKeyNotBetween(String value1, String value2) {
            addCriterion("agg_key not between", value1, value2, "aggKey");
            return (Criteria) this;
        }

        public Criteria andAggValueIsNull() {
            addCriterion("agg_value is null");
            return (Criteria) this;
        }

        public Criteria andAggValueIsNotNull() {
            addCriterion("agg_value is not null");
            return (Criteria) this;
        }

        public Criteria andAggValueEqualTo(String value) {
            addCriterion("agg_value =", value, "aggValue");
            return (Criteria) this;
        }

        public Criteria andAggValueEqualToColumn(ReportSnapshot.Column column) {
            addCriterion(new StringBuilder("agg_value = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andAggValueNotEqualTo(String value) {
            addCriterion("agg_value <>", value, "aggValue");
            return (Criteria) this;
        }

        public Criteria andAggValueNotEqualToColumn(ReportSnapshot.Column column) {
            addCriterion(new StringBuilder("agg_value <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andAggValueGreaterThan(String value) {
            addCriterion("agg_value >", value, "aggValue");
            return (Criteria) this;
        }

        public Criteria andAggValueGreaterThanColumn(ReportSnapshot.Column column) {
            addCriterion(new StringBuilder("agg_value > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andAggValueGreaterThanOrEqualTo(String value) {
            addCriterion("agg_value >=", value, "aggValue");
            return (Criteria) this;
        }

        public Criteria andAggValueGreaterThanOrEqualToColumn(ReportSnapshot.Column column) {
            addCriterion(new StringBuilder("agg_value >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andAggValueLessThan(String value) {
            addCriterion("agg_value <", value, "aggValue");
            return (Criteria) this;
        }

        public Criteria andAggValueLessThanColumn(ReportSnapshot.Column column) {
            addCriterion(new StringBuilder("agg_value < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andAggValueLessThanOrEqualTo(String value) {
            addCriterion("agg_value <=", value, "aggValue");
            return (Criteria) this;
        }

        public Criteria andAggValueLessThanOrEqualToColumn(ReportSnapshot.Column column) {
            addCriterion(new StringBuilder("agg_value <= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andAggValueLike(String value) {
            addCriterion("agg_value like", value, "aggValue");
            return (Criteria) this;
        }

        public Criteria andAggValueNotLike(String value) {
            addCriterion("agg_value not like", value, "aggValue");
            return (Criteria) this;
        }

        public Criteria andAggValueIn(List<String> values) {
            addCriterion("agg_value in", values, "aggValue");
            return (Criteria) this;
        }

        public Criteria andAggValueNotIn(List<String> values) {
            addCriterion("agg_value not in", values, "aggValue");
            return (Criteria) this;
        }

        public Criteria andAggValueBetween(String value1, String value2) {
            addCriterion("agg_value between", value1, value2, "aggValue");
            return (Criteria) this;
        }

        public Criteria andAggValueNotBetween(String value1, String value2) {
            addCriterion("agg_value not between", value1, value2, "aggValue");
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

        public Criteria andCreateTimeEqualToColumn(ReportSnapshot.Column column) {
            addCriterion(new StringBuilder("create_time = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andCreateTimeNotEqualTo(Date value) {
            addCriterion("create_time <>", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeNotEqualToColumn(ReportSnapshot.Column column) {
            addCriterion(new StringBuilder("create_time <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andCreateTimeGreaterThan(Date value) {
            addCriterion("create_time >", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeGreaterThanColumn(ReportSnapshot.Column column) {
            addCriterion(new StringBuilder("create_time > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andCreateTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("create_time >=", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeGreaterThanOrEqualToColumn(ReportSnapshot.Column column) {
            addCriterion(new StringBuilder("create_time >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andCreateTimeLessThan(Date value) {
            addCriterion("create_time <", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeLessThanColumn(ReportSnapshot.Column column) {
            addCriterion(new StringBuilder("create_time < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andCreateTimeLessThanOrEqualTo(Date value) {
            addCriterion("create_time <=", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeLessThanOrEqualToColumn(ReportSnapshot.Column column) {
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

        public Criteria andDatachangeLasttimeEqualToColumn(ReportSnapshot.Column column) {
            addCriterion(new StringBuilder("DataChange_LastTime = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeNotEqualTo(Date value) {
            addCriterion("DataChange_LastTime <>", value, "datachangeLasttime");
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeNotEqualToColumn(ReportSnapshot.Column column) {
            addCriterion(new StringBuilder("DataChange_LastTime <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeGreaterThan(Date value) {
            addCriterion("DataChange_LastTime >", value, "datachangeLasttime");
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeGreaterThanColumn(ReportSnapshot.Column column) {
            addCriterion(new StringBuilder("DataChange_LastTime > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeGreaterThanOrEqualTo(Date value) {
            addCriterion("DataChange_LastTime >=", value, "datachangeLasttime");
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeGreaterThanOrEqualToColumn(ReportSnapshot.Column column) {
            addCriterion(new StringBuilder("DataChange_LastTime >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeLessThan(Date value) {
            addCriterion("DataChange_LastTime <", value, "datachangeLasttime");
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeLessThanColumn(ReportSnapshot.Column column) {
            addCriterion(new StringBuilder("DataChange_LastTime < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeLessThanOrEqualTo(Date value) {
            addCriterion("DataChange_LastTime <=", value, "datachangeLasttime");
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeLessThanOrEqualToColumn(ReportSnapshot.Column column) {
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

        public Criteria andCountIsNull() {
            addCriterion("`count` is null");
            return (Criteria) this;
        }

        public Criteria andCountIsNotNull() {
            addCriterion("`count` is not null");
            return (Criteria) this;
        }

        public Criteria andCountEqualTo(Long value) {
            addCriterion("`count` =", value, "count");
            return (Criteria) this;
        }

        public Criteria andCountEqualToColumn(ReportSnapshot.Column column) {
            addCriterion(new StringBuilder("`count` = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andCountNotEqualTo(Long value) {
            addCriterion("`count` <>", value, "count");
            return (Criteria) this;
        }

        public Criteria andCountNotEqualToColumn(ReportSnapshot.Column column) {
            addCriterion(new StringBuilder("`count` <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andCountGreaterThan(Long value) {
            addCriterion("`count` >", value, "count");
            return (Criteria) this;
        }

        public Criteria andCountGreaterThanColumn(ReportSnapshot.Column column) {
            addCriterion(new StringBuilder("`count` > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andCountGreaterThanOrEqualTo(Long value) {
            addCriterion("`count` >=", value, "count");
            return (Criteria) this;
        }

        public Criteria andCountGreaterThanOrEqualToColumn(ReportSnapshot.Column column) {
            addCriterion(new StringBuilder("`count` >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andCountLessThan(Long value) {
            addCriterion("`count` <", value, "count");
            return (Criteria) this;
        }

        public Criteria andCountLessThanColumn(ReportSnapshot.Column column) {
            addCriterion(new StringBuilder("`count` < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andCountLessThanOrEqualTo(Long value) {
            addCriterion("`count` <=", value, "count");
            return (Criteria) this;
        }

        public Criteria andCountLessThanOrEqualToColumn(ReportSnapshot.Column column) {
            addCriterion(new StringBuilder("`count` <= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andCountIn(List<Long> values) {
            addCriterion("`count` in", values, "count");
            return (Criteria) this;
        }

        public Criteria andCountNotIn(List<Long> values) {
            addCriterion("`count` not in", values, "count");
            return (Criteria) this;
        }

        public Criteria andCountBetween(Long value1, Long value2) {
            addCriterion("`count` between", value1, value2, "count");
            return (Criteria) this;
        }

        public Criteria andCountNotBetween(Long value1, Long value2) {
            addCriterion("`count` not between", value1, value2, "count");
            return (Criteria) this;
        }

        public Criteria andTargetTagIsNull() {
            addCriterion("target_tag is null");
            return (Criteria) this;
        }

        public Criteria andTargetTagIsNotNull() {
            addCriterion("target_tag is not null");
            return (Criteria) this;
        }

        public Criteria andTargetTagEqualTo(String value) {
            addCriterion("target_tag =", value, "targetTag");
            return (Criteria) this;
        }

        public Criteria andTargetTagEqualToColumn(ReportSnapshot.Column column) {
            addCriterion(new StringBuilder("target_tag = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andTargetTagNotEqualTo(String value) {
            addCriterion("target_tag <>", value, "targetTag");
            return (Criteria) this;
        }

        public Criteria andTargetTagNotEqualToColumn(ReportSnapshot.Column column) {
            addCriterion(new StringBuilder("target_tag <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andTargetTagGreaterThan(String value) {
            addCriterion("target_tag >", value, "targetTag");
            return (Criteria) this;
        }

        public Criteria andTargetTagGreaterThanColumn(ReportSnapshot.Column column) {
            addCriterion(new StringBuilder("target_tag > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andTargetTagGreaterThanOrEqualTo(String value) {
            addCriterion("target_tag >=", value, "targetTag");
            return (Criteria) this;
        }

        public Criteria andTargetTagGreaterThanOrEqualToColumn(ReportSnapshot.Column column) {
            addCriterion(new StringBuilder("target_tag >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andTargetTagLessThan(String value) {
            addCriterion("target_tag <", value, "targetTag");
            return (Criteria) this;
        }

        public Criteria andTargetTagLessThanColumn(ReportSnapshot.Column column) {
            addCriterion(new StringBuilder("target_tag < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andTargetTagLessThanOrEqualTo(String value) {
            addCriterion("target_tag <=", value, "targetTag");
            return (Criteria) this;
        }

        public Criteria andTargetTagLessThanOrEqualToColumn(ReportSnapshot.Column column) {
            addCriterion(new StringBuilder("target_tag <= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andTargetTagLike(String value) {
            addCriterion("target_tag like", value, "targetTag");
            return (Criteria) this;
        }

        public Criteria andTargetTagNotLike(String value) {
            addCriterion("target_tag not like", value, "targetTag");
            return (Criteria) this;
        }

        public Criteria andTargetTagIn(List<String> values) {
            addCriterion("target_tag in", values, "targetTag");
            return (Criteria) this;
        }

        public Criteria andTargetTagNotIn(List<String> values) {
            addCriterion("target_tag not in", values, "targetTag");
            return (Criteria) this;
        }

        public Criteria andTargetTagBetween(String value1, String value2) {
            addCriterion("target_tag between", value1, value2, "targetTag");
            return (Criteria) this;
        }

        public Criteria andTargetTagNotBetween(String value1, String value2) {
            addCriterion("target_tag not between", value1, value2, "targetTag");
            return (Criteria) this;
        }
    }

    public static class Criteria extends GeneratedCriteria {
        private ReportSnapshotExample example;

        protected Criteria(ReportSnapshotExample example) {
            super();
            this.example = example;
        }

        public ReportSnapshotExample example() {
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
        void example(com.ctrip.zeus.dao.entity.ReportSnapshotExample example);
    }
}