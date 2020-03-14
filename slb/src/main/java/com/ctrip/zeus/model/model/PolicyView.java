package com.ctrip.zeus.model.model;

import java.util.ArrayList;
import java.util.List;

public class PolicyView {
    private List<Control> m_viewControls = new ArrayList<Control>();

    public PolicyView() {
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



    public PolicyView addControl(Control control) {
        m_viewControls.add(control);
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PolicyView) {
            PolicyView _o = (PolicyView) obj;

            if (!equals(m_viewControls, _o.getViewControls())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public List<Control> getViewControls() {
        return m_viewControls;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_viewControls == null ? 0 : m_viewControls.hashCode());

        return hash;
    }



}
