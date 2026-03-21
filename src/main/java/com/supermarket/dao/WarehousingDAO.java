package com.supermarket.dao;

import com.supermarket.util.DBUtil;
import java.sql.*;
import java.util.*;

public class WarehousingDAO {

    public boolean addStock(int productId, int quantity, int operatorId, String remark) {
        Connection conn = null; PreparedStatement stmt = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(
                "INSERT INTO warehousing VALUES (seq_warehousing.NEXTVAL,?,?,?,?,SYSDATE)");
            stmt.setInt(1, productId);
            stmt.setInt(2, quantity);
            stmt.setInt(3, operatorId);
            stmt.setString(4, remark);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        finally { DBUtil.close(conn, stmt, null); }
        return false;
    }

    public List<Map<String, Object>> getStockList() {
        List<Map<String, Object>> list = new ArrayList<>();
        Connection conn = null; PreparedStatement stmt = null; ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(
                "SELECT v.product_id, v.product_name, v.stock, p.price, c.category_name " +
                "FROM v_stock v " +
                "JOIN products p ON v.product_id = p.product_id " +
                "JOIN categories c ON p.category_id = c.category_id " +
                "ORDER BY v.product_name");
            rs = stmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("productId",    rs.getInt("product_id"));
                row.put("productName",  rs.getString("product_name"));
                row.put("stock",        rs.getInt("stock"));
                row.put("price",        rs.getDouble("price"));
                row.put("categoryName", rs.getString("category_name"));
                list.add(row);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        finally { DBUtil.close(conn, stmt, rs); }
        return list;
    }

    public List<Map<String, Object>> getWarehousingLogs() {
        List<Map<String, Object>> list = new ArrayList<>();
        Connection conn = null; PreparedStatement stmt = null; ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(
                "SELECT w.*, p.product_name, u.username FROM warehousing w " +
                "JOIN products p ON w.product_id=p.product_id " +
                "LEFT JOIN users u ON w.operator_id=u.user_id " +
                "ORDER BY w.create_time DESC FETCH FIRST 100 ROWS ONLY");
            rs = stmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("warehousingId", rs.getInt("warehousing_id"));
                row.put("productName",   rs.getString("product_name"));
                row.put("quantity",      rs.getInt("quantity"));
                row.put("operator",      rs.getString("username"));
                row.put("remark",        rs.getString("remark"));
                row.put("createTime",    rs.getTimestamp("create_time"));
                list.add(row);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        finally { DBUtil.close(conn, stmt, rs); }
        return list;
    }
}
