package com.supermarket.servlet;

import com.supermarket.bean.User;
import com.supermarket.dao.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class AdminPurchaseServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = (User) request.getSession().getAttribute("user");
        if (user == null || !"admin".equals(user.getRole())) { response.sendRedirect("login.jsp"); return; }

        String action = request.getParameter("action");
        if ("detail".equals(action)) {
            int poId = Integer.parseInt(request.getParameter("poId"));
            List<Map<String, Object>> items = new PurchaseDAO().getPOItems(poId);
            request.setAttribute("items", items);
            request.setAttribute("poId", poId);
            request.getRequestDispatcher("/admin/purchaseDetail.jsp").forward(request, response);
        } else {
            List<Map<String, Object>> pos       = new PurchaseDAO().getAllPOs();
            List<Map<String, Object>> suppliers = new SupplierDAO().getAllSuppliers();
            request.setAttribute("pos",       pos);
            request.setAttribute("suppliers", suppliers);
            request.getRequestDispatcher("/admin/purchaseList.jsp").forward(request, response);
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        User user = (User) request.getSession().getAttribute("user");
        if (user == null || !"admin".equals(user.getRole())) { response.sendRedirect("login.jsp"); return; }

        String action = request.getParameter("action");
        AuditLogDAO auditDAO = new AuditLogDAO();

        if ("addSupplier".equals(action)) {
            new SupplierDAO().addSupplier(
                request.getParameter("supplierName"),
                request.getParameter("contact"),
                request.getParameter("phone"),
                request.getParameter("address")
            );
            auditDAO.log(user.getUserId(), user.getUsername(), "新增供应商",
                request.getParameter("supplierName"), null, request.getRemoteAddr());

        } else if ("createPO".equals(action)) {
            String[] pids  = request.getParameterValues("productId");
            String[] qtys  = request.getParameterValues("quantity");
            String[] costs = request.getParameterValues("unitCost");
            if (pids != null && pids.length > 0) {
                int[] productIds = new int[pids.length];
                int[] quantities = new int[pids.length];
                double[] unitCosts = new double[pids.length];
                for (int i = 0; i < pids.length; i++) {
                    productIds[i] = Integer.parseInt(pids[i]);
                    quantities[i] = Integer.parseInt(qtys[i]);
                    unitCosts[i]  = Double.parseDouble(costs[i]);
                }
                new PurchaseDAO().createPO(
                    Integer.parseInt(request.getParameter("supplierId")),
                    user.getUserId(), request.getParameter("remark"),
                    productIds, quantities, unitCosts
                );
                auditDAO.log(user.getUserId(), user.getUsername(), "创建采购单",
                    "supplier:" + request.getParameter("supplierId"), null, request.getRemoteAddr());
            }

        } else if ("approve".equals(action)) {
            int poId = Integer.parseInt(request.getParameter("poId"));
            new PurchaseDAO().approvePO(poId);
            auditDAO.log(user.getUserId(), user.getUsername(), "审核采购到货",
                "po_id:" + poId, null, request.getRemoteAddr());
        }
        response.sendRedirect(request.getContextPath() + "/adminPurchase");
    }
}
