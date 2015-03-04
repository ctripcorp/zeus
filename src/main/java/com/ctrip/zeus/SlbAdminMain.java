package com.ctrip.zeus;

import com.ctrip.zeus.server.Server;
import com.ctrip.zeus.server.SlbAdminServer;
import com.ctrip.zeus.util.ShutdownHookManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

/**
 * @author:xingchaowang
 * @date: 3/4/2015.
 */
public class SlbAdminMain {
    private static final Logger LOGGER = LoggerFactory.getLogger(SlbAdminMain.class);

    private static String APPLICATION_NAME = "slb-admin";

    private static Server startServer() throws Exception {
        SlbAdminServer slbAdminServer = new SlbAdminServer();
        slbAdminServer.start();
        return slbAdminServer;
    }

    public static void main(String[] args) {

        //Archaius loading configuration depends on this property.
        System.setProperty("archaius.deployment.applicationId", APPLICATION_NAME);

        String environment = System.getProperty("archaius.deployment.environment");
        if(environment==null || environment.equals("")){
            System.setProperty("archaius.deployment.environment", "local");
        }

        printStartupAndShutdownMsg(args);
        Server server = null;
        try {
            server = startServer();

            final Server finalServer = server;
            ShutdownHookManager.get().addShutdownHook(new Runnable() {
                @Override
                public void run() {
                    finalServer.close();
                }
            },Integer.MAX_VALUE);

        } catch (Exception e) {
            if(server!=null)server.close();
            LOGGER.error("Can not to start the SlbAdminServer then is going to shutdown", e);
            e.printStackTrace();
        }
    }

    private static void printStartupAndShutdownMsg(String[] args) {
        String host= "Unknown";
        try {
            host = InetAddress.getLocalHost().toString();
        } catch (UnknownHostException e) {
            LOGGER.warn("Can't get the local host.", e);
        }

        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        StackTraceElement topStack = stackTrace[stackTrace.length - 1];

        final String hostName = host;
        final String className = topStack.getClassName();

        LOGGER.info("STARTUP_MSG:\n" +
                        "*******************************************\n" +
                        "\tStarting : {}\n" +
                        "\tHost : {}\n" +
                        "\tArgs : {}\n" +
                        "*******************************************",
                className, hostName, Arrays.toString(args));

        ShutdownHookManager.get().addShutdownHook(new Runnable() {
            @Override
            public void run() {
                LOGGER.info("SHUTDOWN_MSG:\n" +
                                "*******************************************\n" +
                                "\tShutting down : {}\n" +
                                "\tHost : {}\n" +
                                "*******************************************",
                        className,hostName);

            }
        }, 1);
    }
}
