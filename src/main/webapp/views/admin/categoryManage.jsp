<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<jsp:include page="adminLayout.jsp"><jsp:param name="title" value="分类管理"/></jsp:include>
<div class="mt-4">
    <div class="card">
        <div class="card-header d-flex justify-content-between align-items-center">
            <span>分类管理</span>
            <button class="btn btn-sm btn-primary">新增分类</button>
        </div>
        <div class="card-body">
            <table class="table table-bordered">
                <thead class="table-light"><tr><th>ID</th><th>分类名称</th><th>父分类</th><th>排序</th><th>操作</th></tr></thead>
                <tbody><tr><td colspan="5" class="text-center text-muted py-4">功能开发中...</td></tr></tbody>
            </table>
        </div>
    </div>
</div>
</body></html>
