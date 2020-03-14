package com.ctrip.zeus.service.feedback;

import com.ctrip.zeus.dao.entity.FeedbackCommit;

import java.util.List;

/**
 * Created by fanqq on 2017/7/6.
 */
public interface FeedbackService {
    List<FeedbackCommit> getFeedback() throws Exception;

    FeedbackCommit addFeedback(FeedbackCommit data) throws Exception;
}
