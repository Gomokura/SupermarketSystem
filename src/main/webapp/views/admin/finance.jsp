<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<jsp:include page="adminLayout.jsp"><jsp:param name="title" value="财务报表"/></jsp:include>
<div class="mt-4">
    <div class="row">
        <div class="col-md-4">
            <div class="card text-center border-primary"><div class="card-body"><h3 class="text-primary">-</h3><p>总订单数</p></div></div>
        </div>
        <div class="col-md-4">
            <div class="card text-center border-success"><div class="card-body"><h3 class="text-success">-</h3><p>总销售额</p></div></div>
        </div>
        <div class="col-md-4">
            <div class="card text-center border-warning"><div class="card-body"><h3 class="text-warning">-</h3><p>总利润</p></div></div>
        </div>
    </div>
    <div class="card mt-4">
        <div class="card-header">销售明细</div>
        <div class="card-body">
            <table class="table table-bordered">
                <thead class="table-light"><tr><th>日期</th><th>订单数</th><th>销售额</th></tr></thead>
                <tbody><tr><td colspan="3" class="text-center text-muted py-4">功能开发中...</td></tr></tbody>
            </table>
        </div>
    </div>
</div>
</body></html>
