package com.supermarket.dao;

import com.supermarket.util.DBUtil;
import java.sql.*;
import java.util.*;

public class AuditLogDAO {

    public void log(int userId, String username, String action, String target, String detail, String ip) {
        Connection conn = null; PreparedStatement stmt = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(
                "INSERT INTO audit_logs(log_id,user_id,username,action,target,detail,ip,log_time) " +
                "VALUES (seq_audit_log.NEXTVAL,?,?,?,?,?,?,SYSDATE)");
            stmt.setInt(1, userId);
            stmt.setString(2, username);
            stmt.setString(3, action);
            stmt.setString(4, target);
            stmt.setString(5, detail);
            stmt.setString(6, ip);
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
        finally { DBUtil.close(conn, stmt, null); }
    }

    public List<Map<String, Object>> searchLogs(String username, String action, int limit) {
        List<Map<String, Object>> list = new ArrayList<>();
        Connection conn = null; PreparedStatement stmt = null; ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            StringBuilder sql = new StringBuilder(
                "SELECT * FROM audit_logs WHERE 1=1");
            if (username != null && !username.isEmpty()) sql.append(" AND username LIKE ?");
            if (action   != null && !action.isEmpty())   sql.append(" AND action LIKE ?");
            sql.append(" ORDER BY log_time DESC FETCH FIRST ? ROWS ONLY");
            stmt = conn.prepareStatement(sql.toString());
            int idx = 1;
            if (username != null && !username.isEmpty()) stmt.setString(idx++, "%" + username + "%");
            if (action   != null && !action.isEmpty())   stmt.setString(idx++, "%" + action + "%");
            stmt.setInt(idx, limit);
            rs = stmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("logId",    rs.getInt("log_id"));
                row.put("username", rs.getString("username"));
                row.put("action",   rs.getString("action"));
                row.put("target",   rs.getString("target"));
                row.put("detail",   rs.getString("detail"));
                row.put("ip",       rs.getString("ip"));
                row.put("logTime",  rs.getTimestamp("log_time"));
                list.add(row);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        finally { DBUtil.close(conn, stmt, rs); }
        return list;
    }
}
