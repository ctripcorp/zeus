package com.ctrip.zeus.service.conf;

import java.util.List;

/**
 * @author:xingchaowang
 * @date: 3/15/2015.
 */
public interface ConfService {
    public void activate(List<String> slbNames, List<String> appNames);
}
