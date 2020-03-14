package com.ctrip.zeus.restful.message.view;

import com.ctrip.zeus.service.model.Archive;

import java.util.List;

/**
 * Created by zhoumy on 2016/12/15.
 */
public class ArchiveList<T> {
    private int total;
    private List<Archive<T>> archives;

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<Archive<T>> getArchives() {
        return archives;
    }

    public void setArchives(List<Archive<T>> archives) {
        this.archives = archives;
    }
}
