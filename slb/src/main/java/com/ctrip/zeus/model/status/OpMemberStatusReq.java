package com.ctrip.zeus.model.status;

import java.util.ArrayList;
import java.util.List;

public class OpMemberStatusReq {
    private String m_operation;

    private List<IpGroupname> m_ipGroupnames = new ArrayList<IpGroupname>();

    public OpMemberStatusReq() {
    }

    protected boolean equals(Object o1, Object o2) {
        if (o1 == null) {
            return o2 == null;
        } else if (o2 == null) {
            return false;
        } else {
            return o1.equals(o2);
        }
    }



    public OpMemberStatusReq addIpGroupname(IpGroupname ipGroupname) {
        m_ipGroupnames.add(ipGroupname);
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof OpMemberStatusReq) {
            OpMemberStatusReq _o = (OpMemberStatusReq) obj;

            if (!equals(m_operation, _o.getOperation())) {
                return false;
            }

            if (!equals(m_ipGroupnames, _o.getIpGroupnames())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public List<IpGroupname> getIpGroupnames() {
        return m_ipGroupnames;
    }

    public String getOperation() {
        return m_operation;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_operation == null ? 0 : m_operation.hashCode());
        hash = hash * 31 + (m_ipGroupnames == null ? 0 : m_ipGroupnames.hashCode());

        return hash;
    }



    public OpMemberStatusReq setOperation(String operation) {
        m_operation = operation;
        return this;
    }

}
