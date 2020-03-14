package com.ctrip.zeus.dao.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MessageQueueExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    protected Integer offset;

    protected Integer rows;

    public MessageQueueExample() {
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

    public MessageQueueExample orderBy(String orderByClause) {
        this.setOrderByClause(orderByClause);
        return this;
    }

    public MessageQueueExample orderBy(String ... orderByClauses) {
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

    public MessageQueueExample limit(Integer rows) {
        this.rows = rows;
        return this;
    }

    public MessageQueueExample limit(Integer offset, Integer rows) {
        this.offset = offset;
        this.rows = rows;
        return this;
    }

    public MessageQueueExample page(Integer page, Integer pageSize) {
        this.offset = page * pageSize;
        this.rows = pageSize;
        return this;
    }

    public static Criteria newAndCreateCriteria() {
        MessageQueueExample example = new MessageQueueExample();
        return example.createCriteria();
    }

    public MessageQueueExample when(boolean condition, IExampleWhen then) {
        if (condition) {
            then.example(this);
        }
        return this;
    }

    public MessageQueueExample when(boolean condition, IExampleWhen then, IExampleWhen otherwise) {
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

        public Criteria andIdEqualToColumn(MessageQueue.Column column) {
            addCriterion(new StringBuilder("id = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andIdNotEqualTo(Long value) {
            addCriterion("id <>", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotEqualToColumn(MessageQueue.Column column) {
            addCriterion(new StringBuilder("id <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andIdGreaterThan(Long value) {
            addCriterion("id >", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThanColumn(MessageQueue.Column column) {
            addCriterion(new StringBuilder("id > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andIdGreaterThanOrEqualTo(Long value) {
            addCriterion("id >=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThanOrEqualToColumn(MessageQueue.Column column) {
            addCriterion(new StringBuilder("id >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andIdLessThan(Long value) {
            addCriterion("id <", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThanColumn(MessageQueue.Column column) {
            addCriterion(new StringBuilder("id < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andIdLessThanOrEqualTo(Long value) {
            addCriterion("id <=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThanOrEqualToColumn(MessageQueue.Column column) {
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

        public Criteria andPerformerIsNull() {
            addCriterion("performer is null");
            return (Criteria) this;
        }

        public Criteria andPerformerIsNotNull() {
            addCriterion("performer is not null");
            return (Criteria) this;
        }

        public Criteria andPerformerEqualTo(String value) {
            addCriterion("performer =", value, "performer");
            return (Criteria) this;
        }

        public Criteria andPerformerEqualToColumn(MessageQueue.Column column) {
            addCriterion(new StringBuilder("performer = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andPerformerNotEqualTo(String value) {
            addCriterion("performer <>", value, "performer");
            return (Criteria) this;
        }

        public Criteria andPerformerNotEqualToColumn(MessageQueue.Column column) {
            addCriterion(new StringBuilder("performer <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andPerformerGreaterThan(String value) {
            addCriterion("performer >", value, "performer");
            return (Criteria) this;
        }

        public Criteria andPerformerGreaterThanColumn(MessageQueue.Column column) {
            addCriterion(new StringBuilder("performer > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andPerformerGreaterThanOrEqualTo(String value) {
            addCriterion("performer >=", value, "performer");
            return (Criteria) this;
        }

        public Criteria andPerformerGreaterThanOrEqualToColumn(MessageQueue.Column column) {
            addCriterion(new StringBuilder("performer >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andPerformerLessThan(String value) {
            addCriterion("performer <", value, "performer");
            return (Criteria) this;
        }

        public Criteria andPerformerLessThanColumn(MessageQueue.Column column) {
            addCriterion(new StringBuilder("performer < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andPerformerLessThanOrEqualTo(String value) {
            addCriterion("performer <=", value, "performer");
            return (Criteria) this;
        }

        public Criteria andPerformerLessThanOrEqualToColumn(MessageQueue.Column column) {
            addCriterion(new StringBuilder("performer <= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andPerformerLike(String value) {
            addCriterion("performer like", value, "performer");
            return (Criteria) this;
        }

        public Criteria andPerformerNotLike(String value) {
            addCriterion("performer not like", value, "performer");
            return (Criteria) this;
        }

        public Criteria andPerformerIn(List<String> values) {
            addCriterion("performer in", values, "performer");
            return (Criteria) this;
        }

        public Criteria andPerformerNotIn(List<String> values) {
            addCriterion("performer not in", values, "performer");
            return (Criteria) this;
        }

        public Criteria andPerformerBetween(String value1, String value2) {
            addCriterion("performer between", value1, value2, "performer");
            return (Criteria) this;
        }

        public Criteria andPerformerNotBetween(String value1, String value2) {
            addCriterion("performer not between", value1, value2, "performer");
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

        public Criteria andTypeEqualToColumn(MessageQueue.Column column) {
            addCriterion(new StringBuilder("type = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andTypeNotEqualTo(String value) {
            addCriterion("type <>", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeNotEqualToColumn(MessageQueue.Column column) {
            addCriterion(new StringBuilder("type <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andTypeGreaterThan(String value) {
            addCriterion("type >", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeGreaterThanColumn(MessageQueue.Column column) {
            addCriterion(new StringBuilder("type > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andTypeGreaterThanOrEqualTo(String value) {
            addCriterion("type >=", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeGreaterThanOrEqualToColumn(MessageQueue.Column column) {
            addCriterion(new StringBuilder("type >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andTypeLessThan(String value) {
            addCriterion("type <", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeLessThanColumn(MessageQueue.Column column) {
            addCriterion(new StringBuilder("type < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andTypeLessThanOrEqualTo(String value) {
            addCriterion("type <=", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeLessThanOrEqualToColumn(MessageQueue.Column column) {
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

        public Criteria andTargetIdIsNull() {
            addCriterion("target_id is null");
            return (Criteria) this;
        }

        public Criteria andTargetIdIsNotNull() {
            addCriterion("target_id is not null");
            return (Criteria) this;
        }

        public Criteria andTargetIdEqualTo(Long value) {
            addCriterion("target_id =", value, "targetId");
            return (Criteria) this;
        }

        public Criteria andTargetIdEqualToColumn(MessageQueue.Column column) {
            addCriterion(new StringBuilder("target_id = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andTargetIdNotEqualTo(Long value) {
            addCriterion("target_id <>", value, "targetId");
            return (Criteria) this;
        }

        public Criteria andTargetIdNotEqualToColumn(MessageQueue.Column column) {
            addCriterion(new StringBuilder("target_id <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andTargetIdGreaterThan(Long value) {
            addCriterion("target_id >", value, "targetId");
            return (Criteria) this;
        }

        public Criteria andTargetIdGreaterThanColumn(MessageQueue.Column column) {
            addCriterion(new StringBuilder("target_id > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andTargetIdGreaterThanOrEqualTo(Long value) {
            addCriterion("target_id >=", value, "targetId");
            return (Criteria) this;
        }

        public Criteria andTargetIdGreaterThanOrEqualToColumn(MessageQueue.Column column) {
            addCriterion(new StringBuilder("target_id >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andTargetIdLessThan(Long value) {
            addCriterion("target_id <", value, "targetId");
            return (Criteria) this;
        }

        public Criteria andTargetIdLessThanColumn(MessageQueue.Column column) {
            addCriterion(new StringBuilder("target_id < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andTargetIdLessThanOrEqualTo(Long value) {
            addCriterion("target_id <=", value, "targetId");
            return (Criteria) this;
        }

        public Criteria andTargetIdLessThanOrEqualToColumn(MessageQueue.Column column) {
            addCriterion(new StringBuilder("target_id <= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andTargetIdIn(List<Long> values) {
            addCriterion("target_id in", values, "targetId");
            return (Criteria) this;
        }

        public Criteria andTargetIdNotIn(List<Long> values) {
            addCriterion("target_id not in", values, "targetId");
            return (Criteria) this;
        }

        public Criteria andTargetIdBetween(Long value1, Long value2) {
            addCriterion("target_id between", value1, value2, "targetId");
            return (Criteria) this;
        }

        public Criteria andTargetIdNotBetween(Long value1, Long value2) {
            addCriterion("target_id not between", value1, value2, "targetId");
            return (Criteria) this;
        }

        public Criteria andTargetDataIsNull() {
            addCriterion("target_data is null");
            return (Criteria) this;
        }

        public Criteria andTargetDataIsNotNull() {
            addCriterion("target_data is not null");
            return (Criteria) this;
        }

        public Criteria andTargetDataEqualTo(String value) {
            addCriterion("target_data =", value, "targetData");
            return (Criteria) this;
        }

        public Criteria andTargetDataEqualToColumn(MessageQueue.Column column) {
            addCriterion(new StringBuilder("target_data = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andTargetDataNotEqualTo(String value) {
            addCriterion("target_data <>", value, "targetData");
            return (Criteria) this;
        }

        public Criteria andTargetDataNotEqualToColumn(MessageQueue.Column column) {
            addCriterion(new StringBuilder("target_data <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andTargetDataGreaterThan(String value) {
            addCriterion("target_data >", value, "targetData");
            return (Criteria) this;
        }

        public Criteria andTargetDataGreaterThanColumn(MessageQueue.Column column) {
            addCriterion(new StringBuilder("target_data > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andTargetDataGreaterThanOrEqualTo(String value) {
            addCriterion("target_data >=", value, "targetData");
            return (Criteria) this;
        }

        public Criteria andTargetDataGreaterThanOrEqualToColumn(MessageQueue.Column column) {
            addCriterion(new StringBuilder("target_data >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andTargetDataLessThan(String value) {
            addCriterion("target_data <", value, "targetData");
            return (Criteria) this;
        }

        public Criteria andTargetDataLessThanColumn(MessageQueue.Column column) {
            addCriterion(new StringBuilder("target_data < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andTargetDataLessThanOrEqualTo(String value) {
            addCriterion("target_data <=", value, "targetData");
            return (Criteria) this;
        }

        public Criteria andTargetDataLessThanOrEqualToColumn(MessageQueue.Column column) {
            addCriterion(new StringBuilder("target_data <= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andTargetDataLike(String value) {
            addCriterion("target_data like", value, "targetData");
            return (Criteria) this;
        }

        public Criteria andTargetDataNotLike(String value) {
            addCriterion("target_data not like", value, "targetData");
            return (Criteria) this;
        }

        public Criteria andTargetDataIn(List<String> values) {
            addCriterion("target_data in", values, "targetData");
            return (Criteria) this;
        }

        public Criteria andTargetDataNotIn(List<String> values) {
            addCriterion("target_data not in", values, "targetData");
            return (Criteria) this;
        }

        public Criteria andTargetDataBetween(String value1, String value2) {
            addCriterion("target_data between", value1, value2, "targetData");
            return (Criteria) this;
        }

        public Criteria andTargetDataNotBetween(String value1, String value2) {
            addCriterion("target_data not between", value1, value2, "targetData");
            return (Criteria) this;
        }

        public Criteria andStatusIsNull() {
            addCriterion("status is null");
            return (Criteria) this;
        }

        public Criteria andStatusIsNotNull() {
            addCriterion("status is not null");
            return (Criteria) this;
        }

        public Criteria andStatusEqualTo(String value) {
            addCriterion("status =", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusEqualToColumn(MessageQueue.Column column) {
            addCriterion(new StringBuilder("status = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andStatusNotEqualTo(String value) {
            addCriterion("status <>", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotEqualToColumn(MessageQueue.Column column) {
            addCriterion(new StringBuilder("status <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andStatusGreaterThan(String value) {
            addCriterion("status >", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusGreaterThanColumn(MessageQueue.Column column) {
            addCriterion(new StringBuilder("status > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andStatusGreaterThanOrEqualTo(String value) {
            addCriterion("status >=", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusGreaterThanOrEqualToColumn(MessageQueue.Column column) {
            addCriterion(new StringBuilder("status >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andStatusLessThan(String value) {
            addCriterion("status <", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLessThanColumn(MessageQueue.Column column) {
            addCriterion(new StringBuilder("status < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andStatusLessThanOrEqualTo(String value) {
            addCriterion("status <=", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLessThanOrEqualToColumn(MessageQueue.Column column) {
            addCriterion(new StringBuilder("status <= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andStatusLike(String value) {
            addCriterion("status like", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotLike(String value) {
            addCriterion("status not like", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusIn(List<String> values) {
            addCriterion("status in", values, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotIn(List<String> values) {
            addCriterion("status not in", values, "status");
            return (Criteria) this;
        }

        public Criteria andStatusBetween(String value1, String value2) {
            addCriterion("status between", value1, value2, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotBetween(String value1, String value2) {
            addCriterion("status not between", value1, value2, "status");
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

        public Criteria andCreateTimeEqualToColumn(MessageQueue.Column column) {
            addCriterion(new StringBuilder("create_time = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andCreateTimeNotEqualTo(Date value) {
            addCriterion("create_time <>", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeNotEqualToColumn(MessageQueue.Column column) {
            addCriterion(new StringBuilder("create_time <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andCreateTimeGreaterThan(Date value) {
            addCriterion("create_time >", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeGreaterThanColumn(MessageQueue.Column column) {
            addCriterion(new StringBuilder("create_time > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andCreateTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("create_time >=", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeGreaterThanOrEqualToColumn(MessageQueue.Column column) {
            addCriterion(new StringBuilder("create_time >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andCreateTimeLessThan(Date value) {
            addCriterion("create_time <", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeLessThanColumn(MessageQueue.Column column) {
            addCriterion(new StringBuilder("create_time < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andCreateTimeLessThanOrEqualTo(Date value) {
            addCriterion("create_time <=", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeLessThanOrEqualToColumn(MessageQueue.Column column) {
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

        public Criteria andDatachangeLasttimeEqualToColumn(MessageQueue.Column column) {
            addCriterion(new StringBuilder("DataChange_LastTime = ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeNotEqualTo(Date value) {
            addCriterion("DataChange_LastTime <>", value, "datachangeLasttime");
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeNotEqualToColumn(MessageQueue.Column column) {
            addCriterion(new StringBuilder("DataChange_LastTime <> ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeGreaterThan(Date value) {
            addCriterion("DataChange_LastTime >", value, "datachangeLasttime");
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeGreaterThanColumn(MessageQueue.Column column) {
            addCriterion(new StringBuilder("DataChange_LastTime > ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeGreaterThanOrEqualTo(Date value) {
            addCriterion("DataChange_LastTime >=", value, "datachangeLasttime");
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeGreaterThanOrEqualToColumn(MessageQueue.Column column) {
            addCriterion(new StringBuilder("DataChange_LastTime >= ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeLessThan(Date value) {
            addCriterion("DataChange_LastTime <", value, "datachangeLasttime");
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeLessThanColumn(MessageQueue.Column column) {
            addCriterion(new StringBuilder("DataChange_LastTime < ").append(column.getEscapedColumnName()).toString());
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeLessThanOrEqualTo(Date value) {
            addCriterion("DataChange_LastTime <=", value, "datachangeLasttime");
            return (Criteria) this;
        }

        public Criteria andDatachangeLasttimeLessThanOrEqualToColumn(MessageQueue.Column column) {
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
        private MessageQueueExample example;

        protected Criteria(MessageQueueExample example) {
            super();
            this.example = example;
        }

        public MessageQueueExample example() {
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
        void example(com.ctrip.zeus.dao.entity.MessageQueueExample example);
    }
}