package com.supermarket.servlet.order;

import com.supermarket.entity.Result;
import com.supermarket.service.OrderService;
import com.supermarket.servlet.BaseServlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/** OrderPayServlet - 支付订单 */
public class OrderPayServlet extends BaseServlet {
    public void doAction(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer userId = getLoginUserId(req);
        if (userId == null) { jsonError(resp, "未登录"); return; }
        Integer orderId = getInteger(req, "orderId");
        String payMethod = getString(req, "paymentMethod");
        if (orderId == null) { jsonError(resp, "订单ID不能为空"); return; }
        OrderService orderService = new OrderService();
        Result<?> result = orderService.payOrder(orderId, userId, payMethod);
        if (result.getCode() == 200) jsonMsg(req, resp, result.getMessage());
        else jsonError(resp, result.getMessage());
    }
}
