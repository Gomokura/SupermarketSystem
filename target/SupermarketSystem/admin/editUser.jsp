<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.supermarket.bean.User" %>
<%@ page import="com.supermarket.dao.UserDAO" %>
<%
    int userId = Integer.parseInt(request.getParameter("id"));
    UserDAO dao = new UserDAO();
    java.util.List<User> list = dao.searchUsers(null, null, null);
    User editUser = null;
    for (User u : list) {
        if (u.getUserId() == userId) {
            editUser = u;
            break;
        }
    }
%>
<!DOCTYPE html>
<html>
<head>
    <title>编辑用户</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: Arial; background: #f5f5f5; }
        .header { background: #2c3e50; color: white; padding: 15px 30px; }
        .container { max-width: 600px; margin: 30px auto; }
        .form-box { background: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
        .form-group { margin-bottom: 20px; }
        label { display: block; margin-bottom: 5px; font-weight: bold; color: #333; }
        input, select { width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 4px; }
        .btn { padding: 12px 30px; border: none; border-radius: 4px; cursor: pointer; margin-right: 10px; }
        .btn-submit { background: #3498db; color: white; }
        .btn-cancel { background: #95a5a6; color: white; }
    </style>
</head>
<body>
    <div class="header">
        <h1>编辑用户</h1>
    </div>
    <div class="container">
        <div class="form-box">
            <form action="../user" method="post">
                <input type="hidden" name="action" value="update">
                <input type="hidden" name="userId" value="<%= editUser.getUserId() %>">

                <div class="form-group">
                    <label>用户名</label>
                    <input type="text" value="<%= editUser.getUsername() %>" disabled>
                </div>

                <div class="form-group">
                    <label>真实姓名</label>
                    <input type="text" name="realName" value="<%= editUser.getRealName() %>" required>
                </div>

                <div class="form-group">
                    <label>角色</label>
                    <select name="role" required>
                        <option value="user" <%= "user".equals(editUser.getRole())?"selected":"" %>>普通用户</option>
                        <option value="admin" <%= "admin".equals(editUser.getRole())?"selected":"" %>>管理员</option>
                        <option value="courier" <%= "courier".equals(editUser.getRole())?"selected":"" %>>配送员</option>
                    </select>
                </div>

                <div class="form-group">
                    <label>电话</label>
                    <input type="text" name="phone" value="<%= editUser.getPhone() %>">
                </div>

                <div class="form-group">
                    <label>状态</label>
                    <select name="status">
                        <option value="active" <%= "active".equals(editUser.getStatus())?"selected":"" %>>启用</option>
                        <option value="inactive" <%= "inactive".equals(editUser.getStatus())?"selected":"" %>>禁用</option>
                    </select>
                </div>

                <div>
                    <button type="submit" class="btn btn-submit">保存</button>
                    <button type="button" class="btn btn-cancel" onclick="history.back()">取消</button>
                </div>
            </form>
        </div>
    </div>
</body>
</html>
