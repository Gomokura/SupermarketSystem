package com.supermarket.servlet;

import com.supermarket.bean.Order;
import com.supermarket.bean.OrderItem;
import com.supermarket.bean.User;
import com.supermarket.dao.OrderDAO;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;

public class AdminOrderServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = (User) request.getSession().getAttribute("user");
        if (user == null || !"admin".equals(user.getRole())) {
            response.sendRedirect("login.jsp");
            return;
        }

        String action = request.getParameter("action");
        OrderDAO orderDAO = new OrderDAO();

        if ("detail".equals(action)) {
            // 查看订单详情
            int orderId = Integer.parseInt(request.getParameter("orderId"));
            List<OrderItem> items = orderDAO.getOrderItems(orderId);
            request.setAttribute("items", items);
            request.setAttribute("orderId", orderId);
            request.getRequestDispatcher("/admin/orderDetail.jsp").forward(request, response);
        } else {
            // 默认：查询订单列表
            String keyword = request.getParameter("keyword");
            String status  = request.getParameter("status");
            List<Order> orders = orderDAO.searchAllOrders(keyword, status);
            request.setAttribute("orders", orders);
            request.setAttribute("keyword", keyword);
            request.setAttribute("status", status);
            request.getRequestDispatcher("/admin/orderList.jsp").forward(request, response);
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = (User) request.getSession().getAttribute("user");
        if (user == null || !"admin".equals(user.getRole())) {
            response.sendRedirect("login.jsp");
            return;
        }

        String action = request.getParameter("action");
        OrderDAO orderDAO = new OrderDAO();

        if ("updateStatus".equals(action)) {
            int orderId    = Integer.parseInt(request.getParameter("orderId"));
            String newStatus = request.getParameter("newStatus");
            orderDAO.updateOrderStatus(orderId, newStatus);
        }
        response.sendRedirect(request.getContextPath() + "/adminOrder?keyword=&status=");
    }
}
