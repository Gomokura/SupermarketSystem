package com.supermarket.servlet;

import com.supermarket.bean.User;
import com.supermarket.dao.CartDAO;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;

public class CartServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");
        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        CartDAO cartDAO = new CartDAO();

        if ("add".equals(action)) {
            int productId = Integer.parseInt(request.getParameter("productId"));
            int quantity = Integer.parseInt(request.getParameter("quantity"));
            cartDAO.addToCart(user.getUserId(), productId, quantity);
            response.sendRedirect(request.getContextPath() + "/user/cart.jsp");
        } else if ("delete".equals(action)) {
            int cartId = Integer.parseInt(request.getParameter("cartId"));
            cartDAO.deleteCart(cartId);
            response.sendRedirect(request.getContextPath() + "/user/cart.jsp");
        } else if ("update".equals(action)) {
            int cartId = Integer.parseInt(request.getParameter("cartId"));
            int quantity = Integer.parseInt(request.getParameter("quantity"));
            cartDAO.updateQuantity(cartId, quantity);
            response.sendRedirect(request.getContextPath() + "/user/cart.jsp");
        }
    }
}
