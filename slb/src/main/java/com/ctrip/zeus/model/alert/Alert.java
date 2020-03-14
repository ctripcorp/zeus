package com.ctrip.zeus.model.alert;

public class Alert {
    private AlertItem m_alertItem;

    private AlertItemList m_alertItemList;

    public Alert() {
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


    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Alert) {
            Alert _o = (Alert) obj;

            if (!equals(m_alertItem, _o.getAlertItem())) {
                return false;
            }

            if (!equals(m_alertItemList, _o.getAlertItemList())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public AlertItem getAlertItem() {
        return m_alertItem;
    }

    public AlertItemList getAlertItemList() {
        return m_alertItemList;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_alertItem == null ? 0 : m_alertItem.hashCode());
        hash = hash * 31 + (m_alertItemList == null ? 0 : m_alertItemList.hashCode());

        return hash;
    }



    public Alert setAlertItem(AlertItem alertItem) {
        m_alertItem = alertItem;
        return this;
    }

    public Alert setAlertItemList(AlertItemList alertItemList) {
        m_alertItemList = alertItemList;
        return this;
    }

}
