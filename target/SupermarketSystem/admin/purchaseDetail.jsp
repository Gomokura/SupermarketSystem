<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.supermarket.bean.User, java.util.*" %>
<%
    User user = (User) session.getAttribute("user");
    if (user == null || !"admin".equals(user.getRole())) { response.sendRedirect("../login.jsp"); return; }
    List<Map<String, Object>> items = (List<Map<String, Object>>) request.getAttribute("items");
    int poId = (int) request.getAttribute("poId");
    double total = 0;
    if (items != null) for (Map<String,Object> r : items) total += (Double) r.get("subtotal");
%>
<!DOCTYPE html>
<html>
<head>
    <title>采购单明细 #<%= poId %></title>
    <style>
        * { margin:0; padding:0; box-sizing:border-box; }
        body { font-family:Arial; background:#f5f5f5; }
        .header { background:#2c3e50; color:white; padding:15px 30px; display:flex; justify-content:space-between; align-items:center; }
        .header a { color:white; text-decoration:none; }
        .container { padding:30px; max-width:800px; }
        .card { background:white; padding:25px; border-radius:8px; box-shadow:0 2px 5px rgba(0,0,0,0.1); }
        table { width:100%; border-collapse:collapse; margin-top:15px; }
        th,td { padding:12px; text-align:left; border-bottom:1px solid #ddd; }
        th { background:#34495e; color:white; }
        .total { text-align:right; margin-top:15px; font-size:18px; font-weight:bold; color:#e74c3c; }
    </style>
</head>
<body>
<div class="header">
    <h1>采购单明细 #<%= poId %></h1>
    <a href="../adminPurchase">← 返回采购管理</a>
</div>
<div class="container">
    <div class="card">
        <table>
            <tr><th>商品名称</th><th>单价</th><th>数量</th><th>小计</th></tr>
            <% if (items != null) for (Map<String,Object> r : items) { %>
            <tr>
                <td><%= r.get("productName") %></td>
                <td>¥<%= String.format("%.2f", (Double) r.get("unitCost")) %></td>
                <td><%= r.get("quantity") %></td>
                <td>¥<%= String.format("%.2f", (Double) r.get("subtotal")) %></td>
            </tr>
            <% } %>
        </table>
        <div class="total">总成本：¥<%= String.format("%.2f", total) %></div>
    </div>
</div>
</body>
</html>
