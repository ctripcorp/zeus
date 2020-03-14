package com.ctrip.zeus.auth.entity;

public class CasUserInfo {
    private String m_name;

    private String m_mail;

    private String m_chineseName;

    private String m_displayName;

    private String m_company;

    private String m_employee;

    private String m_memberOf;

    private String m_department;

    private String m_city;

    private String m_distinguishedName;

    public CasUserInfo() {
    }


    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CasUserInfo) {
            CasUserInfo _o = (CasUserInfo) obj;

            if (!equals(m_name, _o.getName())) {
                return false;
            }

            if (!equals(m_mail, _o.getMail())) {
                return false;
            }

            if (!equals(m_chineseName, _o.getChineseName())) {
                return false;
            }

            if (!equals(m_displayName, _o.getDisplayName())) {
                return false;
            }

            if (!equals(m_company, _o.getCompany())) {
                return false;
            }

            if (!equals(m_employee, _o.getEmployee())) {
                return false;
            }

            if (!equals(m_memberOf, _o.getMemberOf())) {
                return false;
            }

            if (!equals(m_department, _o.getDepartment())) {
                return false;
            }

            if (!equals(m_city, _o.getCity())) {
                return false;
            }

            if (!equals(m_distinguishedName, _o.getDistinguishedName())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public String getChineseName() {
        return m_chineseName;
    }

    public String getCity() {
        return m_city;
    }

    public String getCompany() {
        return m_company;
    }

    public String getDepartment() {
        return m_department;
    }

    public String getDisplayName() {
        return m_displayName;
    }

    public String getDistinguishedName() {
        return m_distinguishedName;
    }

    public String getEmployee() {
        return m_employee;
    }

    public String getMail() {
        return m_mail;
    }

    public String getMemberOf() {
        return m_memberOf;
    }

    public String getName() {
        return m_name;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_name == null ? 0 : m_name.hashCode());
        hash = hash * 31 + (m_mail == null ? 0 : m_mail.hashCode());
        hash = hash * 31 + (m_chineseName == null ? 0 : m_chineseName.hashCode());
        hash = hash * 31 + (m_displayName == null ? 0 : m_displayName.hashCode());
        hash = hash * 31 + (m_company == null ? 0 : m_company.hashCode());
        hash = hash * 31 + (m_employee == null ? 0 : m_employee.hashCode());
        hash = hash * 31 + (m_memberOf == null ? 0 : m_memberOf.hashCode());
        hash = hash * 31 + (m_department == null ? 0 : m_department.hashCode());
        hash = hash * 31 + (m_city == null ? 0 : m_city.hashCode());
        hash = hash * 31 + (m_distinguishedName == null ? 0 : m_distinguishedName.hashCode());

        return hash;
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

    public CasUserInfo setChineseName(String chineseName) {
        m_chineseName = chineseName;
        return this;
    }

    public CasUserInfo setCity(String city) {
        m_city = city;
        return this;
    }

    public CasUserInfo setCompany(String company) {
        m_company = company;
        return this;
    }

    public CasUserInfo setDepartment(String department) {
        m_department = department;
        return this;
    }

    public CasUserInfo setDisplayName(String displayName) {
        m_displayName = displayName;
        return this;
    }

    public CasUserInfo setDistinguishedName(String distinguishedName) {
        m_distinguishedName = distinguishedName;
        return this;
    }

    public CasUserInfo setEmployee(String employee) {
        m_employee = employee;
        return this;
    }

    public CasUserInfo setMail(String mail) {
        m_mail = mail;
        return this;
    }

    public CasUserInfo setMemberOf(String memberOf) {
        m_memberOf = memberOf;
        return this;
    }

    public CasUserInfo setName(String name) {
        m_name = name;
        return this;
    }

}
