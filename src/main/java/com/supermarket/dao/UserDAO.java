package com.supermarket.dao;

import com.supermarket.bean.User;
import com.supermarket.util.DBUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    public User login(String username, String password) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT * FROM users WHERE username=? AND password=? AND status='active'";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password);
            rs = stmt.executeQuery();
            if (rs.next()) {
                User user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setUsername(rs.getString("username"));
                user.setRealName(rs.getString("real_name"));
                user.setRole(rs.getString("role"));
                user.setPhone(rs.getString("phone"));
                user.setStatus(rs.getString("status"));
                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, stmt, rs);
        }
        return null;
    }

    public List<User> searchUsers(String keyword, String role, String orderBy) {
        List<User> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            StringBuilder sql = new StringBuilder("SELECT * FROM users WHERE 1=1");
            if (keyword != null && !keyword.trim().isEmpty()) {
                sql.append(" AND (username LIKE ? OR real_name LIKE ?)");
            }
            if (role != null && !role.isEmpty()) {
                sql.append(" AND role=?");
            }
            if (orderBy != null && !orderBy.isEmpty()) {
                sql.append(" ORDER BY ").append(orderBy);
            }
            stmt = conn.prepareStatement(sql.toString());
            int index = 1;
            if (keyword != null && !keyword.trim().isEmpty()) {
                stmt.setString(index++, "%" + keyword + "%");
                stmt.setString(index++, "%" + keyword + "%");
            }
            if (role != null && !role.isEmpty()) {
                stmt.setString(index++, role);
            }
            rs = stmt.executeQuery();
            while (rs.next()) {
                User user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setUsername(rs.getString("username"));
                user.setRealName(rs.getString("real_name"));
                user.setRole(rs.getString("role"));
                user.setPhone(rs.getString("phone"));
                user.setStatus(rs.getString("status"));
                user.setCreateTime(rs.getDate("create_time"));
                list.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, stmt, rs);
        }
        return list;
    }

    public boolean addUser(User user) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "INSERT INTO users VALUES (seq_user.NEXTVAL,?,?,?,?,?,?,SYSDATE)";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getRealName());
            stmt.setString(4, user.getRole());
            stmt.setString(5, user.getPhone());
            stmt.setString(6, user.getStatus());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, stmt, null);
        }
        return false;
    }

    public boolean updateUser(User user) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "UPDATE users SET real_name=?,role=?,phone=?,status=? WHERE user_id=?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, user.getRealName());
            stmt.setString(2, user.getRole());
            stmt.setString(3, user.getPhone());
            stmt.setString(4, user.getStatus());
            stmt.setInt(5, user.getUserId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, stmt, null);
        }
        return false;
    }

    public boolean updatePassword(int userId, String oldPassword, String newPassword) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            String checkSql = "SELECT password FROM users WHERE user_id=?";
            stmt = conn.prepareStatement(checkSql);
            stmt.setInt(1, userId);
            rs = stmt.executeQuery();
            if (rs.next() && oldPassword.equals(rs.getString("password"))) {
                stmt.close();
                String updateSql = "UPDATE users SET password=? WHERE user_id=?";
                stmt = conn.prepareStatement(updateSql);
                stmt.setString(1, newPassword);
                stmt.setInt(2, userId);
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, stmt, rs);
        }
        return false;
    }

    public List<User> getUsersByRole(String role) {
        return searchUsers(null, role, "username");
    }

    public int countAllUsers() {
        Connection conn = null; PreparedStatement stmt = null; ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement("SELECT COUNT(*) FROM users WHERE role='customer'");
            rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        finally { DBUtil.close(conn, stmt, rs); }
        return 0;
    }
}
