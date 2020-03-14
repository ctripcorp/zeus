package com.ctrip.zeus.model.tools;

import java.util.ArrayList;
import java.util.List;

public class AbtestTarget {
    private String m_method;

    private String m_url;

    private String m_body;

    private String m_cookie;

    private String m_vip;

    private List<Param> m_params = new ArrayList<Param>();

    private List<Header> m_customHeaders = new ArrayList<Header>();

    public AbtestTarget() {
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



    public AbtestTarget addHeader(Header header) {
        m_customHeaders.add(header);
        return this;
    }

    public AbtestTarget addParam(Param param) {
        m_params.add(param);
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AbtestTarget) {
            AbtestTarget _o = (AbtestTarget) obj;

            if (!equals(m_method, _o.getMethod())) {
                return false;
            }

            if (!equals(m_url, _o.getUrl())) {
                return false;
            }

            if (!equals(m_body, _o.getBody())) {
                return false;
            }

            if (!equals(m_cookie, _o.getCookie())) {
                return false;
            }

            if (!equals(m_vip, _o.getVip())) {
                return false;
            }

            if (!equals(m_params, _o.getParams())) {
                return false;
            }

            if (!equals(m_customHeaders, _o.getCustomHeaders())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public String getBody() {
        return m_body;
    }

    public String getCookie() {
        return m_cookie;
    }

    public List<Header> getCustomHeaders() {
        return m_customHeaders;
    }

    public String getMethod() {
        return m_method;
    }

    public List<Param> getParams() {
        return m_params;
    }

    public String getUrl() {
        return m_url;
    }

    public String getVip() {
        return m_vip;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_method == null ? 0 : m_method.hashCode());
        hash = hash * 31 + (m_url == null ? 0 : m_url.hashCode());
        hash = hash * 31 + (m_body == null ? 0 : m_body.hashCode());
        hash = hash * 31 + (m_cookie == null ? 0 : m_cookie.hashCode());
        hash = hash * 31 + (m_vip == null ? 0 : m_vip.hashCode());
        hash = hash * 31 + (m_params == null ? 0 : m_params.hashCode());
        hash = hash * 31 + (m_customHeaders == null ? 0 : m_customHeaders.hashCode());

        return hash;
    }



    public AbtestTarget setBody(String body) {
        m_body = body;
        return this;
    }

    public AbtestTarget setCookie(String cookie) {
        m_cookie = cookie;
        return this;
    }

    public AbtestTarget setMethod(String method) {
        m_method = method;
        return this;
    }

    public AbtestTarget setUrl(String url) {
        m_url = url;
        return this;
    }

    public AbtestTarget setVip(String vip) {
        m_vip = vip;
        return this;
    }

}
