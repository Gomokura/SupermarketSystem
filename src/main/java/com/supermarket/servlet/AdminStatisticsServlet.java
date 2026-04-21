package com.supermarket.servlet;

import com.supermarket.bean.User;
import com.supermarket.dao.OrderDAO;
import com.supermarket.dao.ProductDAO;
import com.supermarket.dao.UserDAO;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class AdminStatisticsServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = (User) request.getSession().getAttribute("user");
        if (user == null || !"admin".equals(user.getRole())) {
            response.sendRedirect("login.jsp");
            return;
        }

        OrderDAO   orderDAO   = new OrderDAO();
        ProductDAO productDAO = new ProductDAO();
        UserDAO    userDAO    = new UserDAO();

        int    totalProducts = productDAO.countAllProducts();
        int    totalUsers    = userDAO.countAllUsers();
        int    totalOrders   = orderDAO.countAllOrders();
        double totalRevenue  = orderDAO.sumTotalAmount();
        List<Map<String, Object>> topProducts = orderDAO.getTopProducts(5);

        request.setAttribute("totalProducts", totalProducts);
        request.setAttribute("totalUsers",    totalUsers);
        request.setAttribute("totalOrders",   totalOrders);
        request.setAttribute("totalRevenue",  totalRevenue);
        request.setAttribute("topProducts",   topProducts);

        request.getRequestDispatcher("/admin/statistics.jsp").forward(request, response);
    }
}
