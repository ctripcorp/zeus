package com.ctrip.zeus.server;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author:xingchaowang
 * @date: 3/5/2015.
 */
public class ForwardServlet extends HttpServlet {
    private String target;

    public ForwardServlet(String target) {
        this.target = target;
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher(target).forward(req,resp);
    }
}
