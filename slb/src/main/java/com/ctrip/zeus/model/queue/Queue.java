package com.ctrip.zeus.model.queue;

public class Queue {
    private Message m_message;

    private SlbMessageData m_slbMessageData;

    public Queue() {
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
        if (obj instanceof Queue) {
            Queue _o = (Queue) obj;

            if (!equals(m_message, _o.getMessage())) {
                return false;
            }

            if (!equals(m_slbMessageData, _o.getSlbMessageData())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public Message getMessage() {
        return m_message;
    }

    public SlbMessageData getSlbMessageData() {
        return m_slbMessageData;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_message == null ? 0 : m_message.hashCode());
        hash = hash * 31 + (m_slbMessageData == null ? 0 : m_slbMessageData.hashCode());

        return hash;
    }



    public Queue setMessage(Message message) {
        m_message = message;
        return this;
    }

    public Queue setSlbMessageData(SlbMessageData slbMessageData) {
        m_slbMessageData = slbMessageData;
        return this;
    }

}
