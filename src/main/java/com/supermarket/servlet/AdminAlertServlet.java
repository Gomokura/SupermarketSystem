package com.supermarket.servlet;

import com.supermarket.bean.User;
import com.supermarket.dao.AlertDAO;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;

public class AdminAlertServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = (User) request.getSession().getAttribute("user");
        if (user == null || !"admin".equals(user.getRole())) { response.sendRedirect("login.jsp"); return; }

        AlertDAO dao = new AlertDAO();
        request.setAttribute("negativeStock",  dao.getNegativeStock());
        request.setAttribute("criticalStock",  dao.getCriticalStock());
        request.setAttribute("largeOrders",    dao.getLargeOrders());
        request.setAttribute("staleOrders",    dao.getStaleOrders(3));
        request.getRequestDispatcher("/admin/alertCenter.jsp").forward(request, response);
    }
}
