package com.ctrip.zeus.model.model;

import java.util.ArrayList;
import java.util.List;

public class DrTraffic {
    private Group m_group;

    private List<Destination> m_destinations = new ArrayList<Destination>();

    public DrTraffic() {
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



    public DrTraffic addDestination(Destination destination) {
        m_destinations.add(destination);
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DrTraffic) {
            DrTraffic _o = (DrTraffic) obj;

            if (!equals(m_group, _o.getGroup())) {
                return false;
            }

            if (!equals(m_destinations, _o.getDestinations())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public List<Destination> getDestinations() {
        return m_destinations;
    }

    public Group getGroup() {
        return m_group;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_group == null ? 0 : m_group.hashCode());
        hash = hash * 31 + (m_destinations == null ? 0 : m_destinations.hashCode());

        return hash;
    }



    public DrTraffic setGroup(Group group) {
        m_group = group;
        return this;
    }

}
