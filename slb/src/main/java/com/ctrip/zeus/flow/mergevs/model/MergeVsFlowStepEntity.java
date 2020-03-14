package com.ctrip.zeus.flow.mergevs.model;


import java.util.Date;

public class MergeVsFlowStepEntity {
    Date startTime;
    Date finishTime;
    String status;
    String message;


    public Date getStartTime() {
        return startTime;
    }

    public MergeVsFlowStepEntity setStartTime(Date startTime) {
        this.startTime = startTime;
        return this;
    }

    public Date getFinishTime() {
        return finishTime;
    }

    public MergeVsFlowStepEntity setFinishTime(Date finishTime) {
        this.finishTime = finishTime;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public MergeVsFlowStepEntity setStatus(String status) {
        this.status = status;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public MergeVsFlowStepEntity setMessage(String message) {
        this.message = message;
        return this;
    }
}
