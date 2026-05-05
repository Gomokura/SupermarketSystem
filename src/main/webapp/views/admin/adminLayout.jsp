<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<jsp:include page="/views/include/head.jsp"><jsp:param name="title" value="管理后台"/></jsp:include>
<style>
body { background-color: #f0f2f5; }
.admin-sidebar { width: 220px; min-height: 100vh; background: #fff; border-right: 1px solid #ddd; position: fixed; left: 0; top: 0; bottom: 0; overflow-y: auto; }
.admin-content { margin-left: 220px; padding: 20px; }
.sidebar-brand { padding: 20px; font-size: 18px; font-weight: bold; color: #fff; background: #0d6efd; }
.sidebar-nav { list-style: none; padding: 0; margin: 0; }
.sidebar-nav li a { display: block; padding: 12px 20px; color: #333; text-decoration: none; border-bottom: 1px solid #f0f0f0; }
.sidebar-nav li a:hover, .sidebar-nav li a.active { background: #e7f3ff; color: #0d6efd; }
.topbar { background: #fff; padding: 15px 20px; border-bottom: 1px solid #ddd; display: flex; justify-content: space-between; align-items: center; }
</style>
<body>
<div class="admin-sidebar">
    <div class="sidebar-brand">超市管理后台</div>
    <ul class="sidebar-nav">
        <li><a href="${pageContext.request.contextPath}/adminDashboard.do" class="active">仪表盘</a></li>
        <li><a href="${pageContext.request.contextPath}/views/admin/productManage.jsp">商品管理</a></li>
        <li><a href="${pageContext.request.contextPath}/views/admin/orderManage.jsp">订单管理</a></li>
        <li><a href="${pageContext.request.contextPath}/views/admin/userManage.jsp">用户管理</a></li>
        <li><a href="${pageContext.request.contextPath}/views/admin/inventory.jsp">库存管理</a></li>
        <li><a href="${pageContext.request.contextPath}/views/admin/finance.jsp">财务报表</a></li>
        <li><a href="${pageContext.request.contextPath}/views/admin/delivery.jsp">配送管理</a></li>
        <li><a href="${pageContext.request.contextPath}/views/admin/cashier.jsp">收银台</a></li>
        <li><a href="${pageContext.request.contextPath}/views/admin/categoryManage.jsp">分类管理</a></li>
        <li><a href="${pageContext.request.contextPath}/adminLogout.do">退出登录</a></li>
    </ul>
</div>
<div class="admin-content">
    <div class="topbar">
        <h4 class="mb-0">${param.title != null ? param.title : '仪表盘'}</h4>
        <div>
            <span class="me-3">${sessionScope.adminRealName} (${sessionScope.adminRole})</span>
            <a href="${pageContext.request.contextPath}/adminLogout.do" class="btn btn-sm btn-outline-danger">退出</a>
        </div>
    </div>
    <div class="mt-4" id="adminMainContent">
        <!-- 内容由具体页面填充 -->
    </div>
</div>
</body>
</html>
