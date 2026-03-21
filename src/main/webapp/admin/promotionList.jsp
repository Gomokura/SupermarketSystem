<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.supermarket.bean.User, java.util.*" %>
<%
    User user = (User) session.getAttribute("user");
    if (user == null || !"admin".equals(user.getRole())) { response.sendRedirect("../login.jsp"); return; }
    List<Map<String, Object>> promotions = (List<Map<String, Object>>) request.getAttribute("promotions");
    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
%>
<!DOCTYPE html>
<html>
<head>
    <title>促销管理</title>
    <style>
        * { margin:0; padding:0; box-sizing:border-box; }
        body { font-family:Arial; background:#f5f5f5; }
        .header { background:#2c3e50; color:white; padding:15px 30px; display:flex; justify-content:space-between; align-items:center; }
        .header a { color:white; text-decoration:none; font-size:14px; }
        .container { padding:30px; display:grid; grid-template-columns:1fr 340px; gap:20px; }
        .card { background:white; padding:25px; border-radius:8px; box-shadow:0 2px 5px rgba(0,0,0,0.1); }
        .card h2 { margin-bottom:15px; color:#2c3e50; }
        table { width:100%; border-collapse:collapse; }
        th,td { padding:10px 12px; text-align:left; border-bottom:1px solid #eee; font-size:13px; }
        th { background:#34495e; color:white; }
        tr:hover { background:#f9f9f9; }
        .form-row { margin-bottom:12px; }
        .form-row label { display:block; font-size:13px; color:#555; margin-bottom:4px; }
        .form-row input, .form-row select { width:100%; padding:8px; border:1px solid #ddd; border-radius:4px; font-size:13px; }
        .btn-submit { width:100%; padding:10px; background:#9b59b6; color:white; border:none; border-radius:4px; cursor:pointer; }
        .btn { padding:4px 10px; border:none; border-radius:3px; cursor:pointer; color:white; font-size:12px; }
        .btn-off { background:#95a5a6; }
        .btn-on  { background:#27ae60; }
        .btn-del { background:#e74c3c; }
        .badge-active   { background:#d4edda; color:#155724; padding:2px 8px; border-radius:10px; font-size:12px; }
        .badge-inactive { background:#f8d7da; color:#721c24; padding:2px 8px; border-radius:10px; font-size:12px; }
        .hint { font-size:12px; color:#999; margin-top:4px; }
    </style>
</head>
<body>
<div class="header">
    <h1>促销管理</h1>
    <a href="index.jsp">← 返回首页</a>
</div>
<div class="container">
    <div class="card">
        <h2>促销规则列表</h2>
        <table>
            <tr><th>名称</th><th>类型</th><th>规则</th><th>有效期</th><th>状态</th><th>操作</th></tr>
            <% if (promotions != null) for (Map<String,Object> p : promotions) {
                String type = (String) p.get("promoType");
                double cond = (Double) p.get("conditionVal");
                double disc = (Double) p.get("discountVal");
                String rule = "discount".equals(type)
                    ? String.format("全场%.0f折", disc * 10)
                    : String.format("满%.0f减%.0f", cond, disc);
                boolean active = "active".equals(p.get("status"));
            %>
            <tr>
                <td><%= p.get("promoName") %></td>
                <td><%= "discount".equals(type) ? "折扣" : "满减" %></td>
                <td><%= rule %></td>
                <td><%= sdf.format(p.get("startTime")) %> ~ <%= sdf.format(p.get("endTime")) %></td>
                <td><span class="badge-<%= active ? "active" : "inactive" %>"><%= active ? "启用" : "停用" %></span></td>
                <td>
                    <form style="display:inline" method="post" action="../adminPromotion">
                        <input type="hidden" name="action" value="updateStatus">
                        <input type="hidden" name="promotionId" value="<%= p.get("promotionId") %>">
                        <input type="hidden" name="status" value="<%= active ? "inactive" : "active" %>">
                        <button type="submit" class="btn <%= active ? "btn-off" : "btn-on" %>"><%= active ? "停用" : "启用" %></button>
                    </form>
                    <form style="display:inline" method="post" action="../adminPromotion" onsubmit="return confirm('确认删除？')">
                        <input type="hidden" name="action" value="delete">
                        <input type="hidden" name="promotionId" value="<%= p.get("promotionId") %>">
                        <button type="submit" class="btn btn-del">删除</button>
                    </form>
                </td>
            </tr>
            <% } %>
        </table>
    </div>

    <div class="card" style="height:fit-content;">
        <h2>新建促销</h2>
        <form action="../adminPromotion" method="post">
            <input type="hidden" name="action" value="add">
            <div class="form-row">
                <label>促销名称</label>
                <input type="text" name="promoName" required placeholder="如：双十一全场九折">
            </div>
            <div class="form-row">
                <label>类型</label>
                <select name="promoType" id="promoType" onchange="toggleHint()">
                    <option value="discount">折扣</option>
                    <option value="reduce">满减</option>
                </select>
            </div>
            <div class="form-row">
                <label>条件值</label>
                <input type="number" name="conditionVal" step="0.01" required placeholder="折扣填折扣率如0.9，满减填门槛如100">
                <div class="hint" id="hint">折扣率：0.9 = 九折</div>
            </div>
            <div class="form-row">
                <label>优惠值</label>
                <input type="number" name="discountVal" step="0.01" required placeholder="折扣同条件值，满减填减免金额如20">
            </div>
            <div class="form-row">
                <label>开始日期</label>
                <input type="date" name="startTime" required>
            </div>
            <div class="form-row">
                <label>结束日期</label>
                <input type="date" name="endTime" required>
            </div>
            <button type="submit" class="btn-submit">创建促销</button>
        </form>
    </div>
</div>
<script>
function toggleHint() {
    var t = document.getElementById('promoType').value;
    document.getElementById('hint').textContent = t === 'discount' ? '折扣率：0.9 = 九折' : '满减门槛金额，如100';
}
</script>
</body>
</html>
