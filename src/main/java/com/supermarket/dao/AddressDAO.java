package com.supermarket.dao;

import com.supermarket.bean.Address;
import com.supermarket.util.DBUtil;
import java.sql.*;
import java.util.*;

public class AddressDAO {

    public List<Address> getByUserId(int userId) {
        List<Address> list = new ArrayList<>();
        Connection conn = null; PreparedStatement stmt = null; ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(
                "SELECT * FROM address WHERE user_id=? ORDER BY is_default DESC");
            stmt.setInt(1, userId);
            rs = stmt.executeQuery();
            while (rs.next()) {
                Address a = new Address();
                a.setAddressId(rs.getInt("address_id"));
                a.setUserId(rs.getInt("user_id"));
                a.setReceiver(rs.getString("receiver"));
                a.setPhone(rs.getString("phone"));
                a.setDetail(rs.getString("detail"));
                a.setIsDefault(rs.getInt("is_default"));
                list.add(a);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        finally { DBUtil.close(conn, stmt, rs); }
        return list;
    }

    public boolean add(int userId, String receiver, String phone, String detail) {
        Connection conn = null; PreparedStatement stmt = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(
                "INSERT INTO address VALUES (seq_address.NEXTVAL,?,?,?,?,0)");
            stmt.setInt(1, userId);
            stmt.setString(2, receiver);
            stmt.setString(3, phone);
            stmt.setString(4, detail);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        finally { DBUtil.close(conn, stmt, null); }
        return false;
    }

    public boolean delete(int addressId, int userId) {
        Connection conn = null; PreparedStatement stmt = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(
                "DELETE FROM address WHERE address_id=? AND user_id=?");
            stmt.setInt(1, addressId);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        finally { DBUtil.close(conn, stmt, null); }
        return false;
    }

    public boolean setDefault(int addressId, int userId) {
        Connection conn = null; PreparedStatement stmt = null;
        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);
            stmt = conn.prepareStatement("UPDATE address SET is_default=0 WHERE user_id=?");
            stmt.setInt(1, userId);
            stmt.executeUpdate();
            stmt.close();
            stmt = conn.prepareStatement("UPDATE address SET is_default=1 WHERE address_id=? AND user_id=?");
            stmt.setInt(1, addressId);
            stmt.setInt(2, userId);
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
}
