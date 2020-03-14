package com.ctrip.zeus.model.feedback;

public class Feedback {
    private FeedbackData m_feedbackData;

    public Feedback() {
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
        if (obj instanceof Feedback) {
            Feedback _o = (Feedback) obj;

            if (!equals(m_feedbackData, _o.getFeedbackData())) {
                return false;
            }


            return true;
        }

        return false;
    }

    public FeedbackData getFeedbackData() {
        return m_feedbackData;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash = hash * 31 + (m_feedbackData == null ? 0 : m_feedbackData.hashCode());

        return hash;
    }



    public Feedback setFeedbackData(FeedbackData feedbackData) {
        m_feedbackData = feedbackData;
        return this;
    }

}
