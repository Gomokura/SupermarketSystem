<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>403 - 权限不足</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css">
</head>
<body class="d-flex align-items-center justify-content-center" style="height:100vh; background:#f5f5f5">
    <div class="text-center">
        <h1 class="display-1 text-warning">403</h1>
        <h3>权限不足</h3>
        <p class="text-muted">${not empty errorMsg ? errorMsg : '您没有权限访问该页面'}</p>
        <a href="${pageContext.request.contextPath}/" class="btn btn-primary">返回首页</a>
    </div>
</body>
</html>
