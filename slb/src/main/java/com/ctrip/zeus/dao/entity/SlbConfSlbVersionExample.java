package com.ctrip.zeus.dao.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SlbConfSlbVersionExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    protected Integer offset;

    protected Integer rows;

    public SlbConfSlbVersionExample() {
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

    public SlbConfSlbVersionExample orderBy(String orderByClause) {
        this.setOrderByClause(orderByClause);
        return this;
    }

    public SlbConfSlbVersionExample orderBy(String ... orderByClauses) {
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

    public SlbConfSlbVersionExample limit(Integer rows) {
        this.rows = rows;
        return this;
    }

    public SlbConfSlbVersionExample limit(Integer offset, Integer rows) {
        this.offset = offset;
        this.rows = rows;
        return this;
    }

    public SlbConfSlbVersionExample page(Integer page, Integer pageSize) {
        this.offset = page * pageSize;
        this.rows = pageSize;
        return this;
    }

    public static Criteria newAndCreateCriteria() {
        SlbConfSlbVersionExample example = new SlbConfSlbVersionExample();
        return example.createCriteria();
    }

    public SlbConfSlbVersionExample when(boolean condition, IExampleWhen then) {
        if (condition) {
            then.example(this);
        }
        return this;
    }

    public SlbConfSlbVersionExample when(boolean condition, IExampleWhen then, IExampleWhen otherwise) {
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

        public Criteria andIdEqualToColumn(SlbConfSlbVersion.Column column) {
            addCriterion(new StringBuilder("id = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andIdNotEqualTo(Long value) {
            addCriterion("id <>", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotEqualToColumn(SlbConfSlbVersion.Column column) {
            addCriterion(new StringBuilder("id <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andIdGreaterThan(Long value) {
            addCriterion("id >", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThanColumn(SlbConfSlbVersion.Column column) {
            addCriterion(new StringBuilder("id > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andIdGreaterThanOrEqualTo(Long value) {
            addCriterion("id >=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThanOrEqualToColumn(SlbConfSlbVersion.Column column) {
            addCriterion(new StringBuilder("id >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andIdLessThan(Long value) {
            addCriterion("id <", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThanColumn(SlbConfSlbVersion.Column column) {
            addCriterion(new StringBuilder("id < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andIdLessThanOrEqualTo(Long value) {
            addCriterion("id <=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThanOrEqualToColumn(SlbConfSlbVersion.Column column) {
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

        public Criteria andSlbIdEqualToColumn(SlbConfSlbVersion.Column column) {
            addCriterion(new StringBuilder("slb_id = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andSlbIdNotEqualTo(Long value) {
            addCriterion("slb_id <>", value, "slbId");
            return (Criteria) this;
        }

        public Criteria andSlbIdNotEqualToColumn(SlbConfSlbVersion.Column column) {
            addCriterion(new StringBuilder("slb_id <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andSlbIdGreaterThan(Long value) {
            addCriterion("slb_id >", value, "slbId");
            return (Criteria) this;
        }

        public Criteria andSlbIdGreaterThanColumn(SlbConfSlbVersion.Column column) {
            addCriterion(new StringBuilder("slb_id > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andSlbIdGreaterThanOrEqualTo(Long value) {
            addCriterion("slb_id >=", value, "slbId");
            return (Criteria) this;
        }

        public Criteria andSlbIdGreaterThanOrEqualToColumn(SlbConfSlbVersion.Column column) {
            addCriterion(new StringBuilder("slb_id >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andSlbIdLessThan(Long value) {
            addCriterion("slb_id <", value, "slbId");
            return (Criteria) this;
        }

        public Criteria andSlbIdLessThanColumn(SlbConfSlbVersion.Column column) {
            addCriterion(new StringBuilder("slb_id < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andSlbIdLessThanOrEqualTo(Long value) {
            addCriterion("slb_id <=", value, "slbId");
            return (Criteria) this;
        }

        public Criteria andSlbIdLessThanOrEqualToColumn(SlbConfSlbVersion.Column column) {
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

        public Criteria andPreviousVersionIsNull() {
            addCriterion("previous_version is null");
            return (Criteria) this;
        }

        public Criteria andPreviousVersionIsNotNull() {
            addCriterion("previous_version is not null");
            return (Criteria) this;
        }

        public Criteria andPreviousVersionEqualTo(Long value) {
            addCriterion("previous_version =", value, "previousVersion");
            return (Criteria) this;
        }

        public Criteria andPreviousVersionEqualToColumn(SlbConfSlbVersion.Column column) {
            addCriterion(new StringBuilder("previous_version = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andPreviousVersionNotEqualTo(Long value) {
            addCriterion("previous_version <>", value, "previousVersion");
            return (Criteria) this;
        }

        public Criteria andPreviousVersionNotEqualToColumn(SlbConfSlbVersion.Column column) {
            addCriterion(new StringBuilder("previous_version <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andPreviousVersionGreaterThan(Long value) {
            addCriterion("previous_version >", value, "previousVersion");
            return (Criteria) this;
        }

        public Criteria andPreviousVersionGreaterThanColumn(SlbConfSlbVersion.Column column) {
            addCriterion(new StringBuilder("previous_version > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andPreviousVersionGreaterThanOrEqualTo(Long value) {
            addCriterion("previous_version >=", value, "previousVersion");
            return (Criteria) this;
        }

        public Criteria andPreviousVersionGreaterThanOrEqualToColumn(SlbConfSlbVersion.Column column) {
            addCriterion(new StringBuilder("previous_version >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andPreviousVersionLessThan(Long value) {
            addCriterion("previous_version <", value, "previousVersion");
            return (Criteria) this;
        }

        public Criteria andPreviousVersionLessThanColumn(SlbConfSlbVersion.Column column) {
            addCriterion(new StringBuilder("previous_version < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andPreviousVersionLessThanOrEqualTo(Long value) {
            addCriterion("previous_version <=", value, "previousVersion");
            return (Criteria) this;
        }

        public Criteria andPreviousVersionLessThanOrEqualToColumn(SlbConfSlbVersion.Column column) {
            addCriterion(new StringBuilder("previous_version <= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andPreviousVersionIn(List<Long> values) {
            addCriterion("previous_version in", values, "previousVersion");
            return (Criteria) this;
        }

        public Criteria andPreviousVersionNotIn(List<Long> values) {
            addCriterion("previous_version not in", values, "previousVersion");
            return (Criteria) this;
        }

        public Criteria andPreviousVersionBetween(Long value1, Long value2) {
            addCriterion("previous_version between", value1, value2, "previousVersion");
            return (Criteria) this;
        }

        public Criteria andPreviousVersionNotBetween(Long value1, Long value2) {
            addCriterion("previous_version not between", value1, value2, "previousVersion");
            return (Criteria) this;
        }

        public Criteria andCurrentVersionIsNull() {
            addCriterion("current_version is null");
            return (Criteria) this;
        }

        public Criteria andCurrentVersionIsNotNull() {
            addCriterion("current_version is not null");
            return (Criteria) this;
        }

        public Criteria andCurrentVersionEqualTo(Long value) {
            addCriterion("current_version =", value, "currentVersion");
            return (Criteria) this;
        }

        public Criteria andCurrentVersionEqualToColumn(SlbConfSlbVersion.Column column) {
            addCriterion(new StringBuilder("current_version = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andCurrentVersionNotEqualTo(Long value) {
            addCriterion("current_version <>", value, "currentVersion");
            return (Criteria) this;
        }

        public Criteria andCurrentVersionNotEqualToColumn(SlbConfSlbVersion.Column column) {
            addCriterion(new StringBuilder("current_version <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andCurrentVersionGreaterThan(Long value) {
            addCriterion("current_version >", value, "currentVersion");
            return (Criteria) this;
        }

        public Criteria andCurrentVersionGreaterThanColumn(SlbConfSlbVersion.Column column) {
            addCriterion(new StringBuilder("current_version > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andCurrentVersionGreaterThanOrEqualTo(Long value) {
            addCriterion("current_version >=", value, "currentVersion");
            return (Criteria) this;
        }

        public Criteria andCurrentVersionGreaterThanOrEqualToColumn(SlbConfSlbVersion.Column column) {
            addCriterion(new StringBuilder("current_version >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andCurrentVersionLessThan(Long value) {
            addCriterion("current_version <", value, "currentVersion");
            return (Criteria) this;
        }

        public Criteria andCurrentVersionLessThanColumn(SlbConfSlbVersion.Column column) {
            addCriterion(new StringBuilder("current_version < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andCurrentVersionLessThanOrEqualTo(Long value) {
            addCriterion("current_version <=", value, "currentVersion");
            return (Criteria) this;
        }

        public Criteria andCurrentVersionLessThanOrEqualToColumn(SlbConfSlbVersion.Column column) {
            addCriterion(new StringBuilder("current_version <= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andCurrentVersionIn(List<Long> values) {
            addCriterion("current_version in", values, "currentVersion");
            return (Criteria) this;
        }

        public Criteria andCurrentVersionNotIn(List<Long> values) {
            addCriterion("current_version not in", values, "currentVersion");
            return (Criteria) this;
        }

        public Criteria andCurrentVersionBetween(Long value1, Long value2) {
            addCriterion("current_version between", value1, value2, "currentVersion");
            return (Criteria) this;
        }

        public Criteria andCurrentVersionNotBetween(Long value1, Long value2) {
            addCriterion("current_version not between", value1, value2, "currentVersion");
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

        public Criteria andDatachangeLasttimeEqualToColumn(SlbConfSlbVersion.Column column) {
            addCriterion(new StringBuilder("DataChange_LastTime = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeNotEqualTo(Date value) {
            addCriterion("DataChange_LastTime <>", value, "datachangeLasttime");
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeNotEqualToColumn(SlbConfSlbVersion.Column column) {
            addCriterion(new StringBuilder("DataChange_LastTime <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeGreaterThan(Date value) {
            addCriterion("DataChange_LastTime >", value, "datachangeLasttime");
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeGreaterThanColumn(SlbConfSlbVersion.Column column) {
            addCriterion(new StringBuilder("DataChange_LastTime > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeGreaterThanOrEqualTo(Date value) {
            addCriterion("DataChange_LastTime >=", value, "datachangeLasttime");
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeGreaterThanOrEqualToColumn(SlbConfSlbVersion.Column column) {
            addCriterion(new StringBuilder("DataChange_LastTime >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeLessThan(Date value) {
            addCriterion("DataChange_LastTime <", value, "datachangeLasttime");
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeLessThanColumn(SlbConfSlbVersion.Column column) {
            addCriterion(new StringBuilder("DataChange_LastTime < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeLessThanOrEqualTo(Date value) {
            addCriterion("DataChange_LastTime <=", value, "datachangeLasttime");
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeLessThanOrEqualToColumn(SlbConfSlbVersion.Column column) {
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
        private SlbConfSlbVersionExample example;

        protected Criteria(SlbConfSlbVersionExample example) {
            super();
            this.example = example;
        }

        public SlbConfSlbVersionExample example() {
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
        void example(com.ctrip.zeus.dao.entity.SlbConfSlbVersionExample example);
    }
}