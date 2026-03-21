<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.supermarket.bean.User" %>
<%@ page import="java.util.List, java.util.Map" %>
<%
    User user = (User) session.getAttribute("user");
    if (user == null || !"admin".equals(user.getRole())) {
        response.sendRedirect("../login.jsp");
        return;
    }
    int    totalProducts = (Integer) request.getAttribute("totalProducts");
    int    totalUsers    = (Integer) request.getAttribute("totalUsers");
    int    totalOrders   = (Integer) request.getAttribute("totalOrders");
    double totalRevenue  = (Double)  request.getAttribute("totalRevenue");
    List<Map<String, Object>> topProducts = (List<Map<String, Object>>) request.getAttribute("topProducts");
%>
<!DOCTYPE html>
<html>
<head>
    <title>统计分析</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: Arial; background: #f5f5f5; }
        .header { background: #2c3e50; color: white; padding: 15px 30px; display: flex; justify-content: space-between; align-items: center; }
        .header a { color: white; text-decoration: none; font-size: 14px; }
        .container { padding: 30px; }
        .card { background: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); margin-bottom: 20px; }
        .stats-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px; margin-bottom: 20px; }
        .stat-item { padding: 20px; border-radius: 8px; text-align: center; color: white; }
        .stat-item h3 { font-size: 13px; opacity: 0.9; margin-bottom: 10px; }
        .stat-item .number { font-size: 32px; font-weight: bold; }
        .c1 { background: linear-gradient(135deg, #667eea, #764ba2); }
        .c2 { background: linear-gradient(135deg, #f093fb, #f5576c); }
        .c3 { background: linear-gradient(135deg, #4facfe, #00f2fe); }
        .c4 { background: linear-gradient(135deg, #43e97b, #38f9d7); }
        table { width: 100%; border-collapse: collapse; }
        th, td { padding: 12px; text-align: left; border-bottom: 1px solid #ddd; }
        th { background: #34495e; color: white; }
        tr:hover { background: #f9f9f9; }
        .rank { display: inline-block; width: 24px; height: 24px; border-radius: 50%; background: #3498db; color: white; text-align: center; line-height: 24px; font-size: 12px; }
        .rank-1 { background: #f39c12; }
        .rank-2 { background: #95a5a6; }
        .rank-3 { background: #cd7f32; }
    </style>
</head>
<body>
    <div class="header">
        <h1>统计分析</h1>
        <a href="index.jsp">← 返回首页</a>
    </div>
    <div class="container">
        <div class="card">
            <h2 style="margin-bottom:20px;">系统概览</h2>
            <div class="stats-grid">
                <div class="stat-item c1">
                    <h3>商品总数</h3>
                    <div class="number"><%= totalProducts %></div>
                </div>
                <div class="stat-item c2">
                    <h3>用户总数</h3>
                    <div class="number"><%= totalUsers %></div>
                </div>
                <div class="stat-item c3">
                    <h3>订单总数</h3>
                    <div class="number"><%= totalOrders %></div>
                </div>
                <div class="stat-item c4">
                    <h3>总销售额</h3>
                    <div class="number">¥<%= String.format("%.0f", totalRevenue) %></div>
                </div>
            </div>
        </div>

        <div class="card">
            <h2 style="margin-bottom:15px;">热销商品 TOP 5</h2>
            <table>
                <tr>
                    <th>排名</th>
                    <th>商品名称</th>
                    <th>销售数量</th>
                    <th>销售额</th>
                </tr>
                <% if (topProducts != null) {
                    int rank = 1;
                    for (Map<String, Object> row : topProducts) { %>
                <tr>
                    <td><span class="rank rank-<%= rank %>"><%= rank %></span></td>
                    <td><%= row.get("productName") %></td>
                    <td><%= row.get("totalQty") %></td>
                    <td>¥<%= String.format("%.2f", (Double) row.get("totalRevenue")) %></td>
                </tr>
                <% rank++; } } %>
            </table>
        </div>
    </div>
</body>
</html>
