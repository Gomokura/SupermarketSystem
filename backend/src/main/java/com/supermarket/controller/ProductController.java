package com.supermarket.controller;

import com.supermarket.common.Result;
import com.supermarket.entity.Category;
import com.supermarket.entity.Product;
import com.supermarket.entity.ProductSku;
import com.supermarket.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    // ==================== C端公开接口 ====================

    /**
     * 商品列表（搜索/分类/品牌/排序/价格区间）
     * GET /products/list?categoryId=&keyword=&brandId=&sortBy=&sortOrder=&minPrice=&maxPrice=&pageNum=&pageSize=
     */
    @GetMapping("/list")
    public Result<?> getProductList(
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer brandId,
            @RequestParam(required = false, defaultValue = "createTime") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortOrder,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return productService.getProductList(categoryId, keyword, brandId,
                sortBy, sortOrder, minPrice, maxPrice, pageNum, pageSize);
    }

    /**
     * 商品详情（含SKU）
     * GET /products/{productId}
     */
    @GetMapping("/{productId}")
    public Result<?> getProductById(@PathVariable Integer productId) {
        return productService.getProductById(productId);
    }

    /**
     * 条码扫码查询
     * GET /products/barcode/{barcode}
     */
    @GetMapping("/barcode/{barcode}")
    public Result<?> getProductByBarcode(@PathVariable String barcode) {
        return productService.getProductByBarcode(barcode);
    }

    /**
     * 首页推荐商品
     * GET /products/recommended?limit=8
     */
    @GetMapping("/recommended")
    public Result<?> getRecommendedProducts(
            @RequestParam(defaultValue = "8") Integer limit) {
        return productService.getRecommendedProducts(limit);
    }

    /**
     * 分类树（C端导航用）
     * GET /categories/tree
     */
    @GetMapping("/categories/tree")
    public Result<?> getCategoryTree() {
        return productService.getCategoryTree();
    }

    /**
     * 扁平分类列表（下拉框用）
     * GET /categories/list
     */
    @GetMapping("/categories/list")
    public Result<?> getCategoriesList() {
        return productService.getCategories();
    }

    // ==================== B端管理接口 ====================

    /**
     * 管理后台商品列表（支持所有状态筛选）
     * GET /products/admin/list
     */
    @GetMapping("/admin/list")
    public Result<?> adminGetProductList(
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return productService.adminGetProductList(categoryId, keyword, status, pageNum, pageSize);
    }

    /**
     * 新增商品
     * POST /products
     */
    @PostMapping
    public Result<?> addProduct(@RequestBody Product product) {
        return productService.addProduct(product);
    }

    /**
     * 修改商品
     * PUT /products/{productId}
     */
    @PutMapping("/{productId}")
    public Result<?> updateProduct(
            @PathVariable Integer productId,
            @RequestBody Product product) {
        product.setProductId(productId);
        return productService.updateProduct(product);
    }

    /**
     * 删除商品（逻辑删除）
     * DELETE /products/{productId}
     */
    @DeleteMapping("/{productId}")
    public Result<?> deleteProduct(@PathVariable Integer productId) {
        return productService.deleteProduct(productId);
    }

    /**
     * 上下架商品
     * PUT /products/{productId}/status
     * body: { "status": "active" | "off_shelf" }
     */
    @PutMapping("/{productId}/status")
    public Result<?> updateProductStatus(
            @PathVariable Integer productId,
            @RequestBody Map<String, String> body) {
        return productService.updateProductStatus(productId, body.get("status"));
    }

    /**
     * 批量上下架
     * PUT /products/batch/status
     * body: { "ids": [1,2,3], "status": "active" }
     */
    @PutMapping("/batch/status")
    public Result<?> batchUpdateStatus(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Integer> ids = (List<Integer>) body.get("ids");
        String status = (String) body.get("status");
        return productService.batchUpdateStatus(ids, status);
    }

    /**
     * 低库存预警列表
     * GET /products/low-stock
     */
    @GetMapping("/low-stock")
    public Result<?> getLowStockProducts(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        return productService.getLowStockProducts(pageNum, pageSize);
    }

    // ==================== 分类管理（B端） ====================

    /**
     * 新增分类
     * POST /products/categories
     */
    @PostMapping("/categories")
    public Result<?> addCategory(@RequestBody Category category) {
        return productService.addCategory(category);
    }

    /**
     * 修改分类
     * PUT /products/categories/{categoryId}
     */
    @PutMapping("/categories/{categoryId}")
    public Result<?> updateCategory(
            @PathVariable Integer categoryId,
            @RequestBody Category category) {
        category.setCategoryId(categoryId);
        return productService.updateCategory(category);
    }

    /**
     * 删除分类
     * DELETE /products/categories/{categoryId}
     */
    @DeleteMapping("/categories/{categoryId}")
    public Result<?> deleteCategory(@PathVariable Integer categoryId) {
        return productService.deleteCategory(categoryId);
    }

    // ==================== SKU管理 ====================

    /**
     * 获取商品SKU列表
     * GET /products/{productId}/skus
     */
    @GetMapping("/{productId}/skus")
    public Result<?> getProductSkus(@PathVariable Integer productId) {
        return productService.getProductSkus(productId);
    }

    /**
     * 新增/修改SKU
     * POST /products/{productId}/skus
     */
    @PostMapping("/{productId}/skus")
    public Result<?> saveProductSku(
            @PathVariable Integer productId,
            @RequestBody ProductSku sku) {
        sku.setProductId(productId);
        return productService.saveProductSku(sku);
    }

    /**
     * 删除SKU
     * DELETE /products/skus/{skuId}
     */
    @DeleteMapping("/skus/{skuId}")
    public Result<?> deleteProductSku(@PathVariable Integer skuId) {
        return productService.deleteProductSku(skuId);
    }
}
