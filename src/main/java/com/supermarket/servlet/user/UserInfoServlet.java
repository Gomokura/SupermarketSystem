package com.supermarket.servlet.user;

import com.supermarket.entity.Result;
import com.supermarket.service.AuthService;
import com.supermarket.servlet.BaseServlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * UserInfoServlet - 获取/查看用户信息
 *
 * action=userInfo
 */
public class UserInfoServlet extends BaseServlet {

    public void doAction(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Integer userId = getLoginUserId(req);
        if (userId == null) {
            jsonError(resp, "未登录");
            return;
        }

        AuthService authService = new AuthService();
        Result<?> result = authService.getUserInfo(userId);

        String ajax = req.getHeader("X-Requested-With");
        if ("XMLHttpRequest".equals(ajax)) {
            if (result.getCode() == 200) {
                json(req, resp, result.getData());
            } else {
                jsonError(resp, result.getMessage());
            }
        } else {
            if (result.getCode() == 200) {
                req.setAttribute("user", result.getData());
                forward(req, resp, "/views/userCenter.jsp");
            } else {
                req.setAttribute("error", result.getMessage());
                forward(req, resp, "/views/userCenter.jsp");
            }
        }
    }
}
