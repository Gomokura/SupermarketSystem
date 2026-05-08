package com.supermarket.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.supermarket.common.BusinessException;
import com.supermarket.common.Result;
import com.supermarket.dto.CreateOrderRequest;
import com.supermarket.entity.CashierRecord;
import com.supermarket.entity.CashierRecordItem;
import com.supermarket.entity.CashierShift;
import com.supermarket.entity.Order;
import com.supermarket.entity.OrderItem;
import com.supermarket.entity.Product;
import com.supermarket.entity.ProductSku;
import com.supermarket.entity.User;
import com.supermarket.entity.UserCoupon;
import com.supermarket.mapper.CashierShiftMapper;
import com.supermarket.mapper.CashierRecordMapper;
import com.supermarket.mapper.CashierRecordItemMapper;
import com.supermarket.mapper.CouponMapper;
import com.supermarket.mapper.OrderItemMapper;
import com.supermarket.mapper.OrderMapper;
import com.supermarket.mapper.ProductMapper;
import com.supermarket.mapper.ProductSkuMapper;
import com.supermarket.mapper.UserCouponMapper;
import com.supermarket.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class CashierService extends ServiceImpl<CashierShiftMapper, CashierShift> {

    private final CashierRecordMapper cashierRecordMapper;
    private final CashierRecordItemMapper cashierRecordItemMapper;
    private final ProductMapper productMapper;
    private final ProductSkuMapper productSkuMapper;
    private final UserMapper userMapper;
    private final UserCouponMapper userCouponMapper;
    private final CouponMapper couponMapper;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderService orderService;
    private final CashierShiftMapper cashierShiftMapper;

    public CashierService(CashierRecordMapper cashierRecordMapper,
                          CashierRecordItemMapper cashierRecordItemMapper,
                          ProductMapper productMapper,
                          ProductSkuMapper productSkuMapper,
                          UserMapper userMapper,
                          UserCouponMapper userCouponMapper,
                          CouponMapper couponMapper,
                          OrderMapper orderMapper,
                          OrderItemMapper orderItemMapper,
                          OrderService orderService,
                          CashierShiftMapper cashierShiftMapper) {
        this.cashierRecordMapper = cashierRecordMapper;
        this.cashierRecordItemMapper = cashierRecordItemMapper;
        this.productMapper = productMapper;
        this.productSkuMapper = productSkuMapper;
        this.userMapper = userMapper;
        this.userCouponMapper = userCouponMapper;
        this.couponMapper = couponMapper;
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.orderService = orderService;
        this.cashierShiftMapper = cashierShiftMapper;
    }

    /** 开班 */
    @Transactional
    public Result<?> openShift(Integer cashierId, Double startCash) {
        // 检查是否已有开班中的班次
        LambdaQueryWrapper<CashierShift> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CashierShift::getCashierId, cashierId).eq(CashierShift::getStatus, "OPEN");
        if (this.count(wrapper) > 0) return Result.error("已有开班中的班次，请先交班");

        CashierShift shift = new CashierShift();
        shift.setCashierId(cashierId);
        shift.setStartCash(startCash != null ? startCash : 0.0);
        shift.setTotalOrderCount(0);
        shift.setTotalCashAmount(0.0);
        shift.setTotalMockAmount(0.0);
        shift.setStatus("OPEN");
        shift.setStartTime(new Date());
        shift.setShiftId(cashierShiftMapper.getNextId());
        this.save(shift);
        return Result.success(shift);
    }

    /** 获取当前班次 */
    public Result<?> getCurrentShift(Integer cashierId) {
        LambdaQueryWrapper<CashierShift> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CashierShift::getCashierId, cashierId).eq(CashierShift::getStatus, "OPEN");
        CashierShift shift = this.getOne(wrapper);
        if (shift == null) return Result.error(404, "无开班中的班次");
        return Result.success(shift);
    }

    /** 交班 */
    @Transactional
    public Result<?> closeShift(Integer cashierId, Double endCash) {
        LambdaQueryWrapper<CashierShift> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CashierShift::getCashierId, cashierId).eq(CashierShift::getStatus, "OPEN");
        CashierShift shift = this.getOne(wrapper);
        if (shift == null) return Result.error("无开班中的班次");

        shift.setEndCash(endCash);
        shift.setStatus("CLOSED");
        shift.setEndTime(new Date());
        Double cashDiff = endCash != null
                ? (endCash - (shift.getStartCash() != null ? shift.getStartCash() : 0.0)
                - (shift.getTotalCashAmount() != null ? shift.getTotalCashAmount() : 0.0))
                : null;
        shift.setCashDiff(cashDiff);
        this.updateById(shift);

        Map<String, Object> summary = new HashMap<>();
        summary.put("shift", shift);
        summary.put("cashDiff", cashDiff);
        return Result.success(summary);
    }

    /** 班次统计更新（由 OrderService 调用） */
    @Transactional
    public void recordShiftOrder(Integer cashierId, String payMethod, Double amount) {
        LambdaQueryWrapper<CashierShift> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CashierShift::getCashierId, cashierId).eq(CashierShift::getStatus, "OPEN");
        CashierShift shift = this.getOne(wrapper);
        if (shift == null) return;
        shift.setTotalOrderCount((shift.getTotalOrderCount() != null ? shift.getTotalOrderCount() : 0) + 1);
        if ("CASH".equalsIgnoreCase(payMethod)) {
            shift.setTotalCashAmount((shift.getTotalCashAmount() != null ? shift.getTotalCashAmount() : 0.0) + amount);
        } else {
            shift.setTotalMockAmount((shift.getTotalMockAmount() != null ? shift.getTotalMockAmount() : 0.0) + amount);
        }
        this.updateById(shift);
    }

    /** 历史班次列表 */
    public Result<?> getShiftHistory(Integer cashierId) {
        LambdaQueryWrapper<CashierShift> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CashierShift::getCashierId, cashierId).orderByDesc(CashierShift::getStartTime);
        List<CashierShift> list = this.list(wrapper);
        return Result.success(list);
    }

    /** K-05: 搜索商品（支持 keyword 或 barcode） */
    public Result<?> searchProducts(String keywordOrBarcode, Integer limit) {
        int n = (limit != null && limit > 0) ? Math.min(limit, 50) : 20;
        LambdaQueryWrapper<Product> w = new LambdaQueryWrapper<>();
        w.eq(Product::getIsDeleted, 0)
                .and(x -> x.like(Product::getProductName, keywordOrBarcode)
                        .or().like(Product::getBarcode, keywordOrBarcode))
                .orderByAsc(Product::getProductName);
        List<Product> list = productMapper.selectList(w);
        if (list.size() > n) list = list.subList(0, n);
        return Result.success(list);
    }

    /**
        * K-10~K-12: 收银结账
        * 说明：会创建一笔 CASHIER 订单（用于报表/库存）+ 一笔 cashier_records（用于班次报表/POS小票）
        */
    @Transactional
    public Result<?> checkout(Integer cashierId,
                              String memberPhone,
                              Integer couponId,
                              String payMethod,
                              Double receivedAmount,
                              List<Map<String, Object>> items) {
        CashierShift shift = (CashierShift) getCurrentShift(cashierId).getData();
        if (shift == null) throw new BusinessException("请先开班");

        Integer userId = null;
        String memberSnapshot = null;
        Integer ucId = null;
        if (memberPhone != null && !memberPhone.isEmpty()) {
            User u = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getPhone, memberPhone));
            if (u != null) {
                userId = u.getUserId();
                memberSnapshot = memberPhone;
                if (couponId != null) {
                    UserCoupon uc = userCouponMapper.selectOne(new LambdaQueryWrapper<UserCoupon>()
                            .eq(UserCoupon::getUserId, userId)
                            .eq(UserCoupon::getCouponId, couponId)
                            .eq(UserCoupon::getStatus, "unused")
                            .orderByAsc(UserCoupon::getGetTime)
                            .last("FETCH FIRST 1 ROWS ONLY"));
                    if (uc != null) ucId = uc.getUcId();
                }
            }
        }

        // 1) 组装为 OrderService 的 items（复用库存扣减与订单落库）
        List<CreateOrderRequest.CartItem> orderItems = new ArrayList<>();
        for (Map<String, Object> raw : items) {
            CreateOrderRequest.CartItem it = new CreateOrderRequest.CartItem();
            it.setProductId((Integer) raw.get("productId"));
            it.setSkuId((Integer) raw.get("skuId"));
            it.setQuantity(((Number) raw.get("quantity")).intValue());
            orderItems.add(it);
        }
        // 2) 创建收银订单（订单侧不做会员券抵扣，券抵扣走 cashier_records 展示；库存仍以订单出库为准）
        Result<?> orderRet = orderService.cashierCreateOrder(cashierId, userId, orderItems, payMethod, receivedAmount);
        if (orderRet.getCode() != 200) {
            throw new BusinessException(orderRet.getMessage());
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> orderData = (Map<String, Object>) orderRet.getData();
        if (orderData == null) {
            throw new BusinessException("订单创建失败");
        }
        Integer orderId = (Integer) orderData.get("orderId");
        Double totalAmount = orderData.get("totalAmount") != null ? ((Number) orderData.get("totalAmount")).doubleValue() : null;
        if (orderId == null || totalAmount == null) {
            throw new BusinessException("订单数据异常");
        }

        // 3) 计算优惠券抵扣（POS 场景：折扣直接从 totalAmount 减）
        double discountAmount = 0.0;
        if (userId != null && couponId != null && ucId != null && totalAmount != null) {
            var c = couponMapper.selectById(couponId);
            if (c != null && "active".equalsIgnoreCase(c.getStatus())) {
                double min = c.getMinAmount() != null ? c.getMinAmount() : 0.0;
                if (totalAmount >= min) {
                    if ("discount".equalsIgnoreCase(c.getCouponType())) {
                        double face = c.getFaceValue() != null ? c.getFaceValue() : 0;
                        if (face > 0 && face < 1) discountAmount = totalAmount * (1 - face);
                    } else {
                        double face = c.getFaceValue() != null ? c.getFaceValue() : 0;
                        discountAmount = Math.min(face, totalAmount);
                    }
                }
            }
        }
        discountAmount = Math.round(discountAmount * 100.0) / 100.0;
        double payAmount = Math.max(0, (totalAmount != null ? totalAmount : 0.0) - discountAmount);

        double change = 0.0;
        if ("CASH".equalsIgnoreCase(payMethod)) {
            if (receivedAmount == null) throw new BusinessException("现金支付必须填写实收金额");
            if (receivedAmount < payAmount) throw new BusinessException("实收金额不足");
            change = Math.round((receivedAmount - payAmount) * 100.0) / 100.0;
        }

        // 4) 写 cashier_records + items
        CashierRecord record = new CashierRecord();
        record.setShiftId(shift.getShiftId());
        record.setUserId(userId);
        record.setMemberPhone(memberSnapshot);
        record.setTotalAmount(totalAmount);
        record.setDiscountAmount(discountAmount);
        record.setCouponId(couponId);
        record.setUcId(ucId);
        record.setPayAmount(payAmount);
        record.setPayMethod(payMethod != null ? payMethod : "CASH");
        record.setReceivedAmount(receivedAmount);
        record.setChangeAmount(change);
        record.setCashierId(cashierId);
        record.setCreateTime(new Date());
        record.setRecordId(cashierRecordMapper.getNextId());
        cashierRecordMapper.insert(record);

        for (CreateOrderRequest.CartItem it : orderItems) {
            Product p = productMapper.selectById(it.getProductId());
            if (p == null) continue;
            Double unit = p.getPrice();
            String skuName = null;
            if (it.getSkuId() != null) {
                ProductSku sku = productSkuMapper.selectById(it.getSkuId());
                if (sku != null) {
                    unit = sku.getPrice();
                    skuName = sku.getSkuName();
                }
            }
            CashierRecordItem cri = new CashierRecordItem();
            cri.setRecordId(record.getRecordId());
            cri.setProductId(it.getProductId());
            cri.setSkuId(it.getSkuId());
            cri.setProductName(p.getProductName());
            cri.setSkuName(skuName);
            cri.setUnitPrice(unit);
            cri.setQuantity(it.getQuantity());
            cri.setSubtotal(Math.round(unit * it.getQuantity() * 100.0) / 100.0);
            cri.setItemId(cashierRecordItemMapper.getNextId());
            cashierRecordItemMapper.insert(cri);
        }

        // 5) 班次统计
        recordShiftOrder(cashierId, payMethod, payAmount);

        Map<String, Object> data = new HashMap<>();
        data.put("orderId", orderId);
        data.put("recordId", record.getRecordId());
        data.put("orderNo", orderData != null ? orderData.get("orderNo") : null);
        data.put("totalAmount", totalAmount);
        data.put("discountAmount", discountAmount);
        data.put("payAmount", payAmount);
        data.put("receivedAmount", receivedAmount);
        data.put("changeAmount", change);
        return Result.success(data);
    }

    /** K-13: 按订单号查询收银订单（用于退款） */
    public Result<?> findCashierOrder(String orderNo) {
        if (orderNo == null || orderNo.isEmpty()) return Result.error("orderNo不能为空");
        Order order = orderMapper.selectOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getOrderNo, orderNo)
                .eq(Order::getSource, "CASHIER"));
        if (order == null) return Result.error(404, "订单不存在");
        List<OrderItem> items = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getOrderId, order.getOrderId()));
        order.setItems(items);
        return Result.success(order);
    }

    /** K-13: 历史订单查询（支持订单号/手机号搜索） */
    public Result<?> getOrderHistory(Integer cashierId, String orderNo, String phone, Integer pageNum, Integer pageSize) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Order> page =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Order> w = new LambdaQueryWrapper<>();
        w.eq(Order::getSource, "CASHIER");
        if (orderNo != null && !orderNo.isEmpty()) w.eq(Order::getOrderNo, orderNo);
        if (phone != null && !phone.isEmpty()) w.eq(Order::getReceiverPhone, phone);
        w.orderByDesc(Order::getCreateTime);
        orderMapper.selectPage(page, w);
        // 填充 items
        for (Order o : page.getRecords()) {
            List<OrderItem> items = orderItemMapper.selectList(
                    new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, o.getOrderId()));
            o.setItems(items);
        }
        return Result.success(page);
    }

    /** K-14: 收银台退款（整单退款，库存回滚） */
    @Transactional
    public Result<?> refundCashierOrder(Integer cashierId, String orderNo, String reason) {
        Order order = orderMapper.selectOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getOrderNo, orderNo)
                .eq(Order::getSource, "CASHIER"));
        if (order == null) return Result.error(404, "订单不存在");
        if (!"COMPLETED".equals(order.getStatus())) return Result.error("只有已完成订单可退款");
        return orderService.refundCashierOrder(order.getOrderId(), cashierId, reason);
    }
}
