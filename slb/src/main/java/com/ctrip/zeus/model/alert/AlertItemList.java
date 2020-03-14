package com.ctrip.zeus.model.alert;

import java.util.ArrayList;
import java.util.List;

public class AlertItemList {
    private Integer m_total;

    private List<AlertItem> m_alertItems = new ArrayList<AlertItem>();

    public AlertItemList() {
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



    public AlertItemList addAlertItem(AlertItem alertItem) {
        m_alertItems.add(alertItem);
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AlertItemList) {
            AlertItemList _o = (AlertItemList) obj;

            if (!equals(m_total, _o.getTotal())) {
                return false;
            }

            if (!equals(m_alertItems, _o.getAlertItems())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public List<AlertItem> getAlertItems() {
        return m_alertItems;
    }

    public Integer getTotal() {
        return m_total;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_total == null ? 0 : m_total.hashCode());
        hash = hash * 31 + (m_alertItems == null ? 0 : m_alertItems.hashCode());

        return hash;
    }



    public AlertItemList setTotal(Integer total) {
        m_total = total;
        return this;
    }

}
