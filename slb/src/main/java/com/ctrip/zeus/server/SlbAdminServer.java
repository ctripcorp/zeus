package com.ctrip.zeus.server;

import com.ctrip.zeus.auth.impl.IPAuthenticationFilter;
import com.ctrip.zeus.auth.impl.SetAuthTokenFilter;
import com.ctrip.zeus.auth.impl.SlbServerTokenAuthFilter;
import com.ctrip.zeus.auth.impl.TokenAuthFilter;
import com.ctrip.zeus.restful.resource.SlbResourcePackage;
import com.ctrip.zeus.server.config.SlbAdminResourceConfig;
import com.ctrip.zeus.service.auth.auto.filter.UserCookieAuthFilter;
import com.ctrip.zeus.service.auth.auto.filter.UserLoginCheckFilter;
import com.ctrip.zeus.service.auto.DBInitializationFilter;
import com.ctrip.zeus.service.config.impl.SlbConfigConfiguration;
import com.ctrip.zeus.startup.SpringInitializationNotifier;
import com.ctrip.zeus.util.AccessLogFilter;
import com.netflix.config.*;
import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.jasper.servlet.JspServlet;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.StatisticsHandler;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlets.GzipFilter;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.ContextLoaderListener;

import javax.servlet.DispatcherType;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.EnumSet;

/**
 * @author:xingchaowang
 * @date: 3/4/2015.
 */
public class SlbAdminServer extends AbstractServer {

    private Server server;
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public SlbAdminServer() throws Exception {
    }

    @Override
    protected void preLoad() {
        loadConfiguration();
    }

    private void loadConfiguration() {
        System.setProperty(DynamicPropertyFactory.ENABLE_JMX, "true");

        // Loading properties via archaius.
        if (null != appName) {
            try {
                ConfigurationManager.loadCascadedPropertiesFromResources(appName);

                AbstractConfiguration instance = ConfigurationManager.getConfigInstance();
                if (instance instanceof ConcurrentCompositeConfiguration) {
                    ConcurrentCompositeConfiguration compositeConfiguration = (ConcurrentCompositeConfiguration) instance;

                    String temp = System.getProperty("slb.config.url", "http://localhost:8099/api/config/all");
                    DynamicConfiguration slbConfigConfiguration = new SlbConfigConfiguration(60000, 30000, false, temp);
                    compositeConfiguration.addConfigurationAtFront(slbConfigConfiguration, "slbConfig");
                }

            } catch (IOException e) {
                logger.error(String.format(
                        "Failed to load properties for application id: %s and environment: %s. This is ok, if you do not have application level properties.",
                        appName,
                        ConfigurationManager.getDeploymentContext().getDeploymentEnvironment()), e);
            }
        } else {
            logger.warn(
                    "Application identifier not defined, skipping application level properties loading. You must set a property 'archaius.deployment.applicationId' to be able to load application level properties.");
        }
    }

    @Override
    protected void init() throws Exception {

        DynamicIntProperty serverPort = DynamicPropertyFactory.getInstance().getIntProperty("server.port", 8099);
        DynamicStringProperty wwwBaseDir = DynamicPropertyFactory.getInstance().getStringProperty("server.www.base-dir", ".");
        DynamicStringProperty springContextFile = DynamicPropertyFactory.getInstance().getStringProperty("server.spring.context-file", "spring-context.xml");
        DynamicIntProperty sessionTimeout = DynamicPropertyFactory.getInstance().getIntProperty("server.session.timeout", 60 * 5);
        DynamicIntProperty maxThreads = DynamicPropertyFactory.getInstance().getIntProperty("server.max.threads", 300);
        DynamicIntProperty minThreads = DynamicPropertyFactory.getInstance().getIntProperty("server.min.threads", 50);
        DynamicStringProperty appId = DynamicPropertyFactory.getInstance().getStringProperty("appId", "100000716");
        //Config Jersey
        ResourceConfig config = new SlbAdminResourceConfig();
        config.packages(SlbResourcePackage.class.getPackage().getName());
        //Create and Config Jetty Request Handler
        ServletContextHandler handler = new ServletContextHandler();
        handler.setContextPath("/");
        SessionHandler sessionHandler = new SessionHandler();
        sessionHandler.getSessionManager().setMaxInactiveInterval(sessionTimeout.get());
        handler.setSessionHandler(sessionHandler);
        //Add Default Servlet
        handler.setResourceBase(wwwBaseDir.get());
        handler.setWelcomeFiles(new String[]{"index.htm", "index.html", "main.jsp", "index.jsp"});
        DefaultServlet staticServlet = new DefaultServlet();
        ServletHolder staticServletHolder = new ServletHolder(staticServlet);
        //Support jsp
        supportJsp(handler);
        //Support Spring
        handler.setInitParameter("contextConfigLocation", "classpath*:" + springContextFile.get()); //+ ",classpath*:spring-context-security.xml");
        handler.setInitParameter("org.eclipse.jetty.servlet.Default.dirAllowed", "false");
        handler.setInitParameter("org.eclipse.jetty.servlet.SessionIdPathParameterName", "none");
        ContextLoaderListener sprintContextListener = new ContextLoaderListener();
        handler.addEventListener(sprintContextListener);
        handler.addEventListener(new SpringInitializationNotifier());
        //Support Jersey
        ServletContainer jerseyServletContainer = new ServletContainer(config);
        ServletHolder jerseyServletHolder = new ServletHolder(jerseyServletContainer);
        //Support CrossDomain
        handler.addFilter(CrossDomainFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
        //Support GZip
        handler.addFilter(GzipFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST))
                .setInitParameter("mimeTypes", "application/json, application/xml,text/xml, text/html");
        handler.addFilter(SlbServerTokenAuthFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
        handler.addFilter(TokenAuthFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
        handler.addFilter(IPAuthenticationFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));

        handler.addFilter(SetAuthTokenFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
        handler.addFilter(UserCookieAuthFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
        handler.addFilter(DBInitializationFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
        handler.addFilter(UserLoginCheckFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));

        handler.addFilter(PreCheckFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
        handler.addFilter(AccessLogFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD));

        //Config Servlet
        handler.addServlet(jerseyServletHolder, "/api/*");
        handler.addServlet(staticServletHolder, "/");
        handler.addServlet(new ServletHolder(new PageServlet()), "/portal/*");
        handler.addServlet(new ServletHolder(new ForwardServlet("/status.jsp")), "/status");
        handler.addServlet(new ServletHolder(new ForwardServlet("/index.jsp")), "/test/*");
        handler.addServlet(new ServletHolder(new ForwardServlet("/main.jsp")), "/slb");
        handler.addServlet(new ServletHolder(new ForwardServlet("/app.jsp")), "/app");
        handler.addServlet(new ServletHolder(new ForwardServlet("/op.jsp")), "/op");


        // Set Statistics Handler for graceful shutdown handling
        StatisticsHandler statsHandler = new StatisticsHandler();
        statsHandler.setHandler(handler);

        //Create Jetty Server
        QueuedThreadPool threadPool = new QueuedThreadPool(maxThreads.get(), minThreads.get());
        server = new Server(threadPool);
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(serverPort.get());
        server.setHandler(statsHandler);
        server.setConnectors(new Connector[]{connector});
        server.setStopTimeout(30000L);
    }


    @Override
    protected void doStart() throws Exception {
        server.start();
    }

    @Override
    protected void doClose() throws Exception {
        server.stop();
    }

    private void supportJsp(ServletContextHandler handler) {
        DynamicStringProperty tempDir = DynamicPropertyFactory.getInstance().getStringProperty("server.temp-dir", ".");

        //Support jsp
        System.setProperty("org.apache.jasper.compiler.disablejsr199", "false");
        ClassLoader jspClassLoader = new URLClassLoader(new URL[0], this.getClass().getClassLoader());
        handler.setClassLoader(jspClassLoader);
        File f = new File(tempDir.get());
        if (!f.exists()) {
            f.mkdirs();
        }
        handler.setAttribute("javax.servlet.context.tempdir", f);
        ServletHolder jspServletHolder = new ServletHolder("jsp", JspServlet.class);
        jspServletHolder.setInitOrder(0);

        handler.addServlet(jspServletHolder, "*.jsp");
    }
}
