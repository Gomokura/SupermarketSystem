package com.supermarket.dao;

import com.supermarket.util.DBUtil;
import java.sql.*;

public class OutboundDAO {

    public boolean createOutbound(Connection conn, int orderId, int productId, int quantity) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(
            "INSERT INTO outbound VALUES (seq_outbound.NEXTVAL,?,?,?,SYSDATE)");
        stmt.setInt(1, orderId);
        stmt.setInt(2, productId);
        stmt.setInt(3, quantity);
        boolean result = stmt.executeUpdate() > 0;
        stmt.close();
        return result;
    }

    // 检查库存是否充足（动态计算）
    public int getStock(int productId) {
        Connection conn = null; PreparedStatement stmt = null; ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement("SELECT stock FROM v_stock WHERE product_id=?");
            stmt.setInt(1, productId);
            rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("stock");
        } catch (SQLException e) { e.printStackTrace(); }
        finally { DBUtil.close(conn, stmt, rs); }
        return 0;
    }
}
