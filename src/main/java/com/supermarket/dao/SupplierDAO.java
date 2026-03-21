package com.supermarket.dao;

import com.supermarket.util.DBUtil;
import java.sql.*;
import java.util.*;

public class SupplierDAO {

    public List<Map<String, Object>> getAllSuppliers() {
        List<Map<String, Object>> list = new ArrayList<>();
        Connection conn = null; PreparedStatement stmt = null; ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement("SELECT * FROM suppliers ORDER BY create_time DESC");
            rs = stmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("supplierId",   rs.getInt("supplier_id"));
                row.put("supplierName", rs.getString("supplier_name"));
                row.put("contact",      rs.getString("contact"));
                row.put("phone",        rs.getString("phone"));
                row.put("address",      rs.getString("address"));
                row.put("status",       rs.getString("status"));
                list.add(row);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        finally { DBUtil.close(conn, stmt, rs); }
        return list;
    }

    public boolean addSupplier(String name, String contact, String phone, String address) {
        Connection conn = null; PreparedStatement stmt = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(
                "INSERT INTO suppliers VALUES (seq_supplier.NEXTVAL,?,?,?,?,'active',SYSDATE)");
            stmt.setString(1, name);
            stmt.setString(2, contact);
            stmt.setString(3, phone);
            stmt.setString(4, address);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        finally { DBUtil.close(conn, stmt, null); }
        return false;
    }

    public boolean updateStatus(int supplierId, String status) {
        Connection conn = null; PreparedStatement stmt = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement("UPDATE suppliers SET status=? WHERE supplier_id=?");
            stmt.setString(1, status);
            stmt.setInt(2, supplierId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        finally { DBUtil.close(conn, stmt, null); }
        return false;
    }
}
