package com.ctrip.zeus.restful.message.view;

import java.util.List;

/**
 * Created by zhoumy on 2016/7/26.
 */
public class SlbListView extends ListView<ExtendedView.ExtendedSlb> {
    public SlbListView() {
    }

    public SlbListView(int total) {
        super(total);
    }

    public List<ExtendedView.ExtendedSlb> getSlbs() {
        return getList();
    }
}