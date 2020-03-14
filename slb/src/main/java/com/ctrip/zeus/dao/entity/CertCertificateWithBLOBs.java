package com.ctrip.zeus.dao.entity;

import java.util.ArrayList;
import java.util.Arrays;

public class CertCertificateWithBLOBs extends CertCertificate {
    private byte[] cert;

    private byte[] key;

    public byte[] getCert() {
        return cert;
    }

    public void setCert(byte[] cert) {
        this.cert = cert;
    }

    public byte[] getKey() {
        return key;
    }

    public void setKey(byte[] key) {
        this.key = key;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", cert=").append(cert);
        sb.append(", key=").append(key);
        sb.append("]");
        return sb.toString();
    }

    public static CertCertificateWithBLOBs.Builder builder() {
        return new CertCertificateWithBLOBs.Builder();
    }

    public static class Builder extends CertCertificate.Builder {
        private CertCertificateWithBLOBs obj;

        public Builder() {
            this.obj = new CertCertificateWithBLOBs();
        }

        public Builder id(Long id) {
            obj.setId(id);
            return this;
        }

        public Builder domain(String domain) {
            obj.setDomain(domain);
            return this;
        }

        public Builder state(Boolean state) {
            obj.setState(state);
            return this;
        }

        public Builder version(Integer version) {
            obj.setVersion(version);
            return this;
        }

        public Builder datachangeLasttime(java.util.Date datachangeLasttime) {
            obj.setDatachangeLasttime(datachangeLasttime);
            return this;
        }

        public Builder cid(String cid) {
            obj.setCid(cid);
            return this;
        }

        public Builder cert(byte[] cert) {
            obj.setCert(cert);
            return this;
        }

        public Builder key(byte[] key) {
            obj.setKey(key);
            return this;
        }

        public CertCertificateWithBLOBs build() {
            return this.obj;
        }
    }

    public enum Column {
        id("id", "id", "BIGINT", false),
        domain("domain", "domain", "VARCHAR", true),
        state("state", "state", "BIT", true),
        version("version", "version", "INTEGER", false),
        datachangeLasttime("DataChange_LastTime", "datachangeLasttime", "TIMESTAMP", false),
        cid("cid", "cid", "VARCHAR", false),
        cert("cert", "cert", "LONGVARBINARY", false),
        key("key", "key", "LONGVARBINARY", true);

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