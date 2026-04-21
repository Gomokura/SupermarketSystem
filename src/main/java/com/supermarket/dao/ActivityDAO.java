package com.supermarket.dao;

import com.supermarket.util.DBUtil;
import java.sql.*;
import java.util.*;

public class ActivityDAO {

    public List<Map<String, Object>> getAllActivities() {
        List<Map<String, Object>> list = new ArrayList<>();
        Connection conn = null; PreparedStatement stmt = null; ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement("SELECT * FROM activities ORDER BY sort_order, create_time DESC");
            rs = stmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("activityId",  rs.getInt("activity_id"));
                row.put("title",       rs.getString("title"));
                row.put("description", rs.getString("description"));
                row.put("bannerUrl",   rs.getString("banner_url"));
                row.put("startTime",   rs.getTimestamp("start_time"));
                row.put("endTime",     rs.getTimestamp("end_time"));
                row.put("status",      rs.getString("status"));
                row.put("sortOrder",   rs.getInt("sort_order"));
                list.add(row);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        finally { DBUtil.close(conn, stmt, rs); }
        return list;
    }

    public boolean addActivity(String title, String description, String bannerUrl,
                                String startTime, String endTime, int sortOrder) {
        Connection conn = null; PreparedStatement stmt = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(
                "INSERT INTO activities(activity_id,title,description,banner_url,start_time,end_time,status,sort_order,create_time) " +
                "VALUES (seq_activity.NEXTVAL,?,?,?,TO_DATE(?,'YYYY-MM-DD'),TO_DATE(?,'YYYY-MM-DD'),'active',?,SYSDATE)");
            stmt.setString(1, title);
            stmt.setString(2, description);
            stmt.setString(3, bannerUrl);
            stmt.setString(4, startTime);
            stmt.setString(5, endTime);
            stmt.setInt(6, sortOrder);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        finally { DBUtil.close(conn, stmt, null); }
        return false;
    }

    public boolean updateStatus(int activityId, String status) {
        Connection conn = null; PreparedStatement stmt = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement("UPDATE activities SET status=? WHERE activity_id=?");
            stmt.setString(1, status);
            stmt.setInt(2, activityId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        finally { DBUtil.close(conn, stmt, null); }
        return false;
    }

    public boolean deleteActivity(int activityId) {
        Connection conn = null; PreparedStatement stmt = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement("DELETE FROM activities WHERE activity_id=?");
            stmt.setInt(1, activityId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        finally { DBUtil.close(conn, stmt, null); }
        return false;
    }
}
