package com.ctrip.zeus.dao.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SlbVsSlbRExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    protected Integer offset;

    protected Integer rows;

    public SlbVsSlbRExample() {
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

    public SlbVsSlbRExample orderBy(String orderByClause) {
        this.setOrderByClause(orderByClause);
        return this;
    }

    public SlbVsSlbRExample orderBy(String ... orderByClauses) {
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

    public SlbVsSlbRExample limit(Integer rows) {
        this.rows = rows;
        return this;
    }

    public SlbVsSlbRExample limit(Integer offset, Integer rows) {
        this.offset = offset;
        this.rows = rows;
        return this;
    }

    public SlbVsSlbRExample page(Integer page, Integer pageSize) {
        this.offset = page * pageSize;
        this.rows = pageSize;
        return this;
    }

    public static Criteria newAndCreateCriteria() {
        SlbVsSlbRExample example = new SlbVsSlbRExample();
        return example.createCriteria();
    }

    public SlbVsSlbRExample when(boolean condition, IExampleWhen then) {
        if (condition) {
            then.example(this);
        }
        return this;
    }

    public SlbVsSlbRExample when(boolean condition, IExampleWhen then, IExampleWhen otherwise) {
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

        public Criteria andIdEqualToColumn(SlbVsSlbR.Column column) {
            addCriterion(new StringBuilder("id = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andIdNotEqualTo(Long value) {
            addCriterion("id <>", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotEqualToColumn(SlbVsSlbR.Column column) {
            addCriterion(new StringBuilder("id <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andIdGreaterThan(Long value) {
            addCriterion("id >", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThanColumn(SlbVsSlbR.Column column) {
            addCriterion(new StringBuilder("id > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andIdGreaterThanOrEqualTo(Long value) {
            addCriterion("id >=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThanOrEqualToColumn(SlbVsSlbR.Column column) {
            addCriterion(new StringBuilder("id >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andIdLessThan(Long value) {
            addCriterion("id <", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThanColumn(SlbVsSlbR.Column column) {
            addCriterion(new StringBuilder("id < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andIdLessThanOrEqualTo(Long value) {
            addCriterion("id <=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThanOrEqualToColumn(SlbVsSlbR.Column column) {
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

        public Criteria andVsIdEqualToColumn(SlbVsSlbR.Column column) {
            addCriterion(new StringBuilder("vs_id = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andVsIdNotEqualTo(Long value) {
            addCriterion("vs_id <>", value, "vsId");
            return (Criteria) this;
        }

        public Criteria andVsIdNotEqualToColumn(SlbVsSlbR.Column column) {
            addCriterion(new StringBuilder("vs_id <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andVsIdGreaterThan(Long value) {
            addCriterion("vs_id >", value, "vsId");
            return (Criteria) this;
        }

        public Criteria andVsIdGreaterThanColumn(SlbVsSlbR.Column column) {
            addCriterion(new StringBuilder("vs_id > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andVsIdGreaterThanOrEqualTo(Long value) {
            addCriterion("vs_id >=", value, "vsId");
            return (Criteria) this;
        }

        public Criteria andVsIdGreaterThanOrEqualToColumn(SlbVsSlbR.Column column) {
            addCriterion(new StringBuilder("vs_id >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andVsIdLessThan(Long value) {
            addCriterion("vs_id <", value, "vsId");
            return (Criteria) this;
        }

        public Criteria andVsIdLessThanColumn(SlbVsSlbR.Column column) {
            addCriterion(new StringBuilder("vs_id < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andVsIdLessThanOrEqualTo(Long value) {
            addCriterion("vs_id <=", value, "vsId");
            return (Criteria) this;
        }

        public Criteria andVsIdLessThanOrEqualToColumn(SlbVsSlbR.Column column) {
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

        public Criteria andSlbIdEqualToColumn(SlbVsSlbR.Column column) {
            addCriterion(new StringBuilder("slb_id = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andSlbIdNotEqualTo(Long value) {
            addCriterion("slb_id <>", value, "slbId");
            return (Criteria) this;
        }

        public Criteria andSlbIdNotEqualToColumn(SlbVsSlbR.Column column) {
            addCriterion(new StringBuilder("slb_id <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andSlbIdGreaterThan(Long value) {
            addCriterion("slb_id >", value, "slbId");
            return (Criteria) this;
        }

        public Criteria andSlbIdGreaterThanColumn(SlbVsSlbR.Column column) {
            addCriterion(new StringBuilder("slb_id > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andSlbIdGreaterThanOrEqualTo(Long value) {
            addCriterion("slb_id >=", value, "slbId");
            return (Criteria) this;
        }

        public Criteria andSlbIdGreaterThanOrEqualToColumn(SlbVsSlbR.Column column) {
            addCriterion(new StringBuilder("slb_id >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andSlbIdLessThan(Long value) {
            addCriterion("slb_id <", value, "slbId");
            return (Criteria) this;
        }

        public Criteria andSlbIdLessThanColumn(SlbVsSlbR.Column column) {
            addCriterion(new StringBuilder("slb_id < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andSlbIdLessThanOrEqualTo(Long value) {
            addCriterion("slb_id <=", value, "slbId");
            return (Criteria) this;
        }

        public Criteria andSlbIdLessThanOrEqualToColumn(SlbVsSlbR.Column column) {
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

        public Criteria andVsVersionIsNull() {
            addCriterion("vs_version is null");
            return (Criteria) this;
        }

        public Criteria andVsVersionIsNotNull() {
            addCriterion("vs_version is not null");
            return (Criteria) this;
        }

        public Criteria andVsVersionEqualTo(Integer value) {
            addCriterion("vs_version =", value, "vsVersion");
            return (Criteria) this;
        }

        public Criteria andVsVersionEqualToColumn(SlbVsSlbR.Column column) {
            addCriterion(new StringBuilder("vs_version = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andVsVersionNotEqualTo(Integer value) {
            addCriterion("vs_version <>", value, "vsVersion");
            return (Criteria) this;
        }

        public Criteria andVsVersionNotEqualToColumn(SlbVsSlbR.Column column) {
            addCriterion(new StringBuilder("vs_version <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andVsVersionGreaterThan(Integer value) {
            addCriterion("vs_version >", value, "vsVersion");
            return (Criteria) this;
        }

        public Criteria andVsVersionGreaterThanColumn(SlbVsSlbR.Column column) {
            addCriterion(new StringBuilder("vs_version > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andVsVersionGreaterThanOrEqualTo(Integer value) {
            addCriterion("vs_version >=", value, "vsVersion");
            return (Criteria) this;
        }

        public Criteria andVsVersionGreaterThanOrEqualToColumn(SlbVsSlbR.Column column) {
            addCriterion(new StringBuilder("vs_version >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andVsVersionLessThan(Integer value) {
            addCriterion("vs_version <", value, "vsVersion");
            return (Criteria) this;
        }

        public Criteria andVsVersionLessThanColumn(SlbVsSlbR.Column column) {
            addCriterion(new StringBuilder("vs_version < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andVsVersionLessThanOrEqualTo(Integer value) {
            addCriterion("vs_version <=", value, "vsVersion");
            return (Criteria) this;
        }

        public Criteria andVsVersionLessThanOrEqualToColumn(SlbVsSlbR.Column column) {
            addCriterion(new StringBuilder("vs_version <= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andVsVersionIn(List<Integer> values) {
            addCriterion("vs_version in", values, "vsVersion");
            return (Criteria) this;
        }

        public Criteria andVsVersionNotIn(List<Integer> values) {
            addCriterion("vs_version not in", values, "vsVersion");
            return (Criteria) this;
        }

        public Criteria andVsVersionBetween(Integer value1, Integer value2) {
            addCriterion("vs_version between", value1, value2, "vsVersion");
            return (Criteria) this;
        }

        public Criteria andVsVersionNotBetween(Integer value1, Integer value2) {
            addCriterion("vs_version not between", value1, value2, "vsVersion");
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

        public Criteria andDatachangeLasttimeEqualToColumn(SlbVsSlbR.Column column) {
            addCriterion(new StringBuilder("DataChange_LastTime = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeNotEqualTo(Date value) {
            addCriterion("DataChange_LastTime <>", value, "datachangeLasttime");
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeNotEqualToColumn(SlbVsSlbR.Column column) {
            addCriterion(new StringBuilder("DataChange_LastTime <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeGreaterThan(Date value) {
            addCriterion("DataChange_LastTime >", value, "datachangeLasttime");
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeGreaterThanColumn(SlbVsSlbR.Column column) {
            addCriterion(new StringBuilder("DataChange_LastTime > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeGreaterThanOrEqualTo(Date value) {
            addCriterion("DataChange_LastTime >=", value, "datachangeLasttime");
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeGreaterThanOrEqualToColumn(SlbVsSlbR.Column column) {
            addCriterion(new StringBuilder("DataChange_LastTime >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeLessThan(Date value) {
            addCriterion("DataChange_LastTime <", value, "datachangeLasttime");
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeLessThanColumn(SlbVsSlbR.Column column) {
            addCriterion(new StringBuilder("DataChange_LastTime < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeLessThanOrEqualTo(Date value) {
            addCriterion("DataChange_LastTime <=", value, "datachangeLasttime");
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeLessThanOrEqualToColumn(SlbVsSlbR.Column column) {
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
        private SlbVsSlbRExample example;

        protected Criteria(SlbVsSlbRExample example) {
            super();
            this.example = example;
        }

        public SlbVsSlbRExample example() {
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
        void example(com.ctrip.zeus.dao.entity.SlbVsSlbRExample example);
    }
}