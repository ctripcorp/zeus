package com.ctrip.zeus.auth.entity;

import java.util.ArrayList;
import java.util.List;

public class ResourceList {
    private Integer m_total;

    private List<UserResource> m_userResources = new ArrayList<UserResource>();

    public ResourceList() {
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

    public ResourceList addUserResource(UserResource userResource) {
        m_userResources.add(userResource);
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ResourceList) {
            ResourceList _o = (ResourceList) obj;

            if (!equals(m_total, _o.getTotal())) {
                return false;
            }

            if (!equals(m_userResources, _o.getUserResources())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public Integer getTotal() {
        return m_total;
    }

    public List<UserResource> getUserResources() {
        return m_userResources;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_total == null ? 0 : m_total.hashCode());
        hash = hash * 31 + (m_userResources == null ? 0 : m_userResources.hashCode());

        return hash;
    }

    public ResourceList setTotal(Integer total) {
        m_total = total;
        return this;
    }

}
