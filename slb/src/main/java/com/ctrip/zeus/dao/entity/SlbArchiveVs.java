package com.ctrip.zeus.dao.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class SlbArchiveVs {
    private Long id;

    private Long vsId;

    private Integer version;

    private Integer hash;

    private Date datetimeLastchange;

    private String content;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getVsId() {
        return vsId;
    }

    public void setVsId(Long vsId) {
        this.vsId = vsId;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Integer getHash() {
        return hash;
    }

    public void setHash(Integer hash) {
        this.hash = hash;
    }

    public Date getDatetimeLastchange() {
        return datetimeLastchange;
    }

    public void setDatetimeLastchange(Date datetimeLastchange) {
        this.datetimeLastchange = datetimeLastchange;
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
        sb.append(", id=").append(id);
        sb.append(", vsId=").append(vsId);
        sb.append(", version=").append(version);
        sb.append(", hash=").append(hash);
        sb.append(", datetimeLastchange=").append(datetimeLastchange);
        sb.append(", content=").append(content);
        sb.append("]");
        return sb.toString();
    }

    public static SlbArchiveVs.Builder builder() {
        return new SlbArchiveVs.Builder();
    }

    public static class Builder {
        private SlbArchiveVs obj;

        public Builder() {
            this.obj = new SlbArchiveVs();
        }

        public Builder id(Long id) {
            obj.setId(id);
            return this;
        }

        public Builder vsId(Long vsId) {
            obj.setVsId(vsId);
            return this;
        }

        public Builder version(Integer version) {
            obj.setVersion(version);
            return this;
        }

        public Builder hash(Integer hash) {
            obj.setHash(hash);
            return this;
        }

        public Builder datetimeLastchange(Date datetimeLastchange) {
            obj.setDatetimeLastchange(datetimeLastchange);
            return this;
        }

        public Builder content(String content) {
            obj.setContent(content);
            return this;
        }

        public SlbArchiveVs build() {
            return this.obj;
        }
    }

    public enum Column {
        id("id", "id", "BIGINT", false),
        vsId("vs_id", "vsId", "BIGINT", false),
        version("version", "version", "INTEGER", false),
        hash("hash", "hash", "INTEGER", false),
        datetimeLastchange("DateTime_LastChange", "datetimeLastchange", "TIMESTAMP", false),
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