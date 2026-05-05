<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>404 - 页面未找到</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css">
</head>
<body class="d-flex align-items-center justify-content-center" style="height:100vh; background:#f5f5f5">
    <div class="text-center">
        <h1 class="display-1 text-muted">404</h1>
        <h3>页面未找到</h3>
        <p class="text-muted">您访问的页面不存在</p>
        <a href="${pageContext.request.contextPath}/" class="btn btn-primary">返回首页</a>
    </div>
</body>
</html>
