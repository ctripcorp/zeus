package com.ctrip.zeus.model.status;

public class Status {
    private GroupStatus m_groupStatus;

    private GroupStatusList m_groupStatusList;

    private ServerStatus m_serverStatus;

    private OpServerStatusReq m_opServerStatusReq;

    private OpMemberStatusReq m_opMemberStatusReq;

    private UpdateStatusItem m_updateStatusItem;

    public Status() {
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
        if (obj instanceof Status) {
            Status _o = (Status) obj;

            if (!equals(m_groupStatus, _o.getGroupStatus())) {
                return false;
            }

            if (!equals(m_groupStatusList, _o.getGroupStatusList())) {
                return false;
            }

            if (!equals(m_serverStatus, _o.getServerStatus())) {
                return false;
            }

            if (!equals(m_opServerStatusReq, _o.getOpServerStatusReq())) {
                return false;
            }

            if (!equals(m_opMemberStatusReq, _o.getOpMemberStatusReq())) {
                return false;
            }

            if (!equals(m_updateStatusItem, _o.getUpdateStatusItem())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public GroupStatus getGroupStatus() {
        return m_groupStatus;
    }

    public GroupStatusList getGroupStatusList() {
        return m_groupStatusList;
    }

    public OpMemberStatusReq getOpMemberStatusReq() {
        return m_opMemberStatusReq;
    }

    public OpServerStatusReq getOpServerStatusReq() {
        return m_opServerStatusReq;
    }

    public ServerStatus getServerStatus() {
        return m_serverStatus;
    }

    public UpdateStatusItem getUpdateStatusItem() {
        return m_updateStatusItem;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_groupStatus == null ? 0 : m_groupStatus.hashCode());
        hash = hash * 31 + (m_groupStatusList == null ? 0 : m_groupStatusList.hashCode());
        hash = hash * 31 + (m_serverStatus == null ? 0 : m_serverStatus.hashCode());
        hash = hash * 31 + (m_opServerStatusReq == null ? 0 : m_opServerStatusReq.hashCode());
        hash = hash * 31 + (m_opMemberStatusReq == null ? 0 : m_opMemberStatusReq.hashCode());
        hash = hash * 31 + (m_updateStatusItem == null ? 0 : m_updateStatusItem.hashCode());

        return hash;
    }



    public Status setGroupStatus(GroupStatus groupStatus) {
        m_groupStatus = groupStatus;
        return this;
    }

    public Status setGroupStatusList(GroupStatusList groupStatusList) {
        m_groupStatusList = groupStatusList;
        return this;
    }

    public Status setOpMemberStatusReq(OpMemberStatusReq opMemberStatusReq) {
        m_opMemberStatusReq = opMemberStatusReq;
        return this;
    }

    public Status setOpServerStatusReq(OpServerStatusReq opServerStatusReq) {
        m_opServerStatusReq = opServerStatusReq;
        return this;
    }

    public Status setServerStatus(ServerStatus serverStatus) {
        m_serverStatus = serverStatus;
        return this;
    }

    public Status setUpdateStatusItem(UpdateStatusItem updateStatusItem) {
        m_updateStatusItem = updateStatusItem;
        return this;
    }

}
