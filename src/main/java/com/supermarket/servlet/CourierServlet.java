package com.supermarket.servlet;

import com.supermarket.bean.User;
import com.supermarket.dao.DeliveryDAO;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class CourierServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = (User) request.getSession().getAttribute("user");
        if (user == null || !"courier".equals(user.getRole())) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }
        List<Map<String, Object>> tasks = new DeliveryDAO().getTasksByCourier(user.getUserId());
        request.setAttribute("tasks", tasks);
        request.getRequestDispatcher("/courier/taskList.jsp").forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = (User) request.getSession().getAttribute("user");
        if (user == null || !"courier".equals(user.getRole())) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }
        int deliveryId = Integer.parseInt(request.getParameter("deliveryId"));
        String newStatus = request.getParameter("newStatus");
        new DeliveryDAO().updateStatus(deliveryId, newStatus);
        response.sendRedirect(request.getContextPath() + "/courier/tasks");
    }
}
