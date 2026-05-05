package com.supermarket.servlet.address;

import com.supermarket.entity.Address;
import com.supermarket.servlet.BaseServlet;
import com.supermarket.util.DBUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AddressListServlet extends BaseServlet {
    public void doAction(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer userId = getLoginUserId(req);
        if (userId == null) { jsonError(resp, "未登录"); return; }

        String sql = "SELECT * FROM ADDRESSES WHERE USER_ID = ? ORDER BY IS_DEFAULT DESC, CREATE_TIME DESC";
        List<Address> list = new ArrayList<>();
        try (Connection conn = DBUtil.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(extractAddress(rs));
            }
            json(req, resp, list);
        } catch (SQLException e) {
            e.printStackTrace();
            jsonError(resp, "数据库错误: " + e.getMessage());
        }
    }

    private Address extractAddress(ResultSet rs) throws SQLException {
        Address a = new Address();
        a.setAddressId(rs.getInt("ADDRESS_ID"));
        a.setUserId(rs.getInt("USER_ID"));
        a.setContactName(rs.getString("CONTACT_NAME"));
        a.setContactPhone(rs.getString("CONTACT_PHONE"));
        a.setProvince(rs.getString("PROVINCE"));
        a.setCity(rs.getString("CITY"));
        a.setDistrict(rs.getString("DISTRICT"));
        a.setDetailAddress(rs.getString("DETAIL_ADDRESS"));
        a.setIsDefault(rs.getInt("IS_DEFAULT"));
        a.setTag(rs.getString("TAG"));
        return a;
    }
}
