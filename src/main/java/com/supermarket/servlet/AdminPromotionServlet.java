package com.supermarket.servlet;

import com.supermarket.bean.User;
import com.supermarket.dao.PromotionDAO;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class AdminPromotionServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = (User) request.getSession().getAttribute("user");
        if (user == null || !"admin".equals(user.getRole())) { response.sendRedirect("login.jsp"); return; }

        List<Map<String, Object>> promotions = new PromotionDAO().getAllPromotions();
        request.setAttribute("promotions", promotions);
        request.getRequestDispatcher("/admin/promotionList.jsp").forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        User user = (User) request.getSession().getAttribute("user");
        if (user == null || !"admin".equals(user.getRole())) { response.sendRedirect("login.jsp"); return; }

        String action = request.getParameter("action");
        PromotionDAO dao = new PromotionDAO();

        if ("add".equals(action)) {
            dao.addPromotion(
                request.getParameter("promoName"),
                request.getParameter("promoType"),
                Double.parseDouble(request.getParameter("conditionVal")),
                Double.parseDouble(request.getParameter("discountVal")),
                request.getParameter("startTime"),
                request.getParameter("endTime")
            );
        } else if ("updateStatus".equals(action)) {
            dao.updateStatus(Integer.parseInt(request.getParameter("promotionId")),
                             request.getParameter("status"));
        } else if ("delete".equals(action)) {
            dao.deletePromotion(Integer.parseInt(request.getParameter("promotionId")));
        }
        response.sendRedirect(request.getContextPath() + "/adminPromotion");
    }
}
