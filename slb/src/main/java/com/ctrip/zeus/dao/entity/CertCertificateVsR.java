package com.ctrip.zeus.dao.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class CertCertificateVsR {
    private Long id;

    private Long vsId;

    private Long version;

    private Long certId;

    private Date datachangeLasttime;

    private String status;

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

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public Long getCertId() {
        return certId;
    }

    public void setCertId(Long certId) {
        this.certId = certId;
    }

    public Date getDatachangeLasttime() {
        return datachangeLasttime;
    }

    public void setDatachangeLasttime(Date datachangeLasttime) {
        this.datachangeLasttime = datachangeLasttime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status == null ? null : status.trim();
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
        sb.append(", certId=").append(certId);
        sb.append(", datachangeLasttime=").append(datachangeLasttime);
        sb.append(", status=").append(status);
        sb.append("]");
        return sb.toString();
    }

    public static CertCertificateVsR.Builder builder() {
        return new CertCertificateVsR.Builder();
    }

    public static class Builder {
        private CertCertificateVsR obj;

        public Builder() {
            this.obj = new CertCertificateVsR();
        }

        public Builder id(Long id) {
            obj.setId(id);
            return this;
        }

        public Builder vsId(Long vsId) {
            obj.setVsId(vsId);
            return this;
        }

        public Builder version(Long version) {
            obj.setVersion(version);
            return this;
        }

        public Builder certId(Long certId) {
            obj.setCertId(certId);
            return this;
        }

        public Builder datachangeLasttime(Date datachangeLasttime) {
            obj.setDatachangeLasttime(datachangeLasttime);
            return this;
        }

        public Builder status(String status) {
            obj.setStatus(status);
            return this;
        }

        public CertCertificateVsR build() {
            return this.obj;
        }
    }

    public enum Column {
        id("id", "id", "BIGINT", false),
        vsId("vs_id", "vsId", "BIGINT", false),
        version("version", "version", "BIGINT", false),
        certId("cert_id", "certId", "BIGINT", false),
        datachangeLasttime("DataChange_LastTime", "datachangeLasttime", "TIMESTAMP", false),
        status("status", "status", "VARCHAR", true);

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