package com.supermarket.servlet.user;

import com.supermarket.entity.Result;
import com.supermarket.service.AuthService;
import com.supermarket.servlet.BaseServlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * ChangePasswordServlet - 修改密码
 *
 * action=changePassword
 * 参数：oldPassword, newPassword
 */
public class ChangePasswordServlet extends BaseServlet {

    public void doAction(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Integer userId = getLoginUserId(req);
        if (userId == null) {
            jsonError(resp, "未登录");
            return;
        }

        String oldPassword = getString(req, "oldPassword");
        String newPassword = getString(req, "newPassword");

        if (oldPassword == null || oldPassword.isEmpty()) {
            jsonError(resp, "请输入原密码");
            return;
        }
        if (newPassword == null || newPassword.length() < 6) {
            jsonError(resp, "新密码长度不能少于6位");
            return;
        }

        AuthService authService = new AuthService();
        Result<?> result = authService.changePassword(userId, oldPassword, newPassword);

        if (result.getCode() == 200) {
            jsonMsg(req, resp, result.getMessage());
        } else {
            jsonError(resp, result.getMessage());
        }
    }
}
