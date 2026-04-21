package com.supermarket.dao;

import com.supermarket.util.DBUtil;
import java.sql.*;
import java.util.*;

public class PromotionDAO {

    public List<Map<String, Object>> getAllPromotions() {
        List<Map<String, Object>> list = new ArrayList<>();
        Connection conn = null; PreparedStatement stmt = null; ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(
                "SELECT * FROM promotions ORDER BY create_time DESC");
            rs = stmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("promotionId",  rs.getInt("promotion_id"));
                row.put("promoName",    rs.getString("promo_name"));
                row.put("promoType",    rs.getString("promo_type"));
                row.put("conditionVal", rs.getDouble("condition_val"));
                row.put("discountVal",  rs.getDouble("discount_val"));
                row.put("startTime",    rs.getTimestamp("start_time"));
                row.put("endTime",      rs.getTimestamp("end_time"));
                row.put("status",       rs.getString("status"));
                list.add(row);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        finally { DBUtil.close(conn, stmt, rs); }
        return list;
    }

    public boolean addPromotion(String name, String type, double condVal, double discVal,
                                 String startTime, String endTime) {
        Connection conn = null; PreparedStatement stmt = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(
                "INSERT INTO promotions VALUES (seq_promotion.NEXTVAL,?,?,?,?,TO_DATE(?,'YYYY-MM-DD'),TO_DATE(?,'YYYY-MM-DD'),'active',SYSDATE)");
            stmt.setString(1, name);
            stmt.setString(2, type);
            stmt.setDouble(3, condVal);
            stmt.setDouble(4, discVal);
            stmt.setString(5, startTime);
            stmt.setString(6, endTime);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        finally { DBUtil.close(conn, stmt, null); }
        return false;
    }

    public boolean updateStatus(int promotionId, String status) {
        Connection conn = null; PreparedStatement stmt = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement("UPDATE promotions SET status=? WHERE promotion_id=?");
            stmt.setString(1, status);
            stmt.setInt(2, promotionId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        finally { DBUtil.close(conn, stmt, null); }
        return false;
    }

    public boolean deletePromotion(int promotionId) {
        Connection conn = null; PreparedStatement stmt = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement("DELETE FROM promotions WHERE promotion_id=?");
            stmt.setInt(1, promotionId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        finally { DBUtil.close(conn, stmt, null); }
        return false;
    }
}
