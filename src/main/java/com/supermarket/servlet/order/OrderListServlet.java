package com.supermarket.servlet.order;

import com.supermarket.entity.Result;
import com.supermarket.service.OrderService;
import com.supermarket.servlet.BaseServlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/** OrderListServlet - 订单列表 */
public class OrderListServlet extends BaseServlet {
    public void doAction(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer userId = getLoginUserId(req);
        if (userId == null) { jsonError(resp, "未登录"); return; }
        String status = getString(req, "status");
        int pageNum = getPageNum(req);
        int pageSize = getPageSize(req);

        OrderService orderService = new OrderService();
        Result<?> result = orderService.getUserOrders(userId, status, pageNum, pageSize);

        String ajax = req.getHeader("X-Requested-With");
        if ("XMLHttpRequest".equals(ajax)) {
            if (result.getCode() == 200) json(req, resp, result.getData());
            else jsonError(resp, result.getMessage());
        } else {
            req.setAttribute("orderPage", result.getData());
            forward(req, resp, "/views/orderList.jsp");
        }
    }
}
