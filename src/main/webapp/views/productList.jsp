<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<jsp:include page="include/head.jsp"><jsp:param name="title" value="商品列表"/></jsp:include>
<body>
<jsp:include page="include/header.jsp"/>

<div class="container mt-4">
    <div class="row">
        <!-- 侧边栏：分类 -->
        <div class="col-md-3">
            <div class="card mb-3">
                <div class="card-header bg-primary text-white">商品分类</div>
                <div class="card-body p-0" id="categoryList">
                    <!-- AJAX 加载 -->
                </div>
            </div>
        </div>

        <!-- 主内容：商品列表 -->
        <div class="col-md-9">
            <!-- 搜索栏 -->
            <form class="mb-3" method="get" action="${pageContext.request.contextPath}/productList.do">
                <input type="hidden" name="action" value="productList">
                <div class="input-group">
                    <input type="text" name="keyword" class="form-control" placeholder="搜索商品名称或条码" value="${keyword}">
                    <button class="btn btn-primary" type="submit">搜索</button>
                </div>
            </form>

            <!-- 排序 -->
            <div class="mb-3">
                <span class="me-3">排序：</span>
                <a href="?action=productList&categoryId=${categoryId}&keyword=${keyword}&sortBy=createTime" class="btn btn-sm btn-outline-primary">最新</a>
                <a href="?action=productList&categoryId=${categoryId}&keyword=${keyword}&sortBy=price&sortOrder=asc" class="btn btn-sm btn-outline-primary">价格↑</a>
                <a href="?action=productList&categoryId=${categoryId}&keyword=${keyword}&sortBy=price&sortOrder=desc" class="btn btn-sm btn-outline-primary">价格↓</a>
                <a href="?action=productList&categoryId=${categoryId}&keyword=${keyword}&sortBy=salesCount" class="btn btn-sm btn-outline-primary">销量</a>
            </div>

            <!-- 商品列表 -->
            <div class="row" id="productList">
                <%-- Servlet 转发时带上数据 --%>
            </div>

            <%-- 分页 --%>
            <nav id="pagination"></nav>
        </div>
    </div>
</div>

<jsp:include page="include/footer.jsp"/>

<script>
var ctx = '${pageContext.request.contextPath}';
$(function() {
    // 加载分类
    $.getJSON(ctx + '/categoryTree.do', function(res) {
        if (res.code === 200) {
            var html = '<ul class="list-group list-group-flush">';
            html += '<li class="list-group-item"><a href="?action=productList">全部分类</a></li>';
            $.each(res.data, function(i, cat) {
                html += '<li class="list-group-item">'
                    + '<a href="?action=productList&categoryId=' + cat.categoryId + '">' + cat.categoryName + '</a>';
                if (cat.children && cat.children.length > 0) {
                    html += '<ul class="list-unstyled ms-3">';
                    $.each(cat.children, function(j, child) {
                        html += '<li><a href="?action=productList&categoryId=' + child.categoryId + '">' + child.categoryName + '</a></li>';
                    });
                    html += '</ul>';
                }
                html += '</li>';
            });
            html += '</ul>';
            $('#categoryList').html(html);
        }
    });
});
</script>
</body>
</html>
