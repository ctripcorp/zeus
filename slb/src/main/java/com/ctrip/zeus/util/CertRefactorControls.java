package com.ctrip.zeus.util;

import com.netflix.config.DynamicBooleanProperty;
import com.netflix.config.DynamicPropertyFactory;
import org.springframework.stereotype.Component;

/**
 * @Discription
 **/
@Component
public class CertRefactorControls {

    private final DynamicBooleanProperty writeToNewTable = DynamicPropertyFactory.getInstance().getBooleanProperty("refactor.cert.write.to.new.table", false);

    private final DynamicBooleanProperty writeToOldTable = DynamicPropertyFactory.getInstance().getBooleanProperty("refactor.cert.write.to.old.table", true);

    private final DynamicBooleanProperty readFromOldTable = DynamicPropertyFactory.getInstance().getBooleanProperty("refactor.cert.read.from.old.table", true);

    public boolean writeToNewTable() {
        return writeToNewTable.get();
    }

    public boolean writeToOldTable() {
        return writeToOldTable.get();
    }

    public boolean readFromOldTable() {
        return readFromOldTable.get();
    }
}
