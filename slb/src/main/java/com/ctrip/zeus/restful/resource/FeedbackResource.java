package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.dao.entity.FeedbackCommit;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.restful.message.ResponseHandler;
import com.ctrip.zeus.service.feedback.FeedbackService;
import com.ctrip.zeus.support.ObjectJsonParser;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.List;

/**
 * Created by fanqq on 2017/7/7.
 */
@Component
@Path("/feedback")
public class FeedbackResource {
    @Resource
    private FeedbackService feedbackService;
    @Resource
    private ResponseHandler responseHandler;

    @POST
    @Path("/add")
    public Response add(@Context HttpServletRequest request,
                        @Context HttpHeaders hh,
                        String feedback) throws Exception {
        FeedbackCommit feedbackData = ObjectJsonParser.parse(feedback, FeedbackCommit.class);
        if (feedbackData == null) {
            throw new ValidationException("Invalidate Post Data .");
        }
        feedbackData.setCreateTime(new Date());
        FeedbackCommit res = feedbackService.addFeedback(feedbackData);
        return responseHandler.handle(res, hh.getMediaType());
    }

    @GET
    @Path("/get")
    public Response get(@Context HttpServletRequest request,
                        @Context HttpHeaders hh) throws Exception {
        List<FeedbackCommit> res = feedbackService.getFeedback();
        return responseHandler.handle(res, hh.getMediaType());
    }
}
