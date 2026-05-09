package com.supermarket.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.supermarket.common.Result;
import com.supermarket.entity.*;
import com.supermarket.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class WarehouseService extends ServiceImpl<DamageRecordMapper, DamageRecord> {

    @Autowired private ProductMapper productMapper;
    @Autowired private InventoryLogMapper inventoryLogMapper;
    @Autowired private DamageRecordMapper damageRecordMapper;

    /** 报损登记 */
    @Transactional
    public Result<?> reportDamage(Integer operatorId, Integer productId, Integer quantity, String reason) {
        Product product = productMapper.selectById(productId);
        if (product == null) return Result.error("商品不存在");
        if (product.getStock() < quantity) return Result.error("库存不足，无法报损");

        product.setStock(product.getStock() - quantity);
        productMapper.updateById(product);

        DamageRecord record = new DamageRecord();
        record.setProductId(productId);
        record.setQuantity(quantity);
        record.setReason(reason);
        record.setOperatorId(operatorId);
        record.setCreateTime(new Date());
        // 报损金额计算
        double unitCost = product.getCostPrice() != null ? product.getCostPrice() : 0;
        record.setUnitCost(unitCost);
        record.setTotalCost(unitCost * quantity);
        record.setDamageId(damageRecordMapper.getNextId());
        this.save(record);

        InventoryLog log = new InventoryLog();
        log.setProductId(productId);
        log.setChangeAmount(-quantity);
        log.setLogType("DAMAGE");
        log.setBalanceAfter(product.getStock());
        log.setRemark("报损：" + reason);
        log.setOperatorId(operatorId);
        log.setCreateTime(new Date());
        log.setLogId(inventoryLogMapper.getNextId());
        inventoryLogMapper.insert(log);

        return Result.success(record);
    }

    /** 报损记录列表（分页） */
    public Result<?> damageList(Integer productId, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<DamageRecord> wrapper = new LambdaQueryWrapper<>();
        if (productId != null) wrapper.eq(DamageRecord::getProductId, productId);
        wrapper.orderByDesc(DamageRecord::getCreateTime);
        Page<DamageRecord> page = this.page(new Page<>(pageNum, pageSize), wrapper);
        for (DamageRecord r : page.getRecords()) {
            Product p = productMapper.selectById(r.getProductId());
            if (p != null) r.setProductName(p.getProductName());
        }
        return Result.success(page);
    }

    /** 库存总览 */
    public Result<?> inventoryOverview(Integer pageNum, Integer pageSize, String keyword) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Product::getProductName, keyword);
        }
        wrapper.orderByAsc(Product::getStock);
        Page<Product> page = productMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return Result.success(page);
    }

    /** 低库存预警列表 */
    public Result<?> lowStockList() {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.apply("stock <= stock_warning").orderByAsc(Product::getStock);
        List<Product> list = productMapper.selectList(wrapper);
        return Result.success(list);
    }

    /** 库存流水列表（分页） */
    public Result<?> inventoryLogs(Integer productId, String changeType,
                                    String startDate, String endDate,
                                    Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<InventoryLog> wrapper = new LambdaQueryWrapper<>();
        if (productId != null) wrapper.eq(InventoryLog::getProductId, productId);
        if (changeType != null && !changeType.isEmpty()) wrapper.eq(InventoryLog::getLogType, changeType);
        if (startDate != null && !startDate.isEmpty()) wrapper.ge(InventoryLog::getCreateTime, startDate);
        if (endDate != null && !endDate.isEmpty()) wrapper.le(InventoryLog::getCreateTime, endDate);
        wrapper.orderByDesc(InventoryLog::getCreateTime);
        Page<InventoryLog> page = inventoryLogMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return Result.success(page);
    }
}
