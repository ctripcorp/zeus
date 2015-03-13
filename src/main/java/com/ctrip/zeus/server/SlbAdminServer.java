package com.ctrip.zeus.server;

import com.ctrip.zeus.restful.resource.SlbResourcePackage;
import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;
import org.apache.jasper.servlet.JspServlet;
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
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
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
        DynamicStringProperty springContextFile = DynamicPropertyFactory.getInstance().getStringProperty("server.spring.context-file", "spring-context.xml");

        //Config Jersey
        ResourceConfig config = new ResourceConfig();
        config.packages(SlbResourcePackage.class.getPackage().getName());

        //Create and Config Jetty Request Handler
        ServletContextHandler handler = new ServletContextHandler();
        handler.setContextPath("/");
        handler.setSessionHandler(new SessionHandler());

        //Add Default Servlet
        handler.setResourceBase(wwwBaseDir.get());
        handler.setWelcomeFiles(new String[]{"index.htm", "index.html", "main.jsp", "index.jsp"});
        DefaultServlet staticServlet = new DefaultServlet();
        ServletHolder staticServletHolder = new ServletHolder(staticServlet);

        //Support jsp
        supportJsp(handler);

        //Support Spring
        handler.setInitParameter("contextConfigLocation", "classpath*:" + springContextFile.get());
        ContextLoaderListener sprintContextListener = new ContextLoaderListener();
        handler.addEventListener(sprintContextListener);

        //Support Jersey
        ServletContainer jerseyServletContainer = new ServletContainer(config);
        ServletHolder jerseyServletHolder = new ServletHolder(jerseyServletContainer);

        //Support GZip
        handler.addFilter(GzipFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST))
                .setInitParameter("mimeTypes", "application/json, application/xml,text/xml, text/html");

        //Config Servlet
        handler.addServlet(jerseyServletHolder, "/api/*");
        handler.addServlet(staticServletHolder, "/");
        handler.addServlet(new ServletHolder(new ForwardServlet("/index.jsp")), "/test/*");
        handler.addServlet(new ServletHolder(new ForwardServlet("/main.jsp")), "/app");
        handler.addServlet(new ServletHolder(new ForwardServlet("/main.jsp")), "/slb");

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
