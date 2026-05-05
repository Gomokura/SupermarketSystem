package com.supermarket.servlet.order;

import com.fasterxml.jackson.databind.ObjectMapper;
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

/** OrderCreateServlet - 创建订单 */
public class OrderCreateServlet extends BaseServlet {
    public void doAction(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer userId = getLoginUserId(req);
        if (userId == null) { jsonError(resp, "未登录"); return; }

        Integer addressId = getInteger(req, "addressId");
        String paymentMethod = getString(req, "paymentMethod");
        String cartItemsJson = getString(req, "cartItems");
        Integer couponId = getInteger(req, "couponId");
        Integer pointsUsed = getInteger(req, "pointsUsed");
        String remark = getString(req, "remark");
        String deliveryTimeSlot = getString(req, "deliveryTimeSlot");

        if (addressId == null) { jsonError(resp, "请选择收货地址"); return; }

        List<Map<String, Object>> cartItems = new ArrayList<>();
        if (cartItemsJson != null && !cartItemsJson.isEmpty()) {
            try {
                ObjectMapper om = new ObjectMapper();
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> parsed = om.readValue(cartItemsJson, List.class);
                cartItems.addAll(parsed);
            } catch (Exception e) {
                jsonError(resp, "购物车数据解析失败");
                return;
            }
        }

        OrderService orderService = new OrderService();
        Result<?> result = orderService.createOrder(userId, addressId, paymentMethod,
                cartItems, couponId, pointsUsed, remark, deliveryTimeSlot);

        if (result.getCode() == 200) {
            json(req, resp, result.getData());
        } else {
            jsonError(resp, result.getMessage());
        }
    }
}
