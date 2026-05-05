package com.supermarket.servlet.user;

import com.supermarket.entity.Result;
import com.supermarket.service.AuthService;
import com.supermarket.servlet.BaseServlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * RegisterServlet - C端用户注册
 *
 * action=register
 * 参数：username, password, realName, phone, email, nickname
 */
public class RegisterServlet extends BaseServlet {

    public void doAction(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String username = getString(req, "username");
        String password = getString(req, "password");
        String realName = getString(req, "realName");
        String phone = getString(req, "phone");
        String email = getString(req, "email");
        String nickname = getString(req, "nickname");

        if (username == null || username.isEmpty()) {
            req.setAttribute("error", "用户名不能为空");
            forward(req, resp, "/views/register.jsp");
            return;
        }
        if (password == null || password.isEmpty()) {
            req.setAttribute("error", "密码不能为空");
            forward(req, resp, "/views/register.jsp");
            return;
        }
        if (password.length() < 6) {
            req.setAttribute("error", "密码长度不能少于6位");
            forward(req, resp, "/views/register.jsp");
            return;
        }

        AuthService authService = new AuthService();
        Result<?> result = authService.register(username, password, realName, phone, email, nickname);

        if (result.getCode() == 200) {
            String ajax = req.getHeader("X-Requested-With");
            if ("XMLHttpRequest".equals(ajax)) {
                jsonMsg(req, resp, "注册成功");
            } else {
                req.setAttribute("success", "注册成功，请登录");
                forward(req, resp, "/views/login.jsp");
            }
        } else {
            String ajax = req.getHeader("X-Requested-With");
            if ("XMLHttpRequest".equals(ajax)) {
                jsonError(resp, result.getMessage());
            } else {
                req.setAttribute("error", result.getMessage());
                forward(req, resp, "/views/register.jsp");
            }
        }
    }
}
