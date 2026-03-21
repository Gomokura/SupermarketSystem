package com.supermarket.dao;

import com.supermarket.util.DBUtil;
import java.sql.*;

public class PaymentDAO {

    public boolean createPayment(int orderId, double amount) {
        Connection conn = null; PreparedStatement stmt = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(
                "INSERT INTO payment VALUES (seq_payment.NEXTVAL,?,?,'success',SYSDATE)");
            stmt.setInt(1, orderId);
            stmt.setDouble(2, amount);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        finally { DBUtil.close(conn, stmt, null); }
        return false;
    }

    public boolean existsByOrderId(int orderId) {
        Connection conn = null; PreparedStatement stmt = null; ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(
                "SELECT COUNT(*) FROM payment WHERE order_id=? AND status='success'");
            stmt.setInt(1, orderId);
            rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        finally { DBUtil.close(conn, stmt, rs); }
        return false;
    }
}
