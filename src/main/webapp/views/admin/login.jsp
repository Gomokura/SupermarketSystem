<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>管理员登录 - 超市管理系统</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css">
    <style>
        body { background-color: #1a1a2e; }
        .login-box { max-width: 400px; margin: 120px auto; }
    </style>
</head>
<body>
<div class="login-box">
    <div class="card shadow">
        <div class="card-body p-4">
            <h3 class="text-center mb-4">管理员登录</h3>
            <% if (request.getAttribute("error") != null) { %>
                <div class="alert alert-danger">${error}</div>
            <% } %>
            <form action="${pageContext.request.contextPath}/adminLogin.do" method="post">
                <input type="hidden" name="action" value="adminLogin">
                <div class="mb-3">
                    <label class="form-label">用户名</label>
                    <input type="text" name="username" class="form-control" placeholder="请输入管理员用户名" required>
                </div>
                <div class="mb-3">
                    <label class="form-label">密码</label>
                    <input type="password" name="password" class="form-control" placeholder="请输入密码" required>
                </div>
                <button type="submit" class="btn btn-primary w-100 mb-3">登录</button>
                <div class="text-center">
                    <a href="${pageContext.request.contextPath}/views/login.jsp">返回用户登录</a>
                </div>
            </form>
        </div>
    </div>
</div>
</body>
</html>
