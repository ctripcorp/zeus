package com.ctrip.zeus.clean;

import com.ctrip.zeus.service.clean.CleanFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
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

    private List<CleanFilter> filters = new ArrayList<>();
    private boolean inited = false;
    Logger logger = LoggerFactory.getLogger(this.getClass());

    public void run() throws Exception{
        init();
        for (CleanFilter filter : filters){
            if (filter.interval() == 0){
                continue;
            }
            if (ticks % filter.interval() == 0){
                try {
                    filter.runFilter();
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
