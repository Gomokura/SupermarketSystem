package com.supermarket.servlet;

import com.supermarket.bean.Cart;
import com.supermarket.bean.User;
import com.supermarket.dao.CartDAO;
import com.supermarket.dao.OrderDAO;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;

public class OrderServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            response.sendRedirect("../login.jsp");
            return;
        }

        String paymentMethod = request.getParameter("paymentMethod");
        String discountType = request.getParameter("discount");

        double discount = 1.0;
        if ("discount9".equals(discountType)) discount = 0.9;
        else if ("discount100".equals(discountType)) discount = 0.8;

        CartDAO cartDAO = new CartDAO();
        List<Cart> items = cartDAO.getCartByUser(user.getUserId());

        if (items.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/user/cart.jsp?error=empty");
            return;
        }

        OrderDAO orderDAO = new OrderDAO();
        int orderId = orderDAO.createOrder(user.getUserId(), items, paymentMethod, discount);

        if (orderId > 0) {
            response.sendRedirect(request.getContextPath() + "/user/myOrders.jsp?success=1");
        } else {
            response.sendRedirect(request.getContextPath() + "/user/checkout.jsp?error=1");
        }
    }
}
