<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.supermarket.bean.User, java.util.*" %>
<%
    User user = (User) session.getAttribute("user");
    if (user == null || !"admin".equals(user.getRole())) { response.sendRedirect("../login.jsp"); return; }
    List<Map<String, Object>> activities = (List<Map<String, Object>>) request.getAttribute("activities");
    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
%>
<!DOCTYPE html>
<html>
<head>
    <title>活动运营</title>
    <style>
        * { margin:0; padding:0; box-sizing:border-box; }
        body { font-family:Arial; background:#f5f5f5; }
        .header { background:#2c3e50; color:white; padding:15px 30px; display:flex; justify-content:space-between; align-items:center; }
        .header a { color:white; text-decoration:none; font-size:14px; }
        .container { padding:30px; display:grid; grid-template-columns:1fr 340px; gap:20px; }
        .card { background:white; padding:25px; border-radius:8px; box-shadow:0 2px 5px rgba(0,0,0,0.1); }
        .card h2 { margin-bottom:15px; color:#2c3e50; }
        .activity-grid { display:grid; grid-template-columns:repeat(auto-fill, minmax(280px, 1fr)); gap:15px; }
        .activity-card { border:1px solid #eee; border-radius:8px; overflow:hidden; }
        .activity-banner { height:120px; background:linear-gradient(135deg,#667eea,#764ba2); display:flex; align-items:center; justify-content:center; color:white; font-size:18px; font-weight:bold; }
        .activity-body { padding:15px; }
        .activity-body h3 { margin-bottom:8px; font-size:15px; }
        .activity-body p { font-size:12px; color:#666; margin-bottom:8px; }
        .activity-footer { display:flex; gap:8px; }
        .btn { padding:4px 10px; border:none; border-radius:3px; cursor:pointer; color:white; font-size:12px; }
        .btn-off { background:#95a5a6; }
        .btn-on  { background:#27ae60; }
        .btn-del { background:#e74c3c; }
        .badge-active   { background:#d4edda; color:#155724; padding:2px 8px; border-radius:10px; font-size:11px; }
        .badge-inactive { background:#f8d7da; color:#721c24; padding:2px 8px; border-radius:10px; font-size:11px; }
        .form-row { margin-bottom:12px; }
        .form-row label { display:block; font-size:13px; color:#555; margin-bottom:4px; }
        .form-row input, .form-row textarea { width:100%; padding:8px; border:1px solid #ddd; border-radius:4px; font-size:13px; }
        .btn-submit { width:100%; padding:10px; background:#9b59b6; color:white; border:none; border-radius:4px; cursor:pointer; }
    </style>
</head>
<body>
<div class="header">
    <h1>活动运营</h1>
    <a href="index.jsp">← 返回首页</a>
</div>
<div class="container">
    <div class="card">
        <h2>活动列表</h2>
        <div class="activity-grid">
            <% if (activities != null && !activities.isEmpty()) {
                for (Map<String,Object> a : activities) {
                    boolean active = "active".equals(a.get("status")); %>
            <div class="activity-card">
                <div class="activity-banner"><%= a.get("title") %></div>
                <div class="activity-body">
                    <h3><%= a.get("title") %> <span class="badge-<%= active ? "active" : "inactive" %>"><%= active ? "进行中" : "已停用" %></span></h3>
                    <p><%= a.get("description") != null ? a.get("description") : "" %></p>
                    <p>📅 <%= sdf.format(a.get("startTime")) %> ~ <%= sdf.format(a.get("endTime")) %></p>
                    <div class="activity-footer">
                        <form style="display:inline" method="post" action="../adminActivity">
                            <input type="hidden" name="action" value="updateStatus">
                            <input type="hidden" name="activityId" value="<%= a.get("activityId") %>">
                            <input type="hidden" name="status" value="<%= active ? "inactive" : "active" %>">
                            <button type="submit" class="btn <%= active ? "btn-off" : "btn-on" %>"><%= active ? "停用" : "启用" %></button>
                        </form>
                        <form style="display:inline" method="post" action="../adminActivity" onsubmit="return confirm('确认删除？')">
                            <input type="hidden" name="action" value="delete">
                            <input type="hidden" name="activityId" value="<%= a.get("activityId") %>">
                            <button type="submit" class="btn btn-del">删除</button>
                        </form>
                    </div>
                </div>
            </div>
            <% } } else { %>
            <p style="color:#999;">暂无活动，请在右侧创建</p>
            <% } %>
        </div>
    </div>

    <div class="card" style="height:fit-content;">
        <h2>新建活动</h2>
        <form method="post" action="../adminActivity">
            <input type="hidden" name="action" value="add">
            <div class="form-row"><label>活动标题</label><input type="text" name="title" required placeholder="如：双十一大促"></div>
            <div class="form-row"><label>活动描述</label><textarea name="description" rows="3" placeholder="活动详情说明"></textarea></div>
            <div class="form-row"><label>Banner图片URL（可选）</label><input type="text" name="bannerUrl" placeholder="http://..."></div>
            <div class="form-row"><label>开始日期</label><input type="date" name="startTime" required></div>
            <div class="form-row"><label>结束日期</label><input type="date" name="endTime" required></div>
            <div class="form-row"><label>排序权重（数字越小越靠前）</label><input type="number" name="sortOrder" value="0"></div>
            <button type="submit" class="btn-submit">创建活动</button>
        </form>
    </div>
</div>
</body>
</html>
