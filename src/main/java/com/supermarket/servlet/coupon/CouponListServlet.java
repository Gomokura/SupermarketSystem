package com.supermarket.servlet.coupon;

import com.supermarket.entity.Coupon;
import com.supermarket.servlet.BaseServlet;
import com.supermarket.util.DBUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CouponListServlet extends BaseServlet {
    public void doAction(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String sql = "SELECT * FROM COUPONS WHERE STATUS = 'active' AND START_TIME <= SYSDATE AND END_TIME >= SYSDATE ORDER BY CREATE_TIME DESC";
        List<Coupon> list = new ArrayList<>();
        try (Connection conn = DBUtil.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(extractCoupon(rs));
            }
            json(req, resp, list);
        } catch (SQLException e) {
            e.printStackTrace();
            jsonError(resp, "数据库错误: " + e.getMessage());
        }
    }

    private Coupon extractCoupon(ResultSet rs) throws SQLException {
        Coupon c = new Coupon();
        c.setCouponId(rs.getInt("COUPON_ID"));
        c.setCouponName(rs.getString("COUPON_NAME"));
        c.setCouponType(rs.getString("COUPON_TYPE"));
        c.setFaceValue(rs.getDouble("FACE_VALUE"));
        c.setMinAmount(rs.getDouble("MIN_AMOUNT"));
        c.setTotalCount(rs.getInt("TOTAL_COUNT"));
        c.setIssuedCount(rs.getInt("ISSUED_COUNT"));
        c.setPerLimit(rs.getInt("PER_LIMIT"));
        c.setStartTime(rs.getTimestamp("START_TIME"));
        c.setEndTime(rs.getTimestamp("END_TIME"));
        c.setStatus(rs.getString("STATUS"));
        return c;
    }
}
