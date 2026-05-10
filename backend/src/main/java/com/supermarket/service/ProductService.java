package com.supermarket.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.supermarket.common.Result;
import com.supermarket.entity.Category;
import com.supermarket.entity.Product;
import com.supermarket.entity.ProductSku;
import com.supermarket.entity.InventoryLog;
import com.supermarket.mapper.BrandMapper;
import com.supermarket.mapper.CategoryMapper;
import com.supermarket.mapper.InventoryLogMapper;
import com.supermarket.mapper.ProductMapper;
import com.supermarket.mapper.ProductSkuMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProductService extends ServiceImpl<ProductMapper, Product> {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private ProductSkuMapper productSkuMapper;

    @Autowired
    private BrandMapper brandMapper;

    @Autowired
    private InventoryLogMapper inventoryLogMapper;

    // ==================== C端商品浏览 ====================

    /**
     * 商品列表（支持分类/关键词/品牌/排序/价格区间）
     */
    public Result<?> getProductList(Integer categoryId, String keyword, Integer brandId,
                                    String sortBy, String sortOrder,
                                    Double minPrice, Double maxPrice,
                                    Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        // 只展示上架且未删除的商品
        wrapper.eq(Product::getStatus, "active");
        wrapper.eq(Product::getIsDeleted, 0);

        if (categoryId != null && categoryId > 0) {
            List<Integer> categoryIds = getCategoryAndChildIds(categoryId);
            wrapper.in(Product::getCategoryId, categoryIds);
        }
        if (brandId != null && brandId > 0) {
            wrapper.eq(Product::getBrandId, brandId);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Product::getProductName, keyword)
                    .or().like(Product::getBarcode, keyword));
        }
        if (minPrice != null) {
            wrapper.ge(Product::getPrice, minPrice);
        }
        if (maxPrice != null) {
            wrapper.le(Product::getPrice, maxPrice);
        }

        // 排序（兼容前端 sortBy：sales / salesCount / createTime / new）
        if ("price".equals(sortBy)) {
            if ("asc".equals(sortOrder)) {
                wrapper.orderByAsc(Product::getPrice);
            } else {
                wrapper.orderByDesc(Product::getPrice);
            }
        } else if ("sales".equals(sortBy) || "salesCount".equals(sortBy)) {
            wrapper.orderByDesc(Product::getSalesCount);
        } else if ("rating".equals(sortBy)) {
            wrapper.orderByDesc(Product::getAvgRating);
        } else if ("createTime".equals(sortBy) || "new".equals(sortBy)) {
            wrapper.orderByDesc(Product::getCreateTime);
        } else {
            wrapper.orderByDesc(Product::getCreateTime);
        }

        Page<Product> page = new Page<>(pageNum, pageSize);
        Page<Product> result = this.page(page, wrapper);
        return Result.success(result);
    }

    private List<Integer> getCategoryAndChildIds(Integer categoryId) {
        Set<Integer> ids = new LinkedHashSet<>();
        ids.add(categoryId);
        List<Category> all = categoryMapper.selectList(null);
        collectChildCategoryIds(categoryId, all, ids);
        return new ArrayList<>(ids);
    }

    private void collectChildCategoryIds(Integer parentId, List<Category> all, Set<Integer> ids) {
        for (Category category : all) {
            if (parentId.equals(category.getParentId())) {
                ids.add(category.getCategoryId());
                collectChildCategoryIds(category.getCategoryId(), all, ids);
            }
        }
    }

    /**
     * 按条码查询商品（收银台扫码用）
     */
    public Result<?> getProductByBarcode(String barcode) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getBarcode, barcode);
        wrapper.eq(Product::getIsDeleted, 0);
        Product product = this.getOne(wrapper);
        if (product == null) {
            return Result.error("未找到条码对应商品");
        }
        return Result.success(product);
    }

    /**
     * 商品详情（含SKU列表）
     */
    public Result<?> getProductById(Integer productId) {
        Product product = this.getById(productId);
        if (product == null || product.getIsDeleted() == 1) {
            return Result.error("商品不存在");
        }
        // 查询SKU列表
        LambdaQueryWrapper<ProductSku> skuWrapper = new LambdaQueryWrapper<>();
        skuWrapper.eq(ProductSku::getProductId, productId);
        skuWrapper.eq(ProductSku::getStatus, "active");
        List<ProductSku> skus = productSkuMapper.selectList(skuWrapper);
        product.setSkus(skus);
        return Result.success(product);
    }

    /**
     * 推荐商品（首页轮播区）
     */
    public Result<?> getRecommendedProducts(Integer limit) {
        int maxLimit = (limit != null && limit > 0) ? limit : 10;
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getIsRecommend, 1);
        wrapper.eq(Product::getStatus, "active");
        wrapper.eq(Product::getIsDeleted, 0);
        wrapper.orderByDesc(Product::getSalesCount);
        // Oracle ROWNUM 需在子查询中使用；通过 Page 限制行数绕开 ROWNUM 语法问题
        Page<Product> page = new Page<>(1, maxLimit);
        List<Product> list = this.page(page, wrapper).getRecords();
        return Result.success(list);
    }

    /**
     * 热销商品TOP10（按销量倒序）
     */
    public Result<?> getTopSalesProducts(Integer limit) {
        int maxLimit = (limit != null && limit > 0) ? limit : 10;
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, "active");
        wrapper.eq(Product::getIsDeleted, 0);
        wrapper.gt(Product::getSalesCount, 0);
        wrapper.orderByDesc(Product::getSalesCount);
        Page<Product> page = new Page<>(1, maxLimit);
        List<Product> list = this.page(page, wrapper).getRecords();
        return Result.success(list);
    }

    /**
     * 新品上市（按创建时间倒序）
     */
    public Result<?> getNewProducts(Integer limit) {
        int maxLimit = (limit != null && limit > 0) ? limit : 10;
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, "active");
        wrapper.eq(Product::getIsDeleted, 0);
        wrapper.orderByDesc(Product::getCreateTime);
        Page<Product> page = new Page<>(1, maxLimit);
        List<Product> list = this.page(page, wrapper).getRecords();
        return Result.success(list);
    }

    /**
     * 搜索联想词（返回商品名称中包含关键词的商品名列表）
     */
    public Result<?> getSearchSuggestions(String keyword, Integer limit) {
        if (keyword == null || keyword.isEmpty()) {
            return Result.success(new ArrayList<>());
        }
        int maxLimit = (limit != null && limit > 0) ? limit : 10;
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(Product::getProductName, keyword);
        wrapper.eq(Product::getStatus, "active");
        wrapper.eq(Product::getIsDeleted, 0);
        wrapper.select(Product::getProductId, Product::getProductName);
        // 使用Page限制数量，兼容Oracle语法
        Page<Product> page = new Page<>(1, maxLimit);
        List<Product> list = this.page(page, wrapper).getRecords();
        // 仅返回名称列表
        List<String> names = list.stream()
                .map(Product::getProductName)
                .distinct()
                .collect(Collectors.toList());
        return Result.success(names);
    }

    // ==================== B端商品管理 ====================

    /**
     * 管理后台商品列表（含下架、所有状态）
     */
    public Result<?> adminGetProductList(Integer categoryId, String keyword, String status,
                                          Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getIsDeleted, 0);

        if (categoryId != null && categoryId > 0) {
            wrapper.eq(Product::getCategoryId, categoryId);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Product::getProductName, keyword)
                    .or().like(Product::getBarcode, keyword));
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Product::getStatus, status);
        }
        wrapper.orderByDesc(Product::getCreateTime);

        Page<Product> page = new Page<>(pageNum, pageSize);
        return Result.success(this.page(page, wrapper));
    }

    /**
     * 新增商品
     */
    @Transactional
    public Result<?> addProduct(Product product) {
        // 验证条码唯一性
        if (product.getBarcode() != null && !product.getBarcode().isEmpty()) {
            LambdaQueryWrapper<Product> barcodeCheck = new LambdaQueryWrapper<>();
            barcodeCheck.eq(Product::getBarcode, product.getBarcode());
            barcodeCheck.eq(Product::getIsDeleted, 0);
            if (this.getOne(barcodeCheck) != null) {
                return Result.error("条码已存在：" + product.getBarcode());
            }
        }
        if (product.getIsDeleted() == null) product.setIsDeleted(0);
        if (product.getIsRecommend() == null) product.setIsRecommend(0);
        if (product.getSalesCount() == null) product.setSalesCount(0);
        if (product.getStockWarning() == null) product.setStockWarning(10);
        if (product.getStatus() == null) product.setStatus("active");
        product.setCreateTime(new Date());
        product.setUpdateTime(new Date());
        product.setProductId(productMapper.getNextId());
        this.save(product);
        return Result.success(product.getProductId());
    }

    /**
     * 修改商品
     */
    @Transactional
    public Result<?> updateProduct(Product product) {
        Product existing = this.getById(product.getProductId());
        if (existing == null || existing.getIsDeleted() == 1) {
            return Result.error("商品不存在");
        }
        // 条码唯一性检查
        if (product.getBarcode() != null && !product.getBarcode().equals(existing.getBarcode())) {
            LambdaQueryWrapper<Product> barcodeCheck = new LambdaQueryWrapper<>();
            barcodeCheck.eq(Product::getBarcode, product.getBarcode());
            barcodeCheck.ne(Product::getProductId, product.getProductId());
            barcodeCheck.eq(Product::getIsDeleted, 0);
            if (this.getOne(barcodeCheck) != null) {
                return Result.error("条码已被其他商品使用：" + product.getBarcode());
            }
        }
        product.setUpdateTime(new Date());
        this.updateById(product);
        return Result.success();
    }

    /**
     * 逻辑删除商品
     */
    @Transactional
    public Result<?> deleteProduct(Integer productId) {
        Product product = this.getById(productId);
        if (product == null || product.getIsDeleted() == 1) {
            return Result.error("商品不存在");
        }
        product.setIsDeleted(1);
        product.setStatus("off_shelf");
        product.setUpdateTime(new Date());
        this.updateById(product);
        return Result.success("商品已删除");
    }

    /**
     * 上架/下架商品
     */
    public Result<?> updateProductStatus(Integer productId, String status) {
        Product product = this.getById(productId);
        if (product == null || product.getIsDeleted() == 1) {
            return Result.error("商品不存在");
        }
        if (!"active".equals(status) && !"off_shelf".equals(status)) {
            return Result.error("无效状态值，应为 active 或 off_shelf");
        }
        product.setStatus(status);
        product.setUpdateTime(new Date());
        this.updateById(product);
        return Result.success();
    }

    /**
     * 批量上下架
     */
    @Transactional(rollbackFor = Exception.class)
    public Result<?> batchUpdateStatus(List<Integer> productIds, String status) {
        for (Integer id : productIds) {
            updateProductStatus(id, status);
        }
        return Result.success("批量操作完成，共处理 " + productIds.size() + " 件");
    }

    /**
     * 低库存预警商品列表
     */
    public Result<?> getLowStockProducts(Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getIsDeleted, 0);
        // stock <= stock_warning
        wrapper.apply("STOCK <= STOCK_WARNING");
        wrapper.orderByAsc(Product::getStock);
        Page<Product> page = new Page<>(pageNum, pageSize);
        return Result.success(this.page(page, wrapper));
    }

    /**
     * 手动调整库存（直接设置值）✅ 必须写流水，不能是死数据
     */
    @Transactional
    public Result<?> updateStock(Integer productId, Integer newStock, Integer operatorId, String remark) {
        Product product = this.getById(productId);
        if (product == null || product.getIsDeleted() == 1) {
            return Result.error("商品不存在");
        }
        int oldStock = product.getStock() != null ? product.getStock() : 0;
        int changeAmount = newStock - oldStock;
        product.setStock(newStock);
        product.setUpdateTime(new Date());
        this.updateById(product);

        // ✅ 写库存流水，变动原因为人工调整
        InventoryLog log = new InventoryLog();
        log.setProductId(productId);
        log.setLogType("MANUAL");
        log.setChangeAmount(changeAmount);
        log.setBalanceAfter(newStock);
        log.setOperatorId(operatorId);
        log.setRemark(remark != null ? remark : "手动调整库存");
        log.setCreateTime(new Date());
        log.setLogId(inventoryLogMapper.getNextId());
        inventoryLogMapper.insert(log);

        return Result.success();
    }

    /**
     * 手动调整 SKU 库存（✅ 同步更新主表汇总库存，写流水）
     */
    @Transactional
    public Result<?> updateSkuStock(Integer skuId, Integer newStock, Integer operatorId, String remark) {
        ProductSku sku = productSkuMapper.selectById(skuId);
        if (sku == null) return Result.error("SKU 不存在");

        int oldSkuStock = sku.getStock() != null ? sku.getStock() : 0;
        int changeAmount = newStock - oldSkuStock;
        sku.setStock(newStock);
        productSkuMapper.updateById(sku);

        // 同步主表库存（汇总所有 SKU 库存）
        LambdaQueryWrapper<ProductSku> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductSku::getProductId, sku.getProductId());
        List<ProductSku> allSkus = productSkuMapper.selectList(wrapper);
        int totalStock = allSkus.stream().mapToInt(s -> s.getStock() != null ? s.getStock() : 0).sum();
        Product product = this.getById(sku.getProductId());
        if (product != null) {
            product.setStock(totalStock);
            product.setUpdateTime(new Date());
            this.updateById(product);
        }

        // 写库存流水
        InventoryLog log = new InventoryLog();
        log.setProductId(sku.getProductId());
        log.setSkuId(skuId);
        log.setLogType("MANUAL");
        log.setChangeAmount(changeAmount);
        log.setBalanceAfter(newStock);
        log.setOperatorId(operatorId);
        log.setRemark(remark != null ? remark : "手动调整 SKU 库存");
        log.setCreateTime(new Date());
        log.setLogId(inventoryLogMapper.getNextId());
        inventoryLogMapper.insert(log);

        return Result.success();
    }

    // ==================== 分类管理 ====================

    /**
     * 获取分类树（两级）
     */
    public Result<?> getCategoryTree() {
        List<Category> all = categoryMapper.selectList(null);
        // 找出所有一级分类（parentId为null或0）
        List<Category> roots = all.stream()
                .filter(c -> c.getParentId() == null || c.getParentId() == 0)
                .sorted((a, b) -> {
                    int sa = a.getSortOrder() != null ? a.getSortOrder() : 0;
                    int sb = b.getSortOrder() != null ? b.getSortOrder() : 0;
                    return sa - sb;
                })
                .collect(Collectors.toList());
        // 为每个一级分类挂上子分类
        for (Category root : roots) {
            List<Category> children = all.stream()
                    .filter(c -> root.getCategoryId().equals(c.getParentId()))
                    .sorted((a, b) -> {
                        int sa = a.getSortOrder() != null ? a.getSortOrder() : 0;
                        int sb = b.getSortOrder() != null ? b.getSortOrder() : 0;
                        return sa - sb;
                    })
                    .collect(Collectors.toList());
            root.setChildren(children);
        }
        return Result.success(roots);
    }

    /**
     * 获取扁平分类列表（后台下拉选择用）
     */
    public Result<?> getCategories() {
        List<Category> list = categoryMapper.selectList(null);
        return Result.success(list);
    }

    public Result<?> addCategory(Category category) {
        categoryMapper.insert(category);
        return Result.success(category.getCategoryId());
    }

    public Result<?> updateCategory(Category category) {
        categoryMapper.updateById(category);
        return Result.success();
    }

    public Result<?> deleteCategory(Integer categoryId) {
        // 检查是否有商品使用此分类
        LambdaQueryWrapper<Product> check = new LambdaQueryWrapper<>();
        check.eq(Product::getCategoryId, categoryId);
        check.eq(Product::getIsDeleted, 0);
        if (this.count(check) > 0) {
            return Result.error("该分类下还有商品，不能删除");
        }
        categoryMapper.deleteById(categoryId);
        return Result.success();
    }

    // ==================== SKU管理 ====================

    public Result<?> getProductSkus(Integer productId) {
        LambdaQueryWrapper<ProductSku> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductSku::getProductId, productId);
        return Result.success(productSkuMapper.selectList(wrapper));
    }

    @Transactional
    public Result<?> saveProductSku(ProductSku sku) {
        Product product = null;
        if (sku.getSkuId() == null) {
            productSkuMapper.insert(sku);
        } else {
            productSkuMapper.updateById(sku);
        }
        // 同步主表汇总库存
        if (sku.getProductId() != null) {
            product = productMapper.selectById(sku.getProductId());
        }
        if (product != null) {
            LambdaQueryWrapper<ProductSku> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ProductSku::getProductId, sku.getProductId());
            List<ProductSku> allSkus = productSkuMapper.selectList(wrapper);
            int totalStock = allSkus.stream().mapToInt(s -> s.getStock() != null ? s.getStock() : 0).sum();
            product.setStock(totalStock);
            product.setUpdateTime(new Date());
            productMapper.updateById(product);
        }
        return Result.success();
    }

    @Transactional
    public Result<?> saveProductSkus(Integer productId, List<ProductSku> skus) {
        if (skus == null || skus.isEmpty()) return Result.success();
        for (ProductSku sku : skus) {
            sku.setProductId(productId);
            if (sku.getSkuId() == null) {
                sku.setSkuId(productSkuMapper.getNextId());
                productSkuMapper.insert(sku);
            } else {
                productSkuMapper.updateById(sku);
            }
        }
        // 同步主表汇总库存
        LambdaQueryWrapper<ProductSku> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductSku::getProductId, productId);
        List<ProductSku> allSkus = productSkuMapper.selectList(wrapper);
        int totalStock = allSkus.stream().mapToInt(s -> s.getStock() != null ? s.getStock() : 0).sum();
        Product product = productMapper.selectById(productId);
        if (product != null) {
            product.setStock(totalStock);
            product.setUpdateTime(new Date());
            productMapper.updateById(product);
        }
        return Result.success();
    }

    public Result<?> deleteProductSku(Integer skuId) {
        productSkuMapper.deleteById(skuId);
        return Result.success();
    }
}
