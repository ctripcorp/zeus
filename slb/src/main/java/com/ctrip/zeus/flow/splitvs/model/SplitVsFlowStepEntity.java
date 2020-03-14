package com.ctrip.zeus.flow.splitvs.model;

import java.util.Date;

public class SplitVsFlowStepEntity {
    Date startTime;
    Date finishTime;
    String status;
    String message;

    public Date getStartTime() {
        return startTime;
    }

    public SplitVsFlowStepEntity setStartTime(Date startTime) {
        this.startTime = startTime;
        return this;
    }

    public Date getFinishTime() {
        return finishTime;
    }

    public SplitVsFlowStepEntity setFinishTime(Date finishTime) {
        this.finishTime = finishTime;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public SplitVsFlowStepEntity setStatus(String status) {
        this.status = status;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public SplitVsFlowStepEntity setMessage(String message) {
        this.message = message;
        return this;
    }
}
