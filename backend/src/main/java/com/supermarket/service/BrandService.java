package com.supermarket.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.supermarket.common.Result;
import com.supermarket.entity.Brand;
import com.supermarket.entity.Product;
import com.supermarket.mapper.BrandMapper;
import com.supermarket.mapper.ProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BrandService extends ServiceImpl<BrandMapper, Brand> {

    @Autowired private ProductMapper productMapper;

    public Result<?> listAll(String status) {
        LambdaQueryWrapper<Brand> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) wrapper.eq(Brand::getStatus, status);
        wrapper.orderByAsc(Brand::getSortOrder);
        List<Brand> brands = this.list(wrapper);
        for (Brand b : brands) {
            LambdaQueryWrapper<Product> pw = new LambdaQueryWrapper<>();
            pw.eq(Product::getBrandId, b.getBrandId());
            b.setProductCount(productMapper.selectCount(pw).intValue());
        }
        return Result.success(brands);
    }

    public Result<?> create(Brand brand) {
        if (brand.getStatus() == null) brand.setStatus("active");
        if (brand.getSortOrder() == null) brand.setSortOrder(0);
        this.save(brand);
        return Result.success(brand);
    }

    public Result<?> update(Integer brandId, Brand brand) {
        Brand existing = this.getById(brandId);
        if (existing == null) return Result.error("品牌不存在");
        brand.setBrandId(brandId);
        this.updateById(brand);
        return Result.success(brand);
    }

    public Result<?> delete(Integer brandId) {
        LambdaQueryWrapper<Product> pw = new LambdaQueryWrapper<>();
        pw.eq(Product::getBrandId, brandId);
        if (productMapper.selectCount(pw) > 0L) return Result.error("该品牌下有商品，无法删除");
        this.removeById(brandId);
        return Result.success(null);
    }
}
