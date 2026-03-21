package com.supermarket.servlet;

import com.supermarket.bean.User;
import com.supermarket.dao.PriceHistoryDAO;
import com.supermarket.dao.ProductDAO;
import com.supermarket.dao.AuditLogDAO;
import com.supermarket.bean.Product;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class AdminPriceServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = (User) request.getSession().getAttribute("user");
        if (user == null || !"admin".equals(user.getRole())) { response.sendRedirect("login.jsp"); return; }

        String pidStr = request.getParameter("productId");
        PriceHistoryDAO dao = new PriceHistoryDAO();

        if (pidStr != null && !pidStr.isEmpty()) {
            int pid = Integer.parseInt(pidStr);
            request.setAttribute("history",   dao.getHistory(pid));
            request.setAttribute("productId", pid);
        } else {
            request.setAttribute("recent", dao.getRecentChanges(50));
        }
        request.setAttribute("products", new ProductDAO().searchProducts(null, null, null));
        request.getRequestDispatcher("/admin/priceHistory.jsp").forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        User user = (User) request.getSession().getAttribute("user");
        if (user == null || !"admin".equals(user.getRole())) { response.sendRedirect("login.jsp"); return; }

        int    productId = Integer.parseInt(request.getParameter("productId"));
        double newPrice  = Double.parseDouble(request.getParameter("newPrice"));
        String remark    = request.getParameter("remark");

        // 查旧价格
        List<Product> products = new ProductDAO().searchProducts(null, null, null);
        double oldPrice = products.stream()
            .filter(p -> p.getProductId() == productId)
            .findFirst().map(Product::getPrice).orElse(0.0);

        // 更新价格
        Product p = new Product();
        p.setProductId(productId);
        p.setPrice(newPrice);
        products.stream().filter(pr -> pr.getProductId() == productId).findFirst().ifPresent(pr -> {
            p.setProductName(pr.getProductName());
            p.setCategoryId(pr.getCategoryId());
            p.setStock(pr.getStock());
            p.setUnit(pr.getUnit());
            p.setSupplier(pr.getSupplier());
            p.setStatus(pr.getStatus());
        });
        new ProductDAO().updateProduct(p);

        // 记录历史
        new PriceHistoryDAO().record(productId, oldPrice, newPrice, user.getUserId(), remark);
        new AuditLogDAO().log(user.getUserId(), user.getUsername(), "调整价格",
            "product_id:" + productId, oldPrice + " -> " + newPrice, request.getRemoteAddr());

        response.sendRedirect(request.getContextPath() + "/adminPrice");
    }
}
