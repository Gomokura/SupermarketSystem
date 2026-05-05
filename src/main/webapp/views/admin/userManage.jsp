<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<jsp:include page="adminLayout.jsp"><jsp:param name="title" value="用户管理"/></jsp:include>
<div class="mt-4">
    <div class="card">
        <div class="card-header">用户管理</div>
        <div class="card-body">
            <table class="table table-bordered">
                <thead class="table-light"><tr><th>ID</th><th>用户名</th><th>手机号</th><th>会员等级</th><th>积分</th><th>状态</th><th>注册时间</th></tr></thead>
                <tbody id="userTableBody"><tr><td colspan="7" class="text-center py-4">加载中...</td></tr></tbody>
            </table>
        </div>
    </div>
</div>
<script>
var ctx = '${pageContext.request.contextPath}';
$(function() {
    $('#userTableBody').html('<tr><td colspan="7" class="text-center text-muted py-4">功能开发中...</td></tr>');
});
</script>
</body></html>
