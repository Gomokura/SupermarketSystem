package com.supermarket.dao;

import com.supermarket.util.DBUtil;
import java.sql.*;
import java.util.*;

public class AlertDAO {

    // 库存异常（stock < 0）
    public List<Map<String, Object>> getNegativeStock() {
        List<Map<String, Object>> list = new ArrayList<>();
        Connection conn = null; PreparedStatement stmt = null; ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(
                "SELECT product_id, product_name, stock FROM products WHERE stock < 0");
            rs = stmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("productId",   rs.getInt("product_id"));
                row.put("productName", rs.getString("product_name"));
                row.put("stock",       rs.getInt("stock"));
                list.add(row);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        finally { DBUtil.close(conn, stmt, rs); }
        return list;
    }

    // 异常大额订单（超过平均值3倍）
    public List<Map<String, Object>> getLargeOrders() {
        List<Map<String, Object>> list = new ArrayList<>();
        Connection conn = null; PreparedStatement stmt = null; ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(
                "SELECT o.order_id, o.total_amount, o.order_time, u.username " +
                "FROM orders o JOIN users u ON o.user_id=u.user_id " +
                "WHERE o.total_amount > (SELECT AVG(total_amount)*3 FROM orders) " +
                "ORDER BY o.total_amount DESC FETCH FIRST 20 ROWS ONLY");
            rs = stmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("orderId",     rs.getInt("order_id"));
                row.put("totalAmount", rs.getDouble("total_amount"));
                row.put("orderTime",   rs.getTimestamp("order_time"));
                row.put("username",    rs.getString("username"));
                list.add(row);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        finally { DBUtil.close(conn, stmt, rs); }
        return list;
    }

    // 长期未处理订单（pending 超过 N 天）
    public List<Map<String, Object>> getStaleOrders(int days) {
        List<Map<String, Object>> list = new ArrayList<>();
        Connection conn = null; PreparedStatement stmt = null; ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(
                "SELECT o.order_id, o.total_amount, o.order_time, u.username " +
                "FROM orders o JOIN users u ON o.user_id=u.user_id " +
                "WHERE o.order_status='pending' AND o.order_time < SYSDATE - ? " +
                "ORDER BY o.order_time");
            stmt.setInt(1, days);
            rs = stmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("orderId",     rs.getInt("order_id"));
                row.put("totalAmount", rs.getDouble("total_amount"));
                row.put("orderTime",   rs.getTimestamp("order_time"));
                row.put("username",    rs.getString("username"));
                list.add(row);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        finally { DBUtil.close(conn, stmt, rs); }
        return list;
    }

    // 低库存商品（stock <= 5）
    public List<Map<String, Object>> getCriticalStock() {
        List<Map<String, Object>> list = new ArrayList<>();
        Connection conn = null; PreparedStatement stmt = null; ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(
                "SELECT product_id, product_name, stock, unit FROM products " +
                "WHERE stock >= 0 AND stock <= 5 AND status='active' ORDER BY stock");
            rs = stmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("productId",   rs.getInt("product_id"));
                row.put("productName", rs.getString("product_name"));
                row.put("stock",       rs.getInt("stock"));
                row.put("unit",        rs.getString("unit"));
                list.add(row);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        finally { DBUtil.close(conn, stmt, rs); }
        return list;
    }
}
