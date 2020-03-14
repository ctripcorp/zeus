package com.ctrip.zeus.dao.entity;

import java.util.ArrayList;
import java.util.Arrays;

public class RuleRuleWithBLOBs extends RuleRule {
    private String attributes;

    private String content;

    public String getAttributes() {
        return attributes;
    }

    public void setAttributes(String attributes) {
        this.attributes = attributes == null ? null : attributes.trim();
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content == null ? null : content.trim();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", attributes=").append(attributes);
        sb.append(", content=").append(content);
        sb.append("]");
        return sb.toString();
    }

    public static RuleRuleWithBLOBs.Builder builder() {
        return new RuleRuleWithBLOBs.Builder();
    }

    public static class Builder extends RuleRule.Builder {
        private RuleRuleWithBLOBs obj;

        public Builder() {
            this.obj = new RuleRuleWithBLOBs();
        }

        public Builder id(Long id) {
            obj.setId(id);
            return this;
        }

        public Builder name(String name) {
            obj.setName(name);
            return this;
        }

        public Builder ruleType(Integer ruleType) {
            obj.setRuleType(ruleType);
            return this;
        }

        public Builder datachangeLasttime(java.util.Date datachangeLasttime) {
            obj.setDatachangeLasttime(datachangeLasttime);
            return this;
        }

        public Builder attributes(String attributes) {
            obj.setAttributes(attributes);
            return this;
        }

        public Builder content(String content) {
            obj.setContent(content);
            return this;
        }

        public RuleRuleWithBLOBs build() {
            return this.obj;
        }
    }

    public enum Column {
        id("id", "id", "BIGINT", false),
        name("name", "name", "VARCHAR", false),
        ruleType("rule_type", "ruleType", "INTEGER", false),
        datachangeLasttime("DataChange_LastTime", "datachangeLasttime", "TIMESTAMP", false),
        attributes("attributes", "attributes", "LONGVARCHAR", false),
        content("content", "content", "LONGVARCHAR", false);

        private static final String BEGINNING_DELIMITER = "`";

        private static final String ENDING_DELIMITER = "`";

        private final String column;

        private final boolean isColumnNameDelimited;

        private final String javaProperty;

        private final String jdbcType;

        public String value() {
            return this.column;
        }

        public String getValue() {
            return this.column;
        }

        public String getJavaProperty() {
            return this.javaProperty;
        }

        public String getJdbcType() {
            return this.jdbcType;
        }

        Column(String column, String javaProperty, String jdbcType, boolean isColumnNameDelimited) {
            this.column = column;
            this.javaProperty = javaProperty;
            this.jdbcType = jdbcType;
            this.isColumnNameDelimited = isColumnNameDelimited;
        }

        public String desc() {
            return this.getEscapedColumnName() + " DESC";
        }

        public String asc() {
            return this.getEscapedColumnName() + " ASC";
        }

        public static Column[] excludes(Column ... excludes) {
            ArrayList<Column> columns = new ArrayList<>(Arrays.asList(Column.values()));
            if (excludes != null && excludes.length > 0) {
                columns.removeAll(new ArrayList<>(Arrays.asList(excludes)));
            }
            return columns.toArray(new Column[]{});
        }

        public String getEscapedColumnName() {
            if (this.isColumnNameDelimited) {
                return new StringBuilder().append(BEGINNING_DELIMITER).append(this.column).append(ENDING_DELIMITER).toString();
            } else {
                return this.column;
            }
        }

        public String getAliasedEscapedColumnName() {
            return this.getEscapedColumnName();
        }
    }
}