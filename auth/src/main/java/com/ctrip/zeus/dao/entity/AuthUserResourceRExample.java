package com.ctrip.zeus.dao.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AuthUserResourceRExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    protected Integer offset;

    protected Integer rows;

    public AuthUserResourceRExample() {
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

    public AuthUserResourceRExample orderBy(String orderByClause) {
        this.setOrderByClause(orderByClause);
        return this;
    }

    public AuthUserResourceRExample orderBy(String ... orderByClauses) {
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

    public AuthUserResourceRExample limit(Integer rows) {
        this.rows = rows;
        return this;
    }

    public AuthUserResourceRExample limit(Integer offset, Integer rows) {
        this.offset = offset;
        this.rows = rows;
        return this;
    }

    public AuthUserResourceRExample page(Integer page, Integer pageSize) {
        this.offset = page * pageSize;
        this.rows = pageSize;
        return this;
    }

    public static Criteria newAndCreateCriteria() {
        AuthUserResourceRExample example = new AuthUserResourceRExample();
        return example.createCriteria();
    }

    public AuthUserResourceRExample when(boolean condition, IExampleWhen then) {
        if (condition) {
            then.example(this);
        }
        return this;
    }

    public AuthUserResourceRExample when(boolean condition, IExampleWhen then, IExampleWhen otherwise) {
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

        public Criteria andIdEqualToColumn(AuthUserResourceR.Column column) {
            addCriterion(new StringBuilder("id = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andIdNotEqualTo(Long value) {
            addCriterion("id <>", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotEqualToColumn(AuthUserResourceR.Column column) {
            addCriterion(new StringBuilder("id <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andIdGreaterThan(Long value) {
            addCriterion("id >", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThanColumn(AuthUserResourceR.Column column) {
            addCriterion(new StringBuilder("id > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andIdGreaterThanOrEqualTo(Long value) {
            addCriterion("id >=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThanOrEqualToColumn(AuthUserResourceR.Column column) {
            addCriterion(new StringBuilder("id >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andIdLessThan(Long value) {
            addCriterion("id <", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThanColumn(AuthUserResourceR.Column column) {
            addCriterion(new StringBuilder("id < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andIdLessThanOrEqualTo(Long value) {
            addCriterion("id <=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThanOrEqualToColumn(AuthUserResourceR.Column column) {
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

        public Criteria andUserIdIsNull() {
            addCriterion("user_id is null");
            return (Criteria) this;
        }

        public Criteria andUserIdIsNotNull() {
            addCriterion("user_id is not null");
            return (Criteria) this;
        }

        public Criteria andUserIdEqualTo(Long value) {
            addCriterion("user_id =", value, "userId");
            return (Criteria) this;
        }

        public Criteria andUserIdEqualToColumn(AuthUserResourceR.Column column) {
            addCriterion(new StringBuilder("user_id = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andUserIdNotEqualTo(Long value) {
            addCriterion("user_id <>", value, "userId");
            return (Criteria) this;
        }

        public Criteria andUserIdNotEqualToColumn(AuthUserResourceR.Column column) {
            addCriterion(new StringBuilder("user_id <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andUserIdGreaterThan(Long value) {
            addCriterion("user_id >", value, "userId");
            return (Criteria) this;
        }

        public Criteria andUserIdGreaterThanColumn(AuthUserResourceR.Column column) {
            addCriterion(new StringBuilder("user_id > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andUserIdGreaterThanOrEqualTo(Long value) {
            addCriterion("user_id >=", value, "userId");
            return (Criteria) this;
        }

        public Criteria andUserIdGreaterThanOrEqualToColumn(AuthUserResourceR.Column column) {
            addCriterion(new StringBuilder("user_id >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andUserIdLessThan(Long value) {
            addCriterion("user_id <", value, "userId");
            return (Criteria) this;
        }

        public Criteria andUserIdLessThanColumn(AuthUserResourceR.Column column) {
            addCriterion(new StringBuilder("user_id < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andUserIdLessThanOrEqualTo(Long value) {
            addCriterion("user_id <=", value, "userId");
            return (Criteria) this;
        }

        public Criteria andUserIdLessThanOrEqualToColumn(AuthUserResourceR.Column column) {
            addCriterion(new StringBuilder("user_id <= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andUserIdIn(List<Long> values) {
            addCriterion("user_id in", values, "userId");
            return (Criteria) this;
        }

        public Criteria andUserIdNotIn(List<Long> values) {
            addCriterion("user_id not in", values, "userId");
            return (Criteria) this;
        }

        public Criteria andUserIdBetween(Long value1, Long value2) {
            addCriterion("user_id between", value1, value2, "userId");
            return (Criteria) this;
        }

        public Criteria andUserIdNotBetween(Long value1, Long value2) {
            addCriterion("user_id not between", value1, value2, "userId");
            return (Criteria) this;
        }

        public Criteria andTypeIsNull() {
            addCriterion("`type` is null");
            return (Criteria) this;
        }

        public Criteria andTypeIsNotNull() {
            addCriterion("`type` is not null");
            return (Criteria) this;
        }

        public Criteria andTypeEqualTo(String value) {
            addCriterion("`type` =", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeEqualToColumn(AuthUserResourceR.Column column) {
            addCriterion(new StringBuilder("`type` = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andTypeNotEqualTo(String value) {
            addCriterion("`type` <>", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeNotEqualToColumn(AuthUserResourceR.Column column) {
            addCriterion(new StringBuilder("`type` <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andTypeGreaterThan(String value) {
            addCriterion("`type` >", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeGreaterThanColumn(AuthUserResourceR.Column column) {
            addCriterion(new StringBuilder("`type` > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andTypeGreaterThanOrEqualTo(String value) {
            addCriterion("`type` >=", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeGreaterThanOrEqualToColumn(AuthUserResourceR.Column column) {
            addCriterion(new StringBuilder("`type` >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andTypeLessThan(String value) {
            addCriterion("`type` <", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeLessThanColumn(AuthUserResourceR.Column column) {
            addCriterion(new StringBuilder("`type` < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andTypeLessThanOrEqualTo(String value) {
            addCriterion("`type` <=", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeLessThanOrEqualToColumn(AuthUserResourceR.Column column) {
            addCriterion(new StringBuilder("`type` <= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andTypeLike(String value) {
            addCriterion("`type` like", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeNotLike(String value) {
            addCriterion("`type` not like", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeIn(List<String> values) {
            addCriterion("`type` in", values, "type");
            return (Criteria) this;
        }

        public Criteria andTypeNotIn(List<String> values) {
            addCriterion("`type` not in", values, "type");
            return (Criteria) this;
        }

        public Criteria andTypeBetween(String value1, String value2) {
            addCriterion("`type` between", value1, value2, "type");
            return (Criteria) this;
        }

        public Criteria andTypeNotBetween(String value1, String value2) {
            addCriterion("`type` not between", value1, value2, "type");
            return (Criteria) this;
        }

        public Criteria andDataIsNull() {
            addCriterion("`data` is null");
            return (Criteria) this;
        }

        public Criteria andDataIsNotNull() {
            addCriterion("`data` is not null");
            return (Criteria) this;
        }

        public Criteria andDataEqualTo(String value) {
            addCriterion("`data` =", value, "data");
            return (Criteria) this;
        }

        public Criteria andDataEqualToColumn(AuthUserResourceR.Column column) {
            addCriterion(new StringBuilder("`data` = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andDataNotEqualTo(String value) {
            addCriterion("`data` <>", value, "data");
            return (Criteria) this;
        }

        public Criteria andDataNotEqualToColumn(AuthUserResourceR.Column column) {
            addCriterion(new StringBuilder("`data` <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andDataGreaterThan(String value) {
            addCriterion("`data` >", value, "data");
            return (Criteria) this;
        }

        public Criteria andDataGreaterThanColumn(AuthUserResourceR.Column column) {
            addCriterion(new StringBuilder("`data` > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andDataGreaterThanOrEqualTo(String value) {
            addCriterion("`data` >=", value, "data");
            return (Criteria) this;
        }

        public Criteria andDataGreaterThanOrEqualToColumn(AuthUserResourceR.Column column) {
            addCriterion(new StringBuilder("`data` >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andDataLessThan(String value) {
            addCriterion("`data` <", value, "data");
            return (Criteria) this;
        }

        public Criteria andDataLessThanColumn(AuthUserResourceR.Column column) {
            addCriterion(new StringBuilder("`data` < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andDataLessThanOrEqualTo(String value) {
            addCriterion("`data` <=", value, "data");
            return (Criteria) this;
        }

        public Criteria andDataLessThanOrEqualToColumn(AuthUserResourceR.Column column) {
            addCriterion(new StringBuilder("`data` <= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andDataLike(String value) {
            addCriterion("`data` like", value, "data");
            return (Criteria) this;
        }

        public Criteria andDataNotLike(String value) {
            addCriterion("`data` not like", value, "data");
            return (Criteria) this;
        }

        public Criteria andDataIn(List<String> values) {
            addCriterion("`data` in", values, "data");
            return (Criteria) this;
        }

        public Criteria andDataNotIn(List<String> values) {
            addCriterion("`data` not in", values, "data");
            return (Criteria) this;
        }

        public Criteria andDataBetween(String value1, String value2) {
            addCriterion("`data` between", value1, value2, "data");
            return (Criteria) this;
        }

        public Criteria andDataNotBetween(String value1, String value2) {
            addCriterion("`data` not between", value1, value2, "data");
            return (Criteria) this;
        }

        public Criteria andOperationIsNull() {
            addCriterion("`operation` is null");
            return (Criteria) this;
        }

        public Criteria andOperationIsNotNull() {
            addCriterion("`operation` is not null");
            return (Criteria) this;
        }

        public Criteria andOperationEqualTo(String value) {
            addCriterion("`operation` =", value, "operation");
            return (Criteria) this;
        }

        public Criteria andOperationEqualToColumn(AuthUserResourceR.Column column) {
            addCriterion(new StringBuilder("`operation` = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andOperationNotEqualTo(String value) {
            addCriterion("`operation` <>", value, "operation");
            return (Criteria) this;
        }

        public Criteria andOperationNotEqualToColumn(AuthUserResourceR.Column column) {
            addCriterion(new StringBuilder("`operation` <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andOperationGreaterThan(String value) {
            addCriterion("`operation` >", value, "operation");
            return (Criteria) this;
        }

        public Criteria andOperationGreaterThanColumn(AuthUserResourceR.Column column) {
            addCriterion(new StringBuilder("`operation` > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andOperationGreaterThanOrEqualTo(String value) {
            addCriterion("`operation` >=", value, "operation");
            return (Criteria) this;
        }

        public Criteria andOperationGreaterThanOrEqualToColumn(AuthUserResourceR.Column column) {
            addCriterion(new StringBuilder("`operation` >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andOperationLessThan(String value) {
            addCriterion("`operation` <", value, "operation");
            return (Criteria) this;
        }

        public Criteria andOperationLessThanColumn(AuthUserResourceR.Column column) {
            addCriterion(new StringBuilder("`operation` < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andOperationLessThanOrEqualTo(String value) {
            addCriterion("`operation` <=", value, "operation");
            return (Criteria) this;
        }

        public Criteria andOperationLessThanOrEqualToColumn(AuthUserResourceR.Column column) {
            addCriterion(new StringBuilder("`operation` <= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andOperationLike(String value) {
            addCriterion("`operation` like", value, "operation");
            return (Criteria) this;
        }

        public Criteria andOperationNotLike(String value) {
            addCriterion("`operation` not like", value, "operation");
            return (Criteria) this;
        }

        public Criteria andOperationIn(List<String> values) {
            addCriterion("`operation` in", values, "operation");
            return (Criteria) this;
        }

        public Criteria andOperationNotIn(List<String> values) {
            addCriterion("`operation` not in", values, "operation");
            return (Criteria) this;
        }

        public Criteria andOperationBetween(String value1, String value2) {
            addCriterion("`operation` between", value1, value2, "operation");
            return (Criteria) this;
        }

        public Criteria andOperationNotBetween(String value1, String value2) {
            addCriterion("`operation` not between", value1, value2, "operation");
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

        public Criteria andDatachangeLasttimeEqualToColumn(AuthUserResourceR.Column column) {
            addCriterion(new StringBuilder("DataChange_LastTime = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeNotEqualTo(Date value) {
            addCriterion("DataChange_LastTime <>", value, "datachangeLasttime");
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeNotEqualToColumn(AuthUserResourceR.Column column) {
            addCriterion(new StringBuilder("DataChange_LastTime <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeGreaterThan(Date value) {
            addCriterion("DataChange_LastTime >", value, "datachangeLasttime");
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeGreaterThanColumn(AuthUserResourceR.Column column) {
            addCriterion(new StringBuilder("DataChange_LastTime > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeGreaterThanOrEqualTo(Date value) {
            addCriterion("DataChange_LastTime >=", value, "datachangeLasttime");
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeGreaterThanOrEqualToColumn(AuthUserResourceR.Column column) {
            addCriterion(new StringBuilder("DataChange_LastTime >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeLessThan(Date value) {
            addCriterion("DataChange_LastTime <", value, "datachangeLasttime");
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeLessThanColumn(AuthUserResourceR.Column column) {
            addCriterion(new StringBuilder("DataChange_LastTime < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeLessThanOrEqualTo(Date value) {
            addCriterion("DataChange_LastTime <=", value, "datachangeLasttime");
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeLessThanOrEqualToColumn(AuthUserResourceR.Column column) {
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
        private AuthUserResourceRExample example;

        protected Criteria(AuthUserResourceRExample example) {
            super();
            this.example = example;
        }

        public AuthUserResourceRExample example() {
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
        void example(com.ctrip.zeus.dao.entity.AuthUserResourceRExample example);
    }
}