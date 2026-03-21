package com.supermarket.servlet;

import com.supermarket.bean.User;
import com.supermarket.dao.FinanceDAO;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class AdminFinanceServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = (User) request.getSession().getAttribute("user");
        if (user == null || !"admin".equals(user.getRole())) { response.sendRedirect("login.jsp"); return; }

        FinanceDAO dao = new FinanceDAO();
        request.setAttribute("summary",         dao.getSummary());
        request.setAttribute("monthlyRevenue",  dao.getMonthlyRevenue(6));
        request.setAttribute("monthlyCost",     dao.getMonthlyCost(6));
        request.setAttribute("categoryRevenue", dao.getCategoryRevenue());
        request.getRequestDispatcher("/admin/financeReport.jsp").forward(request, response);
    }
}
