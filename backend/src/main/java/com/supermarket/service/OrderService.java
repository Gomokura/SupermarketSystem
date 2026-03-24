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

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderService extends ServiceImpl<OrderMapper, Order> {

    @Autowired private OrderMapper orderMapper;
    @Autowired private OrderItemMapper orderItemMapper;
    @Autowired private CartMapper cartMapper;
    @Autowired private ProductMapper productMapper;
    @Autowired private PaymentMapper paymentMapper;
    @Autowired private AddressMapper addressMapper;
    @Autowired private InventoryLogMapper inventoryLogMapper;

    // ===== 查询 =====

    public Result<?> getUserOrders(Integer userId, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getUserId, userId);
        wrapper.orderByDesc(Order::getOrderTime);

        Page<Order> page = new Page<>(pageNum, pageSize);
        Page<Order> result = this.page(page, wrapper);
        return Result.success(result);
    }

    public Result<?> getOrderDetail(Integer orderId) {
        Order order = this.getById(orderId);
        if (order == null) {
            return Result.error("订单不存在");
        }

        LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderItem::getOrderId, orderId);
        List<OrderItem> items = orderItemMapper.selectList(wrapper);

        Map<String, Object> data = new HashMap<>();
        data.put("order", order);
        data.put("items", items);
        return Result.success(data);
    }

    // ===== 创建订单 =====

    @Transactional
    public Result<?> createOrder(Integer userId, Integer addressId, String paymentMethod,
                                 List<CreateOrderRequest.CartItem> cartItems) {
        Address address = addressMapper.selectById(addressId);
        if (address == null) {
            return Result.error("收货地址不存在");
        }

        // 第一轮：校验库存 + 计算金额
        double totalAmount = 0;
        for (CreateOrderRequest.CartItem item : cartItems) {
            Product product = productMapper.selectById(item.getProductId());
            if (product == null) {
                return Result.error("商品不存在: " + item.getProductId());
            }
            if (product.getStock() < item.getQuantity()) {
                return Result.error("商品库存不足: " + product.getProductName());
            }
            totalAmount += product.getPrice() * item.getQuantity();
        }

        // 创建订单主表
        Order order = new Order();
        order.setUserId(userId);
        order.setAddressId(addressId);
        order.setTotalAmount(totalAmount);
        order.setOrderStatus("待支付");
        order.setPaymentMethod(paymentMethod);
        order.setOrderTime(new Date());
        orderMapper.insert(order);

        // 第二轮：写订单明细 + 扣库存 + 写库存日志 + 清购物车
        for (CreateOrderRequest.CartItem item : cartItems) {
            Product product = productMapper.selectById(item.getProductId());

            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getOrderId());
            orderItem.setProductId(item.getProductId());
            orderItem.setProductName(product.getProductName());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setPrice(product.getPrice());
            orderItem.setSubtotal(product.getPrice() * item.getQuantity());
            orderItemMapper.insert(orderItem);

            product.setStock(product.getStock() - item.getQuantity());
            productMapper.updateById(product);

            InventoryLog log = new InventoryLog();
            log.setProductId(product.getProductId());
            log.setChangeType("出库");
            log.setQuantity(-item.getQuantity());
            log.setOperatorId(userId);
            log.setRemark("订单出库，订单号: " + order.getOrderId());
            log.setLogTime(new Date());
            inventoryLogMapper.insert(log);

            LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Cart::getUserId, userId);
            wrapper.eq(Cart::getProductId, item.getProductId());
            cartMapper.delete(wrapper);
        }

        // 若支付方式标识为"已支付"，直接置为待发货
        if ("已支付".equals(paymentMethod) || "微信支付".equals(paymentMethod)
                || "支付宝".equals(paymentMethod) || "银行卡".equals(paymentMethod)) {
            Payment payment = new Payment();
            payment.setOrderId(order.getOrderId());
            payment.setAmount(totalAmount);
            payment.setStatus("已支付");
            payment.setPayTime(new Date());
            paymentMapper.insert(payment);

            order.setOrderStatus("待发货");
            orderMapper.updateById(order);
        }

        return Result.success(order.getOrderId());
    }

    // ===== 取消订单 =====

    @Transactional
    public Result<?> cancelOrder(Integer orderId, Integer userId) {
        Order order = this.getById(orderId);
        if (order == null) {
            return Result.error("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            return Result.error("无权操作此订单");
        }
        if (!"待支付".equals(order.getOrderStatus()) && !"待发货".equals(order.getOrderStatus())) {
            return Result.error("当前状态无法取消");
        }

        // 回滚库存
        LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderItem::getOrderId, orderId);
        List<OrderItem> items = orderItemMapper.selectList(wrapper);

        for (OrderItem item : items) {
            Product product = productMapper.selectById(item.getProductId());
            product.setStock(product.getStock() + item.getQuantity());
            productMapper.updateById(product);

            InventoryLog log = new InventoryLog();
            log.setProductId(product.getProductId());
            log.setChangeType("入库");
            log.setQuantity(item.getQuantity());
            log.setOperatorId(userId);
            log.setRemark("订单取消退货，订单号: " + orderId);
            log.setLogTime(new Date());
            inventoryLogMapper.insert(log);
        }

        order.setOrderStatus("已取消");
        this.updateById(order);
        return Result.success();
    }

    // ===== 确认收货 =====

    @Transactional
    public Result<?> confirmReceipt(Integer orderId, Integer userId) {
        Order order = this.getById(orderId);
        if (order == null) {
            return Result.error("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            return Result.error("无权操作此订单");
        }
        if (!"已发货".equals(order.getOrderStatus())) {
            return Result.error("订单尚未发货，无法确认收货");
        }

        order.setOrderStatus("已完成");
        order.setCompleteTime(new Date());
        this.updateById(order);
        return Result.success("确认收货成功");
    }
}
