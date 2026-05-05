package com.supermarket.service;

import com.supermarket.entity.Result;
import com.supermarket.entity.User;
import com.supermarket.entity.Admin;
import com.supermarket.entity.Courier;
import com.supermarket.util.PasswordEncoder;
import com.supermarket.util.DBUtil;

import java.sql.*;
import java.util.*;

/**
 * AuthService - 认证服务（轻量级实现，不依赖 MyBatis-Plus ServiceImpl）
 * 直接使用 JDBC 操作数据库，配合 Spring 的 JdbcTemplate 或原生 JDBC
 */
public class AuthService {

    public Result<?> login(String username, String phone, String password) {
        String sql;
        if (phone != null && !phone.isEmpty()) {
            sql = "SELECT * FROM USERS WHERE PHONE = ?";
        } else {
            sql = "SELECT * FROM USERS WHERE USERNAME = ?";
        }

        try (Connection conn = DBUtil.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, phone != null && !phone.isEmpty() ? phone : username);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                return Result.error("用户不存在");
            }
            User user = extractUser(rs);
            if ("banned".equals(user.getStatus())) {
                return Result.error("账号已被封禁");
            }
            if (!PasswordEncoder.matches(password, user.getPassword())) {
                return Result.error("密码错误");
            }
            Map<String, Object> data = new HashMap<>();
            data.put("userId", user.getUserId());
            data.put("username", user.getUsername());
            data.put("nickname", user.getNickname());
            data.put("avatarUrl", user.getAvatarUrl());
            data.put("memberLevel", user.getMemberLevel());
            data.put("points", user.getPoints());
            data.put("phone", user.getPhone());
            return Result.success(data);
        } catch (SQLException e) {
            e.printStackTrace();
            return Result.error("数据库错误: " + e.getMessage());
        }
    }

    public Result<?> register(String username, String password, String realName,
                             String phone, String email, String nickname) {
        try (Connection conn = DBUtil.getDataSource().getConnection()) {
            // 检查用户名
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT USER_ID FROM USERS WHERE USERNAME = ?")) {
                ps.setString(1, username);
                if (ps.executeQuery().next()) {
                    return Result.error("用户名已存在");
                }
            }
            // 检查手机号
            if (phone != null && !phone.isEmpty()) {
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT USER_ID FROM USERS WHERE PHONE = ?")) {
                    ps.setString(1, phone);
                    if (ps.executeQuery().next()) {
                        return Result.error("手机号已被注册");
                    }
                }
            }

            Integer userId = DBUtil.getNextId("SEQ_USERS");
            String encodedPwd = PasswordEncoder.encode(password);
            String finalNickname = (nickname != null && !nickname.isEmpty()) ? nickname : username;

            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO USERS (USER_ID, USERNAME, PASSWORD, REAL_NAME, PHONE, EMAIL, NICKNAME, STATUS, MEMBER_LEVEL, POINTS, CREATE_TIME) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, 'active', 'NORMAL', 0, SYSDATE)")) {
                ps.setInt(1, userId);
                ps.setString(2, username);
                ps.setString(3, encodedPwd);
                ps.setString(4, realName);
                ps.setString(5, phone);
                ps.setString(6, email);
                ps.setString(7, finalNickname);
                ps.executeUpdate();
            }
            return Result.success("注册成功");
        } catch (SQLException e) {
            e.printStackTrace();
            return Result.error("数据库错误: " + e.getMessage());
        }
    }

    public Result<?> getUserInfo(Integer userId) {
        try (Connection conn = DBUtil.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM USERS WHERE USER_ID = ?")) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return Result.error("用户不存在");
            User user = extractUser(rs);
            user.setPassword(null);
            return Result.success(user);
        } catch (SQLException e) {
            e.printStackTrace();
            return Result.error("数据库错误: " + e.getMessage());
        }
    }

    public Result<?> updateUserInfo(Integer userId, User updateData) {
        StringBuilder sql = new StringBuilder("UPDATE USERS SET UPDATE_TIME = SYSDATE");
        List<Object> params = new ArrayList<>();
        if (updateData.getNickname() != null) { sql.append(", NICKNAME = ?"); params.add(updateData.getNickname()); }
        if (updateData.getAvatarUrl() != null) { sql.append(", AVATAR_URL = ?"); params.add(updateData.getAvatarUrl()); }
        if (updateData.getGender() != null) { sql.append(", GENDER = ?"); params.add(updateData.getGender()); }
        if (updateData.getEmail() != null) { sql.append(", EMAIL = ?"); params.add(updateData.getEmail()); }
        if (updateData.getRealName() != null) { sql.append(", REAL_NAME = ?"); params.add(updateData.getRealName()); }
        if (updateData.getPhone() != null) { sql.append(", PHONE = ?"); params.add(updateData.getPhone()); }
        sql.append(" WHERE USER_ID = ?");
        params.add(userId);

        try (Connection conn = DBUtil.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            ps.executeUpdate();
            return Result.success(getUserInfo(userId).getData());
        } catch (SQLException e) {
            e.printStackTrace();
            return Result.error("数据库错误: " + e.getMessage());
        }
    }

    public Result<?> changePassword(Integer userId, String oldPassword, String newPassword) {
        try (Connection conn = DBUtil.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT PASSWORD FROM USERS WHERE USER_ID = ?")) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return Result.error("用户不存在");
            String encodedPwd = rs.getString("PASSWORD");
            if (!PasswordEncoder.matches(oldPassword, encodedPwd)) {
                return Result.error("原密码错误");
            }
            String newEncoded = PasswordEncoder.encode(newPassword);
            try (PreparedStatement ups = conn.prepareStatement(
                    "UPDATE USERS SET PASSWORD = ?, UPDATE_TIME = SYSDATE WHERE USER_ID = ?")) {
                ups.setString(1, newEncoded);
                ups.setInt(2, userId);
                ups.executeUpdate();
            }
            return Result.success("密码修改成功");
        } catch (SQLException e) {
            e.printStackTrace();
            return Result.error("数据库错误: " + e.getMessage());
        }
    }

    public Result<?> adminLogin(String username, String password) {
        try (Connection conn = DBUtil.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT * FROM ADMIN_USERS WHERE USERNAME = ?")) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return Result.error("管理员不存在");
            Admin admin = extractAdmin(rs);
            if (!PasswordEncoder.matches(password, admin.getPassword())) {
                return Result.error("密码错误");
            }
            if ("disabled".equals(admin.getStatus())) return Result.error("账号已被禁用");
            Map<String, Object> data = new HashMap<>();
            data.put("adminId", admin.getAdminId());
            data.put("username", admin.getUsername());
            data.put("realName", admin.getRealName());
            data.put("role", admin.getRole());
            return Result.success(data);
        } catch (SQLException e) {
            e.printStackTrace();
            return Result.error("数据库错误: " + e.getMessage());
        }
    }

    public Result<?> courierLogin(String username, String password) {
        String sql = "SELECT * FROM DELIVERY_PERSONS WHERE PHONE = ? OR REAL_NAME = ?";
        try (Connection conn = DBUtil.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, username);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return Result.error("配送员账号不存在");
            Courier c = extractCourier(rs);
            if (!PasswordEncoder.matches(password, c.getPassword())) {
                return Result.error("密码错误");
            }
            if ("inactive".equals(c.getStatus())) return Result.error("账号已被禁用");
            Map<String, Object> data = new HashMap<>();
            data.put("courierId", c.getCourierId());
            data.put("courierName", c.getCourierName());
            data.put("phone", c.getPhone());
            data.put("status", c.getStatus());
            return Result.success(data);
        } catch (SQLException e) {
            e.printStackTrace();
            return Result.error("数据库错误: " + e.getMessage());
        }
    }

    public Result<?> getMemberByPhone(String phone) {
        if (phone == null || phone.isEmpty()) return Result.error("手机号不能为空");
        try (Connection conn = DBUtil.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT USER_ID, NICKNAME, PHONE, MEMBER_LEVEL, POINTS FROM USERS WHERE PHONE = ?")) {
            ps.setString(1, phone);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return Result.error(404, "会员不存在");
            Map<String, Object> data = new HashMap<>();
            data.put("userId", rs.getInt("USER_ID"));
            data.put("nickname", rs.getString("NICKNAME"));
            data.put("phone", maskPhone(rs.getString("PHONE")));
            data.put("memberLevel", rs.getString("MEMBER_LEVEL"));
            data.put("points", rs.getInt("POINTS"));
            return Result.success(data);
        } catch (SQLException e) {
            e.printStackTrace();
            return Result.error("数据库错误: " + e.getMessage());
        }
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) return phone;
        return phone.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
    }

    private User extractUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setUserId(rs.getInt("USER_ID"));
        u.setUsername(rs.getString("USERNAME"));
        u.setPassword(rs.getString("PASSWORD"));
        u.setRealName(rs.getString("REAL_NAME"));
        u.setPhone(rs.getString("PHONE"));
        u.setStatus(rs.getString("STATUS"));
        u.setNickname(rs.getString("NICKNAME"));
        u.setAvatarUrl(rs.getString("AVATAR_URL"));
        u.setGender(rs.getString("GENDER"));
        u.setBirthday(rs.getDate("BIRTHDAY"));
        u.setEmail(rs.getString("EMAIL"));
        u.setMemberLevel(rs.getString("MEMBER_LEVEL"));
        u.setPoints(rs.getObject("POINTS") != null ? ((Number) rs.getObject("POINTS")).intValue() : 0);
        return u;
    }

    private Admin extractAdmin(ResultSet rs) throws SQLException {
        Admin a = new Admin();
        a.setAdminId(rs.getInt("ADMIN_ID"));
        a.setUsername(rs.getString("USERNAME"));
        a.setPassword(rs.getString("PASSWORD"));
        a.setRealName(rs.getString("REAL_NAME"));
        a.setRole(rs.getString("ROLE"));
        a.setPhone(rs.getString("PHONE"));
        a.setStatus(rs.getString("STATUS"));
        return a;
    }

    private Courier extractCourier(ResultSet rs) throws SQLException {
        Courier c = new Courier();
        c.setCourierId(rs.getInt("COURIER_ID"));
        c.setCourierName(rs.getString("REAL_NAME"));
        c.setPhone(rs.getString("PHONE"));
        c.setPassword(rs.getString("PASSWORD"));
        c.setStatus(rs.getString("STATUS"));
        return c;
    }
}
