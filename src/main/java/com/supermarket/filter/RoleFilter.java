package com.supermarket.filter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.*;

/**
 * 角色权限过滤器
 *
 * 根据请求路径和 session 中的角色信息，进行权限检查。
 *
 * Session 属性：
 * - loginRole  : String  "user" | "admin" | "courier"
 * - adminRole  : String  SUPER_ADMIN | MANAGER | PRODUCT | FINANCE | SERVICE | WAREHOUSE | CASHIER
 *
 * 路径权限规则：
 * - /admin*      → loginRole = "admin"
 * - /courier*    → loginRole = "courier"
 * - /cart*       → loginRole = "user"
 * - /order*      → loginRole = "user" 或 "admin"
 * - 其他         → 已由 LoginFilter 保证了至少一种登录
 */
public class RoleFilter implements Filter {

    // admin 端角色配置：角色 → 允许访问的模块（action 前缀）
    private static final Map<String, Set<String>> ADMIN_ROLE_PERMISSIONS = new HashMap<>();
    static {
        ADMIN_ROLE_PERMISSIONS.put("SUPER_ADMIN", null); // null = 所有权限

        Set<String> managerPerms = new HashSet<>();
        managerPerms.add("admin");
        managerPerms.add("adminDashboard");
        managerPerms.add("adminOrder");
        managerPerms.add("adminUser");
        managerPerms.add("adminInventory");
        managerPerms.add("adminFinance");
        managerPerms.add("adminDelivery");
        managerPerms.add("adminProduct");
        managerPerms.add("adminCategory");
        managerPerms.add("adminBrand");
        managerPerms.add("adminSupplier");
        managerPerms.add("adminPromotion");
        managerPerms.add("adminBanner");
        managerPerms.add("cashier");
        ADMIN_ROLE_PERMISSIONS.put("MANAGER", managerPerms);

        Set<String> productPerms = new HashSet<>();
        productPerms.add("adminProduct");
        productPerms.add("adminCategory");
        productPerms.add("adminBrand");
        productPerms.add("adminSupplier");
        productPerms.add("adminPromotion");
        productPerms.add("adminBanner");
        ADMIN_ROLE_PERMISSIONS.put("PRODUCT", productPerms);

        Set<String> financePerms = new HashSet<>();
        financePerms.add("adminFinance");
        financePerms.add("adminOrder");
        ADMIN_ROLE_PERMISSIONS.put("FINANCE", financePerms);

        Set<String> servicePerms = new HashSet<>();
        servicePerms.add("adminUser");
        servicePerms.add("adminOrder");
        servicePerms.add("adminDelivery");
        ADMIN_ROLE_PERMISSIONS.put("SERVICE", servicePerms);

        Set<String> warehousePerms = new HashSet<>();
        warehousePerms.add("adminInventory");
        warehousePerms.add("adminPurchase");
        warehousePerms.add("adminDamage");
        ADMIN_ROLE_PERMISSIONS.put("WAREHOUSE", warehousePerms);

        Set<String> cashierPerms = new HashSet<>();
        cashierPerms.add("cashier");
        ADMIN_ROLE_PERMISSIONS.put("CASHIER", cashierPerms);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse resp = (HttpServletResponse) servletResponse;

        HttpSession session = req.getSession(false);
        if (session == null) {
            chain.doFilter(req, resp);
            return;
        }

        String loginRole = (String) session.getAttribute("loginRole");
        String adminRole = (String) session.getAttribute("adminRole");

        // 非管理员直接放行
        if (!"admin".equals(loginRole)) {
            chain.doFilter(req, resp);
            return;
        }

        // 超级管理员放行
        if ("SUPER_ADMIN".equals(adminRole)) {
            chain.doFilter(req, resp);
            return;
        }

        // 其他角色：根据 adminRole 权限表检查
        String action = req.getParameter("action");
        if (action == null) {
            chain.doFilter(req, resp);
            return;
        }

        Set<String> allowedActions = ADMIN_ROLE_PERMISSIONS.get(adminRole);
        if (allowedActions == null) {
            // 没有配置权限的角色，拒绝访问
            sendForbidden(req, resp);
            return;
        }

        // 检查是否有权限（action 以允许的任意一个前缀开头）
        boolean hasPermission = false;
        for (String prefix : allowedActions) {
            if (action.startsWith(prefix)) {
                hasPermission = true;
                break;
            }
        }

        if (!hasPermission) {
            sendForbidden(req, resp);
            return;
        }

        chain.doFilter(req, resp);
    }

    private void sendForbidden(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String ajax = req.getHeader("X-Requested-With");
        if ("XMLHttpRequest".equals(ajax)) {
            resp.setContentType("application/json; charset=UTF-8");
            resp.getWriter().write("{\"code\":403,\"message\":\"权限不足\"}");
            resp.setStatus(403);
        } else {
            req.setAttribute("errorMsg", "权限不足，您没有权限访问该功能");
            req.getRequestDispatcher("/views/error/403.jsp").forward(req, resp);
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {
    }
}
