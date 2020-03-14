package com.ctrip.zeus.model.page;

public class Page {
    private PageContext m_pageContext;

    private DefaultFile m_defaultFile;

    public Page() {
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
        if (obj instanceof Page) {
            Page _o = (Page) obj;

            if (!equals(m_pageContext, _o.getPageContext())) {
                return false;
            }

            if (!equals(m_defaultFile, _o.getDefaultFile())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public DefaultFile getDefaultFile() {
        return m_defaultFile;
    }

    public PageContext getPageContext() {
        return m_pageContext;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_pageContext == null ? 0 : m_pageContext.hashCode());
        hash = hash * 31 + (m_defaultFile == null ? 0 : m_defaultFile.hashCode());

        return hash;
    }



    public Page setDefaultFile(DefaultFile defaultFile) {
        m_defaultFile = defaultFile;
        return this;
    }

    public Page setPageContext(PageContext pageContext) {
        m_pageContext = pageContext;
        return this;
    }

}
