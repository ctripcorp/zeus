package com.ctrip.zeus.dao.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SlbTrafficPolicyExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    protected Integer offset;

    protected Integer rows;

    public SlbTrafficPolicyExample() {
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

    public SlbTrafficPolicyExample orderBy(String orderByClause) {
        this.setOrderByClause(orderByClause);
        return this;
    }

    public SlbTrafficPolicyExample orderBy(String ... orderByClauses) {
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

    public SlbTrafficPolicyExample limit(Integer rows) {
        this.rows = rows;
        return this;
    }

    public SlbTrafficPolicyExample limit(Integer offset, Integer rows) {
        this.offset = offset;
        this.rows = rows;
        return this;
    }

    public SlbTrafficPolicyExample page(Integer page, Integer pageSize) {
        this.offset = page * pageSize;
        this.rows = pageSize;
        return this;
    }

    public static Criteria newAndCreateCriteria() {
        SlbTrafficPolicyExample example = new SlbTrafficPolicyExample();
        return example.createCriteria();
    }

    public SlbTrafficPolicyExample when(boolean condition, IExampleWhen then) {
        if (condition) {
            then.example(this);
        }
        return this;
    }

    public SlbTrafficPolicyExample when(boolean condition, IExampleWhen then, IExampleWhen otherwise) {
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

        public Criteria andIdEqualToColumn(SlbTrafficPolicy.Column column) {
            addCriterion(new StringBuilder("id = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andIdNotEqualTo(Long value) {
            addCriterion("id <>", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotEqualToColumn(SlbTrafficPolicy.Column column) {
            addCriterion(new StringBuilder("id <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andIdGreaterThan(Long value) {
            addCriterion("id >", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThanColumn(SlbTrafficPolicy.Column column) {
            addCriterion(new StringBuilder("id > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andIdGreaterThanOrEqualTo(Long value) {
            addCriterion("id >=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThanOrEqualToColumn(SlbTrafficPolicy.Column column) {
            addCriterion(new StringBuilder("id >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andIdLessThan(Long value) {
            addCriterion("id <", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThanColumn(SlbTrafficPolicy.Column column) {
            addCriterion(new StringBuilder("id < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andIdLessThanOrEqualTo(Long value) {
            addCriterion("id <=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThanOrEqualToColumn(SlbTrafficPolicy.Column column) {
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

        public Criteria andNameIsNull() {
            addCriterion("`name` is null");
            return (Criteria) this;
        }

        public Criteria andNameIsNotNull() {
            addCriterion("`name` is not null");
            return (Criteria) this;
        }

        public Criteria andNameEqualTo(String value) {
            addCriterion("`name` =", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameEqualToColumn(SlbTrafficPolicy.Column column) {
            addCriterion(new StringBuilder("`name` = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andNameNotEqualTo(String value) {
            addCriterion("`name` <>", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameNotEqualToColumn(SlbTrafficPolicy.Column column) {
            addCriterion(new StringBuilder("`name` <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andNameGreaterThan(String value) {
            addCriterion("`name` >", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameGreaterThanColumn(SlbTrafficPolicy.Column column) {
            addCriterion(new StringBuilder("`name` > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andNameGreaterThanOrEqualTo(String value) {
            addCriterion("`name` >=", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameGreaterThanOrEqualToColumn(SlbTrafficPolicy.Column column) {
            addCriterion(new StringBuilder("`name` >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andNameLessThan(String value) {
            addCriterion("`name` <", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameLessThanColumn(SlbTrafficPolicy.Column column) {
            addCriterion(new StringBuilder("`name` < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andNameLessThanOrEqualTo(String value) {
            addCriterion("`name` <=", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameLessThanOrEqualToColumn(SlbTrafficPolicy.Column column) {
            addCriterion(new StringBuilder("`name` <= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andNameLike(String value) {
            addCriterion("`name` like", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameNotLike(String value) {
            addCriterion("`name` not like", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameIn(List<String> values) {
            addCriterion("`name` in", values, "name");
            return (Criteria) this;
        }

        public Criteria andNameNotIn(List<String> values) {
            addCriterion("`name` not in", values, "name");
            return (Criteria) this;
        }

        public Criteria andNameBetween(String value1, String value2) {
            addCriterion("`name` between", value1, value2, "name");
            return (Criteria) this;
        }

        public Criteria andNameNotBetween(String value1, String value2) {
            addCriterion("`name` not between", value1, value2, "name");
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

        public Criteria andVersionEqualToColumn(SlbTrafficPolicy.Column column) {
            addCriterion(new StringBuilder("version = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andVersionNotEqualTo(Integer value) {
            addCriterion("version <>", value, "version");
            return (Criteria) this;
        }

        public Criteria andVersionNotEqualToColumn(SlbTrafficPolicy.Column column) {
            addCriterion(new StringBuilder("version <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andVersionGreaterThan(Integer value) {
            addCriterion("version >", value, "version");
            return (Criteria) this;
        }

        public Criteria andVersionGreaterThanColumn(SlbTrafficPolicy.Column column) {
            addCriterion(new StringBuilder("version > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andVersionGreaterThanOrEqualTo(Integer value) {
            addCriterion("version >=", value, "version");
            return (Criteria) this;
        }

        public Criteria andVersionGreaterThanOrEqualToColumn(SlbTrafficPolicy.Column column) {
            addCriterion(new StringBuilder("version >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andVersionLessThan(Integer value) {
            addCriterion("version <", value, "version");
            return (Criteria) this;
        }

        public Criteria andVersionLessThanColumn(SlbTrafficPolicy.Column column) {
            addCriterion(new StringBuilder("version < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andVersionLessThanOrEqualTo(Integer value) {
            addCriterion("version <=", value, "version");
            return (Criteria) this;
        }

        public Criteria andVersionLessThanOrEqualToColumn(SlbTrafficPolicy.Column column) {
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

        public Criteria andNxActiveVersionIsNull() {
            addCriterion("nx_active_version is null");
            return (Criteria) this;
        }

        public Criteria andNxActiveVersionIsNotNull() {
            addCriterion("nx_active_version is not null");
            return (Criteria) this;
        }

        public Criteria andNxActiveVersionEqualTo(Integer value) {
            addCriterion("nx_active_version =", value, "nxActiveVersion");
            return (Criteria) this;
        }

        public Criteria andNxActiveVersionEqualToColumn(SlbTrafficPolicy.Column column) {
            addCriterion(new StringBuilder("nx_active_version = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andNxActiveVersionNotEqualTo(Integer value) {
            addCriterion("nx_active_version <>", value, "nxActiveVersion");
            return (Criteria) this;
        }

        public Criteria andNxActiveVersionNotEqualToColumn(SlbTrafficPolicy.Column column) {
            addCriterion(new StringBuilder("nx_active_version <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andNxActiveVersionGreaterThan(Integer value) {
            addCriterion("nx_active_version >", value, "nxActiveVersion");
            return (Criteria) this;
        }

        public Criteria andNxActiveVersionGreaterThanColumn(SlbTrafficPolicy.Column column) {
            addCriterion(new StringBuilder("nx_active_version > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andNxActiveVersionGreaterThanOrEqualTo(Integer value) {
            addCriterion("nx_active_version >=", value, "nxActiveVersion");
            return (Criteria) this;
        }

        public Criteria andNxActiveVersionGreaterThanOrEqualToColumn(SlbTrafficPolicy.Column column) {
            addCriterion(new StringBuilder("nx_active_version >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andNxActiveVersionLessThan(Integer value) {
            addCriterion("nx_active_version <", value, "nxActiveVersion");
            return (Criteria) this;
        }

        public Criteria andNxActiveVersionLessThanColumn(SlbTrafficPolicy.Column column) {
            addCriterion(new StringBuilder("nx_active_version < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andNxActiveVersionLessThanOrEqualTo(Integer value) {
            addCriterion("nx_active_version <=", value, "nxActiveVersion");
            return (Criteria) this;
        }

        public Criteria andNxActiveVersionLessThanOrEqualToColumn(SlbTrafficPolicy.Column column) {
            addCriterion(new StringBuilder("nx_active_version <= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andNxActiveVersionIn(List<Integer> values) {
            addCriterion("nx_active_version in", values, "nxActiveVersion");
            return (Criteria) this;
        }

        public Criteria andNxActiveVersionNotIn(List<Integer> values) {
            addCriterion("nx_active_version not in", values, "nxActiveVersion");
            return (Criteria) this;
        }

        public Criteria andNxActiveVersionBetween(Integer value1, Integer value2) {
            addCriterion("nx_active_version between", value1, value2, "nxActiveVersion");
            return (Criteria) this;
        }

        public Criteria andNxActiveVersionNotBetween(Integer value1, Integer value2) {
            addCriterion("nx_active_version not between", value1, value2, "nxActiveVersion");
            return (Criteria) this;
        }

        public Criteria andActiveVersionIsNull() {
            addCriterion("active_version is null");
            return (Criteria) this;
        }

        public Criteria andActiveVersionIsNotNull() {
            addCriterion("active_version is not null");
            return (Criteria) this;
        }

        public Criteria andActiveVersionEqualTo(Integer value) {
            addCriterion("active_version =", value, "activeVersion");
            return (Criteria) this;
        }

        public Criteria andActiveVersionEqualToColumn(SlbTrafficPolicy.Column column) {
            addCriterion(new StringBuilder("active_version = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andActiveVersionNotEqualTo(Integer value) {
            addCriterion("active_version <>", value, "activeVersion");
            return (Criteria) this;
        }

        public Criteria andActiveVersionNotEqualToColumn(SlbTrafficPolicy.Column column) {
            addCriterion(new StringBuilder("active_version <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andActiveVersionGreaterThan(Integer value) {
            addCriterion("active_version >", value, "activeVersion");
            return (Criteria) this;
        }

        public Criteria andActiveVersionGreaterThanColumn(SlbTrafficPolicy.Column column) {
            addCriterion(new StringBuilder("active_version > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andActiveVersionGreaterThanOrEqualTo(Integer value) {
            addCriterion("active_version >=", value, "activeVersion");
            return (Criteria) this;
        }

        public Criteria andActiveVersionGreaterThanOrEqualToColumn(SlbTrafficPolicy.Column column) {
            addCriterion(new StringBuilder("active_version >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andActiveVersionLessThan(Integer value) {
            addCriterion("active_version <", value, "activeVersion");
            return (Criteria) this;
        }

        public Criteria andActiveVersionLessThanColumn(SlbTrafficPolicy.Column column) {
            addCriterion(new StringBuilder("active_version < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andActiveVersionLessThanOrEqualTo(Integer value) {
            addCriterion("active_version <=", value, "activeVersion");
            return (Criteria) this;
        }

        public Criteria andActiveVersionLessThanOrEqualToColumn(SlbTrafficPolicy.Column column) {
            addCriterion(new StringBuilder("active_version <= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andActiveVersionIn(List<Integer> values) {
            addCriterion("active_version in", values, "activeVersion");
            return (Criteria) this;
        }

        public Criteria andActiveVersionNotIn(List<Integer> values) {
            addCriterion("active_version not in", values, "activeVersion");
            return (Criteria) this;
        }

        public Criteria andActiveVersionBetween(Integer value1, Integer value2) {
            addCriterion("active_version between", value1, value2, "activeVersion");
            return (Criteria) this;
        }

        public Criteria andActiveVersionNotBetween(Integer value1, Integer value2) {
            addCriterion("active_version not between", value1, value2, "activeVersion");
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

        public Criteria andDatachangeLasttimeEqualToColumn(SlbTrafficPolicy.Column column) {
            addCriterion(new StringBuilder("DataChange_LastTime = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeNotEqualTo(Date value) {
            addCriterion("DataChange_LastTime <>", value, "datachangeLasttime");
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeNotEqualToColumn(SlbTrafficPolicy.Column column) {
            addCriterion(new StringBuilder("DataChange_LastTime <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeGreaterThan(Date value) {
            addCriterion("DataChange_LastTime >", value, "datachangeLasttime");
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeGreaterThanColumn(SlbTrafficPolicy.Column column) {
            addCriterion(new StringBuilder("DataChange_LastTime > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeGreaterThanOrEqualTo(Date value) {
            addCriterion("DataChange_LastTime >=", value, "datachangeLasttime");
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeGreaterThanOrEqualToColumn(SlbTrafficPolicy.Column column) {
            addCriterion(new StringBuilder("DataChange_LastTime >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeLessThan(Date value) {
            addCriterion("DataChange_LastTime <", value, "datachangeLasttime");
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeLessThanColumn(SlbTrafficPolicy.Column column) {
            addCriterion(new StringBuilder("DataChange_LastTime < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeLessThanOrEqualTo(Date value) {
            addCriterion("DataChange_LastTime <=", value, "datachangeLasttime");
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeLessThanOrEqualToColumn(SlbTrafficPolicy.Column column) {
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
        private SlbTrafficPolicyExample example;

        protected Criteria(SlbTrafficPolicyExample example) {
            super();
            this.example = example;
        }

        public SlbTrafficPolicyExample example() {
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
        void example(com.ctrip.zeus.dao.entity.SlbTrafficPolicyExample example);
    }
}