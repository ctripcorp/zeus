package com.ctrip.zeus.server;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author:xingchaowang
 * @date: 3/3/2015.
 */
public class SLBAdminServer {

    public static void main(String[] args) {
        Logger logger = LoggerFactory.getLogger(SLBAdminServer.class);

        ResourceConfig config = new ResourceConfig();
        config.property("contextConfigLocation", "spring-context.xml");

        ServletContainer servletContainer = new ServletContainer(config);
        ServletHolder servletHolder = new ServletHolder(servletContainer);

        int listenPort = 8099;
        Server server = new Server(listenPort);
        try {
            DefaultServlet staticServlet = new DefaultServlet();

            ServletContextHandler handler = new ServletContextHandler();
            handler.setResourceBase("d:/");
            handler.setContextPath("/");
            handler.setSessionHandler(new SessionHandler());

            handler.addServlet(servletHolder, "/api/*");
            handler.addServlet(new ServletHolder(staticServlet), "/*");

            server.setHandler(handler);

            server.start();
        } catch (Exception e) {
            logger.error("Exception in building AdminResourcesContainer ", e);
        }

    }
}
