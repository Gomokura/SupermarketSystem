package com.supermarket.servlet;

import com.supermarket.bean.User;
import com.supermarket.dao.UserProfileDAO;
import com.supermarket.dao.AuditLogDAO;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class AdminUserProfileServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = (User) request.getSession().getAttribute("user");
        if (user == null || !"admin".equals(user.getRole())) { response.sendRedirect("login.jsp"); return; }

        String keyword = request.getParameter("keyword");
        String uidStr  = request.getParameter("userId");
        UserProfileDAO dao = new UserProfileDAO();

        if (uidStr != null && !uidStr.isEmpty()) {
            int uid = Integer.parseInt(uidStr);
            request.setAttribute("preferences", dao.getUserCategoryPreference(uid));
            request.setAttribute("targetUserId", uid);
        }
        request.setAttribute("profiles", dao.getUserProfiles(keyword));
        request.setAttribute("keyword",  keyword);
        request.getRequestDispatcher("/admin/userProfile.jsp").forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = (User) request.getSession().getAttribute("user");
        if (user == null || !"admin".equals(user.getRole())) { response.sendRedirect("login.jsp"); return; }

        String action = request.getParameter("action");
        int targetId  = Integer.parseInt(request.getParameter("targetUserId"));
        UserProfileDAO dao = new UserProfileDAO();
        AuditLogDAO auditDAO = new AuditLogDAO();

        if ("adjustPoints".equals(action)) {
            int delta = Integer.parseInt(request.getParameter("delta"));
            dao.adjustPoints(targetId, delta);
            auditDAO.log(user.getUserId(), user.getUsername(), "调整积分",
                "user_id:" + targetId, (delta > 0 ? "+" : "") + delta, request.getRemoteAddr());
        } else if ("freeze".equals(action)) {
            dao.updateUserStatus(targetId, "frozen");
            auditDAO.log(user.getUserId(), user.getUsername(), "冻结账户",
                "user_id:" + targetId, null, request.getRemoteAddr());
        } else if ("unfreeze".equals(action)) {
            dao.updateUserStatus(targetId, "active");
            auditDAO.log(user.getUserId(), user.getUsername(), "解冻账户",
                "user_id:" + targetId, null, request.getRemoteAddr());
        }
        response.sendRedirect(request.getContextPath() + "/adminUserProfile");
    }
}
