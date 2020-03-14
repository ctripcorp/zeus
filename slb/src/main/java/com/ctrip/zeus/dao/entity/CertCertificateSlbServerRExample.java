package com.ctrip.zeus.dao.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CertCertificateSlbServerRExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    protected Integer offset;

    protected Integer rows;

    public CertCertificateSlbServerRExample() {
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

    public CertCertificateSlbServerRExample orderBy(String orderByClause) {
        this.setOrderByClause(orderByClause);
        return this;
    }

    public CertCertificateSlbServerRExample orderBy(String ... orderByClauses) {
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

    public CertCertificateSlbServerRExample limit(Integer rows) {
        this.rows = rows;
        return this;
    }

    public CertCertificateSlbServerRExample limit(Integer offset, Integer rows) {
        this.offset = offset;
        this.rows = rows;
        return this;
    }

    public CertCertificateSlbServerRExample page(Integer page, Integer pageSize) {
        this.offset = page * pageSize;
        this.rows = pageSize;
        return this;
    }

    public static Criteria newAndCreateCriteria() {
        CertCertificateSlbServerRExample example = new CertCertificateSlbServerRExample();
        return example.createCriteria();
    }

    public CertCertificateSlbServerRExample when(boolean condition, IExampleWhen then) {
        if (condition) {
            then.example(this);
        }
        return this;
    }

    public CertCertificateSlbServerRExample when(boolean condition, IExampleWhen then, IExampleWhen otherwise) {
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

        public Criteria andIdEqualToColumn(CertCertificateSlbServerR.Column column) {
            addCriterion(new StringBuilder("id = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andIdNotEqualTo(Long value) {
            addCriterion("id <>", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotEqualToColumn(CertCertificateSlbServerR.Column column) {
            addCriterion(new StringBuilder("id <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andIdGreaterThan(Long value) {
            addCriterion("id >", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThanColumn(CertCertificateSlbServerR.Column column) {
            addCriterion(new StringBuilder("id > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andIdGreaterThanOrEqualTo(Long value) {
            addCriterion("id >=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThanOrEqualToColumn(CertCertificateSlbServerR.Column column) {
            addCriterion(new StringBuilder("id >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andIdLessThan(Long value) {
            addCriterion("id <", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThanColumn(CertCertificateSlbServerR.Column column) {
            addCriterion(new StringBuilder("id < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andIdLessThanOrEqualTo(Long value) {
            addCriterion("id <=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThanOrEqualToColumn(CertCertificateSlbServerR.Column column) {
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

        public Criteria andCertIdEqualToColumn(CertCertificateSlbServerR.Column column) {
            addCriterion(new StringBuilder("cert_id = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andCertIdNotEqualTo(Long value) {
            addCriterion("cert_id <>", value, "certId");
            return (Criteria) this;
        }

        public Criteria andCertIdNotEqualToColumn(CertCertificateSlbServerR.Column column) {
            addCriterion(new StringBuilder("cert_id <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andCertIdGreaterThan(Long value) {
            addCriterion("cert_id >", value, "certId");
            return (Criteria) this;
        }

        public Criteria andCertIdGreaterThanColumn(CertCertificateSlbServerR.Column column) {
            addCriterion(new StringBuilder("cert_id > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andCertIdGreaterThanOrEqualTo(Long value) {
            addCriterion("cert_id >=", value, "certId");
            return (Criteria) this;
        }

        public Criteria andCertIdGreaterThanOrEqualToColumn(CertCertificateSlbServerR.Column column) {
            addCriterion(new StringBuilder("cert_id >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andCertIdLessThan(Long value) {
            addCriterion("cert_id <", value, "certId");
            return (Criteria) this;
        }

        public Criteria andCertIdLessThanColumn(CertCertificateSlbServerR.Column column) {
            addCriterion(new StringBuilder("cert_id < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andCertIdLessThanOrEqualTo(Long value) {
            addCriterion("cert_id <=", value, "certId");
            return (Criteria) this;
        }

        public Criteria andCertIdLessThanOrEqualToColumn(CertCertificateSlbServerR.Column column) {
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

        public Criteria andCommandIsNull() {
            addCriterion("command is null");
            return (Criteria) this;
        }

        public Criteria andCommandIsNotNull() {
            addCriterion("command is not null");
            return (Criteria) this;
        }

        public Criteria andCommandEqualTo(Long value) {
            addCriterion("command =", value, "command");
            return (Criteria) this;
        }

        public Criteria andCommandEqualToColumn(CertCertificateSlbServerR.Column column) {
            addCriterion(new StringBuilder("command = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andCommandNotEqualTo(Long value) {
            addCriterion("command <>", value, "command");
            return (Criteria) this;
        }

        public Criteria andCommandNotEqualToColumn(CertCertificateSlbServerR.Column column) {
            addCriterion(new StringBuilder("command <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andCommandGreaterThan(Long value) {
            addCriterion("command >", value, "command");
            return (Criteria) this;
        }

        public Criteria andCommandGreaterThanColumn(CertCertificateSlbServerR.Column column) {
            addCriterion(new StringBuilder("command > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andCommandGreaterThanOrEqualTo(Long value) {
            addCriterion("command >=", value, "command");
            return (Criteria) this;
        }

        public Criteria andCommandGreaterThanOrEqualToColumn(CertCertificateSlbServerR.Column column) {
            addCriterion(new StringBuilder("command >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andCommandLessThan(Long value) {
            addCriterion("command <", value, "command");
            return (Criteria) this;
        }

        public Criteria andCommandLessThanColumn(CertCertificateSlbServerR.Column column) {
            addCriterion(new StringBuilder("command < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andCommandLessThanOrEqualTo(Long value) {
            addCriterion("command <=", value, "command");
            return (Criteria) this;
        }

        public Criteria andCommandLessThanOrEqualToColumn(CertCertificateSlbServerR.Column column) {
            addCriterion(new StringBuilder("command <= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andCommandIn(List<Long> values) {
            addCriterion("command in", values, "command");
            return (Criteria) this;
        }

        public Criteria andCommandNotIn(List<Long> values) {
            addCriterion("command not in", values, "command");
            return (Criteria) this;
        }

        public Criteria andCommandBetween(Long value1, Long value2) {
            addCriterion("command between", value1, value2, "command");
            return (Criteria) this;
        }

        public Criteria andCommandNotBetween(Long value1, Long value2) {
            addCriterion("command not between", value1, value2, "command");
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

        public Criteria andVsIdEqualToColumn(CertCertificateSlbServerR.Column column) {
            addCriterion(new StringBuilder("vs_id = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andVsIdNotEqualTo(Long value) {
            addCriterion("vs_id <>", value, "vsId");
            return (Criteria) this;
        }

        public Criteria andVsIdNotEqualToColumn(CertCertificateSlbServerR.Column column) {
            addCriterion(new StringBuilder("vs_id <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andVsIdGreaterThan(Long value) {
            addCriterion("vs_id >", value, "vsId");
            return (Criteria) this;
        }

        public Criteria andVsIdGreaterThanColumn(CertCertificateSlbServerR.Column column) {
            addCriterion(new StringBuilder("vs_id > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andVsIdGreaterThanOrEqualTo(Long value) {
            addCriterion("vs_id >=", value, "vsId");
            return (Criteria) this;
        }

        public Criteria andVsIdGreaterThanOrEqualToColumn(CertCertificateSlbServerR.Column column) {
            addCriterion(new StringBuilder("vs_id >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andVsIdLessThan(Long value) {
            addCriterion("vs_id <", value, "vsId");
            return (Criteria) this;
        }

        public Criteria andVsIdLessThanColumn(CertCertificateSlbServerR.Column column) {
            addCriterion(new StringBuilder("vs_id < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andVsIdLessThanOrEqualTo(Long value) {
            addCriterion("vs_id <=", value, "vsId");
            return (Criteria) this;
        }

        public Criteria andVsIdLessThanOrEqualToColumn(CertCertificateSlbServerR.Column column) {
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

        public Criteria andIpIsNull() {
            addCriterion("ip is null");
            return (Criteria) this;
        }

        public Criteria andIpIsNotNull() {
            addCriterion("ip is not null");
            return (Criteria) this;
        }

        public Criteria andIpEqualTo(String value) {
            addCriterion("ip =", value, "ip");
            return (Criteria) this;
        }

        public Criteria andIpEqualToColumn(CertCertificateSlbServerR.Column column) {
            addCriterion(new StringBuilder("ip = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andIpNotEqualTo(String value) {
            addCriterion("ip <>", value, "ip");
            return (Criteria) this;
        }

        public Criteria andIpNotEqualToColumn(CertCertificateSlbServerR.Column column) {
            addCriterion(new StringBuilder("ip <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andIpGreaterThan(String value) {
            addCriterion("ip >", value, "ip");
            return (Criteria) this;
        }

        public Criteria andIpGreaterThanColumn(CertCertificateSlbServerR.Column column) {
            addCriterion(new StringBuilder("ip > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andIpGreaterThanOrEqualTo(String value) {
            addCriterion("ip >=", value, "ip");
            return (Criteria) this;
        }

        public Criteria andIpGreaterThanOrEqualToColumn(CertCertificateSlbServerR.Column column) {
            addCriterion(new StringBuilder("ip >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andIpLessThan(String value) {
            addCriterion("ip <", value, "ip");
            return (Criteria) this;
        }

        public Criteria andIpLessThanColumn(CertCertificateSlbServerR.Column column) {
            addCriterion(new StringBuilder("ip < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andIpLessThanOrEqualTo(String value) {
            addCriterion("ip <=", value, "ip");
            return (Criteria) this;
        }

        public Criteria andIpLessThanOrEqualToColumn(CertCertificateSlbServerR.Column column) {
            addCriterion(new StringBuilder("ip <= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andIpLike(String value) {
            addCriterion("ip like", value, "ip");
            return (Criteria) this;
        }

        public Criteria andIpNotLike(String value) {
            addCriterion("ip not like", value, "ip");
            return (Criteria) this;
        }

        public Criteria andIpIn(List<String> values) {
            addCriterion("ip in", values, "ip");
            return (Criteria) this;
        }

        public Criteria andIpNotIn(List<String> values) {
            addCriterion("ip not in", values, "ip");
            return (Criteria) this;
        }

        public Criteria andIpBetween(String value1, String value2) {
            addCriterion("ip between", value1, value2, "ip");
            return (Criteria) this;
        }

        public Criteria andIpNotBetween(String value1, String value2) {
            addCriterion("ip not between", value1, value2, "ip");
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

        public Criteria andDatachangeLasttimeEqualToColumn(CertCertificateSlbServerR.Column column) {
            addCriterion(new StringBuilder("DataChange_LastTime = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeNotEqualTo(Date value) {
            addCriterion("DataChange_LastTime <>", value, "datachangeLasttime");
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeNotEqualToColumn(CertCertificateSlbServerR.Column column) {
            addCriterion(new StringBuilder("DataChange_LastTime <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeGreaterThan(Date value) {
            addCriterion("DataChange_LastTime >", value, "datachangeLasttime");
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeGreaterThanColumn(CertCertificateSlbServerR.Column column) {
            addCriterion(new StringBuilder("DataChange_LastTime > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeGreaterThanOrEqualTo(Date value) {
            addCriterion("DataChange_LastTime >=", value, "datachangeLasttime");
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeGreaterThanOrEqualToColumn(CertCertificateSlbServerR.Column column) {
            addCriterion(new StringBuilder("DataChange_LastTime >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeLessThan(Date value) {
            addCriterion("DataChange_LastTime <", value, "datachangeLasttime");
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeLessThanColumn(CertCertificateSlbServerR.Column column) {
            addCriterion(new StringBuilder("DataChange_LastTime < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeLessThanOrEqualTo(Date value) {
            addCriterion("DataChange_LastTime <=", value, "datachangeLasttime");
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeLessThanOrEqualToColumn(CertCertificateSlbServerR.Column column) {
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
        private CertCertificateSlbServerRExample example;

        protected Criteria(CertCertificateSlbServerRExample example) {
            super();
            this.example = example;
        }

        public CertCertificateSlbServerRExample example() {
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
        void example(com.ctrip.zeus.dao.entity.CertCertificateSlbServerRExample example);
    }
}