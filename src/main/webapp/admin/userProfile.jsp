<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.supermarket.bean.User, java.util.*" %>
<%
    User user = (User) session.getAttribute("user");
    if (user == null || !"admin".equals(user.getRole())) { response.sendRedirect("../login.jsp"); return; }
    List<Map<String,Object>> profiles    = (List<Map<String,Object>>) request.getAttribute("profiles");
    List<Map<String,Object>> preferences = (List<Map<String,Object>>) request.getAttribute("preferences");
    String keyword     = request.getAttribute("keyword") != null ? (String) request.getAttribute("keyword") : "";
    Object targetUid   = request.getAttribute("targetUserId");
%>
<!DOCTYPE html>
<html>
<head>
    <title>用户画像</title>
    <style>
        * { margin:0; padding:0; box-sizing:border-box; }
        body { font-family:Arial; background:#f5f5f5; }
        .header { background:#2c3e50; color:white; padding:15px 30px; display:flex; justify-content:space-between; align-items:center; }
        .header a { color:white; text-decoration:none; font-size:14px; }
        .container { padding:30px; display:grid; grid-template-columns:1fr 300px; gap:20px; }
        .card { background:white; padding:25px; border-radius:8px; box-shadow:0 2px 5px rgba(0,0,0,0.1); margin-bottom:20px; }
        .card h2 { margin-bottom:15px; color:#2c3e50; font-size:16px; }
        .search-row { display:flex; gap:10px; margin-bottom:15px; }
        .search-row input { flex:1; padding:8px; border:1px solid #ddd; border-radius:4px; }
        .search-row button { padding:8px 16px; background:#3498db; color:white; border:none; border-radius:4px; cursor:pointer; }
        table { width:100%; border-collapse:collapse; }
        th,td { padding:10px 12px; text-align:left; border-bottom:1px solid #eee; font-size:13px; }
        th { background:#34495e; color:white; }
        tr:hover { background:#f9f9f9; }
        .btn { padding:4px 10px; border:none; border-radius:3px; cursor:pointer; color:white; font-size:12px; margin-right:3px; }
        .btn-freeze   { background:#e74c3c; }
        .btn-unfreeze { background:#27ae60; }
        .btn-points   { background:#9b59b6; }
        .btn-profile  { background:#3498db; }
        .badge-active { background:#d4edda; color:#155724; padding:2px 8px; border-radius:10px; font-size:11px; }
        .badge-frozen { background:#f8d7da; color:#721c24; padding:2px 8px; border-radius:10px; font-size:11px; }
        .pref-bar { height:8px; background:#3498db; border-radius:4px; display:inline-block; }
        .form-row { margin-bottom:12px; }
        .form-row label { display:block; font-size:13px; color:#555; margin-bottom:4px; }
        .form-row input, .form-row select { width:100%; padding:8px; border:1px solid #ddd; border-radius:4px; }
        .btn-submit { width:100%; padding:10px; background:#9b59b6; color:white; border:none; border-radius:4px; cursor:pointer; }
    </style>
</head>
<body>
<div class="header">
    <h1>用户画像</h1>
    <a href="index.jsp">← 返回首页</a>
</div>
<div class="container">
    <div>
        <!-- 用户列表 -->
        <div class="card">
            <h2>用户消费概览</h2>
            <form class="search-row" action="../adminUserProfile" method="get">
                <input type="text" name="keyword" placeholder="用户名/姓名" value="<%= keyword %>">
                <button type="submit">搜索</button>
            </form>
            <table>
                <tr><th>用户名</th><th>姓名</th><th>状态</th><th>积分</th><th>累计消费</th><th>订单数</th><th>操作</th></tr>
                <% if (profiles != null) for (Map<String,Object> p : profiles) {
                    boolean frozen = "frozen".equals(p.get("status")); %>
                <tr>
                    <td><%= p.get("username") %></td>
                    <td><%= p.get("realName") != null ? p.get("realName") : "-" %></td>
                    <td><span class="badge-<%= frozen ? "frozen" : "active" %>"><%= frozen ? "已冻结" : "正常" %></span></td>
                    <td><%= p.get("points") %></td>
                    <td>¥<%= String.format("%.2f", (Double)p.get("totalSpent")) %></td>
                    <td><%= p.get("orderCount") %></td>
                    <td>
                        <button class="btn btn-profile" onclick="location.href='../adminUserProfile?userId=<%= p.get("userId") %>'">画像</button>
                        <% if (frozen) { %>
                        <form style="display:inline" method="post" action="../adminUserProfile">
                            <input type="hidden" name="action" value="unfreeze">
                            <input type="hidden" name="targetUserId" value="<%= p.get("userId") %>">
                            <button type="submit" class="btn btn-unfreeze">解冻</button>
                        </form>
                        <% } else { %>
                        <form style="display:inline" method="post" action="../adminUserProfile" onsubmit="return confirm('确认冻结该账户？')">
                            <input type="hidden" name="action" value="freeze">
                            <input type="hidden" name="targetUserId" value="<%= p.get("userId") %>">
                            <button type="submit" class="btn btn-freeze">冻结</button>
                        </form>
                        <% } %>
                    </td>
                </tr>
                <% } %>
            </table>
        </div>

        <!-- 偏好分类 -->
        <% if (preferences != null && !preferences.isEmpty()) {
            double maxSpent = ((Double)preferences.get(0).get("spent")); %>
        <div class="card">
            <h2>用户 #<%= targetUid %> 消费偏好</h2>
            <table>
                <tr><th>分类</th><th>购买数量</th><th>消费金额</th><th>占比</th></tr>
                <% for (Map<String,Object> r : preferences) {
                    double spent = (Double)r.get("spent");
                    int pct = maxSpent > 0 ? (int)(spent / maxSpent * 100) : 0; %>
                <tr>
                    <td><%= r.get("category") %></td>
                    <td><%= r.get("qty") %></td>
                    <td>¥<%= String.format("%.2f", spent) %></td>
                    <td><span class="pref-bar" style="width:<%= pct %>px;"></span> <%= pct %>%</td>
                </tr>
                <% } %>
            </table>
        </div>
        <% } %>
    </div>

    <!-- 积分调整 -->
    <div class="card" style="height:fit-content;">
        <h2>积分调整</h2>
        <form method="post" action="../adminUserProfile">
            <input type="hidden" name="action" value="adjustPoints">
            <div class="form-row">
                <label>选择用户</label>
                <select name="targetUserId" required>
                    <option value="">请选择</option>
                    <% if (profiles != null) for (Map<String,Object> p : profiles) { %>
                    <option value="<%= p.get("userId") %>" <%= targetUid != null && targetUid.equals(p.get("userId")) ? "selected" : "" %>>
                        <%= p.get("username") %>（当前 <%= p.get("points") %> 分）
                    </option>
                    <% } %>
                </select>
            </div>
            <div class="form-row">
                <label>调整积分（负数为扣减）</label>
                <input type="number" name="delta" placeholder="如 +100 或 -50" required>
            </div>
            <button type="submit" class="btn-submit">确认调整</button>
        </form>
    </div>
</div>
</body>
</html>
