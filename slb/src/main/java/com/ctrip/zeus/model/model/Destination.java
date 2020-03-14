package com.ctrip.zeus.model.model;

import java.util.ArrayList;
import java.util.List;

public class Destination {
    private VirtualServer m_virtualServer;

    private List<TrafficControl> m_controls = new ArrayList<TrafficControl>();

    public Destination() {
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



    public Destination addTrafficControl(TrafficControl trafficControl) {
        m_controls.add(trafficControl);
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Destination) {
            Destination _o = (Destination) obj;

            if (!equals(m_virtualServer, _o.getVirtualServer())) {
                return false;
            }

            if (!equals(m_controls, _o.getControls())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public List<TrafficControl> getControls() {
        return m_controls;
    }

    public VirtualServer getVirtualServer() {
        return m_virtualServer;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_virtualServer == null ? 0 : m_virtualServer.hashCode());
        hash = hash * 31 + (m_controls == null ? 0 : m_controls.hashCode());

        return hash;
    }



    public Destination setVirtualServer(VirtualServer virtualServer) {
        m_virtualServer = virtualServer;
        return this;
    }

}
