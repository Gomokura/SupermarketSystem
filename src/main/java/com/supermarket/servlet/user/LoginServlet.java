package com.supermarket.servlet.user;

import com.supermarket.entity.Result;
import com.supermarket.service.AuthService;
import com.supermarket.servlet.BaseServlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * LoginServlet - C端用户登录
 *
 * action=login
 * 参数：username, phone, password
 */
public class LoginServlet extends BaseServlet {

    public void doAction(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String username = getString(req, "username");
        String phone = getString(req, "phone");
        String password = getString(req, "password");

        if ((username == null || username.isEmpty()) && (phone == null || phone.isEmpty())) {
            req.setAttribute("error", "请输入用户名或手机号");
            forward(req, resp, "/views/login.jsp");
            return;
        }
        if (password == null || password.isEmpty()) {
            req.setAttribute("error", "请输入密码");
            forward(req, resp, "/views/login.jsp");
            return;
        }

        AuthService authService = new AuthService();
        Result<?> result = authService.login(username, phone, password);

        if (result.getCode() == 200) {
            HttpSession session = req.getSession(true);
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> data = (java.util.Map<String, Object>) result.getData();
            session.setAttribute("userId", data.get("userId"));
            session.setAttribute("username", data.get("username"));
            session.setAttribute("nickname", data.get("nickname"));
            session.setAttribute("avatarUrl", data.get("avatarUrl"));
            session.setAttribute("memberLevel", data.get("memberLevel"));
            session.setAttribute("points", data.get("points"));
            session.setAttribute("loginRole", "user");

            // 检查是否是 AJAX 请求
            String ajax = req.getHeader("X-Requested-With");
            if ("XMLHttpRequest".equals(ajax)) {
                json(req, resp, result.getData());
            } else {
                redirectAbs(resp, "/index.do");
            }
        } else {
            String ajax = req.getHeader("X-Requested-With");
            if ("XMLHttpRequest".equals(ajax)) {
                jsonError(resp, result.getMessage());
            } else {
                req.setAttribute("error", result.getMessage());
                forward(req, resp, "/views/login.jsp");
            }
        }
    }
}
