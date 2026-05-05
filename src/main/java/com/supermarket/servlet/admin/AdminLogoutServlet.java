package com.supermarket.servlet.admin;

import com.supermarket.servlet.BaseServlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * AdminLogoutServlet - 管理员退出
 * action=adminLogout
 */
public class AdminLogoutServlet extends BaseServlet {
    public void doAction(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session != null) session.invalidate();
        redirectAbs(resp, "/views/admin/login.jsp");
    }
}
