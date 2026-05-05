<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<jsp:include page="include/head.jsp">
    <jsp:param name="title" value="首页"/>
</jsp:include>
<body>
<jsp:include page="include/header.jsp"/>

<div class="container mt-4">
    <!-- Banner -->
    <div id="homeBanner" class="carousel slide mb-4" data-bs-ride="carousel">
        <div class="carousel-inner">
            <div class="carousel-item active">
                <div class="p-5 bg-primary text-white rounded">
                    <h1>欢迎来到超市管理系统</h1>
                    <p>新鲜食材，优质生活</p>
                </div>
            </div>
        </div>
    </div>

    <!-- 推荐商品 -->
    <h3 class="mb-3">推荐商品</h3>
    <div id="recommendedProducts" class="row">
        <!-- AJAX 加载 -->
    </div>

    <!-- 热销商品 -->
    <h3 class="mb-3 mt-4">热销排行</h3>
    <div id="topSalesProducts" class="row">
        <!-- AJAX 加载 -->
    </div>
</div>

<jsp:include page="include/footer.jsp"/>

<script>
var ctx = '${pageContext.request.contextPath}';

// 加载推荐商品
$.getJSON(ctx + '/productRecommended.do?limit=8', function(res) {
    if (res.code === 200) {
        var html = '';
        $.each(res.data, function(i, p) {
            html += '<div class="col-md-3 col-sm-6 mb-3">'
                + '<div class="card h-100">'
                + '<img src="' + (p.coverImage || '/images/default.png') + '" class="card-img-top" style="height:180px;object-fit:cover">'
                + '<div class="card-body">'
                + '<h6 class="card-title">' + p.productName + '</h6>'
                + '<p class="text-danger fw-bold">¥' + p.price + '</p>'
                + '<button class="btn btn-sm btn-primary w-100" onclick="addToCart(' + p.productId + ')">加入购物车</button>'
                + '</div></div></div>';
        });
        $('#recommendedProducts').html(html);
    }
});

// 加载热销商品
$.getJSON(ctx + '/productTopSales.do?limit=8', function(res) {
    if (res.code === 200) {
        var html = '';
        $.each(res.data, function(i, p) {
            html += '<div class="col-md-3 col-sm-6 mb-3">'
                + '<div class="card h-100">'
                + '<img src="' + (p.coverImage || '/images/default.png') + '" class="card-img-top" style="height:180px;object-fit:cover">'
                + '<div class="card-body">'
                + '<h6 class="card-title">' + p.productName + '</h6>'
                + '<p class="text-danger fw-bold">¥' + p.price + '</p>'
                + '<p class="small text-muted">销量: ' + (p.salesCount || 0) + '</p>'
                + '<button class="btn btn-sm btn-primary w-100" onclick="addToCart(' + p.productId + ')">加入购物车</button>'
                + '</div></div></div>';
        });
        $('#topSalesProducts').html(html);
    }
});

function addToCart(productId) {
    $.post(ctx + '/cartAdd.do', {productId: productId, quantity: 1}, function(res) {
        if (res.code === 200) {
            alert('已加入购物车');
        } else {
            alert(res.message);
        }
    });
}
</script>
</body>
</html>
