package com.supermarket.servlet;

import com.supermarket.bean.User;
import com.supermarket.dao.ActivityDAO;
import com.supermarket.dao.AuditLogDAO;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class AdminActivityServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = (User) request.getSession().getAttribute("user");
        if (user == null || !"admin".equals(user.getRole())) { response.sendRedirect("login.jsp"); return; }

        List<Map<String, Object>> activities = new ActivityDAO().getAllActivities();
        request.setAttribute("activities", activities);
        request.getRequestDispatcher("/admin/activityList.jsp").forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        User user = (User) request.getSession().getAttribute("user");
        if (user == null || !"admin".equals(user.getRole())) { response.sendRedirect("login.jsp"); return; }

        String action = request.getParameter("action");
        ActivityDAO dao = new ActivityDAO();
        AuditLogDAO auditDAO = new AuditLogDAO();

        if ("add".equals(action)) {
            String sortStr = request.getParameter("sortOrder");
            int sort = (sortStr != null && !sortStr.isEmpty()) ? Integer.parseInt(sortStr) : 0;
            dao.addActivity(
                request.getParameter("title"),
                request.getParameter("description"),
                request.getParameter("bannerUrl"),
                request.getParameter("startTime"),
                request.getParameter("endTime"),
                sort
            );
            auditDAO.log(user.getUserId(), user.getUsername(), "创建活动",
                request.getParameter("title"), null, request.getRemoteAddr());

        } else if ("updateStatus".equals(action)) {
            int id = Integer.parseInt(request.getParameter("activityId"));
            dao.updateStatus(id, request.getParameter("status"));
            auditDAO.log(user.getUserId(), user.getUsername(), "更新活动状态",
                "activity_id:" + id, request.getParameter("status"), request.getRemoteAddr());

        } else if ("delete".equals(action)) {
            int id = Integer.parseInt(request.getParameter("activityId"));
            dao.deleteActivity(id);
            auditDAO.log(user.getUserId(), user.getUsername(), "删除活动",
                "activity_id:" + id, null, request.getRemoteAddr());
        }
        response.sendRedirect(request.getContextPath() + "/adminActivity");
    }
}
