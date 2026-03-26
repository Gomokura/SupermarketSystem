package com.supermarket.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.supermarket.common.Result;
import com.supermarket.entity.Supplier;
import com.supermarket.mapper.SupplierMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SupplierService extends ServiceImpl<SupplierMapper, Supplier> {

    public Result<?> listAll(String status) {
        LambdaQueryWrapper<Supplier> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) wrapper.eq(Supplier::getStatus, status);
        wrapper.orderByAsc(Supplier::getSupplierName);
        List<Supplier> list = this.list(wrapper);
        return Result.success(list);
    }

    public Result<?> create(Supplier supplier) {
        if (supplier.getStatus() == null) supplier.setStatus("active");
        this.save(supplier);
        return Result.success(supplier);
    }

    public Result<?> update(Integer supplierId, Supplier supplier) {
        if (this.getById(supplierId) == null) return Result.error("供应商不存在");
        supplier.setSupplierId(supplierId);
        this.updateById(supplier);
        return Result.success(supplier);
    }

    public Result<?> delete(Integer supplierId) {
        if (this.getById(supplierId) == null) return Result.error("供应商不存在");
        this.removeById(supplierId);
        return Result.success(null);
    }

    public Result<?> detail(Integer supplierId) {
        Supplier s = this.getById(supplierId);
        if (s == null) return Result.error("供应商不存在");
        return Result.success(s);
    }
}
