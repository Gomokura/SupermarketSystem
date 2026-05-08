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
    @Autowired private AddressMapper addressMapper;
    @Autowired private InventoryLogMapper inventoryLogMapper;
    @Autowired private CouponMapper couponMapper;
    @Autowired private UserCouponMapper userCouponMapper;
    @Autowired private UserMapper userMapper;
    @Autowired private PointsLogMapper pointsLogMapper;
    @Autowired private OrderStatusLogMapper orderStatusLogMapper;
    @Autowired private DeliveryTaskMapper deliveryTaskMapper;
    @Autowired private CourierMapper courierMapper;

    private static final double POINTS_TO_CASH_RATE = 0.01; // 1 point = 0.01 yuan (100积分=1元)

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
        
        if (order.getReceiverSnapshot() != null && !order.getReceiverSnapshot().isEmpty()) {
            String[] parts = order.getReceiverSnapshot().split(" ");
            if (parts.length >= 2) {
                order.setReceiverName(parts[0]);
                order.setReceiverPhone(parts[1]);
                if (parts.length >= 3) {
                    order.setReceiverAddress(parts[2]);
                }
            }
        }
        
        DeliveryTask task = deliveryTaskMapper.selectOne(new LambdaQueryWrapper<DeliveryTask>()
                .eq(DeliveryTask::getOrderId, orderId));
        if (task != null) {
            order.setPickupTime(task.getPickupTime());
            order.setDeliverTime(task.getDeliverTime());
        }
        return Result.success(order);
    }

    /** 结算预览（不落库） */
    public Result<?> previewOrder(Integer userId, Integer addressId, List<CreateOrderRequest.CartItem> cartItems,
                                  Integer couponId, Integer pointsUsed) {
        if (cartItems == null || cartItems.isEmpty()) return Result.error("请选择结算商品");
        Address address = addressMapper.selectById(addressId);
        if (address == null || !address.getUserId().equals(userId)) return Result.error("收货地址不存在");

        double totalAmount = 0;
        List<Product> productList = new ArrayList<>();
        for (CreateOrderRequest.CartItem item : cartItems) {
            Product product = productMapper.selectById(item.getProductId());
            if (product == null || product.getIsDeleted() == 1) return Result.error("商品不存在: " + item.getProductId());
            if (!"active".equals(product.getStatus())) return Result.error("商品已下架: " + product.getProductName());
            if (item.getSkuId() != null) {
                ProductSku sku = productSkuMapper.selectById(item.getSkuId());
                if (sku == null || !"active".equals(sku.getStatus())) return Result.error("规格不存在或已下架");
                if (sku.getStock() < item.getQuantity()) return Result.error("规格库存不足");
                totalAmount += sku.getPrice() * item.getQuantity();
            } else {
                if (product.getStock() < item.getQuantity()) return Result.error("商品库存不足");
                totalAmount += product.getPrice() * item.getQuantity();
            }
            productList.add(product);
        }

        DiscountCalc calc = calcDiscount(userId, cartItems, productList, totalAmount, couponId, pointsUsed);
        double freight = 0.0;
        double payAmount = round2(totalAmount - calc.couponDiscount - calc.pointsDeductAmount + freight);

        Map<String, Object> data = new HashMap<>();
        data.put("totalAmount", round2(totalAmount));
        data.put("couponId", calc.couponId);
        data.put("couponDiscount", round2(calc.couponDiscount));
        data.put("pointsUsed", calc.pointsUsed);
        data.put("pointsDeductAmount", round2(calc.pointsDeductAmount));
        data.put("freightAmount", freight);
        data.put("discountAmount", round2(calc.couponDiscount + calc.pointsDeductAmount));
        data.put("payAmount", payAmount < 0 ? 0 : payAmount);
        return Result.success(data);
    }

    /** 订单状态时间线 */
    public Result<?> getOrderStatusLogs(Integer orderId, Integer userId, boolean adminView) {
        Order order = this.getById(orderId);
        if (order == null) return Result.error("订单不存在");
        if (!adminView && !order.getUserId().equals(userId)) return Result.error("无权查看此订单");
        List<OrderStatusLog> logs = orderStatusLogMapper.selectList(new LambdaQueryWrapper<OrderStatusLog>()
                .eq(OrderStatusLog::getOrderId, orderId)
                .orderByAsc(OrderStatusLog::getCreateTime));
        return Result.success(logs);
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
                                 Integer couponId, Integer pointsUsed, String remark,
                                 String deliveryTimeSlot) {
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

        // 3. 计算优惠券与积分抵扣（与 v3.0 清单一致）
        DiscountCalc calc = calcDiscount(userId, cartItems, productList, totalAmount, couponId, pointsUsed);

        // 4. 生成订单
        String orderNo = "SM" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())
                + String.format("%04d", new Random().nextInt(10000));
        String receiverSnapshot = address.getReceiverName() + " " + address.getPhone()
                + " " + address.getProvince() + address.getCity() + address.getDistrict() + address.getDetail();

        Order order = new Order();
        order.setUserId(userId);
        order.setOrderNo(orderNo);
        order.setAddressId(addressId);
        order.setReceiverSnapshot(receiverSnapshot);
        order.setSource("ONLINE");
        order.setTotalAmount(round2(totalAmount));
        order.setCouponId(calc.couponId);
        order.setUcId(calc.ucId);
        order.setPointsUsed(calc.pointsUsed);
        order.setCouponDiscount(round2(calc.couponDiscount));
        order.setPointsDeductAmount(round2(calc.pointsDeductAmount));
        order.setFreightAmount(0.0);
        order.setDiscountAmount(round2(calc.couponDiscount + calc.pointsDeductAmount));
        order.setPayAmount(round2(totalAmount - calc.couponDiscount - calc.pointsDeductAmount));
        String payMethod = paymentMethod != null ? paymentMethod.toUpperCase() : "ALIPAY";
        if (!payMethod.matches("^(MOCK|CASH|MOCK_CARD|WECHAT|ALIPAY)$")) {
            payMethod = "MOCK";
        }
        order.setPayMethod(payMethod);
        order.setRemark(remark);
        order.setDeliveryTimeSlot(deliveryTimeSlot);
        order.setStatus("PENDING_PAY");
        order.setCreateTime(new Date());
        order.setUpdateTime(new Date());
        order.setOrderId(orderMapper.getNextId());
        orderMapper.insert(order);
        writeStatusLog(order.getOrderId(), null, "PENDING_PAY", "USER", userId, null, "订单创建");

        // 5. 逐项插入订单明细 + 扣库存 + 写流水
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
            orderItem.setCostPrice(product.getCostPrice() != null ? product.getCostPrice() : 0.0);
            orderItem.setQuantity(item.getQuantity());
            orderItem.setSubtotal(unitPrice * item.getQuantity());
            orderItem.setSkuId(item.getSkuId());
            orderItem.setSkuName(skuName);
            orderItem.setItemId(orderItemMapper.getNextId());
            orderItemMapper.insert(orderItem);

            // ✅ 扣库存：只操作实际存储库存的表
            if (sku != null) {
                // 有 SKU：只扣 SKU 独立库存（主表 PRODUCTS.stock 仅在无SKU时使用）
                if (sku.getStock() < item.getQuantity()) {
                    return Result.error("SKU「" + sku.getSkuName() + "」库存不足");
                }
                int newSkuStock = sku.getStock() - item.getQuantity();
                sku.setStock(newSkuStock);
                productSkuMapper.updateById(sku);
            } else {
                // 无 SKU：只扣主表
                if (product.getStock() < item.getQuantity()) {
                    return Result.error("商品「" + product.getProductName() + "」库存不足");
                }
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

        // 6. 预扣：核销优惠券&扣积分（保证与订单落库一致）
        if (order.getUcId() != null) {
            markUserCouponUsed(order.getUcId(), order.getOrderId());
        }
        if (order.getPointsUsed() != null && order.getPointsUsed() > 0) {
            deductUserPoints(userId, order.getPointsUsed(), order.getOrderId());
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

        String from = order.getStatus();
        // v3.0：支付成功后进入“待发货”
        order.setStatus("PENDING_SHIP");
        order.setPayMethod(payMethod != null ? payMethod : order.getPayMethod());
        order.setPayTime(new Date());
        order.setUpdateTime(new Date());
        this.updateById(order);
        writeStatusLog(orderId, from, "PENDING_SHIP", "USER", userId, null, "支付成功");
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
        if (!"PENDING_PAY".equals(order.getStatus()) && !"PAID".equals(order.getStatus()) && !"PENDING_SHIP".equals(order.getStatus()))
            return Result.error("当前状态（" + order.getStatus() + "）无法取消");

        restoreStock(order, userId);
        rollbackDiscounts(order, "取消订单回滚");

        String from = order.getStatus();
        order.setStatus("CANCELLED");
        order.setCancelTime(new Date());
        order.setUpdateTime(new Date());
        this.updateById(order);
        writeStatusLog(orderId, from, "CANCELLED", "USER", userId, null, "用户取消订单");
        return Result.success("订单已取消");
    }

    /** 确认收货（shipped/pending_received→completed） */
    public Result<?> confirmReceipt(Integer orderId, Integer userId) {
        Order order = this.getById(orderId);
        if (order == null) return Result.error("订单不存在");
        if (!order.getUserId().equals(userId)) return Result.error("无权操作");
        String status = order.getStatus();
        if (!"SHIPPING".equals(status) && !"PENDING_RECEIVED".equals(status)) {
            return Result.error("当前订单状态不允许确认收货");
        }
        String from = order.getStatus();
        order.setStatus("COMPLETED");
        order.setConfirmTime(new Date());
        order.setCompleteTime(new Date());
        order.setUpdateTime(new Date());
        this.updateById(order);
        writeStatusLog(orderId, from, "COMPLETED", "USER", userId, null, "确认收货");
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

    /** 管理员查看订单详情（可查看任意订单） */
    public Result<?> adminGetOrderDetail(Integer orderId) {
        Order order = this.getById(orderId);
        if (order == null) return Result.error("订单不存在");
        LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderItem::getOrderId, orderId);
        order.setItems(orderItemMapper.selectList(wrapper));

        if (order.getReceiverSnapshot() != null && !order.getReceiverSnapshot().isEmpty()) {
            String[] parts = order.getReceiverSnapshot().split(" ");
            if (parts.length >= 2) {
                order.setReceiverName(parts[0]);
                order.setReceiverPhone(parts[1]);
                if (parts.length >= 3) order.setReceiverAddress(parts[2]);
            }
        }

        DeliveryTask task = deliveryTaskMapper.selectOne(new LambdaQueryWrapper<DeliveryTask>()
                .eq(DeliveryTask::getOrderId, orderId));
        if (task != null) {
            order.setPickupTime(task.getPickupTime());
            order.setDeliverTime(task.getDeliverTime());
        }
        return Result.success(order);
    }

    /** 管理员发货（paid→shipped） */
    public Result<?> shipOrder(Integer orderId, Integer operatorId, String expressCompany, String expressNo) {
        Order order = this.getById(orderId);
        if (order == null) return Result.error("订单不存在");
        if (!"PENDING_SHIP".equals(order.getStatus())) return Result.error("只有待发货订单才能发货");
        String from = order.getStatus();
        order.setStatus("SHIPPING");
        order.setShipTime(new Date());
        order.setUpdateTime(new Date());
        if (expressCompany != null) order.setExpressCompany(expressCompany);
        if (expressNo != null) order.setExpressNo(expressNo);
        this.updateById(order);
        writeStatusLog(orderId, from, "SHIPPING", "ADMIN", operatorId, null,
                "管理员发货" + (expressCompany != null && expressNo != null ? "，快递：" + expressCompany + " " + expressNo : ""));
        return Result.success("发货成功");
    }

    /** 后台按订单分配配送员 */
    @Transactional
    public Result<?> assignCourier(Integer orderId, Integer courierId, Integer adminId) {
        Order order = this.getById(orderId);
        if (order == null) return Result.error("订单不存在");
        if (!"PENDING_SHIP".equals(order.getStatus()) && !"SHIPPING".equals(order.getStatus())) {
            return Result.error("当前订单状态不支持分配配送员");
        }
        Courier courier = courierMapper.selectById(courierId);
        if (courier == null) return Result.error("配送员不存在");

        DeliveryTask task = deliveryTaskMapper.selectOne(new LambdaQueryWrapper<DeliveryTask>()
                .eq(DeliveryTask::getOrderId, orderId));
        if (task == null) {
            task = new DeliveryTask();
            task.setTaskId(deliveryTaskMapper.getNextId());
            task.setOrderId(orderId);
            task.setCourierId(courierId);
            task.setStatus("ASSIGNED");
            task.setAssignTime(new Date());
            deliveryTaskMapper.insert(task);
        } else {
            task.setCourierId(courierId);
            task.setStatus("ASSIGNED");
            task.setAssignTime(new Date());
            deliveryTaskMapper.updateById(task);
        }

        writeStatusLog(orderId, order.getStatus(), order.getStatus(), "ADMIN", adminId, null,
                "分配配送员：" + courier.getCourierName());
        return Result.success(task);
    }

    /** 管理员强制取消（✅ 同步退还库存） */
    @Transactional
    public Result<?> adminCancelOrder(Integer orderId, Integer operatorId, String reason) {
        Order order = this.getById(orderId);
        if (order == null) return Result.error("订单不存在");
        if ("COMPLETED".equals(order.getStatus()) || "CANCELLED".equals(order.getStatus()) || "REFUNDED".equals(order.getStatus()))
            return Result.error("已完成或已取消的订单不能操作");

        restoreStock(order, operatorId);
        rollbackDiscounts(order, "管理员取消回滚");

        String from = order.getStatus();
        order.setStatus("CANCELLED");
        order.setCancelTime(new Date());
        order.setCancelReason(reason);
        order.setUpdateTime(new Date());
        this.updateById(order);
        writeStatusLog(orderId, from, "CANCELLED", "ADMIN", operatorId, null, reason != null ? ("管理员取消：" + reason) : "管理员取消");
        return Result.success("订单已取消");
    }

    /** 管理员修改收货地址 */
    public Result<?> updateOrderAddress(Integer orderId, Integer adminId,
                                       String name, String phone, String address) {
        Order order = this.getById(orderId);
        if (order == null) return Result.error("订单不存在");
        if ("COMPLETED".equals(order.getStatus()) || "CANCELLED".equals(order.getStatus()) || "REFUNDED".equals(order.getStatus()))
            return Result.error("已完成或已取消的订单不能修改地址");

        String oldSnapshot = order.getReceiverSnapshot();
        String newSnapshot = (name != null ? name : "") + " "
                + (phone != null ? phone : "") + " "
                + (address != null ? address : "");
        order.setReceiverSnapshot(newSnapshot.trim());
        order.setUpdateTime(new Date());
        this.updateById(order);
        writeStatusLog(orderId, order.getStatus(), order.getStatus(), "ADMIN", adminId, null,
                "修改收货地址：" + oldSnapshot + " → " + newSnapshot);
        return Result.success("地址已更新");
    }

    /** 收银退款（整单退款：回滚库存+标记REFUNDED，不走取消） */
    @Transactional
    public Result<?> refundCashierOrder(Integer orderId, Integer cashierId, String reason) {
        Order order = this.getById(orderId);
        if (order == null) return Result.error("订单不存在");
        if (!"CASHIER".equals(order.getSource())) return Result.error("仅支持收银订单退款");
        if (!"COMPLETED".equals(order.getStatus())) return Result.error("当前订单状态不允许退款");

        restoreStock(order, cashierId);
        // 收银订单没有券/积分抵扣，这里不回滚折扣
        String from = order.getStatus();
        order.setStatus("REFUNDED");
        order.setRefundAmount(order.getPayAmount());
        order.setRefundTime(new Date());
        order.setUpdateTime(new Date());
        this.updateById(order);

        writeStatusLog(orderId, from, "REFUNDED", "ADMIN", cashierId, null,
                "收银退款" + (reason != null && !reason.isEmpty() ? ("：" + reason) : ""));
        return Result.success("退款成功");
    }

    /**
     * 收银台下单（线下POS，source=cashier）
     * ✅ 同样支持 SKU 库存扣减
     */
    @Transactional
    public Result<?> cashierCreateOrder(Integer cashierId, Integer userId, List<CreateOrderRequest.CartItem> cartItems,
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

        String orderNo = "POS" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())
                + String.format("%04d", new Random().nextInt(10000));

        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(userId);  // 收银会员ID（可为null表示散客）
        order.setTotalAmount(totalAmount);
        order.setDiscountAmount(0.0);
        order.setCouponDiscount(0.0);
        order.setPointsDeductAmount(0.0);
        order.setFreightAmount(0.0);
        order.setPayAmount(totalAmount);
        order.setStatus("COMPLETED");
        String pm = payMethod != null ? payMethod.toUpperCase() : "CASH";
        if (!pm.matches("^(CASH|WECHAT|ALIPAY|MEMBER_CARD)$")) {
            pm = "CASH";
        }
        order.setPayMethod(pm);
        order.setSource("CASHIER");
        order.setCreateTime(new Date());
        order.setPayTime(new Date());
        order.setCompleteTime(new Date());
        order.setUpdateTime(new Date());
        order.setOrderId(orderMapper.getNextId());
        orderMapper.insert(order);
        writeStatusLog(order.getOrderId(), null, "COMPLETED", "ADMIN", cashierId, null, "收银台完成交易");

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
            orderItem.setItemId(orderItemMapper.getNextId());
            orderItemMapper.insert(orderItem);

            if (sku != null) {
                sku.setStock(sku.getStock() - item.getQuantity());
                productSkuMapper.updateById(sku);
            } else {
                product.setStock(product.getStock() - item.getQuantity());
            }
            product.setSalesCount(product.getSalesCount() + item.getQuantity());
            productMapper.updateById(product);

            writeInventoryLog(product.getProductId(), item.getSkuId(), "ORDER_OUT",
                    -item.getQuantity(), product.getStock(), order.getOrderId(),
                    "收银台出库，单号: " + orderNo, cashierId);
        }

        double change = receivedAmount != null ? receivedAmount - totalAmount : 0;
        Map<String, Object> data = new HashMap<>();
        data.put("orderId", order.getOrderId());
        data.put("orderNo", orderNo);
        data.put("totalAmount", totalAmount);
        data.put("receivedAmount", receivedAmount);
        data.put("change", Math.max(0, change));
        return Result.success(data);
    }

    // ==================== 收银台端 ====================
    /** C-46 再次购买：将历史订单商品加入购物车 */
    public Result<?> reorder(Integer orderId, Integer userId) {
        Order order = this.getById(orderId);
        if (order == null) return Result.error("订单不存在");
        if (!order.getUserId().equals(userId)) return Result.error("无权操作");

        LambdaQueryWrapper<OrderItem> iw = new LambdaQueryWrapper<>();
        iw.eq(OrderItem::getOrderId, orderId);
        List<OrderItem> items = orderItemMapper.selectList(iw);
        if (items == null || items.isEmpty()) return Result.error("订单无商品");

        int count = 0;
        for (OrderItem item : items) {
            Product product = productMapper.selectById(item.getProductId());
            if (product == null || product.getIsDeleted() == 1 || !"active".equals(product.getStatus())) continue;

            LambdaQueryWrapper<Cart> cw = new LambdaQueryWrapper<>();
            cw.eq(Cart::getUserId, userId).eq(Cart::getProductId, item.getProductId());
            if (item.getSkuId() != null) cw.eq(Cart::getSkuId, item.getSkuId());
            else cw.isNull(Cart::getSkuId);
            Cart existing = cartMapper.selectOne(cw);

            if (existing != null) {
                existing.setQuantity(existing.getQuantity() + item.getQuantity());
                cartMapper.updateById(existing);
            } else {
                Cart cartItem = new Cart();
                cartItem.setUserId(userId);
                cartItem.setProductId(item.getProductId());
                cartItem.setSkuId(item.getSkuId());
                cartItem.setQuantity(item.getQuantity());
                cartItem.setAddTime(new Date());
                cartMapper.insert(cartItem);
            }
            count++;
        }
        return Result.success("已加入购物车（" + count + " 件商品）");
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
            } else {
                product.setStock(product.getStock() + item.getQuantity());
            }
            product.setSalesCount(Math.max(0, product.getSalesCount() - item.getQuantity()));
            productMapper.updateById(product);

            writeInventoryLog(product.getProductId(), item.getSkuId(), "MANUAL",
                    item.getQuantity(), product.getStock(), order.getOrderId(),
                    "取消回库，单号: " + order.getOrderNo(), operatorId);
        }
    }

    private void rollbackDiscounts(Order order, String remark) {
        if (order == null) return;
        // 返还积分
        if (order.getPointsUsed() != null && order.getPointsUsed() > 0) {
            User user = userMapper.selectById(order.getUserId());
            if (user != null) {
                int before = user.getPoints() != null ? user.getPoints() : 0;
                int after = before + order.getPointsUsed();
                user.setPoints(after);
                userMapper.updateById(user);

                PointsLog log = new PointsLog();
                log.setUserId(order.getUserId());
                log.setChangeAmount(order.getPointsUsed());
                log.setBalanceAfter(after);
                log.setReason("REFUND_ROLLBACK");
                log.setRefId(order.getOrderId());
                log.setOperatorId(null);
                log.setCreateTime(new Date());
                log.setLogId(pointsLogMapper.getNextId());
                pointsLogMapper.insert(log);
            }
        }
        // 返还优惠券
        if (order.getUcId() != null) {
            UserCoupon uc = userCouponMapper.selectById(order.getUcId());
            if (uc != null && "used".equalsIgnoreCase(uc.getStatus())) {
                uc.setStatus("unused");
                uc.setOrderId(null);
                uc.setUseTime(null);
                userCouponMapper.updateById(uc);
            }
        }
        // 订单金额字段不回写（保留历史）
    }

    private void markUserCouponUsed(Integer ucId, Integer orderId) {
        UserCoupon uc = userCouponMapper.selectById(ucId);
        if (uc == null) return;
        uc.setStatus("used");
        uc.setOrderId(orderId);
        uc.setUseTime(new Date());
        userCouponMapper.updateById(uc);
    }

    private void deductUserPoints(Integer userId, int pointsUsed, Integer orderId) {
        if (pointsUsed <= 0) return;
        User user = userMapper.selectById(userId);
        if (user == null) return;
        int before = user.getPoints() != null ? user.getPoints() : 0;
        int after = before - pointsUsed;
        if (after < 0) after = 0;
        user.setPoints(after);
        userMapper.updateById(user);

        PointsLog log = new PointsLog();
        log.setUserId(userId);
        log.setChangeAmount(-pointsUsed);
        log.setBalanceAfter(after);
        log.setReason("ORDER_DEDUCT");
        log.setRefId(orderId);
        log.setOperatorId(null);
        log.setCreateTime(new Date());
        log.setLogId(pointsLogMapper.getNextId());
        pointsLogMapper.insert(log);
    }

    private static class DiscountCalc {
        Integer couponId;
        Integer ucId;
        int pointsUsed;
        double couponDiscount;
        double pointsDeductAmount;
    }

    private DiscountCalc calcDiscount(Integer userId,
                                      List<CreateOrderRequest.CartItem> cartItems,
                                      List<Product> productList,
                                      double totalAmount,
                                      Integer selectedCouponId,
                                      Integer requestedPointsUsed) {
        DiscountCalc out = new DiscountCalc();
        out.pointsUsed = 0;
        out.couponDiscount = 0;
        out.pointsDeductAmount = 0;

        // 1) 计算优惠券（优先用指定券；否则自动选最优）
        CouponPick pick = pickCoupon(userId, selectedCouponId, cartItems, productList, totalAmount);
        if (pick != null) {
            out.couponId = pick.couponId;
            out.ucId = pick.ucId;
            out.couponDiscount = pick.discountAmount;
        }

        // 2) 计算积分抵扣（最多抵扣30%）
        User user = userMapper.selectById(userId);
        int userPoints = user != null && user.getPoints() != null ? user.getPoints() : 0;
        int req = requestedPointsUsed != null ? requestedPointsUsed : 0;
        if (req < 0) req = 0;

        double base = Math.max(0, totalAmount - out.couponDiscount);
        double maxDeductMoney = base * 0.30;
        int maxPointsByRule = (int) Math.floor(maxDeductMoney / POINTS_TO_CASH_RATE);
        int used = Math.min(req, Math.min(userPoints, maxPointsByRule));
        out.pointsUsed = used;
        out.pointsDeductAmount = used * POINTS_TO_CASH_RATE;
        return out;
    }

    private static class CouponPick {
        Integer couponId;
        Integer ucId;
        double discountAmount;
    }

    private CouponPick pickCoupon(Integer userId,
                                  Integer selectedCouponId,
                                  List<CreateOrderRequest.CartItem> cartItems,
                                  List<Product> products,
                                  double totalAmount) {
        Date now = new Date();
        List<UserCoupon> userCoupons = userCouponMapper.selectList(new LambdaQueryWrapper<UserCoupon>()
                .eq(UserCoupon::getUserId, userId)
                .eq(UserCoupon::getStatus, "unused"));

        CouponPick best = null;

        for (UserCoupon uc : userCoupons) {
            Coupon c = couponMapper.selectById(uc.getCouponId());
            if (c == null) continue;
            if (!"active".equalsIgnoreCase(c.getStatus())) continue;
            if (c.getStartTime() != null && now.before(c.getStartTime())) continue;
            if (c.getEndTime() != null && now.after(c.getEndTime())) continue;
            if (selectedCouponId != null && !selectedCouponId.equals(c.getCouponId())) continue;

            double discount = computeCouponDiscount(c, cartItems, products, totalAmount);
            if (discount <= 0) continue;

            if (best == null || discount > best.discountAmount) {
                best = new CouponPick();
                best.couponId = c.getCouponId();
                best.ucId = uc.getUcId();
                best.discountAmount = discount;
            }

            if (selectedCouponId != null) {
                // 指定券只需要找一个可用的记录
                break;
            }
        }
        return best;
    }

    private double computeCouponDiscount(Coupon c,
                                         List<CreateOrderRequest.CartItem> cartItems,
                                         List<Product> products,
                                         double totalAmount) {
        if (c == null) return 0;
        double min = c.getMinAmount() != null ? c.getMinAmount() : 0;

        // category券：只计算指定分类商品金额
        double base = totalAmount;
        if ("category".equalsIgnoreCase(c.getCouponType()) && c.getCategoryId() != null) {
            double sum = 0;
            for (int i = 0; i < cartItems.size(); i++) {
                CreateOrderRequest.CartItem it = cartItems.get(i);
                Product p = products.get(i);
                if (p != null && c.getCategoryId().equals(p.getCategoryId())) {
                    // sku价已体现在 totalAmount 里，但这里需要按 item 重新算成交价
                    double unit = p.getPrice() != null ? p.getPrice() : 0;
                    sum += unit * it.getQuantity();
                }
            }
            base = sum;
        }

        if (base < min) return 0;

        double face = c.getFaceValue() != null ? c.getFaceValue() : 0;
        if ("discount".equalsIgnoreCase(c.getCouponType())) {
            // 约定：faceValue 为折扣率（0~1），如 0.9 表示 9折
            if (face <= 0 || face >= 1) return 0;
            return round2(base * (1 - face));
        }

        // 满减/品类满减：faceValue 视为直接抵扣金额
        return round2(Math.min(face, base));
    }

    private void writeStatusLog(Integer orderId, String from, String to,
                                String operatorType, Integer operatorId, String operatorName, String remark) {
        OrderStatusLog log = new OrderStatusLog();
        log.setOrderId(orderId);
        log.setFromStatus(from);
        log.setToStatus(to);
        log.setOperatorType(operatorType);
        log.setOperatorId(operatorId);
        log.setOperatorName(operatorName);
        log.setRemark(remark);
        log.setCreateTime(new Date());
        log.setLogId(orderStatusLogMapper.getNextId());
        orderStatusLogMapper.insert(log);
    }

    private static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
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
        log.setLogId(inventoryLogMapper.getNextId());
        inventoryLogMapper.insert(log);
    }

}
