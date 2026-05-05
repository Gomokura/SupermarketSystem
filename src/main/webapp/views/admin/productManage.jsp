<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<jsp:include page="adminLayout.jsp"><jsp:param name="title" value="商品管理"/></jsp:include>

<div class="mt-4">
    <div class="card">
        <div class="card-header d-flex justify-content-between align-items-center">
            <span>商品列表</span>
            <button class="btn btn-sm btn-primary" onclick="location.href='${pageContext.request.contextPath}/views/admin/productAdd.jsp'">新增商品</button>
        </div>
        <div class="card-body">
            <form class="row g-2 mb-3" id="searchForm">
                <div class="col-md-3">
                    <input type="text" name="keyword" class="form-control" placeholder="商品名称/条码">
                </div>
                <div class="col-md-2">
                    <select name="status" class="form-select">
                        <option value="">全部状态</option>
                        <option value="active">上架</option>
                        <option value="off_shelf">下架</option>
                    </select>
                </div>
                <div class="col-md-2">
                    <button type="button" class="btn btn-primary" onclick="loadProducts()">搜索</button>
                    <button type="reset" class="btn btn-outline-secondary">重置</button>
                </div>
            </form>

            <table class="table table-hover table-bordered">
                <thead class="table-light">
                    <tr>
                        <th>ID</th>
                        <th>商品名称</th>
                        <th>分类</th>
                        <th>价格</th>
                        <th>库存</th>
                        <th>销量</th>
                        <th>状态</th>
                        <th>操作</th>
                    </tr>
                </thead>
                <tbody id="productTableBody">
                    <tr><td colspan="8" class="text-center text-muted py-4">加载中...</td></tr>
                </tbody>
            </table>

            <nav id="productPagination"></nav>
        </div>
    </div>
</div>

<script>
var ctx = '${pageContext.request.contextPath}';
function loadProducts(pageNum) {
    pageNum = pageNum || 1;
    var params = $('#searchForm').serialize() + '&pageNum=' + pageNum + '&action=productList';
    $.getJSON(ctx + '/adminProductList.do', params, function(res) {
        if (res.code !== 200 || !res.data || !res.data.records) {
            $('#productTableBody').html('<tr><td colspan="8" class="text-center text-muted py-4">暂无数据</td></tr>');
            return;
        }
        var html = '';
        $.each(res.data.records, function(i, p) {
            var statusBadge = p.status === 'active'
                ? '<span class="badge bg-success">上架</span>'
                : '<span class="badge bg-secondary">下架</span>';
            html += '<tr>'
                + '<td>' + p.productId + '</td>'
                + '<td>' + p.productName + '</td>'
                + '<td>' + (p.categoryName || '-') + '</td>'
                + '<td>¥' + (p.price || 0).toFixed(2) + '</td>'
                + '<td class="' + (p.stock <= (p.stockWarning || 10) ? 'text-danger fw-bold' : '') + '">' + p.stock + '</td>'
                + '<td>' + (p.salesCount || 0) + '</td>'
                + '<td>' + statusBadge + '</td>'
                + '<td>'
                + '<button class="btn btn-sm btn-outline-primary me-1" onclick="editProduct(' + p.productId + ')">编辑</button>'
                + '<button class="btn btn-sm btn-outline-danger" onclick="deleteProduct(' + p.productId + ')">删除</button>'
                + '</td></tr>';
        });
        $('#productTableBody').html(html);
    });
}
function editProduct(id) { location.href = ctx + '/views/admin/productEdit.jsp?id=' + id; }
function deleteProduct(id) {
    if (!confirm('确认删除？')) return;
    $.post(ctx + '/adminProductDelete.do', {action: 'delete', productId: id}, function() { loadProducts(); });
}
$(function() { loadProducts(); });
</script>
</body>
</html>
