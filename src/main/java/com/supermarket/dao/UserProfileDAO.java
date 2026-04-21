package com.supermarket.dao;

import com.supermarket.util.DBUtil;
import java.sql.*;
import java.util.*;

public class UserProfileDAO {

    // 用户消费画像列表
    public List<Map<String, Object>> getUserProfiles(String keyword) {
        List<Map<String, Object>> list = new ArrayList<>();
        Connection conn = null; PreparedStatement stmt = null; ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            StringBuilder sql = new StringBuilder(
                "SELECT u.user_id, u.username, u.real_name, u.phone, u.status, u.create_time, " +
                "NVL(up.points,0) AS points, NVL(up.total_spent,0) AS total_spent, " +
                "NVL(oc.order_count,0) AS order_count " +
                "FROM users u " +
                "LEFT JOIN user_points up ON u.user_id=up.user_id " +
                "LEFT JOIN (SELECT user_id, COUNT(*) AS order_count FROM orders GROUP BY user_id) oc ON u.user_id=oc.user_id " +
                "WHERE u.role='user'");
            if (keyword != null && !keyword.trim().isEmpty())
                sql.append(" AND (u.username LIKE ? OR u.real_name LIKE ?)");
            sql.append(" ORDER BY total_spent DESC");
            stmt = conn.prepareStatement(sql.toString());
            if (keyword != null && !keyword.trim().isEmpty()) {
                stmt.setString(1, "%" + keyword + "%");
                stmt.setString(2, "%" + keyword + "%");
            }
            rs = stmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("userId",      rs.getInt("user_id"));
                row.put("username",    rs.getString("username"));
                row.put("realName",    rs.getString("real_name"));
                row.put("phone",       rs.getString("phone"));
                row.put("status",      rs.getString("status"));
                row.put("points",      rs.getInt("points"));
                row.put("totalSpent",  rs.getDouble("total_spent"));
                row.put("orderCount",  rs.getInt("order_count"));
                list.add(row);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        finally { DBUtil.close(conn, stmt, rs); }
        return list;
    }

    // 用户偏好分类（消费最多的分类）
    public List<Map<String, Object>> getUserCategoryPreference(int userId) {
        List<Map<String, Object>> list = new ArrayList<>();
        Connection conn = null; PreparedStatement stmt = null; ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(
                "SELECT c.category_name, SUM(oi.quantity) AS qty, SUM(oi.quantity*oi.price) AS spent " +
                "FROM orders o JOIN order_items oi ON o.order_id=oi.order_id " +
                "JOIN products p ON oi.product_id=p.product_id " +
                "JOIN categories c ON p.category_id=c.category_id " +
                "WHERE o.user_id=? GROUP BY c.category_name ORDER BY spent DESC");
            stmt.setInt(1, userId);
            rs = stmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("category", rs.getString("category_name"));
                row.put("qty",      rs.getInt("qty"));
                row.put("spent",    rs.getDouble("spent"));
                list.add(row);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        finally { DBUtil.close(conn, stmt, rs); }
        return list;
    }

    // 调整积分
    public boolean adjustPoints(int userId, int delta) {
        Connection conn = null; PreparedStatement stmt = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(
                "MERGE INTO user_points up USING (SELECT ? AS uid FROM dual) src ON (up.user_id=src.uid) " +
                "WHEN MATCHED THEN UPDATE SET up.points=up.points+? " +
                "WHEN NOT MATCHED THEN INSERT VALUES(?,?,0)");
            stmt.setInt(1, userId);
            stmt.setInt(2, delta);
            stmt.setInt(3, userId);
            stmt.setInt(4, Math.max(0, delta));
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        finally { DBUtil.close(conn, stmt, null); }
        return false;
    }

    // 冻结/解冻账户
    public boolean updateUserStatus(int userId, String status) {
        Connection conn = null; PreparedStatement stmt = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement("UPDATE users SET status=? WHERE user_id=?");
            stmt.setString(1, status);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        finally { DBUtil.close(conn, stmt, null); }
        return false;
    }
}
