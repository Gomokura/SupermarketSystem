<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.supermarket.bean.User, com.supermarket.bean.Product" %>
<%@ page import="com.supermarket.dao.ProductDAO, java.util.*" %>
<%
    User user = (User) session.getAttribute("user");
    if (user == null || !"admin".equals(user.getRole())) { response.sendRedirect("../login.jsp"); return; }
    List<Map<String, Object>> pos       = (List<Map<String, Object>>) request.getAttribute("pos");
    List<Map<String, Object>> suppliers = (List<Map<String, Object>>) request.getAttribute("suppliers");
    List<Product> products = new ProductDAO().searchProducts(null, null, null);
    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm");
%>
<!DOCTYPE html>
<html>
<head>
    <title>采购管理</title>
    <style>
        * { margin:0; padding:0; box-sizing:border-box; }
        body { font-family:Arial; background:#f5f5f5; }
        .header { background:#2c3e50; color:white; padding:15px 30px; display:flex; justify-content:space-between; align-items:center; }
        .header a { color:white; text-decoration:none; font-size:14px; }
        .container { padding:30px; display:grid; grid-template-columns:1fr 360px; gap:20px; }
        .card { background:white; padding:25px; border-radius:8px; box-shadow:0 2px 5px rgba(0,0,0,0.1); margin-bottom:20px; }
        .card h2 { margin-bottom:15px; color:#2c3e50; }
        table { width:100%; border-collapse:collapse; }
        th,td { padding:10px 12px; text-align:left; border-bottom:1px solid #eee; font-size:13px; }
        th { background:#34495e; color:white; }
        tr:hover { background:#f9f9f9; }
        .form-row { margin-bottom:12px; }
        .form-row label { display:block; font-size:13px; color:#555; margin-bottom:4px; }
        .form-row input, .form-row select, .form-row textarea { width:100%; padding:8px; border:1px solid #ddd; border-radius:4px; font-size:13px; }
        .btn { padding:5px 12px; border:none; border-radius:3px; cursor:pointer; color:white; font-size:12px; }
        .btn-approve { background:#27ae60; }
        .btn-detail  { background:#3498db; }
        .btn-submit  { width:100%; padding:10px; background:#e67e22; color:white; border:none; border-radius:4px; cursor:pointer; }
        .btn-add-row { padding:6px 14px; background:#95a5a6; color:white; border:none; border-radius:3px; cursor:pointer; font-size:13px; margin-bottom:10px; }
        .s-pending  { color:#f39c12; font-weight:bold; }
        .s-arrived  { color:#27ae60; font-weight:bold; }
        .item-row td input { width:100%; padding:4px; border:1px solid #ddd; border-radius:3px; }
        .tabs { display:flex; gap:10px; margin-bottom:20px; }
        .tab { padding:8px 20px; border-radius:4px; cursor:pointer; border:1px solid #ddd; background:white; }
        .tab.active { background:#2c3e50; color:white; border-color:#2c3e50; }
    </style>
</head>
<body>
<div class="header">
    <h1>采购管理</h1>
    <a href="index.jsp">← 返回首页</a>
</div>
<div class="container">
    <div>
        <!-- 供应商列表 -->
        <div class="card">
            <h2>供应商列表</h2>
            <table>
                <tr><th>ID</th><th>名称</th><th>联系人</th><th>电话</th><th>地址</th><th>状态</th></tr>
                <% if (suppliers != null) for (Map<String,Object> s : suppliers) { %>
                <tr>
                    <td><%= s.get("supplierId") %></td>
                    <td><%= s.get("supplierName") %></td>
                    <td><%= s.get("contact") != null ? s.get("contact") : "-" %></td>
                    <td><%= s.get("phone") != null ? s.get("phone") : "-" %></td>
                    <td><%= s.get("address") != null ? s.get("address") : "-" %></td>
                    <td><%= "active".equals(s.get("status")) ? "启用" : "停用" %></td>
                </tr>
                <% } %>
            </table>
        </div>

        <!-- 采购单列表 -->
        <div class="card">
            <h2>采购订单</h2>
            <table>
                <tr><th>单号</th><th>供应商</th><th>总成本</th><th>状态</th><th>操作人</th><th>创建时间</th><th>操作</th></tr>
                <% if (pos != null) for (Map<String,Object> po : pos) {
                    String s = (String) po.get("status"); %>
                <tr>
                    <td><%= po.get("poId") %></td>
                    <td><%= po.get("supplierName") %></td>
                    <td>¥<%= String.format("%.2f", (Double) po.get("totalCost")) %></td>
                    <td><span class="s-<%= s %>"><%= "pending".equals(s) ? "待到货" : "已到货" %></span></td>
                    <td><%= po.get("operator") != null ? po.get("operator") : "-" %></td>
                    <td><%= po.get("createTime") != null ? sdf.format(po.get("createTime")) : "-" %></td>
                    <td>
                        <button class="btn btn-detail" onclick="location.href='../adminPurchase?action=detail&poId=<%= po.get("poId") %>'">明细</button>
                        <% if ("pending".equals(s)) { %>
                        <form style="display:inline" method="post" action="../adminPurchase" onsubmit="return confirm('确认到货入库？')">
                            <input type="hidden" name="action" value="approve">
                            <input type="hidden" name="poId" value="<%= po.get("poId") %>">
                            <button type="submit" class="btn btn-approve">到货入库</button>
                        </form>
                        <% } %>
                    </td>
                </tr>
                <% } %>
            </table>
        </div>
    </div>

    <!-- 右侧表单 -->
    <div>
        <!-- 新增供应商 -->
        <div class="card">
            <h2>新增供应商</h2>
            <form method="post" action="../adminPurchase">
                <input type="hidden" name="action" value="addSupplier">
                <div class="form-row"><label>供应商名称</label><input type="text" name="supplierName" required></div>
                <div class="form-row"><label>联系人</label><input type="text" name="contact"></div>
                <div class="form-row"><label>电话</label><input type="text" name="phone"></div>
                <div class="form-row"><label>地址</label><input type="text" name="address"></div>
                <button type="submit" class="btn-submit" style="background:#3498db;">添加供应商</button>
            </form>
        </div>

        <!-- 创建采购单 -->
        <div class="card">
            <h2>创建采购单</h2>
            <form method="post" action="../adminPurchase" id="poForm">
                <input type="hidden" name="action" value="createPO">
                <div class="form-row">
                    <label>供应商</label>
                    <select name="supplierId" required>
                        <option value="">请选择</option>
                        <% if (suppliers != null) for (Map<String,Object> s : suppliers) { %>
                        <option value="<%= s.get("supplierId") %>"><%= s.get("supplierName") %></option>
                        <% } %>
                    </select>
                </div>
                <div class="form-row"><label>备注</label><input type="text" name="remark"></div>
                <button type="button" class="btn-add-row" onclick="addRow()">+ 添加商品</button>
                <table id="itemTable">
                    <tr><th>商品</th><th>数量</th><th>单价</th></tr>
                </table>
                <br>
                <button type="submit" class="btn-submit">提交采购单</button>
            </form>
        </div>
    </div>
</div>
<script>
var products = [
    <% for (Product p : products) { %>
    {id: <%= p.getProductId() %>, name: "<%= p.getProductName() %>"},
    <% } %>
];
function addRow() {
    var opts = products.map(p => '<option value="'+p.id+'">'+p.name+'</option>').join('');
    var tr = document.createElement('tr');
    tr.className = 'item-row';
    tr.innerHTML = '<td><select name="productId" required><option value="">选择商品</option>'+opts+'</select></td>' +
        '<td><input type="number" name="quantity" min="1" required></td>' +
        '<td><input type="number" name="unitCost" step="0.01" min="0" required></td>';
    document.getElementById('itemTable').appendChild(tr);
}
addRow();
</script>
</body>
</html>
