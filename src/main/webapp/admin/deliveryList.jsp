<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.supermarket.bean.User, java.util.*" %>
<%
    User user = (User) session.getAttribute("user");
    if (user == null || !"admin".equals(user.getRole())) { response.sendRedirect("../login.jsp"); return; }
    List<Map<String, Object>> deliveries = (List<Map<String, Object>>) request.getAttribute("deliveries");
    List<User> couriers = (List<User>) request.getAttribute("couriers");
    String status = request.getAttribute("status") != null ? (String) request.getAttribute("status") : "";
    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm");
%>
<!DOCTYPE html>
<html>
<head>
    <title>配送调度</title>
    <style>
        * { margin:0; padding:0; box-sizing:border-box; }
        body { font-family:Arial; background:#f5f5f5; }
        .header { background:#2c3e50; color:white; padding:15px 30px; display:flex; justify-content:space-between; align-items:center; }
        .header a { color:white; text-decoration:none; font-size:14px; }
        .container { padding:30px; }
        .card { background:white; padding:25px; border-radius:8px; box-shadow:0 2px 5px rgba(0,0,0,0.1); }
        .card h2 { margin-bottom:15px; color:#2c3e50; }
        .filter-row { display:flex; gap:10px; margin-bottom:15px; }
        .filter-row select, .filter-row button { padding:8px 14px; border:1px solid #ddd; border-radius:4px; }
        .filter-row button { background:#3498db; color:white; border:none; cursor:pointer; }
        table { width:100%; border-collapse:collapse; }
        th,td { padding:10px 12px; text-align:left; border-bottom:1px solid #eee; font-size:13px; }
        th { background:#34495e; color:white; }
        tr:hover { background:#f9f9f9; }
        .btn { padding:4px 10px; border:none; border-radius:3px; cursor:pointer; color:white; font-size:12px; margin-right:3px; }
        .btn-dispatch   { background:#f39c12; }
        .btn-delivering { background:#3498db; }
        .btn-done       { background:#27ae60; }
        .s-pending    { background:#fff3cd; color:#856404; padding:2px 8px; border-radius:10px; font-size:12px; }
        .s-dispatched { background:#cce5ff; color:#004085; padding:2px 8px; border-radius:10px; font-size:12px; }
        .s-delivering { background:#d1ecf1; color:#0c5460; padding:2px 8px; border-radius:10px; font-size:12px; }
        .s-done       { background:#d4edda; color:#155724; padding:2px 8px; border-radius:10px; font-size:12px; }
    </style>
</head>
<body>
<div class="header">
    <h1>配送调度</h1>
    <a href="index.jsp">← 返回首页</a>
</div>
<div class="container">
    <div class="card">
        <h2>配送订单列表</h2>
        <form class="filter-row" action="../adminDelivery" method="get">
            <select name="status">
                <option value="">全部状态</option>
                <option value="pending"    <%= "pending".equals(status)    ? "selected" : "" %>>待派送</option>
                <option value="dispatched" <%= "dispatched".equals(status) ? "selected" : "" %>>已派送</option>
                <option value="delivering" <%= "delivering".equals(status) ? "selected" : "" %>>配送中</option>
                <option value="done"       <%= "done".equals(status)       ? "selected" : "" %>>已完成</option>
            </select>
            <button type="submit">筛选</button>
        </form>
        <table>
            <tr><th>配送单号</th><th>订单号</th><th>用户</th><th>金额</th><th>收件人</th><th>电话</th><th>地址</th><th>配送员</th><th>状态</th><th>派送时间</th><th>操作</th></tr>
            <% if (deliveries != null && !deliveries.isEmpty()) {
                for (Map<String,Object> d : deliveries) {
                    String s = (String) d.get("status"); %>
            <tr>
                <td><%= d.get("deliveryId") %></td>
                <td><%= d.get("orderId") %></td>
                <td><%= d.get("username") %></td>
                <td>¥<%= String.format("%.2f", (Double) d.get("totalAmount")) %></td>
                <td><%= d.get("receiver") != null ? d.get("receiver") : "-" %></td>
                <td><%= d.get("phone") != null ? d.get("phone") : "-" %></td>
                <td><%= d.get("address") != null ? d.get("address") : "-" %></td>
                <td><%= d.get("courierName") != null ? d.get("courierName") : "<span style='color:#999'>未指派</span>" %></td>
                <td><span class="s-<%= s %>">
                    <% if ("pending".equals(s))    out.print("待派送");
                       else if ("dispatched".equals(s)) out.print("已派送");
                       else if ("delivering".equals(s)) out.print("配送中");
                       else out.print("已完成"); %>
                </span></td>
                <td><%= d.get("dispatchTime") != null ? sdf.format(d.get("dispatchTime")) : "-" %></td>
                <td>
                    <% if ("pending".equals(s)) { %>
                    <form style="display:inline;display:flex;gap:4px;align-items:center" method="post" action="../adminDelivery">
                        <input type="hidden" name="deliveryId" value="<%= d.get("deliveryId") %>">
                        <input type="hidden" name="action" value="assign">
                        <select name="courierId" required style="padding:3px;font-size:12px;border-radius:3px;border:1px solid #ddd">
                            <option value="">选择配送员</option>
                            <% if (couriers != null) for (User c : couriers) { %>
                            <option value="<%= c.getUserId() %>"><%= c.getUsername() %></option>
                            <% } %>
                        </select>
                        <button type="submit" class="btn btn-dispatch">指派</button>
                    </form>
                    <% } else if ("delivering".equals(s)) { %>
                    <form style="display:inline" method="post" action="../adminDelivery">
                        <input type="hidden" name="deliveryId" value="<%= d.get("deliveryId") %>">
                        <input type="hidden" name="newStatus"  value="done">
                        <button type="submit" class="btn btn-done">完成</button>
                    </form>
                    <% } %>
                </td>
            </tr>
            <% } } else { %>
            <tr><td colspan="11" style="text-align:center;color:#999;padding:30px;">暂无配送数据</td></tr>
            <% } %>
        </table>
    </div>
</div>
</body>
</html>
