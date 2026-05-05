package com.supermarket.servlet.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.supermarket.entity.Order;
import com.supermarket.entity.Result;
import com.supermarket.service.OrderService;
import com.supermarket.servlet.BaseServlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** CashierServlet - 收银台 */
public class CashierServlet extends BaseServlet {
    public void doAction(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String method = getString(req, "method");
        if ("order".equals(method)) {
            doOrder(req, resp);
        } else if ("refund".equals(method)) {
            doRefund(req, resp);
        } else {
            forward(req, resp, "/views/admin/cashier.jsp");
        }
    }

    private void doOrder(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer adminId = getLoginAdminId(req);
        if (adminId == null) { jsonError(resp, "请先登录"); return; }
        String cartItemsJson = getString(req, "cartItems");
        String payMethod = getString(req, "payMethod");
        Double receivedAmount = getDouble(req, "receivedAmount");

        List<Map<String, Object>> cartItems = new ArrayList<>();
        if (cartItemsJson != null && !cartItemsJson.isEmpty()) {
            try {
                ObjectMapper om = new ObjectMapper();
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> parsed = om.readValue(cartItemsJson, List.class);
                cartItems.addAll(parsed);
            } catch (Exception e) {
                jsonError(resp, "商品数据解析失败"); return;
            }
        }
        if (cartItems.isEmpty()) { jsonError(resp, "请添加商品"); return; }

        OrderService orderService = new OrderService();
        Result<?> result = orderService.cashierCreateOrder(adminId, cartItems, payMethod, receivedAmount);
        if (result.getCode() == 200) json(req, resp, result.getData());
        else jsonError(resp, result.getMessage());
    }

    private void doRefund(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        jsonError(resp, "退款功能开发中");
    }
}
