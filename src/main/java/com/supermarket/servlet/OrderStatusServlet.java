package com.supermarket.servlet;

import com.supermarket.dao.OrderDAO;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;

public class OrderStatusServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int orderId = Integer.parseInt(request.getParameter("orderId"));
        String status = request.getParameter("status");

        OrderDAO orderDAO = new OrderDAO();
        orderDAO.updateOrderStatus(orderId, status);

        response.sendRedirect(request.getContextPath() + "/user/myOrders.jsp");
    }
}
