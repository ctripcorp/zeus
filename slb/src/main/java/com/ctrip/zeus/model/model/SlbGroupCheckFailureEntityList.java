package com.ctrip.zeus.model.model;

import java.util.ArrayList;
import java.util.List;

public class SlbGroupCheckFailureEntityList {
    private Integer m_total;

    private List<SlbGroupCheckFailureEntity> m_slbGroupCheckFailureEntities = new ArrayList<SlbGroupCheckFailureEntity>();

    public SlbGroupCheckFailureEntityList() {
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



    public SlbGroupCheckFailureEntityList addSlbGroupCheckFailureEntity(SlbGroupCheckFailureEntity slbGroupCheckFailureEntity) {
        m_slbGroupCheckFailureEntities.add(slbGroupCheckFailureEntity);
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SlbGroupCheckFailureEntityList) {
            SlbGroupCheckFailureEntityList _o = (SlbGroupCheckFailureEntityList) obj;

            if (!equals(m_total, _o.getTotal())) {
                return false;
            }

            if (!equals(m_slbGroupCheckFailureEntities, _o.getSlbGroupCheckFailureEntities())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public List<SlbGroupCheckFailureEntity> getSlbGroupCheckFailureEntities() {
        return m_slbGroupCheckFailureEntities;
    }

    public Integer getTotal() {
        return m_total;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_total == null ? 0 : m_total.hashCode());
        hash = hash * 31 + (m_slbGroupCheckFailureEntities == null ? 0 : m_slbGroupCheckFailureEntities.hashCode());

        return hash;
    }



    public SlbGroupCheckFailureEntityList setTotal(Integer total) {
        m_total = total;
        return this;
    }

}
