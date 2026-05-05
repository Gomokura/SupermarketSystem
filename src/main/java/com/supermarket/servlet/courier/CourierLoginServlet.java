package com.supermarket.servlet.courier;

import com.supermarket.entity.Result;
import com.supermarket.service.AuthService;
import com.supermarket.servlet.BaseServlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Map;

/** CourierLoginServlet - 配送员登录 */
public class CourierLoginServlet extends BaseServlet {
    public void doAction(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String username = getString(req, "username");
        String password = getString(req, "password");
        if (username == null || username.isEmpty()) {
            req.setAttribute("error", "手机号/姓名不能为空");
            forward(req, resp, "/views/courier/login.jsp"); return;
        }
        if (password == null || password.isEmpty()) {
            req.setAttribute("error", "密码不能为空");
            forward(req, resp, "/views/courier/login.jsp"); return;
        }
        AuthService authService = new AuthService();
        Result<?> result = authService.courierLogin(username, password);
        if (result.getCode() == 200) {
            HttpSession session = req.getSession(true);
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) result.getData();
            session.setAttribute("courierId", data.get("courierId"));
            session.setAttribute("courierName", data.get("courierName"));
            session.setAttribute("loginRole", "courier");
            redirectAbs(resp, "/courierTaskList.do");
        } else {
            req.setAttribute("error", result.getMessage());
            forward(req, resp, "/views/courier/login.jsp");
        }
    }
}
