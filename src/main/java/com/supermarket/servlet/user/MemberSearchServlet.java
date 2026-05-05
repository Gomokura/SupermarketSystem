package com.supermarket.servlet.user;

import com.supermarket.entity.Result;
import com.supermarket.service.AuthService;
import com.supermarket.servlet.BaseServlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * MemberSearchServlet - 收银台按手机号查找会员
 *
 * action=memberSearch
 * 参数：phone
 */
public class MemberSearchServlet extends BaseServlet {

    public void doAction(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String phone = getString(req, "phone");

        AuthService authService = new AuthService();
        Result<?> result = authService.getMemberByPhone(phone);

        if (result.getCode() == 200) {
            json(req, resp, result.getData());
        } else {
            jsonError(resp, result.getCode(), result.getMessage());
        }
    }
}
