package com.ctrip.zeus.dao.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CertCertificateVsRExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    protected Integer offset;

    protected Integer rows;

    public CertCertificateVsRExample() {
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

    public CertCertificateVsRExample orderBy(String orderByClause) {
        this.setOrderByClause(orderByClause);
        return this;
    }

    public CertCertificateVsRExample orderBy(String ... orderByClauses) {
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

    public CertCertificateVsRExample limit(Integer rows) {
        this.rows = rows;
        return this;
    }

    public CertCertificateVsRExample limit(Integer offset, Integer rows) {
        this.offset = offset;
        this.rows = rows;
        return this;
    }

    public CertCertificateVsRExample page(Integer page, Integer pageSize) {
        this.offset = page * pageSize;
        this.rows = pageSize;
        return this;
    }

    public static Criteria newAndCreateCriteria() {
        CertCertificateVsRExample example = new CertCertificateVsRExample();
        return example.createCriteria();
    }

    public CertCertificateVsRExample when(boolean condition, IExampleWhen then) {
        if (condition) {
            then.example(this);
        }
        return this;
    }

    public CertCertificateVsRExample when(boolean condition, IExampleWhen then, IExampleWhen otherwise) {
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

        public Criteria andIdEqualToColumn(CertCertificateVsR.Column column) {
            addCriterion(new StringBuilder("id = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andIdNotEqualTo(Long value) {
            addCriterion("id <>", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotEqualToColumn(CertCertificateVsR.Column column) {
            addCriterion(new StringBuilder("id <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andIdGreaterThan(Long value) {
            addCriterion("id >", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThanColumn(CertCertificateVsR.Column column) {
            addCriterion(new StringBuilder("id > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andIdGreaterThanOrEqualTo(Long value) {
            addCriterion("id >=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThanOrEqualToColumn(CertCertificateVsR.Column column) {
            addCriterion(new StringBuilder("id >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andIdLessThan(Long value) {
            addCriterion("id <", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThanColumn(CertCertificateVsR.Column column) {
            addCriterion(new StringBuilder("id < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andIdLessThanOrEqualTo(Long value) {
            addCriterion("id <=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThanOrEqualToColumn(CertCertificateVsR.Column column) {
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

        public Criteria andVsIdEqualToColumn(CertCertificateVsR.Column column) {
            addCriterion(new StringBuilder("vs_id = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andVsIdNotEqualTo(Long value) {
            addCriterion("vs_id <>", value, "vsId");
            return (Criteria) this;
        }

        public Criteria andVsIdNotEqualToColumn(CertCertificateVsR.Column column) {
            addCriterion(new StringBuilder("vs_id <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andVsIdGreaterThan(Long value) {
            addCriterion("vs_id >", value, "vsId");
            return (Criteria) this;
        }

        public Criteria andVsIdGreaterThanColumn(CertCertificateVsR.Column column) {
            addCriterion(new StringBuilder("vs_id > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andVsIdGreaterThanOrEqualTo(Long value) {
            addCriterion("vs_id >=", value, "vsId");
            return (Criteria) this;
        }

        public Criteria andVsIdGreaterThanOrEqualToColumn(CertCertificateVsR.Column column) {
            addCriterion(new StringBuilder("vs_id >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andVsIdLessThan(Long value) {
            addCriterion("vs_id <", value, "vsId");
            return (Criteria) this;
        }

        public Criteria andVsIdLessThanColumn(CertCertificateVsR.Column column) {
            addCriterion(new StringBuilder("vs_id < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andVsIdLessThanOrEqualTo(Long value) {
            addCriterion("vs_id <=", value, "vsId");
            return (Criteria) this;
        }

        public Criteria andVsIdLessThanOrEqualToColumn(CertCertificateVsR.Column column) {
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

        public Criteria andVersionEqualToColumn(CertCertificateVsR.Column column) {
            addCriterion(new StringBuilder("version = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andVersionNotEqualTo(Long value) {
            addCriterion("version <>", value, "version");
            return (Criteria) this;
        }

        public Criteria andVersionNotEqualToColumn(CertCertificateVsR.Column column) {
            addCriterion(new StringBuilder("version <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andVersionGreaterThan(Long value) {
            addCriterion("version >", value, "version");
            return (Criteria) this;
        }

        public Criteria andVersionGreaterThanColumn(CertCertificateVsR.Column column) {
            addCriterion(new StringBuilder("version > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andVersionGreaterThanOrEqualTo(Long value) {
            addCriterion("version >=", value, "version");
            return (Criteria) this;
        }

        public Criteria andVersionGreaterThanOrEqualToColumn(CertCertificateVsR.Column column) {
            addCriterion(new StringBuilder("version >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andVersionLessThan(Long value) {
            addCriterion("version <", value, "version");
            return (Criteria) this;
        }

        public Criteria andVersionLessThanColumn(CertCertificateVsR.Column column) {
            addCriterion(new StringBuilder("version < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andVersionLessThanOrEqualTo(Long value) {
            addCriterion("version <=", value, "version");
            return (Criteria) this;
        }

        public Criteria andVersionLessThanOrEqualToColumn(CertCertificateVsR.Column column) {
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

        public Criteria andCertIdIsNull() {
            addCriterion("cert_id is null");
            return (Criteria) this;
        }

        public Criteria andCertIdIsNotNull() {
            addCriterion("cert_id is not null");
            return (Criteria) this;
        }

        public Criteria andCertIdEqualTo(Long value) {
            addCriterion("cert_id =", value, "certId");
            return (Criteria) this;
        }

        public Criteria andCertIdEqualToColumn(CertCertificateVsR.Column column) {
            addCriterion(new StringBuilder("cert_id = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andCertIdNotEqualTo(Long value) {
            addCriterion("cert_id <>", value, "certId");
            return (Criteria) this;
        }

        public Criteria andCertIdNotEqualToColumn(CertCertificateVsR.Column column) {
            addCriterion(new StringBuilder("cert_id <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andCertIdGreaterThan(Long value) {
            addCriterion("cert_id >", value, "certId");
            return (Criteria) this;
        }

        public Criteria andCertIdGreaterThanColumn(CertCertificateVsR.Column column) {
            addCriterion(new StringBuilder("cert_id > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andCertIdGreaterThanOrEqualTo(Long value) {
            addCriterion("cert_id >=", value, "certId");
            return (Criteria) this;
        }

        public Criteria andCertIdGreaterThanOrEqualToColumn(CertCertificateVsR.Column column) {
            addCriterion(new StringBuilder("cert_id >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andCertIdLessThan(Long value) {
            addCriterion("cert_id <", value, "certId");
            return (Criteria) this;
        }

        public Criteria andCertIdLessThanColumn(CertCertificateVsR.Column column) {
            addCriterion(new StringBuilder("cert_id < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andCertIdLessThanOrEqualTo(Long value) {
            addCriterion("cert_id <=", value, "certId");
            return (Criteria) this;
        }

        public Criteria andCertIdLessThanOrEqualToColumn(CertCertificateVsR.Column column) {
            addCriterion(new StringBuilder("cert_id <= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andCertIdIn(List<Long> values) {
            addCriterion("cert_id in", values, "certId");
            return (Criteria) this;
        }

        public Criteria andCertIdNotIn(List<Long> values) {
            addCriterion("cert_id not in", values, "certId");
            return (Criteria) this;
        }

        public Criteria andCertIdBetween(Long value1, Long value2) {
            addCriterion("cert_id between", value1, value2, "certId");
            return (Criteria) this;
        }

        public Criteria andCertIdNotBetween(Long value1, Long value2) {
            addCriterion("cert_id not between", value1, value2, "certId");
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

        public Criteria andDatachangeLasttimeEqualToColumn(CertCertificateVsR.Column column) {
            addCriterion(new StringBuilder("DataChange_LastTime = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeNotEqualTo(Date value) {
            addCriterion("DataChange_LastTime <>", value, "datachangeLasttime");
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeNotEqualToColumn(CertCertificateVsR.Column column) {
            addCriterion(new StringBuilder("DataChange_LastTime <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeGreaterThan(Date value) {
            addCriterion("DataChange_LastTime >", value, "datachangeLasttime");
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeGreaterThanColumn(CertCertificateVsR.Column column) {
            addCriterion(new StringBuilder("DataChange_LastTime > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeGreaterThanOrEqualTo(Date value) {
            addCriterion("DataChange_LastTime >=", value, "datachangeLasttime");
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeGreaterThanOrEqualToColumn(CertCertificateVsR.Column column) {
            addCriterion(new StringBuilder("DataChange_LastTime >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeLessThan(Date value) {
            addCriterion("DataChange_LastTime <", value, "datachangeLasttime");
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeLessThanColumn(CertCertificateVsR.Column column) {
            addCriterion(new StringBuilder("DataChange_LastTime < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeLessThanOrEqualTo(Date value) {
            addCriterion("DataChange_LastTime <=", value, "datachangeLasttime");
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeLessThanOrEqualToColumn(CertCertificateVsR.Column column) {
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

        public Criteria andStatusEqualToColumn(CertCertificateVsR.Column column) {
            addCriterion(new StringBuilder("`status` = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andStatusNotEqualTo(String value) {
            addCriterion("`status` <>", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotEqualToColumn(CertCertificateVsR.Column column) {
            addCriterion(new StringBuilder("`status` <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andStatusGreaterThan(String value) {
            addCriterion("`status` >", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusGreaterThanColumn(CertCertificateVsR.Column column) {
            addCriterion(new StringBuilder("`status` > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andStatusGreaterThanOrEqualTo(String value) {
            addCriterion("`status` >=", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusGreaterThanOrEqualToColumn(CertCertificateVsR.Column column) {
            addCriterion(new StringBuilder("`status` >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andStatusLessThan(String value) {
            addCriterion("`status` <", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLessThanColumn(CertCertificateVsR.Column column) {
            addCriterion(new StringBuilder("`status` < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andStatusLessThanOrEqualTo(String value) {
            addCriterion("`status` <=", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLessThanOrEqualToColumn(CertCertificateVsR.Column column) {
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
    }

    public static class Criteria extends GeneratedCriteria {
        private CertCertificateVsRExample example;

        protected Criteria(CertCertificateVsRExample example) {
            super();
            this.example = example;
        }

        public CertCertificateVsRExample example() {
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
        void example(com.ctrip.zeus.dao.entity.CertCertificateVsRExample example);
    }
}