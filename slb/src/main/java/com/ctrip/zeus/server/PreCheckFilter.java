package com.ctrip.zeus.server;

import com.ctrip.zeus.service.tools.initialization.impl.InitializationCheckServiceImpl;
import com.ctrip.zeus.util.S;
import com.netflix.config.DynamicBooleanProperty;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.DelegatingFilterProxy;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 * Created by zhoumy on 2016/8/9.
 */
@Component
public class PreCheckFilter extends DelegatingFilterProxy {
    private static final DynamicBooleanProperty preCheckEnabled = DynamicPropertyFactory.getInstance().getBooleanProperty("slb.startup.pre-check.enabled", true);
    private static final DynamicStringProperty preCheckEnabledByIp = DynamicPropertyFactory.getInstance().getStringProperty("slb.startup.pre-check.enabled.ip." + S.getIp(), null);
    private static volatile boolean green;

    public PreCheckFilter() {
        this("preCheckFilter");
    }

    public PreCheckFilter(String targetBeanName) {
        super(targetBeanName);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (InitializationCheckServiceImpl.staticIsInitialized() && isCheckEnabled() && !green) {
            if (response instanceof HttpServletResponse) {
                HttpServletResponse res = (HttpServletResponse) response;
                res.reset();
                res.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
                res.getWriter().write("Service has not prepared.");
                return;
            }
        }
        chain.doFilter(request, response);
    }

    private boolean isCheckEnabled() {
        String serverLevelConfig = preCheckEnabledByIp.get();
        if (serverLevelConfig != null && !serverLevelConfig.isEmpty()) {
            // Pre-check is configured for the current server explictly. So use it.
            return "true".equalsIgnoreCase(serverLevelConfig);
        }
        return preCheckEnabled.get();
    }

    public static void setGreenLight(boolean green) {
        PreCheckFilter.green = green;
    }
}