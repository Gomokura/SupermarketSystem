package com.supermarket.dao;

import com.supermarket.util.DBUtil;
import java.sql.*;
import java.util.*;

public class InventoryDAO {

    // 查询库存日志，支持商品名关键词过滤
    public List<Map<String, Object>> searchLogs(String keyword) {
        List<Map<String, Object>> list = new ArrayList<>();
        Connection conn = null; PreparedStatement stmt = null; ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            StringBuilder sql = new StringBuilder(
                "SELECT il.*, p.product_name, u.username FROM inventory_logs il " +
                "JOIN products p ON il.product_id=p.product_id " +
                "LEFT JOIN users u ON il.operator_id=u.user_id WHERE 1=1");
            if (keyword != null && !keyword.trim().isEmpty())
                sql.append(" AND p.product_name LIKE ?");
            sql.append(" ORDER BY il.log_time DESC");
            stmt = conn.prepareStatement(sql.toString());
            if (keyword != null && !keyword.trim().isEmpty())
                stmt.setString(1, "%" + keyword + "%");
            rs = stmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("logId",       rs.getInt("log_id"));
                row.put("productName", rs.getString("product_name"));
                row.put("changeType",  rs.getString("change_type"));
                row.put("quantity",    rs.getInt("quantity"));
                row.put("operator",    rs.getString("username"));
                row.put("remark",      rs.getString("remark"));
                row.put("logTime",     rs.getTimestamp("log_time"));
                list.add(row);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        finally { DBUtil.close(conn, stmt, rs); }
        return list;
    }

    // 入库/出库：更新库存 + 写日志（事务）
    public boolean adjustStock(int productId, String changeType, int quantity, int operatorId, String remark) {
        Connection conn = null; PreparedStatement stmt = null;
        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);

            // 更新库存
            String delta = "in".equals(changeType) ? "+?" : "-?";
            stmt = conn.prepareStatement("UPDATE products SET stock=stock" + delta + " WHERE product_id=?");
            stmt.setInt(1, quantity);
            stmt.setInt(2, productId);
            stmt.executeUpdate();
            stmt.close();

            // 写日志
            stmt = conn.prepareStatement(
                "INSERT INTO inventory_logs VALUES (seq_inventory_log.NEXTVAL,?,?,?,?,?,SYSDATE)");
            stmt.setInt(1, productId);
            stmt.setString(2, changeType);
            stmt.setInt(3, quantity);
            stmt.setInt(4, operatorId);
            stmt.setString(5, remark);
            stmt.executeUpdate();

            conn.commit();
            return true;
        } catch (SQLException e) {
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) {}
            e.printStackTrace();
        } finally {
            try { if (conn != null) conn.setAutoCommit(true); } catch (SQLException e) {}
            DBUtil.close(conn, stmt, null);
        }
        return false;
    }

    // 低库存商品（stock <= threshold）
    public List<Map<String, Object>> getLowStockProducts(int threshold) {
        List<Map<String, Object>> list = new ArrayList<>();
        Connection conn = null; PreparedStatement stmt = null; ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(
                "SELECT product_id, product_name, stock, unit FROM products WHERE stock <= ? AND status='active' ORDER BY stock ASC");
            stmt.setInt(1, threshold);
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
