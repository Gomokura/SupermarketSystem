package com.supermarket.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.supermarket.common.Result;
import com.supermarket.dto.CreateOrderRequest;
import com.supermarket.entity.*;
import com.supermarket.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class OrderService extends ServiceImpl<OrderMapper, Order> {

    @Autowired private OrderMapper orderMapper;
    @Autowired private OrderItemMapper orderItemMapper;
    @Autowired private CartMapper cartMapper;
    @Autowired private ProductMapper productMapper;
    @Autowired private ProductSkuMapper productSkuMapper;
    @Autowired private PaymentMapper paymentMapper;
    @Autowired private AddressMapper addressMapper;
    @Autowired private InventoryLogMapper inventoryLogMapper;

    // ==================== C端 - 订单查询 ====================

    /** 用户订单列表（支持状态筛选） */
    public Result<?> getUserOrders(Integer userId, String status, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getUserId, userId);
        if (status != null && !status.isEmpty()) wrapper.eq(Order::getStatus, status);
        wrapper.orderByDesc(Order::getCreateTime);

        Page<Order> page = new Page<>(pageNum, pageSize);
        Page<Order> result = this.page(page, wrapper);
        for (Order order : result.getRecords()) {
            LambdaQueryWrapper<OrderItem> iw = new LambdaQueryWrapper<>();
            iw.eq(OrderItem::getOrderId, order.getOrderId());
            order.setItems(orderItemMapper.selectList(iw));
        }
        return Result.success(result);
    }

    /** 订单详情 */
    public Result<?> getOrderDetail(Integer orderId, Integer userId) {
        Order order = this.getById(orderId);
        if (order == null) return Result.error("订单不存在");
        if (userId != null && !order.getUserId().equals(userId)) return Result.error("无权查看此订单");
        LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderItem::getOrderId, orderId);
        order.setItems(orderItemMapper.selectList(wrapper));
        return Result.success(order);
    }

    // ==================== C端 - 下单流程 ====================

    /**
     * 下单核心方法（支持 SKU 多规格，库存完整流水）
     * 逻辑：
     *   1. 有 skuId → 校验并扣 SKU 库存，同步更新 product.stock 汇总值
     *   2. 无 skuId → 只扣 product.stock
     *   3. 每次扣减均写 inventory_logs（含 skuId）
     */
    @Transactional
    public Result<?> createOrder(Integer userId, Integer addressId, String paymentMethod,
                                 List<CreateOrderRequest.CartItem> cartItems,
                                 Integer couponId, Integer pointsUsed, String remark) {
        // 1. 校验收货地址
        Address address = addressMapper.selectById(addressId);
        if (address == null || !address.getUserId().equals(userId)) return Result.error("收货地址不存在");

        // 2. 预校验所有商品库存，并收集价格
        double totalAmount = 0;
        // 用 LinkedHashMap 保存每个 item 对应的 product 和 sku（顺序对齐 cartItems）
        List<Product> productList = new ArrayList<>();
        List<ProductSku> skuList = new ArrayList<>();

        for (CreateOrderRequest.CartItem item : cartItems) {
            Product product = productMapper.selectById(item.getProductId());
            if (product == null || product.getIsDeleted() == 1) return Result.error("商品不存在: " + item.getProductId());
            if (!"active".equals(product.getStatus())) return Result.error("商品已下架: " + product.getProductName());

            if (item.getSkuId() != null) {
                // ✅ 有 SKU：校验 SKU 库存，价格取 sku.price
                ProductSku sku = productSkuMapper.selectById(item.getSkuId());
                if (sku == null || !"active".equals(sku.getStatus()))
                    return Result.error("规格不存在或已下架: " + item.getSkuId());
                if (sku.getStock() < item.getQuantity())
                    return Result.error("规格库存不足: " + product.getProductName()
                            + " [" + sku.getSkuName() + "] 剩余 " + sku.getStock());
                totalAmount += sku.getPrice() * item.getQuantity();
                skuList.add(sku);
            } else {
                // 无 SKU：校验主表库存
                if (product.getStock() < item.getQuantity())
                    return Result.error("商品库存不足: " + product.getProductName() + "（剩余 " + product.getStock() + "）");
                totalAmount += product.getPrice() * item.getQuantity();
                skuList.add(null);
            }
            productList.add(product);
        }

        // 3. 生成订单
        String orderNo = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())
                + String.format("%04d", new Random().nextInt(10000));
        String addressSnapshot = address.getReceiverName() + " " + address.getPhone()
                + " " + address.getProvince() + address.getCity() + address.getDistrict() + address.getDetail();

        Order order = new Order();
        order.setUserId(userId);
        order.setOrderNo(orderNo);
        order.setAddressId(addressId);
        order.setAddressSnapshot(addressSnapshot);
        order.setTotalAmount(totalAmount);
        order.setDiscountAmount(0.0);
        order.setPayAmount(totalAmount);
        order.setStatus("PENDING_PAY");
        order.setPayMethod(paymentMethod);
        order.setRemark(remark);
        order.setSource("ONLINE");
        order.setFreight(0.0);
        order.setCreateTime(new Date());
        if (couponId != null) order.setCouponId(couponId);
        if (pointsUsed != null) order.setPointsUsed(pointsUsed);
        orderMapper.insert(order);

        // 4. 逐项插入订单明细 + 扣库存 + 写流水
        for (int i = 0; i < cartItems.size(); i++) {
            CreateOrderRequest.CartItem item = cartItems.get(i);
            Product product = productList.get(i);
            ProductSku sku = skuList.get(i);

            // 确定成交价
            double unitPrice = (sku != null) ? sku.getPrice() : product.getPrice();
            String skuName  = (sku != null) ? sku.getSkuName() : null;

            // 插入订单明细（字段含 skuName 快照）
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getOrderId());
            orderItem.setProductId(item.getProductId());
            orderItem.setProductName(product.getProductName());
            orderItem.setProductImage(product.getCoverImage());
            orderItem.setUnitPrice(unitPrice);
            orderItem.setQuantity(item.getQuantity());
            orderItem.setSubtotal(unitPrice * item.getQuantity());
            orderItem.setSkuId(item.getSkuId());
            orderItem.setSpecName(skuName);
            orderItemMapper.insert(orderItem);

            // ✅ 扣库存
            if (sku != null) {
                // 有 SKU：优先扣 SKU 独立库存
                int newSkuStock = sku.getStock() - item.getQuantity();
                sku.setStock(newSkuStock);
                productSkuMapper.updateById(sku);

                // 同步汇总主表 stock（减少对应数量）
                product.setStock(product.getStock() - item.getQuantity());
            } else {
                // 无 SKU：只扣主表
                product.setStock(product.getStock() - item.getQuantity());
            }
            product.setSalesCount(product.getSalesCount() + item.getQuantity());
            productMapper.updateById(product);

            // ✅ 写库存流水（含 skuId）
            writeInventoryLog(product.getProductId(),
                    item.getSkuId(),
                    "ORDER_OUT",
                    -item.getQuantity(),
                    product.getStock(),
                    order.getOrderId(),
                    "订单出库，单号: " + orderNo,
                    userId);

            // 清除购物车
            LambdaQueryWrapper<Cart> cartWrapper = new LambdaQueryWrapper<>();
            cartWrapper.eq(Cart::getUserId, userId).eq(Cart::getProductId, item.getProductId());
            if (item.getSkuId() != null) cartWrapper.eq(Cart::getSkuId, item.getSkuId());
            else cartWrapper.isNull(Cart::getSkuId);
            cartMapper.delete(cartWrapper);
        }

        // 5. 即时支付直接标记 paid
        if ("CASH".equals(paymentMethod) || "MOCK".equals(paymentMethod) || "MOCK_CARD".equals(paymentMethod)) {
            order.setStatus("PAID");
            order.setPayTime(new Date());
            orderMapper.updateById(order);
            insertPayment(order.getOrderId(), order.getPayAmount());
        }

        Map<String, Object> data = new HashMap<>();
        data.put("orderId", order.getOrderId());
        data.put("orderNo", orderNo);
        data.put("payAmount", order.getPayAmount());
        data.put("status", order.getStatus());
        return Result.success(data);
    }

    /** 订单支付（pending→paid） */
    @Transactional
    public Result<?> payOrder(Integer orderId, Integer userId, String payMethod) {
        Order order = this.getById(orderId);
        if (order == null) return Result.error("订单不存在");
        if (!order.getUserId().equals(userId)) return Result.error("无权操作");
        if (!"PENDING_PAY".equals(order.getStatus())) return Result.error("订单状态不正确，无法支付");

        order.setStatus("PAID");
        order.setPayMethod(payMethod);
        order.setPayTime(new Date());
        this.updateById(order);
        insertPayment(orderId, order.getPayAmount());
        return Result.success("支付成功");
    }

    /**
     * 取消订单（pending/paid 可取消，✅ 退还 SKU 库存和主表库存）
     */
    @Transactional
    public Result<?> cancelOrder(Integer orderId, Integer userId) {
        Order order = this.getById(orderId);
        if (order == null) return Result.error("订单不存在");
        if (!order.getUserId().equals(userId)) return Result.error("无权操作此订单");
        if (!"PENDING_PAY".equals(order.getStatus()) && !"PAID".equals(order.getStatus()))
            return Result.error("当前状态（" + order.getStatus() + "）无法取消");

        restoreStock(order, userId);

        order.setStatus("CANCELLED");
        order.setCancelTime(new Date());
        this.updateById(order);
        return Result.success("订单已取消");
    }

    /** 确认收货（shipped→completed） */
    public Result<?> confirmReceipt(Integer orderId, Integer userId) {
        Order order = this.getById(orderId);
        if (order == null) return Result.error("订单不存在");
        if (!order.getUserId().equals(userId)) return Result.error("无权操作");
        if (!"SHIPPING".equals(order.getStatus())) return Result.error("订单尚未发货，无法确认收货");
        order.setStatus("COMPLETED");
        order.setCompleteTime(new Date());
        this.updateById(order);
        return Result.success("确认收货成功");
    }

    // ==================== B端 - 订单管理 ====================

    /** 管理后台订单列表 */
    public Result<?> adminGetOrderList(String status, String orderNo, Integer userId,
                                       String startDate, String endDate,
                                       Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) wrapper.eq(Order::getStatus, status);
        if (orderNo != null && !orderNo.isEmpty()) wrapper.like(Order::getOrderNo, orderNo);
        if (userId != null) wrapper.eq(Order::getUserId, userId);
        wrapper.orderByDesc(Order::getCreateTime);
        return Result.success(this.page(new Page<>(pageNum, pageSize), wrapper));
    }

    /** 管理员发货（paid→shipped） */
    public Result<?> shipOrder(Integer orderId, Integer operatorId) {
        Order order = this.getById(orderId);
        if (order == null) return Result.error("订单不存在");
        if (!"PAID".equals(order.getStatus())) return Result.error("只有已支付订单才能发货");
        order.setStatus("SHIPPING");
        order.setShipTime(new Date());
        this.updateById(order);
        return Result.success("发货成功");
    }

    /** 管理员强制取消（✅ 同步退还库存） */
    @Transactional
    public Result<?> adminCancelOrder(Integer orderId, Integer operatorId, String reason) {
        Order order = this.getById(orderId);
        if (order == null) return Result.error("订单不存在");
        if ("COMPLETED".equals(order.getStatus()) || "CANCELLED".equals(order.getStatus()))
            return Result.error("已完成或已取消的订单不能操作");

        restoreStock(order, operatorId);

        order.setStatus("CANCELLED");
        order.setCancelTime(new Date());
        if (reason != null) order.setRemark((order.getRemark() != null ? order.getRemark() + " | " : "") + "管理员取消：" + reason);
        this.updateById(order);
        return Result.success("订单已取消");
    }

    /**
     * 收银台下单（线下POS，source=cashier）
     * ✅ 同样支持 SKU 库存扣减
     */
    @Transactional
    public Result<?> cashierCreateOrder(Integer cashierId, List<CreateOrderRequest.CartItem> cartItems,
                                        String payMethod, Double receivedAmount) {
        double totalAmount = 0;
        List<Product> productList = new ArrayList<>();
        List<ProductSku> skuList = new ArrayList<>();

        for (CreateOrderRequest.CartItem item : cartItems) {
            Product product = productMapper.selectById(item.getProductId());
            if (product == null || product.getIsDeleted() == 1) return Result.error("商品不存在: " + item.getProductId());

            if (item.getSkuId() != null) {
                ProductSku sku = productSkuMapper.selectById(item.getSkuId());
                if (sku == null) return Result.error("规格不存在: " + item.getSkuId());
                if (sku.getStock() < item.getQuantity()) return Result.error("规格库存不足: " + product.getProductName());
                totalAmount += sku.getPrice() * item.getQuantity();
                skuList.add(sku);
            } else {
                if (product.getStock() < item.getQuantity()) return Result.error("库存不足: " + product.getProductName());
                totalAmount += product.getPrice() * item.getQuantity();
                skuList.add(null);
            }
            productList.add(product);
        }

        String orderNo = "C" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())
                + String.format("%04d", new Random().nextInt(10000));

        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setTotalAmount(totalAmount);
        order.setPayAmount(totalAmount);
        order.setStatus("COMPLETED");
        order.setPayMethod(payMethod);
        order.setSource("CASHIER");
        order.setCreateTime(new Date());
        order.setPayTime(new Date());
        order.setCompleteTime(new Date());
        orderMapper.insert(order);

        for (int i = 0; i < cartItems.size(); i++) {
            CreateOrderRequest.CartItem item = cartItems.get(i);
            Product product = productList.get(i);
            ProductSku sku = skuList.get(i);
            double unitPrice = (sku != null) ? sku.getPrice() : product.getPrice();

            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getOrderId());
            orderItem.setProductId(item.getProductId());
            orderItem.setProductName(product.getProductName());
            orderItem.setUnitPrice(unitPrice);
            orderItem.setQuantity(item.getQuantity());
            orderItem.setSubtotal(unitPrice * item.getQuantity());
            orderItem.setSkuId(item.getSkuId());
            orderItem.setSpecName(sku != null ? sku.getSkuName() : null);
            orderItemMapper.insert(orderItem);

            if (sku != null) {
                sku.setStock(sku.getStock() - item.getQuantity());
                productSkuMapper.updateById(sku);
                product.setStock(product.getStock() - item.getQuantity());
            } else {
                product.setStock(product.getStock() - item.getQuantity());
            }
            product.setSalesCount(product.getSalesCount() + item.getQuantity());
            productMapper.updateById(product);

            writeInventoryLog(product.getProductId(), item.getSkuId(), "ORDER_OUT",
                    -item.getQuantity(), product.getStock(), order.getOrderId(),
                    "收银台出库，单号: " + orderNo, cashierId);
        }

        insertPayment(order.getOrderId(), totalAmount);

        double change = receivedAmount != null ? receivedAmount - totalAmount : 0;
        Map<String, Object> data = new HashMap<>();
        data.put("orderId", order.getOrderId());
        data.put("orderNo", orderNo);
        data.put("totalAmount", totalAmount);
        data.put("receivedAmount", receivedAmount);
        data.put("change", Math.max(0, change));
        return Result.success(data);
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 退还库存（取消订单时调用）
     * ✅ 优先退还 SKU 库存，再同步主表
     */
    private void restoreStock(Order order, Integer operatorId) {
        LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderItem::getOrderId, order.getOrderId());
        List<OrderItem> items = orderItemMapper.selectList(wrapper);

        for (OrderItem item : items) {
            Product product = productMapper.selectById(item.getProductId());
            if (product == null) continue;

            if (item.getSkuId() != null) {
                ProductSku sku = productSkuMapper.selectById(item.getSkuId());
                if (sku != null) {
                    sku.setStock(sku.getStock() + item.getQuantity());
                    productSkuMapper.updateById(sku);
                }
                product.setStock(product.getStock() + item.getQuantity());
            } else {
                product.setStock(product.getStock() + item.getQuantity());
            }
            product.setSalesCount(Math.max(0, product.getSalesCount() - item.getQuantity()));
            productMapper.updateById(product);

            writeInventoryLog(product.getProductId(), item.getSkuId(), "ORDER_CANCEL",
                    item.getQuantity(), product.getStock(), order.getOrderId(),
                    "取消回库，单号: " + order.getOrderNo(), operatorId);
        }
    }

    /**
     * 写库存流水（统一入口，含 skuId）
     */
    private void writeInventoryLog(Integer productId, Integer skuId, String logType,
                                   int changeAmount, int balanceAfter,
                                   Integer refId, String remark, Integer operatorId) {
        InventoryLog log = new InventoryLog();
        log.setProductId(productId);
        log.setSkuId(skuId);
        log.setLogType(logType);
        log.setChangeAmount(changeAmount);
        log.setBalanceAfter(balanceAfter);
        log.setRefId(refId);
        log.setRemark(remark);
        log.setOperatorId(operatorId);
        log.setCreateTime(new Date());
        inventoryLogMapper.insert(log);
    }

    /** 插入支付流水 */
    private void insertPayment(Integer orderId, Double amount) {
        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setAmount(amount);
        payment.setStatus("paid");
        payment.setPayTime(new Date());
        paymentMapper.insert(payment);
    }
}
