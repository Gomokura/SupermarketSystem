package com.supermarket.servlet.order;

import com.supermarket.entity.Result;
import com.supermarket.service.OrderService;
import com.supermarket.servlet.BaseServlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/** OrderDetailServlet - 订单详情 */
public class OrderDetailServlet extends BaseServlet {
    public void doAction(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer userId = getLoginUserId(req);
        Integer orderId = getInteger(req, "orderId");
        if (orderId == null) { jsonError(resp, "订单ID不能为空"); return; }

        OrderService orderService = new OrderService();
        Result<?> result = orderService.getOrderDetail(orderId, userId);

        String ajax = req.getHeader("X-Requested-With");
        if ("XMLHttpRequest".equals(ajax)) {
            if (result.getCode() == 200) json(req, resp, result.getData());
            else jsonError(resp, result.getMessage());
        } else {
            if (result.getCode() == 200) {
                req.setAttribute("order", result.getData());
                forward(req, resp, "/views/orderDetail.jsp");
            } else {
                req.setAttribute("error", result.getMessage());
                forward(req, resp, "/views/orderList.jsp");
            }
        }
    }
}
