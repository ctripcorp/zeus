package com.ctrip.zeus.restful.message.view;

import java.util.List;

public class DrListView extends ListView<ExtendedView.ExtendedDr> {

    public DrListView() {
    }

    public DrListView(int total) {
        super(total);
    }

    public List<ExtendedView.ExtendedDr> getDrs() {
        return getList();
    }
}