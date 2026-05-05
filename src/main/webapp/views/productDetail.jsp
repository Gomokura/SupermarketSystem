<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<jsp:include page="include/head.jsp"><jsp:param name="title" value="商品详情"/></jsp:include>
<body>
<jsp:include page="include/header.jsp"/>

<div class="container mt-4" id="productDetail">
    <!-- 商品详情由 AJAX 加载 -->
</div>

<jsp:include page="include/footer.jsp"/>

<script>
var ctx = '${pageContext.request.contextPath}';
$(function() {
    var productId = '${not empty param.productId ? param.productId : ""}';
    if (!productId) { $('#productDetail').html('<div class="alert alert-danger">商品ID不能为空</div>'); return; }

    $.getJSON(ctx + '/productDetail.do', {productId: productId}, function(res) {
        if (res.code === 200) {
            var p = res.data;
            var skuOptions = '';
            if (p.skus && p.skus.length > 0) {
                $.each(p.skus, function(i, sku) {
                    skuOptions += '<option value="' + sku.skuId + '">' + sku.skuName + ' - ¥' + sku.price + '（库存:' + sku.stock + '）</option>';
                });
            }
            var html = '<div class="row"><div class="col-md-5">'
                + '<img src="' + (p.coverImage || '/images/default.png') + '" class="img-fluid rounded">'
                + '</div><div class="col-md-7">'
                + '<h2>' + p.productName + '</h2>'
                + '<p class="text-muted">' + (p.description || '') + '</p>'
                + '<h3 class="text-danger">¥' + p.price + '</h3>'
                + '<p class="text-muted">原价：<s>¥' + (p.originalPrice || p.price) + '</s></p>'
                + '<p>库存：' + p.stock + ' 件</p>';
            if (skuOptions) {
                html += '<div class="mb-3"><label class="form-label">选择规格：</label>'
                    + '<select class="form-select" id="skuSelect">' + skuOptions + '</select></div>';
            }
            html += '<div class="mb-3"><label class="form-label">数量：</label>'
                + '<input type="number" id="quantity" class="form-control" value="1" min="1" max="' + p.stock + '" style="width:120px"></div>'
                + '<button class="btn btn-primary btn-lg" onclick="addToCart()">加入购物车</button>'
                + '</div></div>';
            $('#productDetail').html(html);
        } else {
            $('#productDetail').html('<div class="alert alert-danger">' + res.message + '</div>');
        }
    });
});

function addToCart() {
    var productId = '${param.productId}';
    var quantity = $('#quantity').val();
    var skuId = $('#skuSelect').val();
    $.post(ctx + '/cartAdd.do', {productId: productId, quantity: quantity, skuId: skuId || ''}, function(res) {
        if (res.code === 200) {
            if (confirm('已加入购物车！是否去购物车结算？')) {
                location.href = ctx + '/views/cart.jsp';
            }
        } else {
            alert(res.message);
        }
    });
}
</script>
</body>
</html>
