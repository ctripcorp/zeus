package com.ctrip.zeus.restful.message.view;

import java.util.List;

/**
 * Created by zhoumy on 2016/7/26.
 */
public class VsListView extends ListView<ExtendedView.ExtendedVs> {
    public List<ExtendedView.ExtendedVs> getVirtualServers() {
        return getList();
    }
}
