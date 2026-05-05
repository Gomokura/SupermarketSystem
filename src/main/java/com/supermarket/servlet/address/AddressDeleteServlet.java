package com.supermarket.servlet.address;

import com.supermarket.servlet.BaseServlet;
import com.supermarket.util.DBUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;

public class AddressDeleteServlet extends BaseServlet {
    public void doAction(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer userId = getLoginUserId(req);
        if (userId == null) { jsonError(resp, "未登录"); return; }
        Integer addressId = getInteger(req, "addressId");
        if (addressId == null) { jsonError(resp, "地址ID不能为空"); return; }

        try (Connection conn = DBUtil.getDataSource().getConnection()) {
            String checkSql = "SELECT IS_DEFAULT FROM ADDRESSES WHERE ADDRESS_ID = ? AND USER_ID = ?";
            try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                ps.setInt(1, addressId);
                ps.setInt(2, userId);
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) {
                    jsonError(resp, "地址不存在或无权删除"); return;
                }
                if (rs.getInt("IS_DEFAULT") == 1) {
                    jsonError(resp, "默认地址不能删除，请先设置其他地址为默认"); return;
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM ADDRESSES WHERE ADDRESS_ID = ? AND USER_ID = ?")) {
                ps.setInt(1, addressId);
                ps.setInt(2, userId);
                ps.executeUpdate();
            }
            jsonOk(req, resp);
        } catch (SQLException e) {
            e.printStackTrace();
            jsonError(resp, "数据库错误: " + e.getMessage());
        }
    }
}
