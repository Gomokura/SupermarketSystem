package com.supermarket.servlet.address;

import com.supermarket.entity.Address;
import com.supermarket.servlet.BaseServlet;
import com.supermarket.util.DBUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;

public class AddressAddServlet extends BaseServlet {
    public void doAction(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer userId = getLoginUserId(req);
        if (userId == null) { jsonError(resp, "未登录"); return; }

        // 检查地址数量上限
        try (Connection conn = DBUtil.getDataSource().getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT COUNT(*) FROM ADDRESSES WHERE USER_ID = ?")) {
                ps.setInt(1, userId);
                ResultSet rs = ps.executeQuery();
                if (rs.next() && rs.getInt(1) >= 10) {
                    jsonError(resp, "收货地址最多10条"); return;
                }
            }

            // 如果设为默认，先取消其他默认
            Integer isDefault = getInteger(req, "isDefault", 0);
            if (isDefault == 1) {
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE ADDRESSES SET IS_DEFAULT = 0 WHERE USER_ID = ?")) {
                    ps.setInt(1, userId);
                    ps.executeUpdate();
                }
            }

            Integer addressId = DBUtil.getNextId("SEQ_ADDRESSES");
            String sql = "INSERT INTO ADDRESSES (ADDRESS_ID, USER_ID, RECEIVER_NAME, PHONE, PROVINCE, CITY, DISTRICT, DETAIL_ADDRESS, IS_DEFAULT, CREATE_TIME) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, SYSDATE)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, addressId);
                ps.setInt(2, userId);
                ps.setString(3, getString(req, "receiverName"));
                ps.setString(4, getString(req, "phone"));
                ps.setString(5, getString(req, "province"));
                ps.setString(6, getString(req, "city"));
                ps.setString(7, getString(req, "district"));
                ps.setString(8, getString(req, "detail"));
                ps.setInt(9, isDefault);
                ps.executeUpdate();
            }
            jsonOk(req, resp);
        } catch (SQLException e) {
            e.printStackTrace();
            jsonError(resp, "数据库错误: " + e.getMessage());
        }
    }
}
