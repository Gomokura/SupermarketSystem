<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
String error = (String) request.getAttribute("error");
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>用户注册 - 超市管理系统</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css">
    <style>
        body { background-color: #f5f5f5; }
        .register-container { max-width: 500px; margin: 60px auto; }
    </style>
</head>
<body>
<div class="register-container">
    <div class="card shadow">
        <div class="card-body p-4">
            <h3 class="text-center mb-4">用户注册</h3>
            <% if (error != null) { %>
                <div class="alert alert-danger"><%= error %></div>
            <% } %>
            <form action="${pageContext.request.contextPath}/register.do" method="post">
                <input type="hidden" name="action" value="register">
                <div class="row">
                    <div class="col-md-6 mb-3">
                        <label class="form-label">用户名 <span class="text-danger">*</span></label>
                        <input type="text" name="username" class="form-control" placeholder="请输入用户名" required>
                    </div>
                    <div class="col-md-6 mb-3">
                        <label class="form-label">真实姓名</label>
                        <input type="text" name="realName" class="form-control" placeholder="请输入真实姓名">
                    </div>
                </div>
                <div class="mb-3">
                    <label class="form-label">密码 <span class="text-danger">*</span></label>
                    <input type="password" name="password" class="form-control" placeholder="请输入密码（至少6位）" minlength="6" required>
                </div>
                <div class="row">
                    <div class="col-md-6 mb-3">
                        <label class="form-label">手机号</label>
                        <input type="text" name="phone" class="form-control" placeholder="请输入手机号">
                    </div>
                    <div class="col-md-6 mb-3">
                        <label class="form-label">昵称</label>
                        <input type="text" name="nickname" class="form-control" placeholder="请输入昵称">
                    </div>
                </div>
                <div class="mb-3">
                    <label class="form-label">邮箱</label>
                    <input type="email" name="email" class="form-control" placeholder="请输入邮箱">
                </div>
                <button type="submit" class="btn btn-primary w-100 mb-3">注册</button>
                <div class="text-center">
                    已有账号？<a href="${pageContext.request.contextPath}/views/login.jsp">立即登录</a>
                </div>
            </form>
        </div>
    </div>
</div>
</body>
</html>
