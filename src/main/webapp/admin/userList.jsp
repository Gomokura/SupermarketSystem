<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.supermarket.bean.User" %>
<%@ page import="java.util.List" %>
<%
    User currentUser = (User) session.getAttribute("user");
    if (currentUser == null || !"admin".equals(currentUser.getRole())) {
        response.sendRedirect("../login.jsp");
        return;
    }
    List<User> users = (List<User>) request.getAttribute("users");
%>
<!DOCTYPE html>
<html>
<head>
    <title>用户管理</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: Arial; }
        .header { background: #2c3e50; color: white; padding: 15px 30px; }
        .container { padding: 30px; }
        .search-box { background: white; padding: 20px; border-radius: 8px; margin-bottom: 20px; box-shadow: 0 2px 5px rgba(0,0,0,0.1); }
        .search-box input, .search-box select { padding: 8px; margin-right: 10px; border: 1px solid #ddd; border-radius: 4px; }
        .search-box button { padding: 8px 20px; background: #3498db; color: white; border: none; border-radius: 4px; cursor: pointer; }
        table { width: 100%; background: white; border-collapse: collapse; box-shadow: 0 2px 5px rgba(0,0,0,0.1); }
        th, td { padding: 12px; text-align: left; border-bottom: 1px solid #ddd; }
        th { background: #34495e; color: white; }
        tr:hover { background: #f5f5f5; }
        .btn { padding: 5px 10px; margin: 0 2px; border: none; border-radius: 3px; cursor: pointer; color: white; }
        .btn-edit { background: #3498db; }
        .btn-add { background: #27ae60; padding: 10px 20px; margin-bottom: 20px; }
    </style>
</head>
<body>
    <div class="header">
        <h1>用户管理</h1>
    </div>
    <div class="container">
        <button class="btn btn-add" onclick="location.href='/SupermarketSystem/admin/addUser.jsp'">添加用户</button>

        <div class="search-box">
            <form action="../user" method="get">
                <input type="hidden" name="action" value="search">
                <input type="text" name="keyword" placeholder="用户名/姓名">
                <select name="role">
                    <option value="">全部角色</option>
                    <option value="admin">管理员</option>
                    <option value="user">普通用户</option>
                    <option value="courier">配送员</option>
                </select>
                <select name="orderBy">
                    <option value="">默认排序</option>
                    <option value="create_time DESC">注册时间</option>
                    <option value="username ASC">用户名</option>
                </select>
                <button type="submit">搜索</button>
            </form>
        </div>

        <table>
            <tr>
                <th>ID</th>
                <th>用户名</th>
                <th>姓名</th>
                <th>角色</th>
                <th>电话</th>
                <th>状态</th>
                <th>注册时间</th>
                <th>操作</th>
            </tr>
            <% if (users != null) {
                for (User u : users) { %>
            <tr>
                <td><%= u.getUserId() %></td>
                <td><%= u.getUsername() %></td>
                <td><%= u.getRealName() %></td>
                <td><%= "admin".equals(u.getRole()) ? "管理员" : "courier".equals(u.getRole()) ? "配送员" : "普通用户" %></td>
                <td><%= u.getPhone() %></td>
                <td><%= u.getStatus() %></td>
                <td><%= u.getCreateTime() %></td>
                <td>
                    <button class="btn btn-edit" onclick="location.href='/SupermarketSystem/admin/editUser.jsp?id=<%= u.getUserId() %>'">编辑</button>
                </td>
            </tr>
            <% } } %>
        </table>
    </div>
</body>
</html>
