package com.supermarket.filter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 登录过滤器
 *
 * 检查用户是否已登录（Session 中是否存在登录标识）。
 * 白名单路径无需登录即可访问。
 *
 * Session 属性约定：
 * - session.setAttribute("userId", Integer)     C端用户已登录
 * - session.setAttribute("adminId", Integer)     B端管理员已登录
 * - session.setAttribute("courierId", Integer)  配送员已登录
 */
public class LoginFilter implements Filter {

    // 白名单：无需登录即可访问的 action
    private static final Set<String> WHITE_LIST = new HashSet<>(Arrays.asList(
            "login",
            "register",
            "adminLogin",
            "courierLogin",
            "upload",
            "productList",
            "productDetail",
            "productBarcode",
            "productRecommended",
            "productTopSales",
            "productNew",
            "productSuggestions",
            "categoryTree"
    ));

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse resp = (HttpServletResponse) servletResponse;

        String uri = req.getRequestURI();
        String action = req.getParameter("action");

        // 静态资源放行
        if (uri.contains("/css/") || uri.contains("/js/") || uri.contains("/uploads/")
                || uri.endsWith(".css") || uri.endsWith(".js") || uri.endsWith(".jpg")
                || uri.endsWith(".png") || uri.endsWith(".gif") || uri.endsWith(".ico")
                || uri.endsWith(".woff") || uri.endsWith(".woff2") || uri.endsWith(".ttf")) {
            chain.doFilter(req, resp);
            return;
        }

        // 白名单放行
        if (action != null && WHITE_LIST.contains(action)) {
            chain.doFilter(req, resp);
            return;
        }

        // 检查 Session 登录状态
        HttpSession session = req.getSession(false);
        if (session == null) {
            sendRedirect(req, resp);
            return;
        }

        Object userId = session.getAttribute("userId");
        Object adminId = session.getAttribute("adminId");
        Object courierId = session.getAttribute("courierId");

        if (userId == null && adminId == null && courierId == null) {
            sendRedirect(req, resp);
            return;
        }

        // 已登录，放行
        chain.doFilter(req, resp);
    }

    private void sendRedirect(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String ajax = req.getHeader("X-Requested-With");
        if ("XMLHttpRequest".equals(ajax)) {
            // AJAX 请求，返回 JSON
            resp.setContentType("application/json; charset=UTF-8");
            resp.setCharacterEncoding("UTF-8");
            resp.getWriter().write("{\"code\":401,\"message\":\"未登录，请先登录\"}");
            resp.setStatus(401);
        } else {
            // 普通请求，重定向到登录页
            resp.sendRedirect(req.getContextPath() + "/views/login.jsp");
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {
    }
}
