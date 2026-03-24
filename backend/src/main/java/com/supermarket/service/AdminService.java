package com.supermarket.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.supermarket.common.BusinessException;
import com.supermarket.common.Result;
import com.supermarket.entity.*;
import com.supermarket.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;

@Service
public class AdminService {

    @Autowired private UserMapper userMapper;
    @Autowired private ProductMapper productMapper;
    @Autowired private OrderMapper orderMapper;
    @Autowired private OrderItemMapper orderItemMapper;
    @Autowired private DeliveryMapper deliveryMapper;
    @Autowired private CourierMapper courierMapper;
    @Autowired private InventoryLogMapper inventoryLogMapper;
    @Autowired private PurchaseOrderMapper purchaseOrderMapper;
    @Autowired private PurchaseOrderItemMapper purchaseOrderItemMapper;
    @Autowired private SupplierMapper supplierMapper;
    @Autowired private PromotionMapper promotionMapper;
    @Autowired private AuditLogMapper auditLogMapper;
    @Autowired private PaymentMapper paymentMapper;
    @Autowired private AdminMapper adminMapper;

    // ==================== 用户管理 ====================

    public Result<?> getUserList(Integer pageNum, Integer pageSize, String keyword) {
        Page<User> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(User::getUsername, keyword)
                   .or().like(User::getPhone, keyword)
                   .or().like(User::getNickname, keyword);
        }
        wrapper.orderByDesc(User::getCreateTime);
        userMapper.selectPage(page, wrapper);
        return Result.success(page);
    }

    public Result<?> updateUserStatus(Integer userId, String status) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException(404, "用户不存在");
        user.setStatus(status);
        userMapper.updateById(user);
        return Result.success();
    }

    public Result<?> getUserDetail(Integer userId) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException(404, "用户不存在");
        LambdaQueryWrapper<Order> ow = new LambdaQueryWrapper<>();
        ow.eq(Order::getUserId, userId).ne(Order::getStatus, "cancelled");
        Long orderCount = orderMapper.selectCount(ow);
        user.setOrderCount(orderCount.intValue());
        return Result.success(user);
    }

    // ==================== 统计首页 ====================

    public Result<?> getStatistics() {
        long userCount = userMapper.selectCount(null);
        long productCount = productMapper.selectCount(null);
        long orderCount = orderMapper.selectCount(null);

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date todayStart = cal.getTime();

        LambdaQueryWrapper<Order> todayWrapper = new LambdaQueryWrapper<>();
        todayWrapper.ge(Order::getCreateTime, todayStart);
        long todayOrder = orderMapper.selectCount(todayWrapper);

        List<Order> todayPaid = orderMapper.selectList(
            new LambdaQueryWrapper<Order>()
                .ge(Order::getCreateTime, todayStart)
                .in(Order::getStatus, "paid", "shipped", "completed")
        );
        double todayRevenue = todayPaid.stream()
            .mapToDouble(o -> o.getPayAmount() != null ? o.getPayAmount() : 0)
            .sum();

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("userCount", userCount);
        data.put("productCount", productCount);
        data.put("orderCount", orderCount);
        data.put("todayOrder", todayOrder);
        data.put("todayRevenue", Math.round(todayRevenue * 100.0) / 100.0);
        return Result.success(data);
    }

    // ==================== 库存管理 ====================

    @Transactional
    public Result<?> warehousing(Integer productId, Integer quantity, String remark, Integer operatorId) {
        Product product = productMapper.selectById(productId);
        if (product == null) throw new BusinessException(404, "商品不存在");
        int before = product.getStock() != null ? product.getStock() : 0;
        product.setStock(before + quantity);
        productMapper.updateById(product);

        InventoryLog log = new InventoryLog();
        log.setProductId(productId);
        log.setLogType("purchase_in");
        log.setChangeAmount(quantity);
        log.setBalanceAfter(product.getStock());
        log.setOperatorId(operatorId);
        log.setRemark(remark);
        log.setCreateTime(new Date());
        inventoryLogMapper.insert(log);
        return Result.success();
    }

    @Transactional
    public Result<?> outbound(Integer productId, Integer quantity, String remark, Integer operatorId) {
        Product product = productMapper.selectById(productId);
        if (product == null) throw new BusinessException(404, "商品不存在");
        int before = product.getStock() != null ? product.getStock() : 0;
        if (before < quantity) throw new BusinessException("库存不足，当前库存：" + before);
        product.setStock(before - quantity);
        productMapper.updateById(product);

        InventoryLog log = new InventoryLog();
        log.setProductId(productId);
        log.setLogType("damage");
        log.setChangeAmount(-quantity);
        log.setBalanceAfter(product.getStock());
        log.setOperatorId(operatorId);
        log.setRemark(remark);
        log.setCreateTime(new Date());
        inventoryLogMapper.insert(log);
        return Result.success();
    }

    public Result<?> getInventoryLogs(Integer pageNum, Integer pageSize,
                                       Integer productId, String logType) {
        Page<InventoryLog> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<InventoryLog> wrapper = new LambdaQueryWrapper<>();
        if (productId != null) wrapper.eq(InventoryLog::getProductId, productId);
        if (StringUtils.hasText(logType)) wrapper.eq(InventoryLog::getLogType, logType);
        wrapper.orderByDesc(InventoryLog::getCreateTime);
        inventoryLogMapper.selectPage(page, wrapper);

        page.getRecords().forEach(item -> {
            Product p = productMapper.selectById(item.getProductId());
            if (p != null) item.setProductName(p.getProductName());
        });
        return Result.success(page);
    }

    // ==================== 配送管理 ====================

    public Result<?> getDeliveryList(Integer pageNum, Integer pageSize, String status) {
        Page<Delivery> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Delivery> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(status)) wrapper.eq(Delivery::getStatus, status);
        wrapper.orderByDesc(Delivery::getDeliveryId);
        deliveryMapper.selectPage(page, wrapper);

        page.getRecords().forEach(d -> {
            if (d.getCourierId() != null) {
                Courier c = courierMapper.selectById(d.getCourierId());
                if (c != null) {
                    d.setCourierName(c.getCourierName());
                    d.setCourierPhone(c.getPhone());
                }
            }
        });
        return Result.success(page);
    }

    @Transactional
    public Result<?> assignCourier(Integer deliveryId, Integer courierId) {
        Delivery delivery = deliveryMapper.selectById(deliveryId);
        if (delivery == null) throw new BusinessException(404, "配送记录不存在");
        delivery.setCourierId(courierId);
        delivery.setStatus("picking");
        delivery.setDispatchTime(new Date());
        deliveryMapper.updateById(delivery);
        return Result.success();
    }

    @Transactional
    public Result<?> updateDeliveryStatus(Integer deliveryId, String status) {
        Delivery delivery = deliveryMapper.selectById(deliveryId);
        if (delivery == null) throw new BusinessException(404, "配送记录不存在");
        delivery.setStatus(status);
        if ("done".equals(status)) delivery.setDoneTime(new Date());
        deliveryMapper.updateById(delivery);
        return Result.success();
    }

    // ==================== 促销管理 ====================

    public Result<?> getPromotionList(Integer pageNum, Integer pageSize) {
        Page<Promotion> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Promotion> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Promotion::getPromotionId);
        promotionMapper.selectPage(page, wrapper);
        return Result.success(page);
    }

    @Transactional
    public Result<?> createPromotion(Promotion promotion) {
        promotionMapper.insert(promotion);
        return Result.success();
    }

    @Transactional
    public Result<?> updatePromotion(Promotion promotion) {
        promotionMapper.updateById(promotion);
        return Result.success();
    }

    public Result<?> deletePromotion(Integer promotionId) {
        promotionMapper.deleteById(promotionId);
        return Result.success();
    }

    // ==================== 供应商管理 ====================

    public Result<?> getSupplierList(String keyword) {
        LambdaQueryWrapper<Supplier> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Supplier::getSupplierName, keyword)
                   .or().like(Supplier::getContact, keyword);
        }
        wrapper.orderByAsc(Supplier::getSupplierId);
        List<Supplier> list = supplierMapper.selectList(wrapper);
        return Result.success(list);
    }

    @Transactional
    public Result<?> createSupplier(Supplier supplier) {
        supplier.setStatus("active");
        supplierMapper.insert(supplier);
        return Result.success();
    }

    @Transactional
    public Result<?> updateSupplier(Supplier supplier) {
        supplierMapper.updateById(supplier);
        return Result.success();
    }

    public Result<?> deleteSupplier(Integer supplierId) {
        supplierMapper.deleteById(supplierId);
        return Result.success();
    }

    // ==================== 采购单管理 ====================

    public Result<?> getPurchaseOrders(Integer pageNum, Integer pageSize, String status) {
        Page<PurchaseOrder> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<PurchaseOrder> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(status)) wrapper.eq(PurchaseOrder::getStatus, status);
        wrapper.orderByDesc(PurchaseOrder::getCreateTime);
        purchaseOrderMapper.selectPage(page, wrapper);

        page.getRecords().forEach(po -> {
            if (po.getSupplierId() != null) {
                Supplier s = supplierMapper.selectById(po.getSupplierId());
                if (s != null) po.setSupplierName(s.getSupplierName());
            }
            LambdaQueryWrapper<PurchaseOrderItem> iw = new LambdaQueryWrapper<>();
            iw.eq(PurchaseOrderItem::getPoId, po.getPoId());
            List<PurchaseOrderItem> items = purchaseOrderItemMapper.selectList(iw);
            items.forEach(item -> {
                Product p = productMapper.selectById(item.getProductId());
                if (p != null) item.setProductName(p.getProductName());
            });
            po.setItems(items);
        });
        return Result.success(page);
    }

    @Transactional
    public Result<?> createPurchaseOrder(PurchaseOrder order, List<PurchaseOrderItem> items) {
        order.setPoNo("PO" + System.currentTimeMillis());
        order.setStatus("draft");
        order.setCreateTime(new Date());
        double total = items.stream()
            .mapToDouble(i -> (i.getUnitPrice() != null ? i.getUnitPrice() : 0)
                             * (i.getOrderQuantity() != null ? i.getOrderQuantity() : 0))
            .sum();
        order.setTotalAmount(total);
        purchaseOrderMapper.insert(order);
        for (PurchaseOrderItem item : items) {
            item.setPoId(order.getPoId());
            purchaseOrderItemMapper.insert(item);
        }
        return Result.success(order.getPoId());
    }

    @Transactional
    public Result<?> approvePurchaseOrder(Integer poId) {
        PurchaseOrder po = purchaseOrderMapper.selectById(poId);
        if (po == null) throw new BusinessException(404, "采购单不存在");
        if (!"draft".equals(po.getStatus())) throw new BusinessException("只有草稿状态才能审批");
        po.setStatus("approved");
        purchaseOrderMapper.updateById(po);
        return Result.success();
    }

    @Transactional
    public Result<?> receivePurchaseOrder(Integer poId, List<PurchaseOrderItem> arrivals, Integer operatorId) {
        PurchaseOrder po = purchaseOrderMapper.selectById(poId);
        if (po == null) throw new BusinessException(404, "采购单不存在");
        if (!"approved".equals(po.getStatus())) throw new BusinessException("只有已审批的采购单才能收货");

        for (PurchaseOrderItem arrival : arrivals) {
            PurchaseOrderItem item = purchaseOrderItemMapper.selectById(arrival.getItemId());
            if (item == null) continue;
            item.setArrivedQuantity(arrival.getArrivedQuantity());
            purchaseOrderItemMapper.updateById(item);

            if (arrival.getArrivedQuantity() != null && arrival.getArrivedQuantity() > 0) {
                Product product = productMapper.selectById(item.getProductId());
                if (product != null) {
                    int before = product.getStock() != null ? product.getStock() : 0;
                    product.setStock(before + arrival.getArrivedQuantity());
                    productMapper.updateById(product);

                    InventoryLog log = new InventoryLog();
                    log.setProductId(item.getProductId());
                    log.setLogType("purchase_in");
                    log.setChangeAmount(arrival.getArrivedQuantity());
                    log.setBalanceAfter(product.getStock());
                    log.setRefId(poId);
                    log.setOperatorId(operatorId);
                    log.setRemark("采购入库，单号：" + po.getPoNo());
                    log.setCreateTime(new Date());
                    inventoryLogMapper.insert(log);
                }
            }
        }
        po.setStatus("received");
        po.setCompleteTime(new Date());
        purchaseOrderMapper.updateById(po);
        return Result.success();
    }

    // ==================== 财务数据 ====================

    public Result<?> getFinanceData() {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Order::getStatus, "paid", "shipped", "completed");
        List<Order> orders = orderMapper.selectList(wrapper);

        double totalRevenue = orders.stream()
            .mapToDouble(o -> o.getPayAmount() != null ? o.getPayAmount() : 0)
            .sum();
        long orderCount = orders.size();

        Map<String, Double> payMethodMap = new LinkedHashMap<>();
        for (Order o : orders) {
            String method = o.getPayMethod() != null ? o.getPayMethod() : "unknown";
            payMethodMap.merge(method, o.getPayAmount() != null ? o.getPayAmount() : 0, Double::sum);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("totalRevenue", Math.round(totalRevenue * 100.0) / 100.0);
        data.put("orderCount", orderCount);
        data.put("avgOrderAmount", orderCount > 0
            ? Math.round(totalRevenue / orderCount * 100.0) / 100.0 : 0);
        data.put("payMethodSummary", payMethodMap);
        return Result.success(data);
    }

    // ==================== 审计日志 ====================

    public Result<?> getAuditLogs(Integer pageNum, Integer pageSize, String module) {
        Page<AuditLog> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<AuditLog> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(module)) wrapper.eq(AuditLog::getModule, module);
        wrapper.orderByDesc(AuditLog::getLogId);
        auditLogMapper.selectPage(page, wrapper);
        return Result.success(page);
    }

    // ==================== 骑手管理 ====================

    public Result<?> getCourierList(Integer pageNum, Integer pageSize) {
        Page<Courier> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Courier> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Courier::getCourierId);
        courierMapper.selectPage(page, wrapper);
        return Result.success(page);
    }

    @Transactional
    public Result<?> updateCourierStatus(Integer courierId, Integer isDisabled) {
        Courier courier = courierMapper.selectById(courierId);
        if (courier == null) throw new BusinessException(404, "骑手不存在");
        courier.setIsDisabled(isDisabled);
        courierMapper.updateById(courier);
        return Result.success();
    }
}
