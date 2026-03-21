<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.supermarket.bean.User" %>
<%
    User user = (User) session.getAttribute("user");
    if (user == null || !"admin".equals(user.getRole())) { response.sendRedirect("../login.jsp"); return; }
    String msg     = request.getParameter("msg");
    String success = request.getParameter("success");
    String fail    = request.getParameter("fail");
%>
<!DOCTYPE html>
<html>
<head>
    <title>数据导入导出</title>
    <style>
        * { margin:0; padding:0; box-sizing:border-box; }
        body { font-family:Arial; background:#f5f5f5; }
        .header { background:#2c3e50; color:white; padding:15px 30px; display:flex; justify-content:space-between; align-items:center; }
        .header a { color:white; text-decoration:none; font-size:14px; }
        .container { padding:30px; display:grid; grid-template-columns:1fr 1fr; gap:20px; }
        .card { background:white; padding:25px; border-radius:8px; box-shadow:0 2px 5px rgba(0,0,0,0.1); }
        .card h2 { margin-bottom:15px; color:#2c3e50; }
        .card p  { color:#666; font-size:13px; margin-bottom:15px; line-height:1.8; }
        .btn-export { display:inline-block; padding:10px 24px; background:#27ae60; color:white; border-radius:4px; text-decoration:none; margin-right:10px; margin-bottom:10px; }
        .btn-export:hover { background:#219a52; }
        .upload-area { border:2px dashed #ddd; border-radius:8px; padding:30px; text-align:center; margin-bottom:15px; }
        .upload-area input { display:none; }
        .upload-area label { cursor:pointer; color:#3498db; }
        .file-name { font-size:13px; color:#666; margin-top:8px; }
        .btn-import { width:100%; padding:10px; background:#3498db; color:white; border:none; border-radius:4px; cursor:pointer; }
        .alert-ok   { background:#d4edda; color:#155724; padding:12px; border-radius:4px; margin-bottom:15px; }
        .alert-err  { background:#f8d7da; color:#721c24; padding:12px; border-radius:4px; margin-bottom:15px; }
        .template-hint { font-size:12px; color:#999; margin-top:10px; }
        code { background:#f4f4f4; padding:2px 6px; border-radius:3px; font-size:12px; }
    </style>
</head>
<body>
<div class="header">
    <h1>数据导入导出</h1>
    <a href="index.jsp">← 返回首页</a>
</div>
<div class="container">
    <!-- 导出 -->
    <div class="card">
        <h2>数据导出</h2>
        <p>将系统数据导出为 Excel 文件（.xlsx），可用于备份、分析或报表。</p>
        <a class="btn-export" href="../adminDataIO?action=exportProducts">导出商品数据</a>
        <a class="btn-export" href="../adminDataIO?action=exportOrders" style="background:#e67e22;">导出订单数据</a>
        <p style="margin-top:15px;">导出字段：<br>
            商品：ID / 名称 / 分类 / 价格 / 库存 / 单位 / 供应商 / 状态<br>
            订单：订单号 / 用户 / 金额 / 状态 / 支付方式 / 时间
        </p>
    </div>

    <!-- 导入 -->
    <div class="card">
        <h2>商品数据导入</h2>
        <% if ("ok".equals(msg)) { %>
        <div class="alert-ok">导入完成：成功 <%= success %> 条，失败 <%= fail %> 条</div>
        <% } else if ("error".equals(msg)) { %>
        <div class="alert-err">文件解析失败，请检查格式</div>
        <% } else if ("nofile".equals(msg)) { %>
        <div class="alert-err">请选择文件</div>
        <% } %>
        <p>上传 Excel 文件批量导入商品，第一行为表头，从第二行开始读取数据。</p>
        <form method="post" action="../adminDataIO" enctype="multipart/form-data">
            <div class="upload-area">
                <input type="file" name="file" id="fileInput" accept=".xlsx" onchange="showName(this)">
                <label for="fileInput">📂 点击选择 .xlsx 文件</label>
                <div class="file-name" id="fileName">未选择文件</div>
            </div>
            <button type="submit" class="btn-import">开始导入</button>
        </form>
        <div class="template-hint">
            Excel 列顺序：<code>商品名称</code> <code>分类名称</code> <code>价格</code> <code>库存</code> <code>单位</code> <code>供应商</code>
        </div>
    </div>
</div>
<script>
function showName(input) {
    document.getElementById('fileName').textContent = input.files[0] ? input.files[0].name : '未选择文件';
}
</script>
</body>
</html>
