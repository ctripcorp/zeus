package com.ctrip.zeus.server;

import com.ctrip.zeus.restful.resource.SLBResourcePackage;
import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlets.GzipFilter;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.ContextLoaderListener;

import javax.servlet.DispatcherType;
import java.util.EnumSet;

/**
 * @author:xingchaowang
 * @date: 3/4/2015.
 */
public class SlbAdminServer extends AbstractServer {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private Server server;

    public SlbAdminServer() throws Exception {
    }

    @Override
    protected void init() throws Exception {
        //GetConfig
        DynamicIntProperty serverPort = DynamicPropertyFactory.getInstance().getIntProperty("server.port", 8099);
        DynamicStringProperty wwwBaseDir = DynamicPropertyFactory.getInstance().getStringProperty("server.www.base-dir", ".");

        //Config Jersey
        ResourceConfig config = new ResourceConfig();
        config.packages(SLBResourcePackage.class.getPackage().getName());

        //JerseyServlet
        ServletContainer jerseyServletContainer = new ServletContainer(config);
        ServletHolder jerseyServletHolder = new ServletHolder(jerseyServletContainer);

        //StaticServlet
        DefaultServlet staticServlet = new DefaultServlet();
        ServletHolder staticServletHolder = new ServletHolder(staticServlet);

        //Spring Context Listener
        ContextLoaderListener sprintContextListener = new ContextLoaderListener();

        //Create and Config Jetty Request Handler
        ServletContextHandler handler = new ServletContextHandler();

        handler.setContextPath("/");
        handler.setInitParameter("contextConfigLocation", "classpath*:spring-context.xml");
        handler.setResourceBase(wwwBaseDir.get());
        handler.setWelcomeFiles(new String[]{"index.htm", "index.html", "index.jsp"});

        handler.setSessionHandler(new SessionHandler());
        handler.addEventListener(sprintContextListener);
        handler.addFilter(GzipFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST))
                .setInitParameter("mimeTypes", "application/json, application/xml,text/xml, text/html");

        handler.addServlet(jerseyServletHolder, "/api/*");
        handler.addServlet(staticServletHolder, "/*");

        //Create Jetty Server
        server = new Server(serverPort.get());
        server.setHandler(handler);
    }

    @Override
    protected void doStart() throws Exception {
        server.start();
    }

    @Override
    protected void doClose() throws Exception {
        server.stop();
    }
}
