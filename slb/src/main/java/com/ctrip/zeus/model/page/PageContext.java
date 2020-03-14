package com.ctrip.zeus.model.page;

public class PageContext {
    private String m_title;

    public PageContext() {
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
        if (obj instanceof PageContext) {
            PageContext _o = (PageContext) obj;

            if (!equals(m_title, _o.getTitle())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public String getTitle() {
        return m_title;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_title == null ? 0 : m_title.hashCode());

        return hash;
    }



    public PageContext setTitle(String title) {
        m_title = title;
        return this;
    }

}
