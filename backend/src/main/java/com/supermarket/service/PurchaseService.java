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

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class PurchaseService extends ServiceImpl<PurchaseOrderMapper, PurchaseOrder> {

    @Autowired private PurchaseOrderItemMapper itemMapper;
    @Autowired private SupplierMapper supplierMapper;
    @Autowired private ProductMapper productMapper;
    @Autowired private InventoryLogMapper inventoryLogMapper;

    /** 采购单列表（分页+筛选） */
    public Result<?> list(String status, Integer supplierId, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<PurchaseOrder> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) wrapper.eq(PurchaseOrder::getStatus, status);
        if (supplierId != null) wrapper.eq(PurchaseOrder::getSupplierId, supplierId);
        wrapper.orderByDesc(PurchaseOrder::getCreateTime);
        Page<PurchaseOrder> page = this.page(new Page<>(pageNum, pageSize), wrapper);
        for (PurchaseOrder po : page.getRecords()) {
            fillDetail(po);
        }
        return Result.success(page);
    }

    /** 采购单详情 */
    public Result<?> detail(Integer poId) {
        PurchaseOrder po = this.getById(poId);
        if (po == null) return Result.error("采购单不存在");
        fillDetail(po);
        return Result.success(po);
    }

    private void fillDetail(PurchaseOrder po) {
        Supplier s = supplierMapper.selectById(po.getSupplierId());
        if (s != null) po.setSupplierName(s.getSupplierName());
        LambdaQueryWrapper<PurchaseOrderItem> iw = new LambdaQueryWrapper<>();
        iw.eq(PurchaseOrderItem::getPoId, po.getPoId());
        List<PurchaseOrderItem> items = itemMapper.selectList(iw);
        for (PurchaseOrderItem item : items) {
            Product p = productMapper.selectById(item.getProductId());
            if (p != null) item.setProductName(p.getProductName());
        }
        po.setItems(items);
    }

    /** 创建采购申请 */
    @Transactional
    public Result<?> create(Integer operatorId, Integer supplierId, String expectedDate,
                             String remark, List<Map<String, Object>> items) {
        if (items == null || items.isEmpty()) return Result.error("请添加采购商品");
        Supplier supplier = supplierMapper.selectById(supplierId);
        if (supplier == null) return Result.error("供应商不存在");

        PurchaseOrder po = new PurchaseOrder();
        po.setOperatorId(operatorId);
        po.setSupplierId(supplierId);
        po.setStatus("draft");
        po.setRemark(remark);
        po.setCreateTime(new Date());
        po.setPoNo("PO" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
        if (expectedDate != null) {
            try { po.setExpectedDate(new SimpleDateFormat("yyyy-MM-dd").parse(expectedDate)); } catch (Exception ignored) {}
        }
        double total = 0;
        this.save(po);
        for (Map<String, Object> raw : items) {
            PurchaseOrderItem item = new PurchaseOrderItem();
            item.setPoId(po.getPoId());
            item.setProductId((Integer) raw.get("productId"));
            item.setOrderQuantity((Integer) raw.get("quantity"));
            double price = ((Number) raw.get("unitPrice")).doubleValue();
            item.setUnitPrice(price);
            item.setArrivedQuantity(0);
            itemMapper.insert(item);
            total += price * item.getOrderQuantity();
        }
        po.setTotalAmount(total);
        this.updateById(po);
        return Result.success(po);
    }

    /** 审批通过 */
    @Transactional
    public Result<?> approve(Integer poId) {
        PurchaseOrder po = this.getById(poId);
        if (po == null) return Result.error("采购单不存在");
        if (!"draft".equals(po.getStatus())) return Result.error("只有草稿状态可审批");
        po.setStatus("approved");
        this.updateById(po);
        return Result.success(po);
    }

    /** 确认到货入库 */
    @Transactional
    public Result<?> receive(Integer poId, Integer operatorId, List<Map<String, Object>> receivedItems) {
        PurchaseOrder po = this.getById(poId);
        if (po == null) return Result.error("采购单不存在");
        if (!"approved".equals(po.getStatus())) return Result.error("只有已审批状态可入库");

        for (Map<String, Object> raw : receivedItems) {
            Integer itemId = (Integer) raw.get("itemId");
            int qty = ((Number) raw.get("receivedQty")).intValue();
            PurchaseOrderItem item = itemMapper.selectById(itemId);
            if (item == null || !item.getPoId().equals(poId)) continue;
            item.setArrivedQuantity(qty);
            itemMapper.updateById(item);
            // 增加库存
            Product product = productMapper.selectById(item.getProductId());
            if (product != null) {
                product.setStock(product.getStock() + qty);
                productMapper.updateById(product);
                // 记录库存流水
                InventoryLog log = new InventoryLog();
                log.setProductId(item.getProductId());
                log.setChangeAmount(qty);
                log.setLogType("purchase_in");
                log.setRemark("采购入库 " + po.getPoNo());
                log.setOperatorId(operatorId);
                log.setCreateTime(new Date());
                log.setLogId(inventoryLogMapper.getNextId());
                inventoryLogMapper.insert(log);
            }
        }
        po.setStatus("received");
        po.setCompleteTime(new Date());
        this.updateById(po);
        return Result.success(po);
    }

    /** 取消采购单 */
    @Transactional
    public Result<?> cancel(Integer poId) {
        PurchaseOrder po = this.getById(poId);
        if (po == null) return Result.error("采购单不存在");
        if (!"draft".equals(po.getStatus())) return Result.error("只有草稿状态可取消");
        po.setStatus("cancelled");
        this.updateById(po);
        return Result.success(null);
    }
}
