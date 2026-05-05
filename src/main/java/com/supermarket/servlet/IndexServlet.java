package com.supermarket.servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * IndexServlet - 首页控制器
 *
 * 处理 /index.do 请求，转发到首页 JSP
 */
public class IndexServlet extends BaseServlet {

    public void doAction(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // 转发到首页 JSP
        forward(req, resp, "/views/index.jsp");
    }
}
