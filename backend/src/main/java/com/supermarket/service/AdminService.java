package com.supermarket.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.supermarket.common.Result;
import com.supermarket.entity.*;
import com.supermarket.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class AdminService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private DeliveryMapper deliveryMapper;
    @Autowired
    private InventoryLogMapper inventoryLogMapper;
    @Autowired
    private PurchaseOrderMapper purchaseOrderMapper;
    @Autowired
    private SupplierMapper supplierMapper;
    @Autowired
    private PromotionMapper promotionMapper;
    @Autowired
    private AuditLogMapper auditLogMapper;
    @Autowired
    private PaymentMapper paymentMapper;

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

    public Result<?> getStatistics() {
        long userCount = userMapper.selectCount(null);
        long productCount = productMapper.selectCount(null);
        long orderCount = orderMapper.selectCount(null);
        return Result.success(new Object(){
            public long userCount = userCount;
            public long productCount = productCount;
            public long orderCount = orderCount;
        });
    }

    @Transactional
    public Result<?> warehousing(Integer productId, Integer quantity, Integer operatorId) {
        Product product = productMapper.selectById(productId);
        if (product != null) {
            product.setStock(product.getStock() + quantity);
            productMapper.updateById(product);

            InventoryLog log = new InventoryLog();
            log.setProductId(productId);
            log.setChangeType("入库");
            log.setQuantity(quantity);
            log.setOperatorId(operatorId);
            log.setLogTime(new Date());
            inventoryLogMapper.insert(log);
        }
        return Result.success();
    }

    @Transactional
    public Result<?> outbound(Integer productId, Integer quantity, Integer operatorId) {
        Product product = productMapper.selectById(productId);
        if (product != null) {
            product.setStock(product.getStock() - quantity);
            productMapper.updateById(product);

            InventoryLog log = new InventoryLog();
            log.setProductId(productId);
            log.setChangeType("出库");
            log.setQuantity(-quantity);
            log.setOperatorId(operatorId);
            log.setLogTime(new Date());
            inventoryLogMapper.insert(log);
        }
        return Result.success();
    }

    public Result<?> getInventoryLogs(Integer pageNum, Integer pageSize) {
        Page<InventoryLog> page = new Page<>(pageNum, pageSize);
        return Result.success(page);
    }

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

    public Result<?> getSupplierList() {
        List<Supplier> list = supplierMapper.selectList(null);
        return Result.success(list);
    }

    @Transactional
    public Result<?> createPurchaseOrder(PurchaseOrder order) {
        purchaseOrderMapper.insert(order);
        return Result.success();
    }

    public Result<?> getPurchaseOrders(Integer pageNum, Integer pageSize) {
        Page<PurchaseOrder> page = new Page<>(pageNum, pageSize);
        return Result.success(page);
    }

    public Result<?> getFinanceData() {
        return Result.success(new Object(){public double revenue = 0; public double cost = 0;});
    }

    public Result<?> getAuditLogs(Integer pageNum, Integer pageSize) {
        Page<AuditLog> page = new Page<>(pageNum, pageSize);
        return Result.success(page);
    }
}
