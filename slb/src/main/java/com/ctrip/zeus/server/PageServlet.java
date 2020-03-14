package com.ctrip.zeus.server;

import org.eclipse.jetty.servlet.DefaultServlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author:xingchaowang
 * @date: 3/5/2015.
 */
public class PageServlet extends DefaultServlet {
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String uri = req.getRequestURI();
        if (uri.endsWith(".html") || uri.endsWith(".js") || uri.endsWith(".css") || uri.endsWith(".json") || uri.contains(".woff")|| uri.contains(".ttf")) {
            super.service(req, resp);
        } else {
            req.getRequestDispatcher(uri + ".html").forward(req,resp);
        }
    }
}
