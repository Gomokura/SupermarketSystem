package com.supermarket.dao;

import com.supermarket.util.DBUtil;
import java.sql.*;
import java.util.*;

public class PriceHistoryDAO {

    // 记录价格变更（在 ProductServlet update 时调用）
    public void record(int productId, double oldPrice, double newPrice, int operatorId, String remark) {
        Connection conn = null; PreparedStatement stmt = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(
                "INSERT INTO price_history VALUES (seq_price_history.NEXTVAL,?,?,?,?,SYSDATE,?)");
            stmt.setInt(1, productId);
            stmt.setDouble(2, oldPrice);
            stmt.setDouble(3, newPrice);
            stmt.setInt(4, operatorId);
            stmt.setString(5, remark);
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
        finally { DBUtil.close(conn, stmt, null); }
    }

    // 查询某商品价格历史
    public List<Map<String, Object>> getHistory(int productId) {
        List<Map<String, Object>> list = new ArrayList<>();
        Connection conn = null; PreparedStatement stmt = null; ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(
                "SELECT ph.*, u.username, p.product_name FROM price_history ph " +
                "LEFT JOIN users u ON ph.operator_id=u.user_id " +
                "JOIN products p ON ph.product_id=p.product_id " +
                "WHERE ph.product_id=? ORDER BY ph.change_time DESC");
            stmt.setInt(1, productId);
            rs = stmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("productName", rs.getString("product_name"));
                row.put("oldPrice",    rs.getDouble("old_price"));
                row.put("newPrice",    rs.getDouble("new_price"));
                row.put("operator",    rs.getString("username"));
                row.put("changeTime",  rs.getTimestamp("change_time"));
                row.put("remark",      rs.getString("remark"));
                list.add(row);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        finally { DBUtil.close(conn, stmt, rs); }
        return list;
    }

    // 所有商品最近一次调价记录
    public List<Map<String, Object>> getRecentChanges(int limit) {
        List<Map<String, Object>> list = new ArrayList<>();
        Connection conn = null; PreparedStatement stmt = null; ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(
                "SELECT ph.*, p.product_name, u.username FROM price_history ph " +
                "JOIN products p ON ph.product_id=p.product_id " +
                "LEFT JOIN users u ON ph.operator_id=u.user_id " +
                "ORDER BY ph.change_time DESC FETCH FIRST ? ROWS ONLY");
            stmt.setInt(1, limit);
            rs = stmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("productId",   rs.getInt("product_id"));
                row.put("productName", rs.getString("product_name"));
                row.put("oldPrice",    rs.getDouble("old_price"));
                row.put("newPrice",    rs.getDouble("new_price"));
                row.put("operator",    rs.getString("username"));
                row.put("changeTime",  rs.getTimestamp("change_time"));
                row.put("remark",      rs.getString("remark"));
                list.add(row);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        finally { DBUtil.close(conn, stmt, rs); }
        return list;
    }
}
