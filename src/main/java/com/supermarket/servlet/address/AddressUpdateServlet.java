package com.supermarket.servlet.address;

import com.supermarket.servlet.BaseServlet;
import com.supermarket.util.DBUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;

public class AddressUpdateServlet extends BaseServlet {
    public void doAction(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer userId = getLoginUserId(req);
        if (userId == null) { jsonError(resp, "未登录"); return; }
        Integer addressId = getInteger(req, "addressId");
        if (addressId == null) { jsonError(resp, "地址ID不能为空"); return; }

        try (Connection conn = DBUtil.getDataSource().getConnection()) {
            // 验证归属
            String checkSql = "SELECT USER_ID FROM ADDRESSES WHERE ADDRESS_ID = ?";
            try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                ps.setInt(1, addressId);
                ResultSet rs = ps.executeQuery();
                if (!rs.next() || rs.getInt("USER_ID") != userId) {
                    jsonError(resp, "地址不存在或无权修改"); return;
                }
            }

            // 如果设为默认，先取消其他默认
            Integer isDefault = getInteger(req, "isDefault");
            if (isDefault != null && isDefault == 1) {
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE ADDRESSES SET IS_DEFAULT = 0 WHERE USER_ID = ? AND IS_DEFAULT = 1")) {
                    ps.setInt(1, userId);
                    ps.executeUpdate();
                }
            }

            StringBuilder sql = new StringBuilder("UPDATE ADDRESSES SET ");
            java.util.List<String> fields = new java.util.ArrayList<>();
            if (getString(req, "receiverName") != null) fields.add("RECEIVER_NAME = ?");
            if (getString(req, "phone") != null) fields.add("PHONE = ?");
            if (getString(req, "province") != null) fields.add("PROVINCE = ?");
            if (getString(req, "city") != null) fields.add("CITY = ?");
            if (getString(req, "district") != null) fields.add("DISTRICT = ?");
            if (getString(req, "detail") != null) fields.add("DETAIL_ADDRESS = ?");
            if (isDefault != null) fields.add("IS_DEFAULT = ?");

            if (fields.isEmpty()) { jsonOk(req, resp); return; }

            for (int i = 0; i < fields.size(); i++) {
                sql.append(fields.get(i));
                if (i < fields.size() - 1) sql.append(", ");
            }
            sql.append(" WHERE ADDRESS_ID = ?");

            java.util.List<Object> params = new java.util.ArrayList<>();
            if (getString(req, "receiverName") != null) params.add(getString(req, "receiverName"));
            if (getString(req, "phone") != null) params.add(getString(req, "phone"));
            if (getString(req, "province") != null) params.add(getString(req, "province"));
            if (getString(req, "city") != null) params.add(getString(req, "city"));
            if (getString(req, "district") != null) params.add(getString(req, "district"));
            if (getString(req, "detail") != null) params.add(getString(req, "detail"));
            if (isDefault != null) params.add(isDefault);
            params.add(addressId);

            try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
                ps.executeUpdate();
            }
            jsonOk(req, resp);
        } catch (SQLException e) {
            e.printStackTrace();
            jsonError(resp, "数据库错误: " + e.getMessage());
        }
    }
}
