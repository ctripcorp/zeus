package com.ctrip.zeus.dao.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SlbDrStatusRExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    protected Integer offset;

    protected Integer rows;

    public SlbDrStatusRExample() {
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

    public SlbDrStatusRExample orderBy(String orderByClause) {
        this.setOrderByClause(orderByClause);
        return this;
    }

    public SlbDrStatusRExample orderBy(String ... orderByClauses) {
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

    public SlbDrStatusRExample limit(Integer rows) {
        this.rows = rows;
        return this;
    }

    public SlbDrStatusRExample limit(Integer offset, Integer rows) {
        this.offset = offset;
        this.rows = rows;
        return this;
    }

    public SlbDrStatusRExample page(Integer page, Integer pageSize) {
        this.offset = page * pageSize;
        this.rows = pageSize;
        return this;
    }

    public static Criteria newAndCreateCriteria() {
        SlbDrStatusRExample example = new SlbDrStatusRExample();
        return example.createCriteria();
    }

    public SlbDrStatusRExample when(boolean condition, IExampleWhen then) {
        if (condition) {
            then.example(this);
        }
        return this;
    }

    public SlbDrStatusRExample when(boolean condition, IExampleWhen then, IExampleWhen otherwise) {
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

        public Criteria andIdEqualToColumn(SlbDrStatusR.Column column) {
            addCriterion(new StringBuilder("id = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andIdNotEqualTo(Long value) {
            addCriterion("id <>", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotEqualToColumn(SlbDrStatusR.Column column) {
            addCriterion(new StringBuilder("id <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andIdGreaterThan(Long value) {
            addCriterion("id >", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThanColumn(SlbDrStatusR.Column column) {
            addCriterion(new StringBuilder("id > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andIdGreaterThanOrEqualTo(Long value) {
            addCriterion("id >=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThanOrEqualToColumn(SlbDrStatusR.Column column) {
            addCriterion(new StringBuilder("id >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andIdLessThan(Long value) {
            addCriterion("id <", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThanColumn(SlbDrStatusR.Column column) {
            addCriterion(new StringBuilder("id < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andIdLessThanOrEqualTo(Long value) {
            addCriterion("id <=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThanOrEqualToColumn(SlbDrStatusR.Column column) {
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

        public Criteria andDrIdEqualToColumn(SlbDrStatusR.Column column) {
            addCriterion(new StringBuilder("dr_id = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andDrIdNotEqualTo(Long value) {
            addCriterion("dr_id <>", value, "drId");
            return (Criteria) this;
        }

        public Criteria andDrIdNotEqualToColumn(SlbDrStatusR.Column column) {
            addCriterion(new StringBuilder("dr_id <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andDrIdGreaterThan(Long value) {
            addCriterion("dr_id >", value, "drId");
            return (Criteria) this;
        }

        public Criteria andDrIdGreaterThanColumn(SlbDrStatusR.Column column) {
            addCriterion(new StringBuilder("dr_id > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andDrIdGreaterThanOrEqualTo(Long value) {
            addCriterion("dr_id >=", value, "drId");
            return (Criteria) this;
        }

        public Criteria andDrIdGreaterThanOrEqualToColumn(SlbDrStatusR.Column column) {
            addCriterion(new StringBuilder("dr_id >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andDrIdLessThan(Long value) {
            addCriterion("dr_id <", value, "drId");
            return (Criteria) this;
        }

        public Criteria andDrIdLessThanColumn(SlbDrStatusR.Column column) {
            addCriterion(new StringBuilder("dr_id < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andDrIdLessThanOrEqualTo(Long value) {
            addCriterion("dr_id <=", value, "drId");
            return (Criteria) this;
        }

        public Criteria andDrIdLessThanOrEqualToColumn(SlbDrStatusR.Column column) {
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

        public Criteria andOfflineVersionIsNull() {
            addCriterion("offline_version is null");
            return (Criteria) this;
        }

        public Criteria andOfflineVersionIsNotNull() {
            addCriterion("offline_version is not null");
            return (Criteria) this;
        }

        public Criteria andOfflineVersionEqualTo(Integer value) {
            addCriterion("offline_version =", value, "offlineVersion");
            return (Criteria) this;
        }

        public Criteria andOfflineVersionEqualToColumn(SlbDrStatusR.Column column) {
            addCriterion(new StringBuilder("offline_version = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andOfflineVersionNotEqualTo(Integer value) {
            addCriterion("offline_version <>", value, "offlineVersion");
            return (Criteria) this;
        }

        public Criteria andOfflineVersionNotEqualToColumn(SlbDrStatusR.Column column) {
            addCriterion(new StringBuilder("offline_version <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andOfflineVersionGreaterThan(Integer value) {
            addCriterion("offline_version >", value, "offlineVersion");
            return (Criteria) this;
        }

        public Criteria andOfflineVersionGreaterThanColumn(SlbDrStatusR.Column column) {
            addCriterion(new StringBuilder("offline_version > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andOfflineVersionGreaterThanOrEqualTo(Integer value) {
            addCriterion("offline_version >=", value, "offlineVersion");
            return (Criteria) this;
        }

        public Criteria andOfflineVersionGreaterThanOrEqualToColumn(SlbDrStatusR.Column column) {
            addCriterion(new StringBuilder("offline_version >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andOfflineVersionLessThan(Integer value) {
            addCriterion("offline_version <", value, "offlineVersion");
            return (Criteria) this;
        }

        public Criteria andOfflineVersionLessThanColumn(SlbDrStatusR.Column column) {
            addCriterion(new StringBuilder("offline_version < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andOfflineVersionLessThanOrEqualTo(Integer value) {
            addCriterion("offline_version <=", value, "offlineVersion");
            return (Criteria) this;
        }

        public Criteria andOfflineVersionLessThanOrEqualToColumn(SlbDrStatusR.Column column) {
            addCriterion(new StringBuilder("offline_version <= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andOfflineVersionIn(List<Integer> values) {
            addCriterion("offline_version in", values, "offlineVersion");
            return (Criteria) this;
        }

        public Criteria andOfflineVersionNotIn(List<Integer> values) {
            addCriterion("offline_version not in", values, "offlineVersion");
            return (Criteria) this;
        }

        public Criteria andOfflineVersionBetween(Integer value1, Integer value2) {
            addCriterion("offline_version between", value1, value2, "offlineVersion");
            return (Criteria) this;
        }

        public Criteria andOfflineVersionNotBetween(Integer value1, Integer value2) {
            addCriterion("offline_version not between", value1, value2, "offlineVersion");
            return (Criteria) this;
        }

        public Criteria andOnlineVersionIsNull() {
            addCriterion("online_version is null");
            return (Criteria) this;
        }

        public Criteria andOnlineVersionIsNotNull() {
            addCriterion("online_version is not null");
            return (Criteria) this;
        }

        public Criteria andOnlineVersionEqualTo(Integer value) {
            addCriterion("online_version =", value, "onlineVersion");
            return (Criteria) this;
        }

        public Criteria andOnlineVersionEqualToColumn(SlbDrStatusR.Column column) {
            addCriterion(new StringBuilder("online_version = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andOnlineVersionNotEqualTo(Integer value) {
            addCriterion("online_version <>", value, "onlineVersion");
            return (Criteria) this;
        }

        public Criteria andOnlineVersionNotEqualToColumn(SlbDrStatusR.Column column) {
            addCriterion(new StringBuilder("online_version <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andOnlineVersionGreaterThan(Integer value) {
            addCriterion("online_version >", value, "onlineVersion");
            return (Criteria) this;
        }

        public Criteria andOnlineVersionGreaterThanColumn(SlbDrStatusR.Column column) {
            addCriterion(new StringBuilder("online_version > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andOnlineVersionGreaterThanOrEqualTo(Integer value) {
            addCriterion("online_version >=", value, "onlineVersion");
            return (Criteria) this;
        }

        public Criteria andOnlineVersionGreaterThanOrEqualToColumn(SlbDrStatusR.Column column) {
            addCriterion(new StringBuilder("online_version >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andOnlineVersionLessThan(Integer value) {
            addCriterion("online_version <", value, "onlineVersion");
            return (Criteria) this;
        }

        public Criteria andOnlineVersionLessThanColumn(SlbDrStatusR.Column column) {
            addCriterion(new StringBuilder("online_version < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andOnlineVersionLessThanOrEqualTo(Integer value) {
            addCriterion("online_version <=", value, "onlineVersion");
            return (Criteria) this;
        }

        public Criteria andOnlineVersionLessThanOrEqualToColumn(SlbDrStatusR.Column column) {
            addCriterion(new StringBuilder("online_version <= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andOnlineVersionIn(List<Integer> values) {
            addCriterion("online_version in", values, "onlineVersion");
            return (Criteria) this;
        }

        public Criteria andOnlineVersionNotIn(List<Integer> values) {
            addCriterion("online_version not in", values, "onlineVersion");
            return (Criteria) this;
        }

        public Criteria andOnlineVersionBetween(Integer value1, Integer value2) {
            addCriterion("online_version between", value1, value2, "onlineVersion");
            return (Criteria) this;
        }

        public Criteria andOnlineVersionNotBetween(Integer value1, Integer value2) {
            addCriterion("online_version not between", value1, value2, "onlineVersion");
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

        public Criteria andDatachangeLasttimeEqualToColumn(SlbDrStatusR.Column column) {
            addCriterion(new StringBuilder("DataChange_LastTime = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeNotEqualTo(Date value) {
            addCriterion("DataChange_LastTime <>", value, "datachangeLasttime");
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeNotEqualToColumn(SlbDrStatusR.Column column) {
            addCriterion(new StringBuilder("DataChange_LastTime <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeGreaterThan(Date value) {
            addCriterion("DataChange_LastTime >", value, "datachangeLasttime");
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeGreaterThanColumn(SlbDrStatusR.Column column) {
            addCriterion(new StringBuilder("DataChange_LastTime > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeGreaterThanOrEqualTo(Date value) {
            addCriterion("DataChange_LastTime >=", value, "datachangeLasttime");
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeGreaterThanOrEqualToColumn(SlbDrStatusR.Column column) {
            addCriterion(new StringBuilder("DataChange_LastTime >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeLessThan(Date value) {
            addCriterion("DataChange_LastTime <", value, "datachangeLasttime");
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeLessThanColumn(SlbDrStatusR.Column column) {
            addCriterion(new StringBuilder("DataChange_LastTime < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeLessThanOrEqualTo(Date value) {
            addCriterion("DataChange_LastTime <=", value, "datachangeLasttime");
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeLessThanOrEqualToColumn(SlbDrStatusR.Column column) {
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
        private SlbDrStatusRExample example;

        protected Criteria(SlbDrStatusRExample example) {
            super();
            this.example = example;
        }

        public SlbDrStatusRExample example() {
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
        void example(com.ctrip.zeus.dao.entity.SlbDrStatusRExample example);
    }
}