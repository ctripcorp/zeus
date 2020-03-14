package com.ctrip.zeus.startup;

import com.ctrip.zeus.server.PreCheckFilter;
import com.ctrip.zeus.service.tools.initialization.impl.InitializationCheckServiceImpl;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by zhoumy on 2016/8/9.
 */
@Component
public class SpringInitializationNotifier implements ApplicationListener<ContextRefreshedEvent> {
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private List<PreCheck> preCheckList = new ArrayList<>();

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        Object o = event.getApplicationContext().getBean("preCheckFilter");

        for (PreCheck c : event.getApplicationContext().getBeansOfType(PreCheck.class).values()) {
            preCheckList.add(c);
        }

        if (o instanceof PreCheckFilter) {
            executorService.submit(nextRound((PreCheckFilter) o));
        }
    }

    private Runnable nextRound(final PreCheckFilter f) {
        return new Runnable() {
            @Override
            public void run() {
                if (doCheck()) {
                    f.setGreenLight(true);
                    executorService.shutdown();
                } else {
                    executorService.submit(nextRound(f));
                    try {
                        Thread.sleep(500L);
                    } catch (InterruptedException e) {
                    }
                }
            }
        };
    }

    private boolean doCheck() {
        boolean flag = true;
        for (int i = 0; i < preCheckList.size(); i++) {
            flag = flag & preCheckList.get(i).ready();
        }
        return flag;
    }
}
