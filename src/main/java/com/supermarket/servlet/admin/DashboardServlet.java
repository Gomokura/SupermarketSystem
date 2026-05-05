package com.supermarket.servlet.admin;

import com.supermarket.entity.Order;
import com.supermarket.entity.Product;
import com.supermarket.entity.Result;
import com.supermarket.servlet.BaseServlet;
import com.supermarket.service.ProductService;
import com.supermarket.util.DBUtil;
import com.supermarket.util.StringUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * DashboardServlet - 管理后台仪表盘
 * action=adminDashboard
 */
public class DashboardServlet extends BaseServlet {
    public void doAction(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!"admin".equals(getLoginRole(req))) {
            jsonError(resp, "请先以管理员身份登录");
            return;
        }

        Map<String, Object> dashboard = new HashMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        try (Connection conn = DBUtil.getDataSource().getConnection()) {
            String today = sdf.format(new java.util.Date());

            // 1. 今日订单数
            long todayOrders = countByDate(conn, today, null);
            dashboard.put("todayOrders", todayOrders);

            // 2. 今日销售额
            double todaySales = sumPayAmountByDate(conn, today);
            dashboard.put("todaySales", StringUtil.round2(todaySales));

            // 3. 昨日
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, -1);
            String yesterday = sdf.format(cal.getTime());
            double yesterdaySales = sumPayAmountByDate(conn, yesterday);
            long yesterdayOrders = countByDate(conn, yesterday, null);
            dashboard.put("yesterdaySales", StringUtil.round2(yesterdaySales));
            dashboard.put("yesterdayOrders", yesterdayOrders);

            // 4. 待处理订单（待发货）
            dashboard.put("pendingShipCount", countByStatus(conn, "PENDING_SHIP"));

            // 5. 商品总数
            dashboard.put("productCount", countProducts(conn));

            // 6. 低库存商品数
            dashboard.put("lowStockCount", countLowStock(conn));

            // 7. 用户总数
            dashboard.put("userCount", countUsers(conn));

            // 8. 近7天销售趋势
            List<Map<String, Object>> salesTrend = new ArrayList<>();
            for (int i = 6; i >= 0; i--) {
                Calendar c = Calendar.getInstance();
                c.add(Calendar.DAY_OF_MONTH, -i);
                String date = sdf.format(c.getTime());
                double daySales = sumPayAmountByDate(conn, date);
                Map<String, Object> dayData = new HashMap<>();
                dayData.put("date", date);
                dayData.put("sales", StringUtil.round2(daySales));
                salesTrend.add(dayData);
            }
            dashboard.put("salesTrend", salesTrend);

            String ajax = req.getHeader("X-Requested-With");
            if ("XMLHttpRequest".equals(ajax)) {
                json(req, resp, dashboard);
            } else {
                req.setAttribute("dashboard", dashboard);
                forward(req, resp, "/views/admin/dashboard.jsp");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            jsonError(resp, "数据库错误: " + e.getMessage());
        }
    }

    private long countByDate(Connection conn, String date, String[] statuses) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM ORDERS WHERE TO_CHAR(CREATE_TIME, 'YYYY-MM-DD') = ?");
        if (statuses != null && statuses.length > 0) {
            sql.append(" AND STATUS IN (");
            for (int i = 0; i < statuses.length; i++) sql.append(i > 0 ? ",?" : "?");
            sql.append(")");
        }
        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setString(1, date);
            if (statuses != null) {
                for (int i = 0; i < statuses.length; i++) ps.setString(i + 2, statuses[i]);
            }
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getLong(1);
        }
        return 0;
    }

    private double sumPayAmountByDate(Connection conn, String date) throws SQLException {
        String[] paidStatuses = {"PAID", "PENDING_SHIP", "SHIPPING", "PENDING_RECEIVED", "COMPLETED"};
        StringBuilder sql = new StringBuilder(
                "SELECT NVL(SUM(PAY_AMOUNT), 0) FROM ORDERS WHERE TO_CHAR(CREATE_TIME, 'YYYY-MM-DD') = ? AND STATUS IN (");
        for (int i = 0; i < paidStatuses.length; i++) sql.append(i > 0 ? ",?" : "?");
        sql.append(")");
        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setString(1, date);
            for (int i = 0; i < paidStatuses.length; i++) ps.setString(i + 2, paidStatuses[i]);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        }
        return 0;
    }

    private long countByStatus(Connection conn, String status) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM ORDERS WHERE STATUS = ?")) {
            ps.setString(1, status);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getLong(1);
        }
        return 0;
    }

    private long countProducts(Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM PRODUCTS WHERE IS_DELETED = 0")) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getLong(1);
        }
        return 0;
    }

    private long countLowStock(Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM PRODUCTS WHERE IS_DELETED = 0 AND STOCK <= STOCK_WARNING")) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getLong(1);
        }
        return 0;
    }

    private long countUsers(Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM USERS WHERE IS_DELETED = 0")) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getLong(1);
        }
        return 0;
    }
}
