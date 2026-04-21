package com.supermarket.interceptor;

import com.supermarket.common.Result;
import com.supermarket.config.JwtConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.PrintWriter;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtConfig jwtConfig;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("Authorization");
        
        if (!StringUtils.hasText(token) || !token.startsWith("Bearer ")) {
            sendUnauthorizedResponse(response, "未登录或token无效");
            return false;
        }

        token = token.substring(7);

        try {
            Claims claims = jwtConfig.parseToken(token);
            request.setAttribute("userId", claims.get("userId", Integer.class));
            request.setAttribute("username", claims.getSubject());
            request.setAttribute("role", claims.get("role", String.class));
            return enforceRoleIfNeeded(request, response);
        } catch (ExpiredJwtException e) {
            sendUnauthorizedResponse(response, "token已过期");
            return false;
        } catch (Exception e) {
            sendUnauthorizedResponse(response, "token解析失败");
            return false;
        }
    }

    private boolean enforceRoleIfNeeded(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String path = request.getRequestURI();
        String method = request.getMethod();
        String role = (String) request.getAttribute("role");

        // courier 登录生成的 role 固定是 "courier"
        if (path.startsWith("/courier/") || path.equals("/courier")) {
            if (!"courier".equals(role)) {
                sendForbiddenResponse(response, "无权访问（需要配送员角色）");
                return false;
            }
            return true;
        }

        // 用户侧积分/消息只允许 user 角色
        if (path.startsWith("/points/") || path.equals("/points") || path.startsWith("/messages/") || path.equals("/messages")) {
            if (!"user".equals(role)) {
                sendForbiddenResponse(response, "无权访问（需要普通用户角色）");
                return false;
            }
        }

        // 用户侧：购物车/地址/收藏/用户优惠券/商品评价只允许 user 角色
        boolean isUserOperateArea =
                path.startsWith("/cart/")
                        || path.startsWith("/cart")
                        || path.startsWith("/addresses")
                        || path.startsWith("/favorites")
                        || path.startsWith("/coupons/claim")
                        || path.startsWith("/coupons/my")
                        || path.startsWith("/coupons/available")
                        || (path.startsWith("/reviews") && !path.startsWith("/reviews/admin/") && !path.equals("/reviews/admin/list"));

        if (isUserOperateArea) {
            if (!"user".equals(role)) {
                sendForbiddenResponse(response, "无权访问（需要普通用户角色）");
                return false;
            }
        }

        // admin 相关接口：只允许 admin 角色访问
        boolean isAdminArea =
                path.startsWith("/admin/") ||
                path.startsWith("/seckill/admin/") ||
                path.startsWith("/products/admin/") ||
                path.startsWith("/banners/admin/") ||
                path.startsWith("/reviews/admin/") ||
                path.startsWith("/coupons/admin/") ||
                path.startsWith("/stocktake") ||
                // 产品信息维护（B端：非 GET 都认为是“后台操作”）
                (path.startsWith("/products") && !"GET".equalsIgnoreCase(method) && (
                        path.equals("/products")
                                || path.startsWith("/products/")
                                || path.startsWith("/products/categories")
                ));

        if (isAdminArea) {
            if (!isAdminRole(role)) {
                sendForbiddenResponse(response, "无权访问（需要管理员角色）");
                return false;
            }
        }
        return true;
    }

    private boolean isAdminRole(String role) {
        // DB 中 admin_users.role 约束：SUPER_ADMIN/MANAGER/PRODUCT/FINANCE/SERVICE/WAREHOUSE/CASHIER
        if (role == null) return false;
        return "SUPER_ADMIN".equals(role)
                || "MANAGER".equals(role)
                || "PRODUCT".equals(role)
                || "FINANCE".equals(role)
                || "SERVICE".equals(role)
                || "WAREHOUSE".equals(role)
                || "CASHIER".equals(role);
    }

    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.print("{\"code\":401,\"message\":\"" + message + "\"}");
        out.flush();
    }

    private void sendForbiddenResponse(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.print("{\"code\":403,\"message\":\"" + message + "\"}");
        out.flush();
    }
}
