<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.supermarket.bean.User, java.util.*" %>
<%
    User user = (User) session.getAttribute("user");
    if (user == null || !"admin".equals(user.getRole())) { response.sendRedirect("../login.jsp"); return; }
    List<Map<String, Object>> logs = (List<Map<String, Object>>) request.getAttribute("logs");
    String username = request.getAttribute("username") != null ? (String) request.getAttribute("username") : "";
    String action   = request.getAttribute("action")   != null ? (String) request.getAttribute("action")   : "";
    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
%>
<!DOCTYPE html>
<html>
<head>
    <title>操作日志</title>
    <style>
        * { margin:0; padding:0; box-sizing:border-box; }
        body { font-family:Arial; background:#f5f5f5; }
        .header { background:#2c3e50; color:white; padding:15px 30px; display:flex; justify-content:space-between; align-items:center; }
        .header a { color:white; text-decoration:none; font-size:14px; }
        .container { padding:30px; }
        .card { background:white; padding:25px; border-radius:8px; box-shadow:0 2px 5px rgba(0,0,0,0.1); }
        .search-row { display:flex; gap:10px; margin-bottom:15px; }
        .search-row input { padding:8px; border:1px solid #ddd; border-radius:4px; width:180px; }
        .search-row button { padding:8px 16px; background:#3498db; color:white; border:none; border-radius:4px; cursor:pointer; }
        table { width:100%; border-collapse:collapse; }
        th,td { padding:10px 12px; text-align:left; border-bottom:1px solid #eee; font-size:13px; }
        th { background:#34495e; color:white; }
        tr:hover { background:#f9f9f9; }
        .total { color:#999; font-size:13px; margin-bottom:10px; }
    </style>
</head>
<body>
<div class="header">
    <h1>操作日志</h1>
    <a href="index.jsp">← 返回首页</a>
</div>
<div class="container">
    <div class="card">
        <form class="search-row" action="../adminAudit" method="get">
            <input type="text" name="username" placeholder="操作人" value="<%= username %>">
            <input type="text" name="action"   placeholder="操作类型" value="<%= action %>">
            <button type="submit">搜索</button>
        </form>
        <div class="total">共 <%= logs != null ? logs.size() : 0 %> 条记录（最近200条）</div>
        <table>
            <tr><th>时间</th><th>操作人</th><th>操作</th><th>对象</th><th>详情</th><th>IP</th></tr>
            <% if (logs != null) for (Map<String,Object> r : logs) { %>
            <tr>
                <td><%= r.get("logTime") != null ? sdf.format(r.get("logTime")) : "-" %></td>
                <td><%= r.get("username") %></td>
                <td><%= r.get("action") %></td>
                <td><%= r.get("target") != null ? r.get("target") : "-" %></td>
                <td><%= r.get("detail") != null ? r.get("detail") : "-" %></td>
                <td><%= r.get("ip") != null ? r.get("ip") : "-" %></td>
            </tr>
            <% } %>
        </table>
    </div>
</div>
</body>
</html>
