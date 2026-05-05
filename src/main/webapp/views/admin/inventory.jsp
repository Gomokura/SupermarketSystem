<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<jsp:include page="adminLayout.jsp"><jsp:param name="title" value="库存管理"/></jsp:include>
<div class="mt-4">
    <div class="card">
        <div class="card-header">低库存预警</div>
        <div class="card-body">
            <table class="table table-bordered">
                <thead class="table-light"><tr><th>ID</th><th>商品名称</th><th>当前库存</th><th>预警阈值</th><th>操作</th></tr></thead>
                <tbody id="lowStockTable"><tr><td colspan="5" class="text-center py-4">加载中...</td></tr></tbody>
            </table>
        </div>
    </div>
</div>
<script>
var ctx = '${pageContext.request.contextPath}';
$(function() {
    $('#lowStockTable').html('<tr><td colspan="5" class="text-center text-muted py-4">功能开发中...</td></tr>');
});
</script>
</body></html>
