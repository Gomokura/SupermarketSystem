package com.supermarket.servlet.admin;

import com.supermarket.entity.Result;
import com.supermarket.service.AuthService;
import com.supermarket.servlet.BaseServlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Map;

/**
 * AdminLoginServlet - B端管理员登录
 * action=adminLogin
 */
public class AdminLoginServlet extends BaseServlet {
    public void doAction(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String username = getString(req, "username");
        String password = getString(req, "password");

        if (username == null || username.isEmpty()) {
            req.setAttribute("error", "用户名不能为空");
            forward(req, resp, "/views/admin/login.jsp");
            return;
        }
        if (password == null || password.isEmpty()) {
            req.setAttribute("error", "密码不能为空");
            forward(req, resp, "/views/admin/login.jsp");
            return;
        }

        AuthService authService = new AuthService();
        Result<?> result = authService.adminLogin(username, password);

        if (result.getCode() == 200) {
            HttpSession session = req.getSession(true);
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) result.getData();
            session.setAttribute("adminId", data.get("adminId"));
            session.setAttribute("adminUsername", data.get("username"));
            session.setAttribute("adminRealName", data.get("realName"));
            session.setAttribute("adminRole", data.get("role"));
            session.setAttribute("loginRole", "admin");

            redirectAbs(resp, "/adminDashboard.do");
        } else {
            req.setAttribute("error", result.getMessage());
            forward(req, resp, "/views/admin/login.jsp");
        }
    }
}
