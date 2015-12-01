package com.ctrip.zeus.clean;

import com.ctrip.zeus.service.clean.CleanFilter;
import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by fanqq on 2015/10/16.
 */
@Component("cleanDbManager")
public class CleanDbManager {
    private  static  Long ticks = 0L;
    @Resource
    private CleanFilter oprationLogCleanFilter;
    @Resource
    private CleanFilter taskCleanFilter;
    @Resource
    private CleanFilter archiveCleanFilter;
    @Resource
    private CleanFilter confCleanFilter;
    @Resource
    private CleanFilter reportCleanFilter;

    private static DynamicIntProperty start = DynamicPropertyFactory.getInstance().getIntProperty("clean-job.start.hour",3);

    private List<CleanFilter> filters = new ArrayList<>();
    private boolean inited = false;
    private int startHour = 0 ;
    Logger logger = LoggerFactory.getLogger(this.getClass());

    public void run() throws Exception{
        init();
        if (startHour != start.get()){
            Calendar c = Calendar.getInstance();
            c.setTime(new Date());
            int hours = c.get(Calendar.HOUR_OF_DAY);
            if (hours == start.get()){
                startHour = start.get();
                ticks = 0L;
            } else {
                return;
            }
        }

        for (CleanFilter filter : filters){
            if (filter.interval() == 0){
                continue;
            }
            if (ticks % filter.interval() == 0){
                try {
                    filter.runFilter();
                    logger.info("[Clean Manager] Execute Clean Filter Info : " + filter.getClass().getSimpleName()+"\nTicks:"+ticks);
                }catch (Exception e){
                    logger.warn("[Clean Manager]Execute Clean Filter Failed.Filter:"+filter.getClass().getSimpleName(),e);
                }
            }
        }
        ticks++;
    }

    private void init() {
        if (inited){
            return;
        }

        filters.add(oprationLogCleanFilter);
        filters.add(taskCleanFilter);
        filters.add(confCleanFilter);
        filters.add(archiveCleanFilter);
        filters.add(reportCleanFilter);

        inited = true;
    }
}
