package com.supermarket.servlet.cart;

import com.supermarket.entity.Result;
import com.supermarket.service.CartService;
import com.supermarket.servlet.BaseServlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

/** CartBatchDeleteServlet - 批量删除 */
public class CartBatchDeleteServlet extends BaseServlet {
    public void doAction(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer userId = getLoginUserId(req);
        if (userId == null) { jsonError(resp, "未登录"); return; }
        String ids = getString(req, "cartIds");
        if (ids == null || ids.isEmpty()) { jsonOk(req, resp); return; }
        ArrayList<Integer> cartIds = new ArrayList<>();
        for (String id : ids.split(",")) {
            try { cartIds.add(Integer.valueOf(id.trim())); } catch (Exception ignored) {}
        }
        CartService cartService = new CartService();
        Result<?> result = cartService.batchDelete(userId, cartIds);
        if (result.getCode() == 200) jsonOk(req, resp);
        else jsonError(resp, result.getMessage());
    }
}
