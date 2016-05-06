package com.ctrip.zeus.service.nginx.impl;

import java.util.Date;
import java.util.List;

/**
 * Created by zhoumy on 2016/4/21.
 */
public class FileOpRecord {
    private Date timeStamp;
    private List<String> cleansedFilename;
    private List<String> copiedFilename;
    private List<String> writtenFilename;


    public FileOpRecord() {
        this.timeStamp = new Date();
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public List<String> getCleansedFilename() {
        return cleansedFilename;
    }

    public void setCleansedFilename(List<String> cleansedFilename) {
        this.cleansedFilename = cleansedFilename;
    }

    public List<String> getCopiedFilename() {
        return copiedFilename;
    }

    public void setCopiedFilename(List<String> copiedFilename) {
        this.copiedFilename = copiedFilename;
    }

    public List<String> getWrittenFilename() {
        return writtenFilename;
    }

    public void setWrittenFilename(List<String> writtenFilename) {
        this.writtenFilename = writtenFilename;
    }
}
