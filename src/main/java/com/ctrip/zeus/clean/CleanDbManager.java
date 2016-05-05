package com.ctrip.zeus.clean;

import com.ctrip.zeus.service.clean.CleanFilter;
import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by fanqq on 2015/10/16.
 */
@Component("cleanDbManager")
public class CleanDbManager {

    private static DynamicIntProperty start = DynamicPropertyFactory.getInstance().getIntProperty("clean-job.start.hour", 3);

    private List<CleanFilter> filters = new ArrayList<>();
    Logger logger = LoggerFactory.getLogger(this.getClass());

    public void run() throws Exception {
        Calendar c = Calendar.getInstance();
        int hours = c.get(Calendar.HOUR_OF_DAY);
        if (hours == start.get()) {
            for (CleanFilter filter : filters) {
                try {
                    logger.info("[Clean Manager] Start Execute Clean Filter Info : " + filter.getClass().getSimpleName() );
                    filter.runFilter();
                    logger.info("[Clean Manager] Finish Execute Clean Filter Info : " + filter.getClass().getSimpleName() );
                } catch (Exception e) {
                    logger.warn("[Clean Manager]Execute Clean Filter Failed.Filter:" + filter.getClass().getSimpleName(), e);
                }
            }
        }
    }

    public void add(CleanFilter cleanFilter) {
        filters.add(cleanFilter);
    }
}
