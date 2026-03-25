package com.supermarket.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.supermarket.common.BusinessException;
import com.supermarket.common.Result;
import com.supermarket.entity.*;
import com.supermarket.mapper.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class WarehouseService {

    @Autowired private ProductMapper productMapper;
    @Autowired private ProductSkuMapper productSkuMapper;
    @Autowired private PurchaseOrderMapper purchaseOrderMapper;
    @Autowired private PurchaseOrderItemMapper purchaseOrderItemMapper;
    @Autowired private InventoryLogMapper inventoryLogMapper;
    @Autowired private DamageRecordMapper damageRecordMapper;
    @Autowired private SupplierMapper supplierMapper;

    // ==================== 采购单管理 ====================

    /**
     * 采购单列表
     */
    public Result<?> getPurchaseOrders(String status, Integer pageNum, Integer pageSize) {
        Page<PurchaseOrder> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<PurchaseOrder> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(status)) wrapper.eq(PurchaseOrder::getStatus, status);
        wrapper.orderByDesc(PurchaseOrder::getCreateTime);
        purchaseOrderMapper.selectPage(page, wrapper);

        for (PurchaseOrder po : page.getRecords()) {
            fillPurchaseOrderInfo(po);
        }
        return Result.success(page);
    }

    /**
     * 采购单详情
     */
    public Result<?> getPurchaseOrderDetail(Integer poId) {
        PurchaseOrder po = purchaseOrderMapper.selectById(poId);
        if (po == null) throw new BusinessException(404, "采购单不存在");
        fillPurchaseOrderInfo(po);
        return Result.success(po);
    }

    /**
     * 创建采购单（草稿状态）
     * body: { supplierId, expectedDate, remark, items: [{productId, quantity, unitPrice}] }
     */
    @Transactional
    public Result<?> createPurchaseOrder(Map<String, Object> body, Integer userId) {
        ObjectMapper om = new ObjectMapper();
        PurchaseOrder order = om.convertValue(body.get("order"), PurchaseOrder.class);
        @SuppressWarnings("unchecked")
        List<PurchaseOrderItem> items = om.convertValue(body.get("items"),
            om.getTypeFactory().constructCollectionType(List.class, PurchaseOrderItem.class));

        if (items == null || items.isEmpty()) throw new BusinessException("采购明细不能为空");

        String poNo = "PO" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())
                    + String.format("%04d", new Random().nextInt(10000));
        order.setPoNo(poNo);
        order.setStatus("draft");
        order.setOperatorId(userId);
        order.setCreateTime(new Date());

        double total = items.stream()
            .mapToDouble(i -> (i.getUnitPrice() != null ? i.getUnitPrice() : 0)
                             * (i.getOrderQuantity() != null ? i.getOrderQuantity() : 0))
            .sum();
        order.setTotalAmount(total);
        purchaseOrderMapper.insert(order);

        for (PurchaseOrderItem item : items) {
            item.setPoId(order.getPoId());
            item.setArrivedQuantity(0);
            purchaseOrderItemMapper.insert(item);
        }

        return Result.success(order.getPoId());
    }

    /**
     * 审批采购单（草稿 -> 已审批）
     */
    @Transactional
    public Result<?> approvePurchaseOrder(Integer poId) {
        PurchaseOrder po = purchaseOrderMapper.selectById(poId);
        if (po == null) throw new BusinessException(404, "采购单不存在");
        if (!"draft".equals(po.getStatus())) throw new BusinessException("只有草稿状态才能审批");
        po.setStatus("approved");
        purchaseOrderMapper.updateById(po);
        return Result.success("审批通过");
    }

    /**
     * 取消采购单（草稿 -> 已取消）
     */
    @Transactional
    public Result<?> cancelPurchaseOrder(Integer poId) {
        PurchaseOrder po = purchaseOrderMapper.selectById(poId);
        if (po == null) throw new BusinessException(404, "采购单不存在");
        if (!"draft".equals(po.getStatus())) throw new BusinessException("只有草稿状态才能取消");
        po.setStatus("cancelled");
        purchaseOrderMapper.updateById(po);
        return Result.success("采购单已取消");
    }

    /**
     * 部分到货入库
     * 支持部分收货，每次入库写库存流水
     * body: [{ itemId, arrivedQuantity }]
     */
    @Transactional
    public Result<?> receivePurchaseOrder(Integer poId, Map<String, Object> body, Integer operatorId) {
        PurchaseOrder po = purchaseOrderMapper.selectById(poId);
        if (po == null) throw new BusinessException(404, "采购单不存在");
        if (!"approved".equals(po.getStatus())) throw new BusinessException("只有已审批的采购单才能收货");

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> arrivals = (List<Map<String, Object>>) body.get("arrivals");
        if (arrivals == null || arrivals.isEmpty()) throw new BusinessException("到货数据不能为空");

        int totalArrived = 0;
        for (Map<String, Object> arrival : arrivals) {
            Integer itemId = (Integer) arrival.get("itemId");
            Integer arrivedQty = ((Number) arrival.get("arrivedQuantity")).intValue();

            PurchaseOrderItem item = purchaseOrderItemMapper.selectById(itemId);
            if (item == null || !item.getPoId().equals(poId)) continue;
            if (arrivedQty <= 0) continue;

            // 更新已到货数量
            int newArrived = (item.getArrivedQuantity() != null ? item.getArrivedQuantity() : 0) + arrivedQty;
            item.setArrivedQuantity(newArrived);
            purchaseOrderItemMapper.updateById(item);

            // 入库：加库存 + 写流水
            Product product = productMapper.selectById(item.getProductId());
            if (product != null) {
                int before = product.getStock() != null ? product.getStock() : 0;
                product.setStock(before + arrivedQty);
                productMapper.updateById(product);

                writeInventoryLog(item.getProductId(), null, "PURCHASE_IN",
                        arrivedQty, product.getStock(), poId,
                        "采购入库，单号：" + po.getPoNo() + "，品名：" + product.getProductName(), operatorId);
            }
            totalArrived++;
        }

        // 判断是否全部到货
        boolean allReceived = checkAllReceived(poId);
        if (allReceived) {
            po.setStatus("received");
            po.setCompleteTime(new Date());
        }
        purchaseOrderMapper.updateById(po);

        return Result.success("已入库 " + totalArrived + " 个商品，" + (allReceived ? "采购单已完成" : "继续等待剩余商品"));
    }

    // ==================== 报损管理 ====================

    /**
     * 报损记录列表
     */
    public Result<?> getDamageRecords(Integer pageNum, Integer pageSize) {
        Page<DamageRecord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<DamageRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(DamageRecord::getCreateTime);
        damageRecordMapper.selectPage(page, wrapper);

        for (DamageRecord record : page.getRecords()) {
            Product p = productMapper.selectById(record.getProductId());
            if (p != null) record.setProductName(p.getProductName());
        }
        return Result.success(page);
    }

    /**
     * 创建报损记录
     * 直接从库存扣减 + 写报损流水
     * body: { productId, quantity, reason }
     */
    @Transactional
    public Result<?> createDamageRecord(Map<String, Object> body, Integer userId) {
        Integer productId = (Integer) body.get("productId");
        Integer quantity = ((Number) body.get("quantity")).intValue();
        String reason = (String) body.get("reason");

        if (productId == null || quantity == null || quantity <= 0) {
            throw new BusinessException("商品和数量不能为空");
        }

        Product product = productMapper.selectById(productId);
        if (product == null) throw new BusinessException(404, "商品不存在");

        int before = product.getStock() != null ? product.getStock() : 0;
        if (before < quantity) throw new BusinessException("库存不足，当前库存：" + before);

        // 扣减库存
        product.setStock(before - quantity);
        productMapper.updateById(product);

        // 记录报损
        DamageRecord record = new DamageRecord();
        record.setProductId(productId);
        record.setQuantity(quantity);
        record.setReason(reason != null ? reason : "报损");
        record.setOperatorId(userId);
        record.setCreateTime(new Date());
        damageRecordMapper.insert(record);

        // 写库存流水
        writeInventoryLog(productId, null, "DAMAGE",
                -quantity, product.getStock(), record.getDamageId(),
                "报损出库：" + (reason != null ? reason : ""), userId);

        Map<String, Object> data = new HashMap<>();
        data.put("damageId", record.getDamageId());
        data.put("remainingStock", product.getStock());
        return Result.success(data);
    }

    // ==================== 库存流水 ====================

    /**
     * 库存流水查询
     */
    public Result<?> getInventoryLogs(Integer productId, String logType, Integer pageNum, Integer pageSize) {
        Page<InventoryLog> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<InventoryLog> wrapper = new LambdaQueryWrapper<>();
        if (productId != null) wrapper.eq(InventoryLog::getProductId, productId);
        if (StringUtils.hasText(logType)) wrapper.eq(InventoryLog::getLogType, logType);
        wrapper.orderByDesc(InventoryLog::getCreateTime);
        inventoryLogMapper.selectPage(page, wrapper);

        for (InventoryLog log : page.getRecords()) {
            Product p = productMapper.selectById(log.getProductId());
            if (p != null) log.setProductName(p.getProductName());
        }
        return Result.success(page);
    }

    /**
     * 库存查询（商品库存列表）
     */
    public Result<?> getInventoryList(Integer pageNum, Integer pageSize, String keyword) {
        Page<Product> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getIsDeleted, 0);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Product::getProductName, keyword)
                    .or().like(Product::getBarcode, keyword));
        }
        wrapper.orderByAsc(Product::getProductName);
        productMapper.selectPage(page, wrapper);

        // 只返回关键字段，避免传输冗余
        for (Product p : page.getRecords()) {
            p.setCategoryId(null);
            p.setCoverImage(null);
            p.setImages(null);
            p.setDescription(null);
            p.setBarcode(null);
            p.setCreateTime(null);
        }
        return Result.success(page);
    }

    /**
     * 手动调整库存
     * newStock 为调整后的目标值（非变动值）
     */
    @Transactional
    public Result<?> adjustInventory(Integer productId, Integer newStock, String remark, Integer operatorId) {
        Product product = productMapper.selectById(productId);
        if (product == null) throw new BusinessException(404, "商品不存在");

        int before = product.getStock() != null ? product.getStock() : 0;
        int change = newStock - before;

        product.setStock(newStock);
        productMapper.updateById(product);

        writeInventoryLog(productId, null, "MANUAL",
                change, newStock, null,
                remark != null ? remark : "手动调整库存", operatorId);

        Map<String, Object> data = new HashMap<>();
        data.put("before", before);
        data.put("after", newStock);
        data.put("change", change);
        return Result.success(data);
    }

    /**
     * 低库存预警
     */
    public Result<?> getLowStockProducts(Integer pageNum, Integer pageSize) {
        Page<Product> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getIsDeleted, 0)
                .apply("STOCK <= STOCK_WARNING")
                .orderByAsc(Product::getStock);
        productMapper.selectPage(page, wrapper);
        return Result.success(page);
    }

    // ==================== 私有辅助方法 ====================

    private void fillPurchaseOrderInfo(PurchaseOrder po) {
        if (po.getSupplierId() != null) {
            Supplier s = supplierMapper.selectById(po.getSupplierId());
            if (s != null) {
                po.setSupplierName(s.getSupplierName());
            }
        }
        LambdaQueryWrapper<PurchaseOrderItem> iw = new LambdaQueryWrapper<>();
        iw.eq(PurchaseOrderItem::getPoId, po.getPoId());
        List<PurchaseOrderItem> items = purchaseOrderItemMapper.selectList(iw);
        for (PurchaseOrderItem item : items) {
            Product p = productMapper.selectById(item.getProductId());
            if (p != null) item.setProductName(p.getProductName());
        }
        po.setItems(items);
    }

    private boolean checkAllReceived(Integer poId) {
        LambdaQueryWrapper<PurchaseOrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PurchaseOrderItem::getPoId, poId);
        List<PurchaseOrderItem> items = purchaseOrderItemMapper.selectList(wrapper);
        for (PurchaseOrderItem item : items) {
            int ordered = item.getOrderQuantity() != null ? item.getOrderQuantity() : 0;
            int arrived = item.getArrivedQuantity() != null ? item.getArrivedQuantity() : 0;
            if (arrived < ordered) return false;
        }
        return true;
    }

    private void writeInventoryLog(Integer productId, Integer skuId, String logType,
                                   int changeAmount, int balanceAfter,
                                   Integer refId, String remark, Integer operatorId) {
        InventoryLog log = new InventoryLog();
        log.setProductId(productId);
        log.setSkuId(skuId);
        log.setLogType(logType);
        log.setChangeAmount(changeAmount);
        log.setBalanceAfter(balanceAfter);
        log.setBeforeStock(balanceAfter - changeAmount);
        log.setAfterStock(balanceAfter);
        log.setRefId(refId);
        log.setRemark(remark);
        log.setOperatorId(operatorId);
        log.setCreateTime(new Date());
        inventoryLogMapper.insert(log);
    }
}
