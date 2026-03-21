package com.supermarket.dao;

import com.supermarket.util.DBUtil;
import java.sql.*;
import java.util.*;

public class FinanceDAO {

    // 按月统计收入
    public List<Map<String, Object>> getMonthlyRevenue(int months) {
        List<Map<String, Object>> list = new ArrayList<>();
        Connection conn = null; PreparedStatement stmt = null; ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(
                "SELECT TO_CHAR(order_time,'YYYY-MM') AS month, " +
                "SUM(total_amount) AS revenue, COUNT(*) AS order_count " +
                "FROM orders WHERE order_status != 'cancelled' " +
                "AND order_time >= ADD_MONTHS(SYSDATE, -?) " +
                "GROUP BY TO_CHAR(order_time,'YYYY-MM') ORDER BY month");
            stmt.setInt(1, months);
            rs = stmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("month",      rs.getString("month"));
                row.put("revenue",    rs.getDouble("revenue"));
                row.put("orderCount", rs.getInt("order_count"));
                list.add(row);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        finally { DBUtil.close(conn, stmt, rs); }
        return list;
    }

    // 按月统计采购成本
    public List<Map<String, Object>> getMonthlyCost(int months) {
        List<Map<String, Object>> list = new ArrayList<>();
        Connection conn = null; PreparedStatement stmt = null; ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(
                "SELECT TO_CHAR(create_time,'YYYY-MM') AS month, SUM(total_cost) AS cost " +
                "FROM purchase_orders WHERE status='arrived' " +
                "AND create_time >= ADD_MONTHS(SYSDATE, -?) " +
                "GROUP BY TO_CHAR(create_time,'YYYY-MM') ORDER BY month");
            stmt.setInt(1, months);
            rs = stmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("month", rs.getString("month"));
                row.put("cost",  rs.getDouble("cost"));
                list.add(row);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        finally { DBUtil.close(conn, stmt, rs); }
        return list;
    }

    // 总览指标
    public Map<String, Object> getSummary() {
        Map<String, Object> map = new LinkedHashMap<>();
        Connection conn = null; PreparedStatement stmt = null; ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            // 总收入
            stmt = conn.prepareStatement("SELECT NVL(SUM(total_amount),0) FROM orders WHERE order_status != 'cancelled'");
            rs = stmt.executeQuery(); if (rs.next()) map.put("totalRevenue", rs.getDouble(1)); rs.close(); stmt.close();
            // 总成本
            stmt = conn.prepareStatement("SELECT NVL(SUM(total_cost),0) FROM purchase_orders WHERE status='arrived'");
            rs = stmt.executeQuery(); if (rs.next()) map.put("totalCost", rs.getDouble(1)); rs.close(); stmt.close();
            // 本月收入
            stmt = conn.prepareStatement("SELECT NVL(SUM(total_amount),0) FROM orders WHERE order_status != 'cancelled' AND TO_CHAR(order_time,'YYYY-MM')=TO_CHAR(SYSDATE,'YYYY-MM')");
            rs = stmt.executeQuery(); if (rs.next()) map.put("monthRevenue", rs.getDouble(1)); rs.close(); stmt.close();
            // 本月订单数
            stmt = conn.prepareStatement("SELECT COUNT(*) FROM orders WHERE TO_CHAR(order_time,'YYYY-MM')=TO_CHAR(SYSDATE,'YYYY-MM')");
            rs = stmt.executeQuery(); if (rs.next()) map.put("monthOrders", rs.getInt(1));
            double revenue = (Double) map.getOrDefault("totalRevenue", 0.0);
            double cost    = (Double) map.getOrDefault("totalCost", 0.0);
            map.put("totalProfit", revenue - cost);
            map.put("profitRate",  revenue > 0 ? (revenue - cost) / revenue * 100 : 0.0);
        } catch (SQLException e) { e.printStackTrace(); }
        finally { DBUtil.close(conn, stmt, rs); }
        return map;
    }

    // 分类销售占比
    public List<Map<String, Object>> getCategoryRevenue() {
        List<Map<String, Object>> list = new ArrayList<>();
        Connection conn = null; PreparedStatement stmt = null; ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(
                "SELECT c.category_name, SUM(oi.quantity * oi.price) AS revenue " +
                "FROM order_items oi JOIN products p ON oi.product_id=p.product_id " +
                "JOIN categories c ON p.category_id=c.category_id " +
                "GROUP BY c.category_name ORDER BY revenue DESC");
            rs = stmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("category", rs.getString("category_name"));
                row.put("revenue",  rs.getDouble("revenue"));
                list.add(row);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        finally { DBUtil.close(conn, stmt, rs); }
        return list;
    }
}
