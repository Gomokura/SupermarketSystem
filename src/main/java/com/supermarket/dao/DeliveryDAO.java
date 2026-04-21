package com.supermarket.dao;

import com.supermarket.util.DBUtil;
import java.sql.*;
import java.util.*;

public class DeliveryDAO {

    public List<Map<String, Object>> searchDeliveries(String status) {
        List<Map<String, Object>> list = new ArrayList<>();
        Connection conn = null; PreparedStatement stmt = null; ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            StringBuilder sql = new StringBuilder(
                "SELECT d.*, o.total_amount, u.username, c.username AS courier_name FROM deliveries d " +
                "JOIN orders o ON d.order_id=o.order_id " +
                "JOIN users u ON o.user_id=u.user_id " +
                "LEFT JOIN users c ON d.courier_id=c.user_id WHERE 1=1");
            if (status != null && !status.isEmpty()) sql.append(" AND d.status=?");
            sql.append(" ORDER BY d.delivery_id DESC");
            stmt = conn.prepareStatement(sql.toString());
            if (status != null && !status.isEmpty()) stmt.setString(1, status);
            rs = stmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("deliveryId",   rs.getInt("delivery_id"));
                row.put("orderId",      rs.getInt("order_id"));
                row.put("username",     rs.getString("username"));
                row.put("totalAmount",  rs.getDouble("total_amount"));
                row.put("receiver",     rs.getString("receiver"));
                row.put("phone",        rs.getString("phone"));
                row.put("address",      rs.getString("address"));
                row.put("status",       rs.getString("status"));
                row.put("dispatchTime", rs.getTimestamp("dispatch_time"));
                row.put("doneTime",     rs.getTimestamp("done_time"));
                row.put("remark",       rs.getString("remark"));
                row.put("courierName",  rs.getString("courier_name"));
                list.add(row);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        finally { DBUtil.close(conn, stmt, rs); }
        return list;
    }

    // 订单完成时自动创建配送记录
    public boolean createDelivery(int orderId, String receiver, String phone, String address) {
        Connection conn = null; PreparedStatement stmt = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(
                "INSERT INTO deliveries VALUES (seq_delivery.NEXTVAL,?,?,?,?,'pending',NULL,NULL,NULL)");
            stmt.setInt(1, orderId);
            stmt.setString(2, address);
            stmt.setString(3, receiver);
            stmt.setString(4, phone);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        finally { DBUtil.close(conn, stmt, null); }
        return false;
    }

    public boolean updateStatus(int deliveryId, String status) {
        Connection conn = null; PreparedStatement stmt = null;
        try {
            conn = DBUtil.getConnection();
            String timeField = "dispatched".equals(status) ? ",dispatch_time=SYSDATE"
                             : "done".equals(status)       ? ",done_time=SYSDATE" : "";
            stmt = conn.prepareStatement(
                "UPDATE deliveries SET status=?" + timeField + " WHERE delivery_id=?");
            stmt.setString(1, status);
            stmt.setInt(2, deliveryId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        finally { DBUtil.close(conn, stmt, null); }
        return false;
    }

    public boolean assignCourier(int deliveryId, int courierId) {
        Connection conn = null; PreparedStatement stmt = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(
                "UPDATE deliveries SET courier_id=?, status='dispatched', dispatch_time=SYSDATE WHERE delivery_id=?");
            stmt.setInt(1, courierId);
            stmt.setInt(2, deliveryId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        finally { DBUtil.close(conn, stmt, null); }
        return false;
    }

    public List<Map<String, Object>> getTasksByCourier(int courierId) {
        List<Map<String, Object>> list = new ArrayList<>();
        Connection conn = null; PreparedStatement stmt = null; ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(
                "SELECT d.*, o.total_amount, u.username FROM deliveries d " +
                "JOIN orders o ON d.order_id=o.order_id " +
                "JOIN users u ON o.user_id=u.user_id " +
                "WHERE d.courier_id=? AND d.status != 'done' ORDER BY d.delivery_id DESC");
            stmt.setInt(1, courierId);
            rs = stmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("deliveryId",   rs.getInt("delivery_id"));
                row.put("orderId",      rs.getInt("order_id"));
                row.put("username",     rs.getString("username"));
                row.put("totalAmount",  rs.getDouble("total_amount"));
                row.put("receiver",     rs.getString("receiver"));
                row.put("phone",        rs.getString("phone"));
                row.put("address",      rs.getString("address"));
                row.put("status",       rs.getString("status"));
                row.put("dispatchTime", rs.getTimestamp("dispatch_time"));
                list.add(row);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        finally { DBUtil.close(conn, stmt, rs); }
        return list;
    }
}
