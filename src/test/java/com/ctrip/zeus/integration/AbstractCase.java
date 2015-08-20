package com.ctrip.zeus.integration;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.io.IOException;

/**
 * Created by fanqq on 2015/8/20.
 */
public class AbstractCase {
    @BeforeClass
    public static void before() throws Exception {
        IntegrationData.clean();
        IntegrationData.init();
    }
    @AfterClass
    public static void after() throws IOException {
        IntegrationData.clean();
        IntegrationData.getReqClient().buildReport();
    }
}
