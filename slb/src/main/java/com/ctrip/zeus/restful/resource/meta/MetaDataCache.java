package com.ctrip.zeus.restful.resource.meta;

import com.netflix.config.DynamicLongProperty;
import com.netflix.config.DynamicPropertyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author:xingchaowang
 * @date: 2016/8/11.
 */
abstract public class MetaDataCache<T> {
    DynamicLongProperty buildInterval= DynamicPropertyFactory.getInstance().getLongProperty("meta-cache.refresh.interval", 1000*10);

    Logger logger = LoggerFactory.getLogger(this.getClass());

    AtomicReference<T> cacheRef = new AtomicReference<>(null);
    volatile long lastBuildTime = 0;
    Executor executor = Executors.newFixedThreadPool(1);

    public T get() throws Exception {
        //First build
        if (cacheRef.get() == null) {
            buildCache();
        }

        //Need to rebuild after interval time.
        if (needToRebuild()) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        buildCache();
                    } catch (Exception e) {
                        logger.error("Build Cache Error", e);
                    }
                }
            });
        }
        return cacheRef.get();
    }
    protected boolean needToRebuild() {
        return System.currentTimeMillis() - lastBuildTime > buildInterval.get();
    }
    synchronized protected void buildCache() throws Exception {
        if (needToRebuild()) {
            T data = queryData();
            cacheRef.set(data);
            lastBuildTime = System.currentTimeMillis();
        }
    }
    abstract T queryData() throws Exception;
}
