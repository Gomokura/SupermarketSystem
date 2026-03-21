<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.supermarket.bean.User, java.util.*" %>
<%
    User user = (User) session.getAttribute("user");
    if (user == null || !"admin".equals(user.getRole())) { response.sendRedirect("../login.jsp"); return; }
    List<Map<String,Object>> negativeStock = (List<Map<String,Object>>) request.getAttribute("negativeStock");
    List<Map<String,Object>> criticalStock = (List<Map<String,Object>>) request.getAttribute("criticalStock");
    List<Map<String,Object>> largeOrders  = (List<Map<String,Object>>) request.getAttribute("largeOrders");
    List<Map<String,Object>> staleOrders  = (List<Map<String,Object>>) request.getAttribute("staleOrders");
    int totalAlerts = (negativeStock!=null?negativeStock.size():0) + (criticalStock!=null?criticalStock.size():0)
                    + (largeOrders!=null?largeOrders.size():0) + (staleOrders!=null?staleOrders.size():0);
    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm");
%>
<!DOCTYPE html>
<html>
<head>
    <title>异常预警中心</title>
    <style>
        * { margin:0; padding:0; box-sizing:border-box; }
        body { font-family:Arial; background:#f5f5f5; }
        .header { background:#2c3e50; color:white; padding:15px 30px; display:flex; justify-content:space-between; align-items:center; }
        .header a { color:white; text-decoration:none; font-size:14px; }
        .container { padding:30px; }
        .summary { background:white; padding:20px 25px; border-radius:8px; box-shadow:0 2px 5px rgba(0,0,0,0.1); margin-bottom:20px; display:flex; align-items:center; gap:15px; }
        .summary .count { font-size:36px; font-weight:bold; color:<%= totalAlerts > 0 ? "#e74c3c" : "#27ae60" %>; }
        .summary p { color:#666; font-size:14px; }
        .grid { display:grid; grid-template-columns:1fr 1fr; gap:20px; }
        .card { background:white; padding:20px; border-radius:8px; box-shadow:0 2px 5px rgba(0,0,0,0.1); }
        .card-header { display:flex; align-items:center; gap:10px; margin-bottom:15px; }
        .card-header h2 { font-size:15px; color:#2c3e50; }
        .badge { padding:2px 10px; border-radius:10px; font-size:12px; font-weight:bold; }
        .badge-red    { background:#f8d7da; color:#721c24; }
        .badge-orange { background:#fff3cd; color:#856404; }
        .badge-blue   { background:#cce5ff; color:#004085; }
        table { width:100%; border-collapse:collapse; }
        th,td { padding:9px 10px; text-align:left; border-bottom:1px solid #eee; font-size:13px; }
        th { background:#f8f9fa; color:#555; font-weight:600; }
        .empty { text-align:center; color:#27ae60; padding:20px; font-size:13px; }
    </style>
</head>
<body>
<div class="header">
    <h1>异常预警中心</h1>
    <a href="index.jsp">← 返回首页</a>
</div>
<div class="container">
    <div class="summary">
        <div class="count"><%= totalAlerts %></div>
        <div>
            <p style="font-size:16px;font-weight:bold;color:#2c3e50;"><%= totalAlerts > 0 ? "当前存在异常，请及时处理" : "系统运行正常" %></p>
            <p>库存异常 <%= negativeStock!=null?negativeStock.size():0 %> 项 &nbsp;|&nbsp; 极低库存 <%= criticalStock!=null?criticalStock.size():0 %> 项 &nbsp;|&nbsp; 大额订单 <%= largeOrders!=null?largeOrders.size():0 %> 项 &nbsp;|&nbsp; 滞留订单 <%= staleOrders!=null?staleOrders.size():0 %> 项</p>
        </div>
    </div>

    <div class="grid">
        <!-- 库存为负 -->
        <div class="card">
            <div class="card-header"><h2>库存为负</h2><span class="badge badge-red"><%= negativeStock!=null?negativeStock.size():0 %></span></div>
            <% if (negativeStock != null && !negativeStock.isEmpty()) { %>
            <table><tr><th>商品</th><th>库存</th></tr>
            <% for (Map<String,Object> r : negativeStock) { %><tr><td><%= r.get("productName") %></td><td style="color:#e74c3c;font-weight:bold;"><%= r.get("stock") %></td></tr><% } %>
            </table>
            <% } else { %><div class="empty">✓ 无异常</div><% } %>
        </div>

        <!-- 极低库存 -->
        <div class="card">
            <div class="card-header"><h2>极低库存（≤5）</h2><span class="badge badge-orange"><%= criticalStock!=null?criticalStock.size():0 %></span></div>
            <% if (criticalStock != null && !criticalStock.isEmpty()) { %>
            <table><tr><th>商品</th><th>库存</th><th>单位</th></tr>
            <% for (Map<String,Object> r : criticalStock) { %><tr><td><%= r.get("productName") %></td><td style="color:#f39c12;font-weight:bold;"><%= r.get("stock") %></td><td><%= r.get("unit") %></td></tr><% } %>
            </table>
            <% } else { %><div class="empty">✓ 无异常</div><% } %>
        </div>

        <!-- 大额订单 -->
        <div class="card">
            <div class="card-header"><h2>异常大额订单（超均值3倍）</h2><span class="badge badge-red"><%= largeOrders!=null?largeOrders.size():0 %></span></div>
            <% if (largeOrders != null && !largeOrders.isEmpty()) { %>
            <table><tr><th>订单号</th><th>用户</th><th>金额</th><th>时间</th></tr>
            <% for (Map<String,Object> r : largeOrders) { %><tr><td><%= r.get("orderId") %></td><td><%= r.get("username") %></td><td style="color:#e74c3c;font-weight:bold;">¥<%= String.format("%.2f",(Double)r.get("totalAmount")) %></td><td><%= r.get("orderTime")!=null?sdf.format(r.get("orderTime")):"-" %></td></tr><% } %>
            </table>
            <% } else { %><div class="empty">✓ 无异常</div><% } %>
        </div>

        <!-- 滞留订单 -->
        <div class="card">
            <div class="card-header"><h2>滞留订单（pending超3天）</h2><span class="badge badge-blue"><%= staleOrders!=null?staleOrders.size():0 %></span></div>
            <% if (staleOrders != null && !staleOrders.isEmpty()) { %>
            <table><tr><th>订单号</th><th>用户</th><th>金额</th><th>下单时间</th></tr>
            <% for (Map<String,Object> r : staleOrders) { %><tr><td><%= r.get("orderId") %></td><td><%= r.get("username") %></td><td>¥<%= String.format("%.2f",(Double)r.get("totalAmount")) %></td><td><%= r.get("orderTime")!=null?sdf.format(r.get("orderTime")):"-" %></td></tr><% } %>
            </table>
            <% } else { %><div class="empty">✓ 无异常</div><% } %>
        </div>
    </div>
</div>
</body>
</html>
