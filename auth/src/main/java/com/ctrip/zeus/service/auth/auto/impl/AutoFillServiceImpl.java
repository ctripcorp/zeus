package com.ctrip.zeus.service.auth.auto.impl;

import com.ctrip.zeus.auth.entity.User;
import com.ctrip.zeus.service.auth.auto.AutoFillFilter;
import com.ctrip.zeus.service.auth.auto.AutoFillService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Created by fanqq on 2016/8/25.
 */
@Service("autoFillService")
public class AutoFillServiceImpl implements AutoFillService {

    private List<AutoFillFilter> filters = new ArrayList<>();

    @Override
    public void addFilter(AutoFillFilter fillFilter) {
        filters.add(fillFilter);
        filters.sort(Comparator.comparingInt(AutoFillFilter::order));
    }

    @Override
    public void autoFill(User user, String employee) {
        // do nothing in filter user information
    }
}
