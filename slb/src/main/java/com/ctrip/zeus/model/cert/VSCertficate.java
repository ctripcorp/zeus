package com.ctrip.zeus.model.cert;

import com.ctrip.zeus.dao.entity.CertCertificateWithBLOBs;

import java.util.List;
import java.util.Map;

public class VSCertficate {
    private List<Long> vsIds;

    private Map<Long, String> vsDomain;

    private List<CertCertificateWithBLOBs> certificates;

    protected boolean equals(Object o1, Object o2) {
        if (o1 == null) {
            return o2 == null;
        } else if (o2 == null) {
            return false;
        } else {
            return o1.equals(o2);
        }
    }


    @Override
    public int hashCode() {
        int result = vsIds.hashCode();
        result = 31 * result + vsDomain.hashCode();
        result = 31 * result + certificates.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof VSCertficate) {
            VSCertficate _o = (VSCertficate) obj;
            if (!equals(vsIds, _o.getVsIds())) {
                return false;
            }
            if (!equals(vsDomain, _o.getVsDomain())) {
                return false;
            }
            if (!equals(certificates, _o.getCertificates())) {
                return false;
            }
            return true;
        }

        return false;
    }

    public List<Long> getVsIds() {
        return vsIds;
    }

    public void setVsIds(List<Long> vsIds) {
        this.vsIds = vsIds;
    }

    public Map<Long, String> getVsDomain() {
        return vsDomain;
    }

    public void setVsDomain(Map<Long, String> vsDomain) {
        this.vsDomain = vsDomain;
    }

    public List<CertCertificateWithBLOBs> getCertificates() {
        return certificates;
    }

    public void setCertificates(List<CertCertificateWithBLOBs> certificates) {
        this.certificates = certificates;
    }
}
