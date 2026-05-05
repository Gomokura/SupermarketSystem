package com.supermarket.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * BaseServlet - 所有业务 Servlet 的公共父类
 *
 * 提供统一的工具方法：
 * - forward(path)    转发到 JSP
 * - redirect(url)    重定向
 * - json(obj)       返回 JSON 响应
 * - jsonError(msg)  返回 JSON 错误
 * - getSession()    获取 Session（含登录检查）
 * - getInteger(req, name)  从请求参数获取 Integer（支持空值）
 * - getString(req, name)    从请求参数获取 String（支持空值）
 * - getBoolean(req, name)   从请求参数获取 Boolean
 */
public class BaseServlet extends HttpServlet {

    protected static final ObjectMapper om;

    static {
        om = new ObjectMapper();
        om.registerModule(new JavaTimeModule());
        om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    // ==================== 转发与重定向 ====================

    protected void forward(HttpServletRequest req, HttpServletResponse resp, String path)
            throws ServletException, IOException {
        req.getRequestDispatcher(path).forward(req, resp);
    }

    protected void redirect(HttpServletRequest req, HttpServletResponse resp, String url) throws IOException {
        resp.sendRedirect(req.getContextPath() + url);
    }

    protected void redirectAbs(HttpServletResponse resp, String url) throws IOException {
        resp.sendRedirect(url);
    }

    /**
     * 返回成功 JSON（code=200）
     */
    protected void json(HttpServletRequest req, HttpServletResponse resp, Object data)
            throws IOException {
        writeJson(resp, buildJson(200, "success", data));
    }

    /**
     * 返回成功 JSON（无数据）
     */
    protected void jsonOk(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        json(req, resp, null);
    }

    /**
     * 返回错误 JSON（code=500）
     */
    protected void jsonError(HttpServletResponse resp, String message) throws IOException {
        writeJson(resp, buildJson(500, message, null));
    }

    /**
     * 返回错误 JSON（自定义 code）
     */
    protected void jsonError(HttpServletResponse resp, int code, String message) throws IOException {
        writeJson(resp, buildJson(code, message, null));
    }

    /**
     * 返回成功 JSON 消息（data 为字符串）
     */
    protected void jsonMsg(HttpServletRequest req, HttpServletResponse resp, String message) throws IOException {
        json(req, resp, message);
    }

    protected void writeJson(HttpServletResponse resp, String json) throws IOException {
        resp.setContentType("application/json; charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();
        out.write(json);
        out.flush();
    }

    private String buildJson(int code, String message, Object data) {
        Map<String, Object> map = new HashMap<>();
        map.put("code", code);
        map.put("message", message);
        if (data != null) map.put("data", data);
        try {
            return om.writeValueAsString(map);
        } catch (Exception e) {
            return "{\"code\":" + code + ",\"message\":\"" + message + "\"}";
        }
    }

    // ==================== 参数获取工具 ====================

    protected Integer getInteger(HttpServletRequest req, String name) {
        String v = req.getParameter(name);
        if (v == null || v.trim().isEmpty()) return null;
        try {
            return Integer.parseInt(v.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    protected Integer getInteger(HttpServletRequest req, String name, Integer defaultVal) {
        Integer v = getInteger(req, name);
        return v != null ? v : defaultVal;
    }

    protected Long getLong(HttpServletRequest req, String name) {
        String v = req.getParameter(name);
        if (v == null || v.trim().isEmpty()) return null;
        try {
            return Long.parseLong(v.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    protected String getString(HttpServletRequest req, String name) {
        String v = req.getParameter(name);
        return v != null ? v.trim() : null;
    }

    protected String getString(HttpServletRequest req, String name, String defaultVal) {
        String v = getString(req, name);
        return v != null ? v : defaultVal;
    }

    protected Boolean getBoolean(HttpServletRequest req, String name) {
        String v = req.getParameter(name);
        if (v == null || v.trim().isEmpty()) return null;
        return "1".equals(v) || "true".equalsIgnoreCase(v);
    }

    protected Boolean getBoolean(HttpServletRequest req, String name, Boolean defaultVal) {
        Boolean v = getBoolean(req, name);
        return v != null ? v : defaultVal;
    }

    protected Double getDouble(HttpServletRequest req, String name) {
        String v = req.getParameter(name);
        if (v == null || v.trim().isEmpty()) return null;
        try {
            return Double.parseDouble(v.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // ==================== 分页参数 ====================

    protected int getPageNum(HttpServletRequest req) {
        return getInteger(req, "pageNum", 1);
    }

    protected int getPageSize(HttpServletRequest req) {
        return getInteger(req, "pageSize", 10);
    }

    // ==================== 获取当前登录用户 ID ====================

    /**
     * 从 Session 中获取当前登录用户 ID
     * 返回 null 表示未登录
     */
    protected Integer getLoginUserId(HttpServletRequest req) {
        Object userId = req.getSession(false) != null
                ? req.getSession(false).getAttribute("userId") : null;
        if (userId == null) return null;
        if (userId instanceof Integer) return (Integer) userId;
        if (userId instanceof Long) return ((Long) userId).intValue();
        try {
            return Integer.parseInt(userId.toString());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从 Session 中获取当前登录管理员 ID
     */
    protected Integer getLoginAdminId(HttpServletRequest req) {
        Object adminId = req.getSession(false) != null
                ? req.getSession(false).getAttribute("adminId") : null;
        if (adminId == null) return null;
        if (adminId instanceof Integer) return (Integer) adminId;
        if (adminId instanceof Long) return ((Long) adminId).intValue();
        try {
            return Integer.parseInt(adminId.toString());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从 Session 中获取当前登录配送员 ID
     */
    protected Integer getLoginCourierId(HttpServletRequest req) {
        Object courierId = req.getSession(false) != null
                ? req.getSession(false).getAttribute("courierId") : null;
        if (courierId == null) return null;
        if (courierId instanceof Integer) return (Integer) courierId;
        if (courierId instanceof Long) return ((Long) courierId).intValue();
        try {
            return Integer.parseInt(courierId.toString());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取当前登录用户角色（user/admin/courier）
     */
    protected String getLoginRole(HttpServletRequest req) {
        if (req.getSession(false) == null) return null;
        return (String) req.getSession(false).getAttribute("loginRole");
    }

    /**
     * 获取当前登录管理员角色（SUPER_ADMIN/MANAGER/PRODUCT/...）
     */
    protected String getAdminRole(HttpServletRequest req) {
        if (req.getSession(false) == null) return null;
        return (String) req.getSession(false).getAttribute("adminRole");
    }

    /**
     * 是否已登录（任一角色）
     */
    protected boolean isLoggedIn(HttpServletRequest req) {
        return getLoginUserId(req) != null
                || getLoginAdminId(req) != null
                || getLoginCourierId(req) != null;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        doPost(req, resp);
    }
}
