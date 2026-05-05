<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
String error = (String) request.getAttribute("error");
String success = (String) request.getAttribute("success");
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>用户登录 - 超市管理系统</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css">
    <style>
        body { background-color: #f5f5f5; }
        .login-container { max-width: 400px; margin: 100px auto; }
    </style>
</head>
<body>
<div class="login-container">
    <div class="card shadow">
        <div class="card-body p-4">
            <h3 class="text-center mb-4">用户登录</h3>
            <% if (error != null) { %>
                <div class="alert alert-danger"><%= error %></div>
            <% } %>
            <% if (success != null) { %>
                <div class="alert alert-success"><%= success %></div>
            <% } %>
            <form action="${pageContext.request.contextPath}/login.do" method="post">
                <input type="hidden" name="action" value="login">
                <div class="mb-3">
                    <label class="form-label">用户名/手机号</label>
                    <input type="text" name="username" class="form-control" placeholder="请输入用户名或手机号" required>
                </div>
                <div class="mb-3">
                    <label class="form-label">密码</label>
                    <input type="password" name="password" class="form-control" placeholder="请输入密码" required>
                </div>
                <button type="submit" class="btn btn-primary w-100 mb-3">登录</button>
                <div class="text-center">
                    <a href="${pageContext.request.contextPath}/views/register.jsp">还没有账号？立即注册</a>
                </div>
            </form>
        </div>
    </div>
    <div class="text-center mt-3">
        <a href="${pageContext.request.contextPath}/views/admin/login.jsp">管理员登录</a> |
        <a href="${pageContext.request.contextPath}/views/courier/login.jsp">配送员登录</a>
    </div>
</div>
</body>
</html>
