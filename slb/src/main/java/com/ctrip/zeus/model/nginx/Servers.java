package com.ctrip.zeus.model.nginx;

import java.util.ArrayList;
import java.util.List;

public class Servers {
    private Integer m_total;

    private Integer m_generation;

    private List<Item> m_server = new ArrayList<Item>();

    public Servers() {
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



    public Servers addItem(Item item) {
        m_server.add(item);
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Servers) {
            Servers _o = (Servers) obj;

            if (!equals(m_total, _o.getTotal())) {
                return false;
            }

            if (!equals(m_generation, _o.getGeneration())) {
                return false;
            }

            if (!equals(m_server, _o.getServer())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public Integer getGeneration() {
        return m_generation;
    }

    public List<Item> getServer() {
        return m_server;
    }

    public Integer getTotal() {
        return m_total;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_total == null ? 0 : m_total.hashCode());
        hash = hash * 31 + (m_generation == null ? 0 : m_generation.hashCode());
        hash = hash * 31 + (m_server == null ? 0 : m_server.hashCode());

        return hash;
    }



    public Servers setGeneration(Integer generation) {
        m_generation = generation;
        return this;
    }

    public Servers setTotal(Integer total) {
        m_total = total;
        return this;
    }

}
