package com.supermarket.servlet;

import com.supermarket.bean.User;
import com.supermarket.dao.DeliveryDAO;
import com.supermarket.dao.UserDAO;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class AdminDeliveryServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = (User) request.getSession().getAttribute("user");
        if (user == null || !"admin".equals(user.getRole())) { response.sendRedirect("login.jsp"); return; }

        String status = request.getParameter("status");
        List<Map<String, Object>> deliveries = new DeliveryDAO().searchDeliveries(status);
        List<User> couriers = new UserDAO().getUsersByRole("courier");
        request.setAttribute("deliveries", deliveries);
        request.setAttribute("couriers", couriers);
        request.setAttribute("status", status);
        request.getRequestDispatcher("/admin/deliveryList.jsp").forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = (User) request.getSession().getAttribute("user");
        if (user == null || !"admin".equals(user.getRole())) { response.sendRedirect("login.jsp"); return; }

        String action = request.getParameter("action");
        int deliveryId = Integer.parseInt(request.getParameter("deliveryId"));
        DeliveryDAO dao = new DeliveryDAO();

        if ("assign".equals(action)) {
            int courierId = Integer.parseInt(request.getParameter("courierId"));
            dao.assignCourier(deliveryId, courierId);
        } else {
            String newStatus = request.getParameter("newStatus");
            dao.updateStatus(deliveryId, newStatus);
        }
        response.sendRedirect(request.getContextPath() + "/adminDelivery");
    }
}
