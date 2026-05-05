package com.supermarket.servlet.address;

import com.supermarket.entity.Address;
import com.supermarket.servlet.BaseServlet;
import com.supermarket.util.DBUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;

public class AddressSetDefaultServlet extends BaseServlet {
    public void doAction(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer userId = getLoginUserId(req);
        if (userId == null) { jsonError(resp, "未登录"); return; }
        Integer addressId = getInteger(req, "addressId");
        if (addressId == null) { jsonError(resp, "地址ID不能为空"); return; }

        try (Connection conn = DBUtil.getDataSource().getConnection()) {
            // 验证地址属于当前用户
            String checkSql = "SELECT * FROM ADDRESSES WHERE ADDRESS_ID = ? AND USER_ID = ?";
            try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                ps.setInt(1, addressId);
                ps.setInt(2, userId);
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) {
                    jsonError(resp, "地址不存在或无权修改"); return;
                }
            }

            // 取消所有默认
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE ADDRESSES SET IS_DEFAULT = 0 WHERE USER_ID = ? AND IS_DEFAULT = 1")) {
                ps.setInt(1, userId);
                ps.executeUpdate();
            }

            // 设置新默认
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE ADDRESSES SET IS_DEFAULT = 1 WHERE ADDRESS_ID = ?")) {
                ps.setInt(1, addressId);
                ps.executeUpdate();
            }
            jsonOk(req, resp);
        } catch (SQLException e) {
            e.printStackTrace();
            jsonError(resp, "数据库错误: " + e.getMessage());
        }
    }
}
