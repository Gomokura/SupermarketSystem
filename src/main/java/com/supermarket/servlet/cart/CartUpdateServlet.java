package com.supermarket.servlet.cart;

import com.supermarket.entity.Result;
import com.supermarket.service.CartService;
import com.supermarket.servlet.BaseServlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/** CartUpdateServlet - 更新购物车数量 */
public class CartUpdateServlet extends BaseServlet {
    public void doAction(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer userId = getLoginUserId(req);
        if (userId == null) { jsonError(resp, "未登录"); return; }
        Integer cartId = getInteger(req, "cartId");
        Integer quantity = getInteger(req, "quantity");
        CartService cartService = new CartService();
        Result<?> result = cartService.updateCartQuantity(userId, cartId, quantity);
        if (result.getCode() == 200) jsonOk(req, resp);
        else jsonError(resp, result.getMessage());
    }
}
