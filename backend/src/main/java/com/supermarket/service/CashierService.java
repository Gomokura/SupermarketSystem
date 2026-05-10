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
import java.util.Calendar;
import java.util.Comparator;
import java.util.LinkedHashMap;
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
    private final PointsService pointsService;

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
                          CashierShiftMapper cashierShiftMapper,
                          PointsService pointsService) {
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
        this.pointsService = pointsService;
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

    /** 收银端首页概览 */
    public Result<?> getDashboard(Integer cashierId) {
        return Result.success(buildReport(cashierId, new Date()));
    }

    /** 收银员日结报表 */
    public Result<?> getDailyReport(Integer cashierId) {
        return Result.success(buildReport(cashierId, new Date()));
    }

    private Map<String, Object> buildReport(Integer cashierId, Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date startOfDay = calendar.getTime();
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        Date endOfDay = calendar.getTime();

        List<CashierRecord> todayRecords = cashierRecordMapper.selectList(new LambdaQueryWrapper<CashierRecord>()
                .eq(CashierRecord::getCashierId, cashierId)
                .ge(CashierRecord::getCreateTime, startOfDay)
                .lt(CashierRecord::getCreateTime, endOfDay)
                .orderByDesc(CashierRecord::getCreateTime));

        double todaySales = 0.0;
        double cashAmount = 0.0;
        double scanAmount = 0.0;
        for (CashierRecord record : todayRecords) {
            double amount = record.getPayAmount() != null ? record.getPayAmount() : 0.0;
            todaySales += amount;
            if ("CASH".equalsIgnoreCase(record.getPayMethod())) {
                cashAmount += amount;
            } else {
                scanAmount += amount;
            }
        }

        List<Map<String, Object>> recentOrders = new ArrayList<>();
        List<CashierRecord> recentRecords = cashierRecordMapper.selectList(new LambdaQueryWrapper<CashierRecord>()
                .eq(CashierRecord::getCashierId, cashierId)
                .orderByDesc(CashierRecord::getCreateTime)
                .last("FETCH FIRST 8 ROWS ONLY"));
        for (CashierRecord record : todayRecords) {
            Map<String, Object> item = new HashMap<>();
            item.put("recordId", record.getRecordId());
            item.put("memberPhone", record.getMemberPhone());
            item.put("payAmount", record.getPayAmount());
            item.put("payMethod", record.getPayMethod());
            item.put("createTime", record.getCreateTime());
            List<CashierRecordItem> items = cashierRecordItemMapper.selectList(new LambdaQueryWrapper<CashierRecordItem>()
                    .eq(CashierRecordItem::getRecordId, record.getRecordId()));
            item.put("items", items);
            item.put("itemSummary", items.stream()
                    .limit(2)
                    .map(i -> i.getProductName() + " x" + i.getQuantity())
                    .reduce((a, b) -> a + "，" + b)
                    .orElse("收银订单"));
            recentOrders.add(item);
        }

        Map<String, Map<String, Object>> hotMap = new LinkedHashMap<>();
        for (CashierRecord record : recentRecords) {
            List<CashierRecordItem> items = cashierRecordItemMapper.selectList(new LambdaQueryWrapper<CashierRecordItem>()
                    .eq(CashierRecordItem::getRecordId, record.getRecordId()));
            for (CashierRecordItem row : items) {
                String key = String.valueOf(row.getProductId());
                Map<String, Object> hot = hotMap.computeIfAbsent(key, ignored -> {
                    Map<String, Object> value = new HashMap<>();
                    value.put("productName", row.getProductName());
                    value.put("quantity", 0);
                    value.put("amount", 0.0);
                    return value;
                });
                hot.put("quantity", ((Number) hot.get("quantity")).intValue() + (row.getQuantity() != null ? row.getQuantity() : 0));
                hot.put("amount", ((Number) hot.get("amount")).doubleValue() + (row.getSubtotal() != null ? row.getSubtotal() : 0.0));
            }
        }
        List<Map<String, Object>> hotProducts = new ArrayList<>(hotMap.values());
        hotProducts.sort(Comparator.comparingInt(v -> -((Number) v.get("quantity")).intValue()));
        if (hotProducts.size() > 5) hotProducts = hotProducts.subList(0, 5);

        Map<String, Object> data = new HashMap<>();
        data.put("todayOrderCount", todayRecords.size());
        data.put("todaySales", Math.round(todaySales * 100.0) / 100.0);
        data.put("cashAmount", Math.round(cashAmount * 100.0) / 100.0);
        data.put("scanAmount", Math.round(scanAmount * 100.0) / 100.0);
        data.put("reportDate", startOfDay);
        data.put("recentOrders", recentOrders);
        data.put("hotProducts", hotProducts);
        return data;
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
                              Integer requestedPointsUsed,
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
        String normalizedPayMethod = normalizeCashierPayMethod(payMethod);
        Result<?> orderRet = orderService.cashierCreateOrder(cashierId, userId, orderItems, normalizedPayMethod, receivedAmount);
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

        // 3) 计算优惠券和积分抵扣（POS 场景：折扣直接从 totalAmount 减）
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
        int pointsUsed = 0;
        double pointsDeductAmount = 0.0;
        if (userId != null && requestedPointsUsed != null && requestedPointsUsed > 0 && totalAmount != null) {
            User user = userMapper.selectById(userId);
            int availablePoints = user != null && user.getPoints() != null ? user.getPoints() : 0;
            double pointsBase = Math.max(0, totalAmount - discountAmount);
            int maxPointsByRule = (int) Math.floor((pointsBase * 0.2) / 0.01);
            pointsUsed = Math.min(requestedPointsUsed, Math.min(availablePoints, maxPointsByRule));
            pointsDeductAmount = Math.round(pointsUsed * 0.01 * 100.0) / 100.0;
        }
        double totalDiscountAmount = Math.round((discountAmount + pointsDeductAmount) * 100.0) / 100.0;
        double payAmount = Math.max(0, (totalAmount != null ? totalAmount : 0.0) - totalDiscountAmount);

        double change = 0.0;
        if ("CASH".equalsIgnoreCase(normalizedPayMethod)) {
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
        record.setDiscountAmount(totalDiscountAmount);
        record.setCouponId(couponId);
        record.setUcId(ucId);
        record.setPayAmount(payAmount);
        record.setPayMethod(normalizedPayMethod);
        record.setReceivedAmount(receivedAmount);
        record.setChangeAmount(change);
        record.setCashierId(cashierId);
        record.setCreateTime(new Date());
        record.setRecordId(cashierRecordMapper.getNextId());
        cashierRecordMapper.insert(record);

        if (pointsUsed > 0) {
            pointsService.deductPoints(userId, pointsUsed, "CASHIER_DEDUCT", orderId, cashierId);
            Order order = orderMapper.selectById(orderId);
            if (order != null) {
                order.setPointsUsed(pointsUsed);
                order.setPointsDeductAmount(pointsDeductAmount);
                order.setDiscountAmount(totalDiscountAmount);
                order.setPayAmount(payAmount);
                order.setUpdateTime(new Date());
                orderMapper.updateById(order);
            }
        }

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
        recordShiftOrder(cashierId, normalizedPayMethod, payAmount);

        Map<String, Object> data = new HashMap<>();
        data.put("orderId", orderId);
        data.put("recordId", record.getRecordId());
        data.put("orderNo", orderData != null ? orderData.get("orderNo") : null);
        data.put("totalAmount", totalAmount);
        data.put("discountAmount", totalDiscountAmount);
        data.put("pointsUsed", pointsUsed);
        data.put("pointsDeductAmount", pointsDeductAmount);
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
        if (phone != null && !phone.isEmpty()) {
            User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getPhone, phone));
            if (user == null) {
                page.setRecords(new ArrayList<>());
                page.setTotal(0);
                return Result.success(page);
            }
            w.eq(Order::getUserId, user.getUserId());
        }
        w.orderByDesc(Order::getCreateTime);
        orderMapper.selectPage(page, w);
        // 填充 items
        for (Order o : page.getRecords()) {
            List<OrderItem> items = orderItemMapper.selectList(
                    new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, o.getOrderId()));
            o.setItems(items);
            fillCashierOrderCustomer(o);
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

    private String normalizeCashierPayMethod(String payMethod) {
        if (payMethod == null || payMethod.isBlank()) return "CASH";
        String pm = payMethod.toUpperCase();
        if ("MOCK".equals(pm) || "MOCK_CARD".equals(pm) || "SCAN".equals(pm)) return "ALIPAY";
        if (pm.matches("^(CASH|WECHAT|ALIPAY|MEMBER_CARD)$")) return pm;
        return "CASH";
    }

    private void fillCashierOrderCustomer(Order order) {
        if (order == null) return;
        if (order.getReceiverSnapshot() != null && !order.getReceiverSnapshot().isBlank()) {
            String[] parts = order.getReceiverSnapshot().trim().split("\\s+", 3);
            if (parts.length > 0) order.setReceiverName(parts[0]);
            if (parts.length > 1) order.setReceiverPhone(parts[1]);
            if (parts.length > 2) order.setReceiverAddress(parts[2]);
            return;
        }
        if (order.getUserId() == null) {
            order.setReceiverName("散客");
            return;
        }
        User user = userMapper.selectById(order.getUserId());
        if (user != null) {
            String name = user.getNickname();
            if (name == null || name.isBlank()) name = user.getRealName();
            if (name == null || name.isBlank()) name = user.getUsername();
            order.setReceiverName(name);
            order.setReceiverPhone(user.getPhone());
        }
    }
}
