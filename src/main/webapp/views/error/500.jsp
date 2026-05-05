<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>500 - 服务器错误</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css">
</head>
<body class="d-flex align-items-center justify-content-center" style="height:100vh; background:#f5f5f5">
    <div class="text-center">
        <h1 class="display-1 text-danger">500</h1>
        <h3>服务器内部错误</h3>
        <p class="text-muted"><%= exception != null ? exception.getMessage() : "系统发生了未知错误" %></p>
        <a href="${pageContext.request.contextPath}/" class="btn btn-primary">返回首页</a>
    </div>
</body>
</html>
