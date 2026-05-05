package com.supermarket.servlet.admin;

import com.supermarket.entity.Result;
import com.supermarket.service.OrderService;
import com.supermarket.servlet.BaseServlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * OrderManageServlet - 管理后台订单管理
 * action=adminOrderList / adminOrderShip / adminOrderCancel / adminOrderAssignCourier
 */
public class OrderManageServlet extends BaseServlet {

    public void doAction(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String method = getString(req, "method");
        if ("ship".equals(method)) {
            doShip(req, resp);
        } else if ("cancel".equals(method)) {
            doCancel(req, resp);
        } else if ("assignCourier".equals(method)) {
            doAssignCourier(req, resp);
        } else {
            doList(req, resp);
        }
    }

    private void doList(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String status = getString(req, "status");
        String orderNo = getString(req, "orderNo");
        Integer userId = getInteger(req, "userId");
        int pageNum = getPageNum(req);
        int pageSize = getPageSize(req);

        OrderService orderService = new OrderService();
        Result<?> result = orderService.adminGetOrderList(status, orderNo, userId, pageNum, pageSize);

        String ajax = req.getHeader("X-Requested-With");
        if ("XMLHttpRequest".equals(ajax)) {
            if (result.getCode() == 200) json(req, resp, result.getData());
            else jsonError(resp, result.getMessage());
        } else {
            req.setAttribute("orderPage", result.getData());
            forward(req, resp, "/views/admin/orderManage.jsp");
        }
    }

    private void doShip(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer orderId = getInteger(req, "orderId");
        Integer adminId = getLoginAdminId(req);
        String expressCompany = getString(req, "expressCompany");
        String expressNo = getString(req, "expressNo");
        if (orderId == null) { jsonError(resp, "订单ID不能为空"); return; }

        OrderService orderService = new OrderService();
        Result<?> result = orderService.shipOrder(orderId, adminId, expressCompany, expressNo);
        if (result.getCode() == 200) jsonMsg(req, resp, result.getMessage());
        else jsonError(resp, result.getMessage());
    }

    private void doCancel(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer orderId = getInteger(req, "orderId");
        Integer adminId = getLoginAdminId(req);
        String reason = getString(req, "reason");
        if (orderId == null) { jsonError(resp, "订单ID不能为空"); return; }

        OrderService orderService = new OrderService();
        Result<?> result = orderService.adminCancelOrder(orderId, adminId, reason);
        if (result.getCode() == 200) jsonMsg(req, resp, result.getMessage());
        else jsonError(resp, result.getMessage());
    }

    private void doAssignCourier(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        jsonError(resp, "分配配送员功能开发中");
    }
}
