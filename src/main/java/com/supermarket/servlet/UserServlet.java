package com.supermarket.servlet;

import com.supermarket.bean.User;
import com.supermarket.dao.UserDAO;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;

public class UserServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        UserDAO userDAO = new UserDAO();

        if ("search".equals(action) || action == null) {
            String keyword = request.getParameter("keyword");
            String role = request.getParameter("role");
            String orderBy = request.getParameter("orderBy");

            List<User> users = userDAO.searchUsers(keyword, role, orderBy);
            request.setAttribute("users", users);
            request.getRequestDispatcher("/admin/userList.jsp").forward(request, response);
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");
        UserDAO userDAO = new UserDAO();

        if ("add".equals(action)) {
            User user = new User();
            user.setUsername(request.getParameter("username"));
            user.setPassword(request.getParameter("password"));
            user.setRealName(request.getParameter("realName"));
            user.setRole(request.getParameter("role"));
            user.setPhone(request.getParameter("phone"));
            user.setStatus("active");

            if (userDAO.addUser(user)) {
                response.sendRedirect(request.getContextPath() + "/user?action=search");
            } else {
                response.sendRedirect(request.getContextPath() + "/admin/addUser.jsp?error=用户名已存在或添加失败");
            }
        } else if ("update".equals(action)) {
            User user = new User();
            user.setUserId(Integer.parseInt(request.getParameter("userId")));
            user.setRealName(request.getParameter("realName"));
            user.setRole(request.getParameter("role"));
            user.setPhone(request.getParameter("phone"));
            user.setStatus(request.getParameter("status"));

            if (userDAO.updateUser(user)) {
                response.sendRedirect(request.getContextPath() + "/user?action=search");
            } else {
                response.sendRedirect(request.getContextPath() + "/admin/editUser.jsp?id=" + user.getUserId() + "&error=更新失败");
            }
        } else if ("updateInfo".equals(action)) {
            User currentUser = (User) request.getSession().getAttribute("user");
            currentUser.setRealName(request.getParameter("realName"));
            currentUser.setPhone(request.getParameter("phone"));
            User updatedUser = new User();
            updatedUser.setUserId(currentUser.getUserId());
            updatedUser.setRealName(currentUser.getRealName());
            updatedUser.setPhone(currentUser.getPhone());
            updatedUser.setRole(currentUser.getRole());
            updatedUser.setStatus(currentUser.getStatus());
            if (userDAO.updateUser(updatedUser)) {
                request.getSession().setAttribute("user", currentUser);
                response.sendRedirect(request.getContextPath() + "/user/profile.jsp?msg=success");
            } else {
                response.sendRedirect(request.getContextPath() + "/user/profile.jsp?msg=error");
            }
        } else if ("updatePassword".equals(action)) {
            User currentUser = (User) request.getSession().getAttribute("user");
            String oldPassword = request.getParameter("oldPassword");
            String newPassword = request.getParameter("newPassword");
            if (userDAO.updatePassword(currentUser.getUserId(), oldPassword, newPassword)) {
                response.sendRedirect(request.getContextPath() + "/user/profile.jsp?msg=success");
            } else {
                response.sendRedirect(request.getContextPath() + "/user/profile.jsp?msg=pwd_error");
            }
        }
    }
}
