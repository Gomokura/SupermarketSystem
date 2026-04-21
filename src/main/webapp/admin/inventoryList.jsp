<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.supermarket.bean.User, com.supermarket.dao.ProductDAO" %>
<%@ page import="com.supermarket.bean.Product, java.util.*" %>
<%
    User user = (User) session.getAttribute("user");
    if (user == null || !"admin".equals(user.getRole())) { response.sendRedirect("../login.jsp"); return; }
    List<Map<String, Object>> logs     = (List<Map<String, Object>>) request.getAttribute("logs");
    List<Map<String, Object>> lowStock = (List<Map<String, Object>>) request.getAttribute("lowStock");
    String keyword = request.getAttribute("keyword") != null ? (String) request.getAttribute("keyword") : "";
    List<Product> products = new ProductDAO().searchProducts(null, null, null);
    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm");
%>
<!DOCTYPE html>
<html>
<head>
    <title>库存管理</title>
    <style>
        * { margin:0; padding:0; box-sizing:border-box; }
        body { font-family:Arial; background:#f5f5f5; }
        .header { background:#2c3e50; color:white; padding:15px 30px; display:flex; justify-content:space-between; align-items:center; }
        .header a { color:white; text-decoration:none; font-size:14px; }
        .container { padding:30px; display:grid; grid-template-columns:1fr 320px; gap:20px; }
        .card { background:white; padding:25px; border-radius:8px; box-shadow:0 2px 5px rgba(0,0,0,0.1); }
        .card h2 { margin-bottom:15px; color:#2c3e50; }
        table { width:100%; border-collapse:collapse; }
        th,td { padding:10px 12px; text-align:left; border-bottom:1px solid #eee; font-size:13px; }
        th { background:#34495e; color:white; }
        tr:hover { background:#f9f9f9; }
        .form-row { margin-bottom:12px; }
        .form-row label { display:block; font-size:13px; color:#555; margin-bottom:4px; }
        .form-row input, .form-row select, .form-row textarea { width:100%; padding:8px; border:1px solid #ddd; border-radius:4px; font-size:13px; }
        .btn-submit { width:100%; padding:10px; background:#27ae60; color:white; border:none; border-radius:4px; cursor:pointer; font-size:14px; }
        .badge-in  { color:#27ae60; font-weight:bold; }
        .badge-out { color:#e74c3c; font-weight:bold; }
        .warn { color:#e74c3c; font-weight:bold; }
        .search-row { display:flex; gap:10px; margin-bottom:15px; }
        .search-row input { flex:1; padding:8px; border:1px solid #ddd; border-radius:4px; }
        .search-row button { padding:8px 16px; background:#3498db; color:white; border:none; border-radius:4px; cursor:pointer; }
    </style>
</head>
<body>
<div class="header">
    <h1>库存管理</h1>
    <a href="index.jsp">← 返回首页</a>
</div>
<div class="container">
    <div>
        <!-- 低库存预警 -->
        <% if (lowStock != null && !lowStock.isEmpty()) { %>
        <div class="card" style="margin-bottom:20px; border-left:4px solid #e74c3c;">
            <h2 style="color:#e74c3c;">⚠ 低库存预警（≤10）</h2>
            <table>
                <tr><th>商品</th><th>库存</th><th>单位</th></tr>
                <% for (Map<String,Object> r : lowStock) { %>
                <tr>
                    <td><%= r.get("productName") %></td>
                    <td class="warn"><%= r.get("stock") %></td>
                    <td><%= r.get("unit") %></td>
                </tr>
                <% } %>
            </table>
        </div>
        <% } %>

        <!-- 日志列表 -->
        <div class="card">
            <h2>库存变动记录</h2>
            <form class="search-row" action="../adminInventory" method="get">
                <input type="text" name="keyword" placeholder="商品名称" value="<%= keyword %>">
                <button type="submit">搜索</button>
            </form>
            <table>
                <tr><th>时间</th><th>商品</th><th>类型</th><th>数量</th><th>操作人</th><th>备注</th></tr>
                <% if (logs != null) for (Map<String,Object> r : logs) { %>
                <tr>
                    <td><%= r.get("logTime") != null ? sdf.format(r.get("logTime")) : "-" %></td>
                    <td><%= r.get("productName") %></td>
                    <td><span class="badge-<%= r.get("changeType") %>"><%= "in".equals(r.get("changeType")) ? "入库" : "出库" %></span></td>
                    <td><%= r.get("quantity") %></td>
                    <td><%= r.get("operator") != null ? r.get("operator") : "-" %></td>
                    <td><%= r.get("remark") != null ? r.get("remark") : "" %></td>
                </tr>
                <% } %>
            </table>
        </div>
    </div>

    <!-- 调整库存表单 -->
    <div class="card" style="height:fit-content;">
        <h2>调整库存</h2>
        <form action="../adminInventory" method="post">
            <div class="form-row">
                <label>商品</label>
                <select name="productId" required>
                    <option value="">请选择商品</option>
                    <% for (Product p : products) { %>
                    <option value="<%= p.getProductId() %>"><%= p.getProductName() %>（库存:<%= p.getStock() %>）</option>
                    <% } %>
                </select>
            </div>
            <div class="form-row">
                <label>操作类型</label>
                <select name="changeType" required>
                    <option value="in">入库</option>
                    <option value="out">出库</option>
                </select>
            </div>
            <div class="form-row">
                <label>数量</label>
                <input type="number" name="quantity" min="1" required>
            </div>
            <div class="form-row">
                <label>备注</label>
                <textarea name="remark" rows="3"></textarea>
            </div>
            <button type="submit" class="btn-submit">确认调整</button>
        </form>
    </div>
</div>
</body>
</html>
