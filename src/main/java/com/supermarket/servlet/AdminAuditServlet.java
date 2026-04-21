package com.supermarket.servlet;

import com.supermarket.bean.User;
import com.supermarket.dao.AuditLogDAO;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class AdminAuditServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = (User) request.getSession().getAttribute("user");
        if (user == null || !"admin".equals(user.getRole())) { response.sendRedirect("login.jsp"); return; }

        String username = request.getParameter("username");
        String action   = request.getParameter("action");
        List<Map<String, Object>> logs = new AuditLogDAO().searchLogs(username, action, 200);
        request.setAttribute("logs",     logs);
        request.setAttribute("username", username);
        request.setAttribute("action",   action);
        request.getRequestDispatcher("/admin/auditLog.jsp").forward(request, response);
    }
}
