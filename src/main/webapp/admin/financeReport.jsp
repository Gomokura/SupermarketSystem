<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.supermarket.bean.User, java.util.*" %>
<%
    User user = (User) session.getAttribute("user");
    if (user == null || !"admin".equals(user.getRole())) { response.sendRedirect("../login.jsp"); return; }
    Map<String,Object> summary = (Map<String,Object>) request.getAttribute("summary");
    List<Map<String,Object>> monthlyRevenue  = (List<Map<String,Object>>) request.getAttribute("monthlyRevenue");
    List<Map<String,Object>> monthlyCost     = (List<Map<String,Object>>) request.getAttribute("monthlyCost");
    List<Map<String,Object>> categoryRevenue = (List<Map<String,Object>>) request.getAttribute("categoryRevenue");
%>
<!DOCTYPE html>
<html>
<head>
    <title>财务报表</title>
    <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.0/dist/chart.umd.min.js"></script>
    <style>
        * { margin:0; padding:0; box-sizing:border-box; }
        body { font-family:Arial; background:#f5f5f5; }
        .header { background:#2c3e50; color:white; padding:15px 30px; display:flex; justify-content:space-between; align-items:center; }
        .header a { color:white; text-decoration:none; font-size:14px; }
        .container { padding:30px; }
        .kpi-grid { display:grid; grid-template-columns:repeat(4,1fr); gap:20px; margin-bottom:25px; }
        .kpi { background:white; padding:20px; border-radius:8px; box-shadow:0 2px 5px rgba(0,0,0,0.1); text-align:center; }
        .kpi h3 { font-size:13px; color:#666; margin-bottom:8px; }
        .kpi .val { font-size:28px; font-weight:bold; }
        .kpi.revenue .val { color:#27ae60; }
        .kpi.cost    .val { color:#e74c3c; }
        .kpi.profit  .val { color:#3498db; }
        .kpi.rate    .val { color:#9b59b6; }
        .charts { display:grid; grid-template-columns:2fr 1fr; gap:20px; }
        .card { background:white; padding:25px; border-radius:8px; box-shadow:0 2px 5px rgba(0,0,0,0.1); }
        .card h2 { margin-bottom:15px; color:#2c3e50; font-size:16px; }
    </style>
</head>
<body>
<div class="header">
    <h1>财务报表</h1>
    <a href="index.jsp">← 返回首页</a>
</div>
<div class="container">
    <div class="kpi-grid">
        <div class="kpi revenue"><h3>总收入</h3><div class="val">¥<%= String.format("%.0f", (Double)summary.get("totalRevenue")) %></div></div>
        <div class="kpi cost">  <h3>总成本</h3><div class="val">¥<%= String.format("%.0f", (Double)summary.get("totalCost")) %></div></div>
        <div class="kpi profit"><h3>总利润</h3><div class="val">¥<%= String.format("%.0f", (Double)summary.get("totalProfit")) %></div></div>
        <div class="kpi rate">  <h3>利润率</h3><div class="val"><%= String.format("%.1f", (Double)summary.get("profitRate")) %>%</div></div>
    </div>
    <div class="charts">
        <div class="card">
            <h2>近6个月收入 vs 成本</h2>
            <canvas id="lineChart" height="100"></canvas>
        </div>
        <div class="card">
            <h2>分类销售占比</h2>
            <canvas id="pieChart" height="200"></canvas>
        </div>
    </div>
</div>
<script>
// 收入/成本折线图
var revenueData = {
    <% StringBuilder months = new StringBuilder(); StringBuilder revenues = new StringBuilder(); StringBuilder costs = new StringBuilder();
       Set<String> allMonths = new LinkedHashSet<>();
       Map<String,Double> revMap = new LinkedHashMap<>(), costMap = new LinkedHashMap<>();
       if (monthlyRevenue != null) for (Map<String,Object> r : monthlyRevenue) { allMonths.add((String)r.get("month")); revMap.put((String)r.get("month"), (Double)r.get("revenue")); }
       if (monthlyCost != null)    for (Map<String,Object> r : monthlyCost)    { allMonths.add((String)r.get("month")); costMap.put((String)r.get("month"), (Double)r.get("cost")); }
       for (String m : allMonths) { months.append("'").append(m).append("',"); revenues.append(revMap.getOrDefault(m,0.0)).append(","); costs.append(costMap.getOrDefault(m,0.0)).append(","); }
    %>
    labels: [<%= months %>],
    revenue: [<%= revenues %>],
    cost:    [<%= costs %>]
};
new Chart(document.getElementById('lineChart'), {
    type: 'line',
    data: {
        labels: revenueData.labels,
        datasets: [
            { label:'收入', data: revenueData.revenue, borderColor:'#27ae60', backgroundColor:'rgba(39,174,96,0.1)', tension:0.3, fill:true },
            { label:'成本', data: revenueData.cost,    borderColor:'#e74c3c', backgroundColor:'rgba(231,76,60,0.1)',  tension:0.3, fill:true }
        ]
    },
    options: { responsive:true, plugins:{ legend:{ position:'top' } } }
});

// 分类饼图
var catData = {
    <% StringBuilder catLabels = new StringBuilder(); StringBuilder catValues = new StringBuilder();
       if (categoryRevenue != null) for (Map<String,Object> r : categoryRevenue) { catLabels.append("'").append(r.get("category")).append("',"); catValues.append(r.get("revenue")).append(","); } %>
    labels: [<%= catLabels %>],
    values: [<%= catValues %>]
};
new Chart(document.getElementById('pieChart'), {
    type: 'doughnut',
    data: {
        labels: catData.labels,
        datasets: [{ data: catData.values, backgroundColor: ['#3498db','#e74c3c','#f39c12','#27ae60','#9b59b6','#1abc9c','#e67e22'] }]
    },
    options: { responsive:true, plugins:{ legend:{ position:'bottom' } } }
});
</script>
</body>
</html>
