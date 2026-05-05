package com.supermarket.servlet.cart;

import com.supermarket.entity.Result;
import com.supermarket.service.CartService;
import com.supermarket.servlet.BaseServlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/** CartCheckAllServlet - 全选/全不选 */
public class CartCheckAllServlet extends BaseServlet {
    public void doAction(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer userId = getLoginUserId(req);
        if (userId == null) { jsonError(resp, "未登录"); return; }
        Integer checked = getInteger(req, "checked", 0);
        CartService cartService = new CartService();
        Result<?> result = cartService.checkAll(userId, checked);
        if (result.getCode() == 200) jsonOk(req, resp);
        else jsonError(resp, result.getMessage());
    }
}
