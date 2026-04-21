<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.supermarket.bean.User, com.supermarket.bean.Product, java.util.*" %>
<%
    User user = (User) session.getAttribute("user");
    if (user == null || !"admin".equals(user.getRole())) { response.sendRedirect("../login.jsp"); return; }
    List<Map<String,Object>> recent    = (List<Map<String,Object>>) request.getAttribute("recent");
    List<Map<String,Object>> history   = (List<Map<String,Object>>) request.getAttribute("history");
    List<Product> products = (List<Product>) request.getAttribute("products");
    String pidStr = request.getParameter("productId");
    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm");
%>
<!DOCTYPE html>
<html>
<head>
    <title>价格管理</title>
    <style>
        * { margin:0; padding:0; box-sizing:border-box; }
        body { font-family:Arial; background:#f5f5f5; }
        .header { background:#2c3e50; color:white; padding:15px 30px; display:flex; justify-content:space-between; align-items:center; }
        .header a { color:white; text-decoration:none; font-size:14px; }
        .container { padding:30px; display:grid; grid-template-columns:1fr 320px; gap:20px; }
        .card { background:white; padding:25px; border-radius:8px; box-shadow:0 2px 5px rgba(0,0,0,0.1); margin-bottom:20px; }
        .card h2 { margin-bottom:15px; color:#2c3e50; }
        table { width:100%; border-collapse:collapse; }
        th,td { padding:10px 12px; text-align:left; border-bottom:1px solid #eee; font-size:13px; }
        th { background:#34495e; color:white; }
        tr:hover { background:#f9f9f9; }
        .up   { color:#27ae60; font-weight:bold; }
        .down { color:#e74c3c; font-weight:bold; }
        .form-row { margin-bottom:12px; }
        .form-row label { display:block; font-size:13px; color:#555; margin-bottom:4px; }
        .form-row input, .form-row select { width:100%; padding:8px; border:1px solid #ddd; border-radius:4px; }
        .btn-submit { width:100%; padding:10px; background:#e67e22; color:white; border:none; border-radius:4px; cursor:pointer; }
        .btn-view { padding:4px 10px; background:#3498db; color:white; border:none; border-radius:3px; cursor:pointer; font-size:12px; }
    </style>
</head>
<body>
<div class="header">
    <h1>价格管理</h1>
    <a href="index.jsp">← 返回首页</a>
</div>
<div class="container">
    <div>
        <% if (history != null && !history.isEmpty()) { %>
        <div class="card">
            <h2><%= history.get(0).get("productName") %> — 价格历史</h2>
            <a href="../adminPrice" style="font-size:13px;color:#3498db;">← 返回全部记录</a>
            <table style="margin-top:10px;">
                <tr><th>时间</th><th>原价</th><th>新价</th><th>变动</th><th>操作人</th><th>备注</th></tr>
                <% for (Map<String,Object> r : history) {
                    double old = (Double)r.get("oldPrice"), nw = (Double)r.get("newPrice");
                    boolean up = nw >= old; %>
                <tr>
                    <td><%= r.get("changeTime") != null ? sdf.format(r.get("changeTime")) : "-" %></td>
                    <td>¥<%= String.format("%.2f", old) %></td>
                    <td>¥<%= String.format("%.2f", nw) %></td>
                    <td class="<%= up ? "up" : "down" %>"><%= up ? "▲" : "▼" %> ¥<%= String.format("%.2f", Math.abs(nw-old)) %></td>
                    <td><%= r.get("operator") != null ? r.get("operator") : "-" %></td>
                    <td><%= r.get("remark") != null ? r.get("remark") : "" %></td>
                </tr>
                <% } %>
            </table>
        </div>
        <% } else { %>
        <div class="card">
            <h2>最近调价记录</h2>
            <table>
                <tr><th>商品</th><th>原价</th><th>新价</th><th>变动</th><th>操作人</th><th>时间</th><th>操作</th></tr>
                <% if (recent != null) for (Map<String,Object> r : recent) {
                    double old = (Double)r.get("oldPrice"), nw = (Double)r.get("newPrice");
                    boolean up = nw >= old; %>
                <tr>
                    <td><%= r.get("productName") %></td>
                    <td>¥<%= String.format("%.2f", old) %></td>
                    <td>¥<%= String.format("%.2f", nw) %></td>
                    <td class="<%= up ? "up" : "down" %>"><%= up ? "▲" : "▼" %> ¥<%= String.format("%.2f", Math.abs(nw-old)) %></td>
                    <td><%= r.get("operator") != null ? r.get("operator") : "-" %></td>
                    <td><%= r.get("changeTime") != null ? sdf.format(r.get("changeTime")) : "-" %></td>
                    <td><button class="btn-view" onclick="location.href='../adminPrice?productId=<%= r.get("productId") %>'">历史</button></td>
                </tr>
                <% } %>
                <% if (recent == null || recent.isEmpty()) { %><tr><td colspan="7" style="text-align:center;color:#999;padding:20px;">暂无调价记录</td></tr><% } %>
            </table>
        </div>
        <% } %>
    </div>

    <div class="card" style="height:fit-content;">
        <h2>调整价格</h2>
        <form method="post" action="../adminPrice">
            <div class="form-row">
                <label>选择商品</label>
                <select name="productId" required>
                    <option value="">请选择</option>
                    <% if (products != null) for (Product p : products) { %>
                    <option value="<%= p.getProductId() %>"><%= p.getProductName() %>（当前¥<%= p.getPrice() %>）</option>
                    <% } %>
                </select>
            </div>
            <div class="form-row">
                <label>新价格</label>
                <input type="number" name="newPrice" step="0.01" min="0.01" required>
            </div>
            <div class="form-row">
                <label>调价原因</label>
                <input type="text" name="remark" placeholder="如：成本上涨、促销调整">
            </div>
            <button type="submit" class="btn-submit">确认调价</button>
        </form>
    </div>
</div>
</body>
</html>
