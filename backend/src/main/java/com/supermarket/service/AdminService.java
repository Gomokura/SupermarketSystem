package com.supermarket.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.supermarket.common.Result;
import com.supermarket.entity.*;
import com.supermarket.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminService {

    @Autowired private UserMapper userMapper;
    @Autowired private ProductMapper productMapper;
    @Autowired private OrderMapper orderMapper;
    @Autowired private DeliveryMapper deliveryMapper;
    @Autowired private InventoryLogMapper inventoryLogMapper;
    @Autowired private PurchaseOrderMapper purchaseOrderMapper;
    @Autowired private SupplierMapper supplierMapper;
    @Autowired private PromotionMapper promotionMapper;
    @Autowired private AuditLogMapper auditLogMapper;
    @Autowired private PaymentMapper paymentMapper;
    @Autowired private AddressMapper addressMapper;

    // ===== 用户管理 =====

    public Result<?> getUserList(Integer pageNum, Integer pageSize, String keyword) {
        Page<User> page = new Page<>(pageNum, pageSize);
        return Result.success(page);
    }

    public Result<?> updateUserStatus(Integer userId, String status) {
        User user = userMapper.selectById(userId);
        if (user != null) {
            user.setStatus(status);
            userMapper.updateById(user);
        }
        return Result.success();
    }

    // ===== 统计 =====

    public Result<?> getStatistics() {
        long userCount    = userMapper.selectCount(null);
        long productCount = productMapper.selectCount(null);
        long orderCount   = orderMapper.selectCount(null);

        Map<String, Object> data = new HashMap<>();
        data.put("userCount",    userCount);
        data.put("productCount", productCount);
        data.put("orderCount",   orderCount);
        return Result.success(data);
    }

    // ===== 库存入库 / 出库 =====

    @Transactional
    public Result<?> warehousing(Integer productId, Integer quantity, Integer operatorId) {
        Product product = productMapper.selectById(productId);
        if (product == null) return Result.error("商品不存在");
        product.setStock(product.getStock() + quantity);
        productMapper.updateById(product);

        InventoryLog log = new InventoryLog();
        log.setProductId(productId);
        log.setChangeType("入库");
        log.setQuantity(quantity);
        log.setOperatorId(operatorId);
        log.setLogTime(new Date());
        inventoryLogMapper.insert(log);
        return Result.success();
    }

    @Transactional
    public Result<?> outbound(Integer productId, Integer quantity, Integer operatorId) {
        Product product = productMapper.selectById(productId);
        if (product == null) return Result.error("商品不存在");
        product.setStock(product.getStock() - quantity);
        productMapper.updateById(product);

        InventoryLog log = new InventoryLog();
        log.setProductId(productId);
        log.setChangeType("出库");
        log.setQuantity(-quantity);
        log.setOperatorId(operatorId);
        log.setLogTime(new Date());
        inventoryLogMapper.insert(log);
        return Result.success();
    }

    public Result<?> getInventoryLogs(Integer pageNum, Integer pageSize) {
        Page<InventoryLog> page = new Page<>(pageNum, pageSize);
        return Result.success(page);
    }

    // ===== 发货 =====

    /**
     * 管理员发货：
     * 1. 校验订单状态必须为"待发货"
     * 2. 订单状态改为"已发货"，记录发货时间
     * 3. 在 DELIVERIES 表创建配送记录（状态=待取件），供后续分配快递员
     */
    @Transactional
    public Result<?> shipOrder(Integer orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) return Result.error("订单不存在");
        if (!"待发货".equals(order.getOrderStatus())) {
            return Result.error("当前订单状态不允许发货，状态: " + order.getOrderStatus());
        }

        // 更新订单状态
        order.setOrderStatus("已发货");
        order.setShipTime(new Date());
        orderMapper.updateById(order);

        // 查收货地址快照（尽量从 ADDRESS 表取；无地址时降级为空）
        String receiver = "";
        String phone    = "";
        String detail   = "";
        if (order.getAddressId() != null) {
            Address addr = addressMapper.selectById(order.getAddressId());
            if (addr != null) {
                receiver = addr.getReceiver() != null ? addr.getReceiver() : "";
                phone    = addr.getPhone()    != null ? addr.getPhone()    : "";
                detail   = addr.getDetail()   != null ? addr.getDetail()   : "";
            }
        }

        // 创建配送记录
        Delivery delivery = new Delivery();
        delivery.setOrderId(orderId);
        delivery.setAddress(detail);
        delivery.setReceiver(receiver);
        delivery.setPhone(phone);
        delivery.setStatus("待取件");
        deliveryMapper.insert(delivery);

        return Result.success("发货成功，已创建配送记录");
    }

    // ===== 配送管理 =====

    public Result<?> getDeliveryList(Integer pageNum, Integer pageSize) {
        Page<Delivery> page = new Page<>(pageNum, pageSize);
        return Result.success(page);
    }

    @Transactional
    public Result<?> assignCourier(Integer deliveryId, Integer courierId) {
        Delivery delivery = deliveryMapper.selectById(deliveryId);
        if (delivery != null) {
            delivery.setCourierId(courierId);
            delivery.setStatus("配送中");
            delivery.setDispatchTime(new Date());
            deliveryMapper.updateById(delivery);
        }
        return Result.success();
    }

    public Result<?> updateDeliveryStatus(Integer deliveryId, String status) {
        Delivery delivery = deliveryMapper.selectById(deliveryId);
        if (delivery != null) {
            delivery.setStatus(status);
            if ("已完成".equals(status)) {
                delivery.setDoneTime(new Date());
            }
            deliveryMapper.updateById(delivery);
        }
        return Result.success();
    }

    // ===== 促销管理 =====

    public Result<?> getPromotionList() {
        List<Promotion> list = promotionMapper.selectList(null);
        return Result.success(list);
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

    // ===== 供应商 =====

    public Result<?> getSupplierList() {
        List<Supplier> list = supplierMapper.selectList(null);
        return Result.success(list);
    }

    // ===== 采购单 =====

    @Transactional
    public Result<?> createPurchaseOrder(PurchaseOrder order) {
        purchaseOrderMapper.insert(order);
        return Result.success();
    }

    public Result<?> getPurchaseOrders(Integer pageNum, Integer pageSize) {
        Page<PurchaseOrder> page = new Page<>(pageNum, pageSize);
        return Result.success(page);
    }

    // ===== 财务 =====

    public Result<?> getFinanceData() {
        Map<String, Object> data = new HashMap<>();
        data.put("revenue", 0.0);
        data.put("cost",    0.0);
        return Result.success(data);
    }

    // ===== 审计日志 =====

    public Result<?> getAuditLogs(Integer pageNum, Integer pageSize) {
        Page<AuditLog> page = new Page<>(pageNum, pageSize);
        return Result.success(page);
    }
}
