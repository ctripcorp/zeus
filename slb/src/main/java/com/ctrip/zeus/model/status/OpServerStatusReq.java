package com.ctrip.zeus.model.status;

import java.util.ArrayList;
import java.util.List;

public class OpServerStatusReq {
    private String m_operation;

    private List<IpAddresses> m_ipAddresseses = new ArrayList<IpAddresses>();

    public OpServerStatusReq() {
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



    public OpServerStatusReq addIpAddresses(IpAddresses ipAddresses) {
        m_ipAddresseses.add(ipAddresses);
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof OpServerStatusReq) {
            OpServerStatusReq _o = (OpServerStatusReq) obj;

            if (!equals(m_operation, _o.getOperation())) {
                return false;
            }

            if (!equals(m_ipAddresseses, _o.getIpAddresseses())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public List<IpAddresses> getIpAddresseses() {
        return m_ipAddresseses;
    }

    public String getOperation() {
        return m_operation;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_operation == null ? 0 : m_operation.hashCode());
        hash = hash * 31 + (m_ipAddresseses == null ? 0 : m_ipAddresseses.hashCode());

        return hash;
    }



    public OpServerStatusReq setOperation(String operation) {
        m_operation = operation;
        return this;
    }

}
