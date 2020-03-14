package com.ctrip.zeus.service.feedback.impl;

import com.ctrip.zeus.dao.entity.FeedbackCommit;
import com.ctrip.zeus.dao.entity.FeedbackCommitExample;
import com.ctrip.zeus.dao.mapper.FeedbackCommitMapper;
import com.ctrip.zeus.service.feedback.FeedbackService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by fanqq on 2017/7/6.
 */
@Service("feedbackService")
public class FeedbackServiceImpl implements FeedbackService {
    @Resource
    private FeedbackCommitMapper feedbackCommitMapper;

    @Override
    public List<FeedbackCommit> getFeedback() throws Exception {
        List<FeedbackCommit> result = feedbackCommitMapper.selectByExample(new FeedbackCommitExample().
                createCriteria().
                example());
        return result;
    }

    @Override
    public FeedbackCommit addFeedback(FeedbackCommit data) throws Exception {
        if (data.getId() != null && data.getId() > 0) {
            List<FeedbackCommit> feedbackCommits = new ArrayList<>();
            feedbackCommits.add(data);
            feedbackCommitMapper.batchInsertIncludeId(feedbackCommits);
        } else {
            feedbackCommitMapper.insert(data);
        }
        return data;
    }
}
