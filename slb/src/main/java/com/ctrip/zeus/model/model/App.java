package com.ctrip.zeus.model.model;

public class App {
    private String m_appId;

    private String m_chineseName;

    private String m_englishName;

    private String m_sbu;

    private String m_sbuCode;

    private String m_owner;

    private String m_ownerEmail;

    private String m_backupEmail;

    private String m_description;

    private String m_container;

    public App() {
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
        if (obj instanceof App) {
            App _o = (App) obj;

            if (!equals(m_appId, _o.getAppId())) {
                return false;
            }

            if (!equals(m_chineseName, _o.getChineseName())) {
                return false;
            }

            if (!equals(m_englishName, _o.getEnglishName())) {
                return false;
            }

            if (!equals(m_sbu, _o.getSbu())) {
                return false;
            }

            if (!equals(m_sbuCode, _o.getSbuCode())) {
                return false;
            }

            if (!equals(m_owner, _o.getOwner())) {
                return false;
            }

            if (!equals(m_ownerEmail, _o.getOwnerEmail())) {
                return false;
            }

            if (!equals(m_backupEmail, _o.getBackupEmail())) {
                return false;
            }

            if (!equals(m_description, _o.getDescription())) {
                return false;
            }

            if (!equals(m_container, _o.getContainer())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public String getAppId() {
        return m_appId;
    }

    public String getBackupEmail() {
        return m_backupEmail;
    }

    public String getChineseName() {
        return m_chineseName;
    }

    public String getContainer() {
        return m_container;
    }

    public String getDescription() {
        return m_description;
    }

    public String getEnglishName() {
        return m_englishName;
    }

    public String getOwner() {
        return m_owner;
    }

    public String getOwnerEmail() {
        return m_ownerEmail;
    }

    public String getSbu() {
        return m_sbu;
    }

    public String getSbuCode() {
        return m_sbuCode;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_appId == null ? 0 : m_appId.hashCode());
        hash = hash * 31 + (m_chineseName == null ? 0 : m_chineseName.hashCode());
        hash = hash * 31 + (m_englishName == null ? 0 : m_englishName.hashCode());
        hash = hash * 31 + (m_sbu == null ? 0 : m_sbu.hashCode());
        hash = hash * 31 + (m_sbuCode == null ? 0 : m_sbuCode.hashCode());
        hash = hash * 31 + (m_owner == null ? 0 : m_owner.hashCode());
        hash = hash * 31 + (m_ownerEmail == null ? 0 : m_ownerEmail.hashCode());
        hash = hash * 31 + (m_backupEmail == null ? 0 : m_backupEmail.hashCode());
        hash = hash * 31 + (m_description == null ? 0 : m_description.hashCode());
        hash = hash * 31 + (m_container == null ? 0 : m_container.hashCode());

        return hash;
    }



    public App setAppId(String appId) {
        m_appId = appId;
        return this;
    }

    public App setBackupEmail(String backupEmail) {
        m_backupEmail = backupEmail;
        return this;
    }

    public App setChineseName(String chineseName) {
        m_chineseName = chineseName;
        return this;
    }

    public App setContainer(String container) {
        m_container = container;
        return this;
    }

    public App setDescription(String description) {
        m_description = description;
        return this;
    }

    public App setEnglishName(String englishName) {
        m_englishName = englishName;
        return this;
    }

    public App setOwner(String owner) {
        m_owner = owner;
        return this;
    }

    public App setOwnerEmail(String ownerEmail) {
        m_ownerEmail = ownerEmail;
        return this;
    }

    public App setSbu(String sbu) {
        m_sbu = sbu;
        return this;
    }

    public App setSbuCode(String sbuCode) {
        m_sbuCode = sbuCode;
        return this;
    }

}
