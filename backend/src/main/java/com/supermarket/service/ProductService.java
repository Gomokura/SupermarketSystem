package com.supermarket.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.supermarket.common.Result;
import com.supermarket.entity.Product;
import com.supermarket.entity.Category;
import com.supermarket.mapper.ProductMapper;
import com.supermarket.mapper.CategoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService extends ServiceImpl<ProductMapper, Product> {

    @Autowired
    private CategoryMapper categoryMapper;

    public Result<?> getProductList(Integer categoryId, String keyword, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        if (categoryId != null && categoryId > 0) {
            wrapper.eq(Product::getCategoryId, categoryId);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Product::getProductName, keyword);
        }
        wrapper.eq(Product::getStatus, "上架");
        wrapper.orderByDesc(Product::getCreateTime);

        Page<Product> page = new Page<>(pageNum, pageSize);
        Page<Product> result = this.page(page, wrapper);

        return Result.success(result);
    }

    public Result<?> getProductById(Integer productId) {
        Product product = this.getById(productId);
        if (product == null) {
            return Result.error("商品不存在");
        }
        return Result.success(product);
    }

    public Result<?> addProduct(Product product) {
        this.save(product);
        return Result.success();
    }

    public Result<?> updateProduct(Product product) {
        this.updateById(product);
        return Result.success();
    }

    public Result<?> deleteProduct(Integer productId) {
        this.removeById(productId);
        return Result.success();
    }

    public Result<?> getCategories() {
        List<Category> list = categoryMapper.selectList(null);
        return Result.success(list);
    }

    public Result<?> addCategory(Category category) {
        categoryMapper.insert(category);
        return Result.success();
    }

    public Result<?> updateCategory(Category category) {
        categoryMapper.updateById(category);
        return Result.success();
    }

    public Result<?> deleteCategory(Integer categoryId) {
        categoryMapper.deleteById(categoryId);
        return Result.success();
    }
}
