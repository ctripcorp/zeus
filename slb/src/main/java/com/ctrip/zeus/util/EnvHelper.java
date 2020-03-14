package com.ctrip.zeus.util;

import com.ctrip.zeus.server.LocalInfoPack;
import com.netflix.config.DynamicBooleanProperty;
import com.netflix.config.DynamicPropertyFactory;

public class EnvHelper {
    public static boolean portal() {
        String role = System.getProperty("slb.role");
        if (RoleConsts.ALL.equalsIgnoreCase(role)) {
            return true;
        }
        String portalKey = "slb.server.ip." + LocalInfoPack.INSTANCE.getIp() + ".portal";
        DynamicBooleanProperty isPortal = DynamicPropertyFactory.getInstance().getBooleanProperty(portalKey, false);
        return isPortal.get();
    }
}
