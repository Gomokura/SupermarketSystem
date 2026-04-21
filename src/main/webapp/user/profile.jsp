<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.supermarket.bean.User" %>
<%@ page import="com.supermarket.dao.OrderDAO" %>
<%@ page import="java.util.List" %>
<%
    User user = (User) session.getAttribute("user");
    if (user == null) {
        response.sendRedirect("../login.jsp");
        return;
    }
    String msg = request.getParameter("msg");

    OrderDAO orderDAO = new OrderDAO();
    int orderCount = orderDAO.getOrdersByUser(user.getUserId()).size();
    int points = orderCount * 10;
    String level = "普通会员";
    if (points >= 600) level = "钻石会员";
    else if (points >= 300) level = "金卡会员";
    else if (points >= 100) level = "银卡会员";
%>
<!DOCTYPE html>
<html>
<head>
    <title>个人信息</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: Arial; background: #f5f5f5; }
        .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 15px 30px; }
        .container { max-width: 800px; margin: 30px auto; padding: 0 20px; }
        .card { background: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); margin-bottom: 20px; }
        .form-group { margin-bottom: 15px; }
        label { display: block; margin-bottom: 5px; font-weight: bold; color: #555; }
        input { width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 5px; }
        .btn { padding: 10px 20px; background: #667eea; color: white; border: none; border-radius: 5px; cursor: pointer; }
        .btn:hover { background: #5568d3; }
        .success { color: #27ae60; padding: 10px; background: #d5f4e6; border-radius: 5px; margin-bottom: 15px; }
        .error { color: #e74c3c; padding: 10px; background: #fadbd8; border-radius: 5px; margin-bottom: 15px; }
        .stats { display: flex; justify-content: space-around; text-align: center; }
        .stat-item { flex: 1; }
        .stat-value { font-size: 32px; font-weight: bold; color: #667eea; }
        .stat-label { color: #999; margin-top: 5px; }
    </style>
</head>
<body>
    <div class="header">
        <h1>个人中心</h1>
    </div>
    <div class="container">
        <% if ("success".equals(msg)) { %>
        <div class="success">修改成功！</div>
        <% } else if ("error".equals(msg)) { %>
        <div class="error">修改失败，请重试</div>
        <% } else if ("pwd_error".equals(msg)) { %>
        <div class="error">原密码错误</div>
        <% } %>

        <div class="card">
            <h2>会员信息</h2>
            <div class="stats">
                <div class="stat-item">
                    <div class="stat-value"><%= points %></div>
                    <div class="stat-label">积分</div>
                </div>
                <div class="stat-item">
                    <div class="stat-value"><%= level %></div>
                    <div class="stat-label">会员等级</div>
                </div>
                <div class="stat-item">
                    <div class="stat-value"><%= orderCount %></div>
                    <div class="stat-label">订单数</div>
                </div>
            </div>
        </div>

        <div class="card">
            <h2>修改个人信息</h2>
            <form action="../user" method="post">
                <input type="hidden" name="action" value="updateInfo">
                <div class="form-group">
                    <label>用户名</label>
                    <input type="text" value="<%= user.getUsername() %>" disabled>
                </div>
                <div class="form-group">
                    <label>姓名</label>
                    <input type="text" name="realName" value="<%= user.getRealName() %>" required>
                </div>
                <div class="form-group">
                    <label>电话</label>
                    <input type="text" name="phone" value="<%= user.getPhone() != null ? user.getPhone() : "" %>">
                </div>
                <button type="submit" class="btn">保存信息</button>
            </form>
        </div>

        <div class="card">
            <h2>修改密码</h2>
            <form action="../user" method="post">
                <input type="hidden" name="action" value="updatePassword">
                <div class="form-group">
                    <label>原密码</label>
                    <input type="password" name="oldPassword" required>
                </div>
                <div class="form-group">
                    <label>新密码</label>
                    <input type="password" name="newPassword" required>
                </div>
                <div class="form-group">
                    <label>确认新密码</label>
                    <input type="password" name="confirmPassword" required>
                </div>
                <button type="submit" class="btn">修改密码</button>
            </form>
        </div>

        <button class="btn" onclick="location.href='index.jsp'">返回主页</button>
    </div>
</body>
</html>
