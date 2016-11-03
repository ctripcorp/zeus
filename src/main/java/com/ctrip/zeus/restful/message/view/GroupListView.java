package com.ctrip.zeus.restful.message.view;

import java.util.List;

/**
 * Created by zhoumy on 2016/7/26.
 */
public class GroupListView extends ListView<ExtendedView.ExtendedGroup> {
    public GroupListView() {
    }

    public GroupListView(int total) {
        super(total);
    }

    public List<ExtendedView.ExtendedGroup> getGroups() {
        return getList();
    }
}
