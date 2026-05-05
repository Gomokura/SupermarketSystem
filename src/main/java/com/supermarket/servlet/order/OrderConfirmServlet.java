package com.supermarket.servlet.order;

import com.supermarket.entity.Result;
import com.supermarket.service.OrderService;
import com.supermarket.servlet.BaseServlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/** OrderConfirmServlet - 确认收货 */
public class OrderConfirmServlet extends BaseServlet {
    public void doAction(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer userId = getLoginUserId(req);
        if (userId == null) { jsonError(resp, "未登录"); return; }
        Integer orderId = getInteger(req, "orderId");
        if (orderId == null) { jsonError(resp, "订单ID不能为空"); return; }
        OrderService orderService = new OrderService();
        Result<?> result = orderService.confirmReceipt(orderId, userId);
        if (result.getCode() == 200) jsonMsg(req, resp, result.getMessage());
        else jsonError(resp, result.getMessage());
    }
}
