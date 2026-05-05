package com.supermarket.servlet.cart;

import com.supermarket.entity.Result;
import com.supermarket.service.CartService;
import com.supermarket.servlet.BaseServlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/** CartSummaryServlet - 选中商品统计 */
public class CartSummaryServlet extends BaseServlet {
    public void doAction(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer userId = getLoginUserId(req);
        if (userId == null) { jsonError(resp, "未登录"); return; }
        CartService cartService = new CartService();
        Result<?> result = cartService.checkedSummary(userId);
        if (result.getCode() == 200) json(req, resp, result.getData());
        else jsonError(resp, result.getMessage());
    }
}
