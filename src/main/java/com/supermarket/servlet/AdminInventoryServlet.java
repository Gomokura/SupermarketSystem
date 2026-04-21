package com.supermarket.servlet;

import com.supermarket.bean.User;
import com.supermarket.dao.InventoryDAO;
import com.supermarket.dao.ProductDAO;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class AdminInventoryServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = (User) request.getSession().getAttribute("user");
        if (user == null || !"admin".equals(user.getRole())) { response.sendRedirect("login.jsp"); return; }

        InventoryDAO dao = new InventoryDAO();
        String keyword = request.getParameter("keyword");
        List<Map<String, Object>> logs      = dao.searchLogs(keyword);
        List<Map<String, Object>> lowStock  = dao.getLowStockProducts(10);

        request.setAttribute("logs",     logs);
        request.setAttribute("lowStock", lowStock);
        request.setAttribute("keyword",  keyword);
        request.getRequestDispatcher("/admin/inventoryList.jsp").forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        User user = (User) request.getSession().getAttribute("user");
        if (user == null || !"admin".equals(user.getRole())) { response.sendRedirect("login.jsp"); return; }

        int    productId  = Integer.parseInt(request.getParameter("productId"));
        String changeType = request.getParameter("changeType");
        int    quantity   = Integer.parseInt(request.getParameter("quantity"));
        String remark     = request.getParameter("remark");

        new InventoryDAO().adjustStock(productId, changeType, quantity, user.getUserId(), remark);
        response.sendRedirect(request.getContextPath() + "/adminInventory");
    }
}
