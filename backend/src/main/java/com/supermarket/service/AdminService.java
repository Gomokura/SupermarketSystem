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

import java.text.SimpleDateFormat;
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
    @Autowired private CategoryMapper categoryMapper;
    @Autowired private PromotionMapper promotionMapper;
    @Autowired private AuditLogMapper auditLogMapper;
    @Autowired private AdminMapper adminMapper;
    @Autowired private UserCouponMapper userCouponMapper;
    @Autowired private PointsLogMapper pointsLogMapper;
    @Autowired private CouponMapper couponMapper;

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
        ow.eq(Order::getUserId, userId).ne(Order::getStatus, "CANCELLED");
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
                .in(Order::getStatus, "PAID", "PENDING_SHIP", "SHIPPING", "COMPLETED", "PENDING_RECEIVED")
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
        delivery.setStatus("ASSIGNED");
        deliveryMapper.updateById(delivery);
        return Result.success();
    }

    @Transactional
    public Result<?> updateDeliveryStatus(Integer deliveryId, String status) {
        Delivery delivery = deliveryMapper.selectById(deliveryId);
        if (delivery == null) throw new BusinessException(404, "配送记录不存在");
        delivery.setStatus(status);
        if ("DELIVERED".equals(status)) delivery.setDoneTime(new Date());
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
        wrapper.in(Order::getStatus, "PAID", "PENDING_SHIP", "SHIPPING", "COMPLETED", "PENDING_RECEIVED");
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

    // ==================== 统计看板（完整） ====================

    /**
     * B 端：完整统计看板（销售趋势/同比环比/排行/新老客/优惠券核销/积分经济）
     * 说明：只读统计，不改订单/库存/履约流转逻辑。
     */
    public Result<?> getDashboard(Integer days, Integer topN) {
        int d = (days == null || days <= 0) ? 30 : days;
        int n = (topN == null || topN <= 0) ? 10 : topN;

        Date now = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(now);
        // start: 当天 00:00 往前推 d-1 天
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date endDayStart = cal.getTime();

        cal.add(Calendar.DAY_OF_YEAR, -(d - 1));
        Date startDate = cal.getTime();

        Date endDate = now;

        List<String> revenueStatuses = Arrays.asList(
                "PAID", "PENDING_SHIP", "SHIPPING", "PENDING_RECEIVED", "COMPLETED"
        );

        double currentRevenue = 0;
        int currentOrderCount = 0;

        LambdaQueryWrapper<Order> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.ge(Order::getCreateTime, startDate)
                .le(Order::getCreateTime, endDate)
                .in(Order::getStatus, revenueStatuses);
        List<Order> currentOrders = orderMapper.selectList(orderWrapper);
        for (Order o : currentOrders) {
            currentRevenue += o.getPayAmount() != null ? o.getPayAmount() : 0;
            currentOrderCount++;
        }

        // daily trend series
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Map<String, Double> revenueByDay = new LinkedHashMap<>();
        Map<String, Integer> orderCountByDay = new LinkedHashMap<>();
        Calendar dayCal = Calendar.getInstance();
        dayCal.setTime(startDate);
        for (int i = 0; i < d; i++) {
            Date day = dayCal.getTime();
            String label = sdf.format(day);
            revenueByDay.put(label, 0.0);
            orderCountByDay.put(label, 0);
            dayCal.add(Calendar.DAY_OF_YEAR, 1);
        }

        for (Order o : currentOrders) {
            Date t = o.getCreateTime();
            if (t == null) continue;
            String label = sdf.format(t);
            if (!revenueByDay.containsKey(label)) continue;
            revenueByDay.merge(label, o.getPayAmount() != null ? o.getPayAmount() : 0, Double::sum);
            orderCountByDay.merge(label, 1, Integer::sum);
        }

        // YoY
        Calendar prevYearStartCal = Calendar.getInstance();
        prevYearStartCal.setTime(startDate);
        prevYearStartCal.add(Calendar.YEAR, -1);
        Date prevYearStart = prevYearStartCal.getTime();

        Calendar prevYearEndCal = Calendar.getInstance();
        prevYearEndCal.setTime(endDate);
        prevYearEndCal.add(Calendar.YEAR, -1);
        Date prevYearEnd = prevYearEndCal.getTime();

        double prevYearRevenue = 0;
        LambdaQueryWrapper<Order> yoyWrapper = new LambdaQueryWrapper<>();
        yoyWrapper.ge(Order::getCreateTime, prevYearStart)
                .le(Order::getCreateTime, prevYearEnd)
                .in(Order::getStatus, revenueStatuses);
        List<Order> prevYearOrders = orderMapper.selectList(yoyWrapper);
        for (Order o : prevYearOrders) {
            prevYearRevenue += o.getPayAmount() != null ? o.getPayAmount() : 0;
        }

        // MoM (previous period)
        Calendar prevPeriodStartCal = Calendar.getInstance();
        prevPeriodStartCal.setTime(startDate);
        prevPeriodStartCal.add(Calendar.DAY_OF_YEAR, -d);
        Date prevPeriodStart = prevPeriodStartCal.getTime();

        Calendar prevPeriodEndCal = Calendar.getInstance();
        prevPeriodEndCal.setTime(endDate);
        prevPeriodEndCal.add(Calendar.DAY_OF_YEAR, -d);
        Date prevPeriodEnd = prevPeriodEndCal.getTime();

        double prevPeriodRevenue = 0;
        LambdaQueryWrapper<Order> momWrapper = new LambdaQueryWrapper<>();
        momWrapper.ge(Order::getCreateTime, prevPeriodStart)
                .le(Order::getCreateTime, prevPeriodEnd)
                .in(Order::getStatus, revenueStatuses);
        List<Order> prevPeriodOrders = orderMapper.selectList(momWrapper);
        for (Order o : prevPeriodOrders) {
            prevPeriodRevenue += o.getPayAmount() != null ? o.getPayAmount() : 0;
        }

        Double yoyGrowth = prevYearRevenue > 0 ? (currentRevenue - prevYearRevenue) / prevYearRevenue : null;
        Double momGrowth = prevPeriodRevenue > 0 ? (currentRevenue - prevPeriodRevenue) / prevPeriodRevenue : null;

        Map<String, Object> salesTrend = new LinkedHashMap<>();
        salesTrend.put("currentRevenue", Math.round(currentRevenue * 100.0) / 100.0);
        salesTrend.put("currentOrderCount", currentOrderCount);
        salesTrend.put("series", revenueByDay.keySet());
        salesTrend.put("revenue", new ArrayList<>(revenueByDay.values()));
        salesTrend.put("orderCount", new ArrayList<>(orderCountByDay.values()));
        salesTrend.put("yoyRevenue", Math.round(prevYearRevenue * 100.0) / 100.0);
        salesTrend.put("yoyGrowthRate", yoyGrowth);
        salesTrend.put("momRevenue", Math.round(prevPeriodRevenue * 100.0) / 100.0);
        salesTrend.put("momGrowthRate", momGrowth);

        // Rankings: top products/categories by quantity in current period
        List<Integer> orderIds = new ArrayList<>();
        for (Order o : currentOrders) {
            if (o.getOrderId() != null) orderIds.add(o.getOrderId());
        }

        List<Map<String, Object>> topProducts = new ArrayList<>();
        List<Map<String, Object>> topCategories = new ArrayList<>();

        if (!orderIds.isEmpty()) {
            LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
            itemWrapper.in(OrderItem::getOrderId, orderIds);
            List<OrderItem> items = orderItemMapper.selectList(itemWrapper);

            class Agg {
                int qty;
                double revenue;
            }

            Map<Integer, Agg> productAgg = new HashMap<>();
            for (OrderItem it : items) {
                if (it.getProductId() == null || it.getQuantity() == null) continue;
                Agg a = productAgg.computeIfAbsent(it.getProductId(), k -> new Agg());
                a.qty += it.getQuantity();
                a.revenue += it.getSubtotal() != null ? it.getSubtotal() : 0;
            }

            List<Integer> productIdSorted = new ArrayList<>(productAgg.keySet());
            productIdSorted.sort((a, b) -> {
                Agg aa = productAgg.get(a);
                Agg bb = productAgg.get(b);
                return Integer.compare(bb.qty, aa.qty);
            });

            List<Integer> topProductIds = productIdSorted.subList(0, Math.min(n, productIdSorted.size()));

            Map<Integer, Product> productMap = productMapper.selectBatchIds(topProductIds).stream()
                    .collect(java.util.stream.Collectors.toMap(Product::getProductId, p -> p));

            for (Integer pid : topProductIds) {
                Agg a = productAgg.get(pid);
                Product p = productMap.get(pid);
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("productId", pid);
                row.put("productName", p != null ? p.getProductName() : null);
                row.put("categoryId", p != null ? p.getCategoryId() : null);
                row.put("quantity", a.qty);
                row.put("revenue", Math.round(a.revenue * 100.0) / 100.0);
                topProducts.add(row);
            }

            // categories
            Map<Integer, Agg> categoryAgg = new HashMap<>();
            // Need category_id for products in this period
            List<Integer> allProductIds = new ArrayList<>(productAgg.keySet());
            Map<Integer, Product> allProducts = productMapper.selectBatchIds(allProductIds).stream()
                    .collect(java.util.stream.Collectors.toMap(Product::getProductId, p -> p, (x, y) -> x));
            for (Map.Entry<Integer, Agg> e : productAgg.entrySet()) {
                Integer pid = e.getKey();
                Agg pa = e.getValue();
                Product p = allProducts.get(pid);
                if (p == null || p.getCategoryId() == null) continue;
                Agg ca = categoryAgg.computeIfAbsent(p.getCategoryId(), k -> new Agg());
                ca.qty += pa.qty;
                ca.revenue += pa.revenue;
            }

            List<Integer> categoryIds = new ArrayList<>(categoryAgg.keySet());
            categoryIds.sort((a, b) -> {
                Agg aa = categoryAgg.get(a);
                Agg bb = categoryAgg.get(b);
                return Integer.compare(bb.qty, aa.qty);
            });
            List<Integer> topCategoryIds = categoryIds.subList(0, Math.min(n, categoryIds.size()));

            Map<Integer, Category> categoryMap = categoryMapper.selectBatchIds(topCategoryIds).stream()
                    .collect(java.util.stream.Collectors.toMap(Category::getCategoryId, c -> c, (x, y) -> x));
            for (Integer cid : topCategoryIds) {
                Agg ca = categoryAgg.get(cid);
                Category c = categoryMap.get(cid);
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("categoryId", cid);
                row.put("categoryName", c != null ? c.getCategoryName() : null);
                row.put("quantity", ca.qty);
                row.put("revenue", Math.round(ca.revenue * 100.0) / 100.0);
                topCategories.add(row);
            }
        }

        // New/Old customers
        long totalUsers = userMapper.selectCount(null);
        LambdaQueryWrapper<User> newUserWrapper = new LambdaQueryWrapper<>();
        newUserWrapper.ge(User::getCreateTime, startDate);
        long newUsers = userMapper.selectCount(newUserWrapper);
        long oldUsers = Math.max(0, totalUsers - newUsers);

        Map<String, Object> userAnalysis = new LinkedHashMap<>();
        userAnalysis.put("totalUsers", totalUsers);
        userAnalysis.put("newUsers", newUsers);
        userAnalysis.put("oldUsers", oldUsers);

        // issued: getTime in range
        LambdaQueryWrapper<UserCoupon> issuedWrapper = new LambdaQueryWrapper<>();
        issuedWrapper.ge(UserCoupon::getGetTime, startDate)
                .le(UserCoupon::getGetTime, endDate);
        long issued = userCouponMapper.selectCount(issuedWrapper);

        LambdaQueryWrapper<UserCoupon> usedWrapper = new LambdaQueryWrapper<>();
        usedWrapper.eq(UserCoupon::getStatus, "used")
                .ge(UserCoupon::getUseTime, startDate)
                .le(UserCoupon::getUseTime, endDate);
        long used = userCouponMapper.selectCount(usedWrapper);

        LambdaQueryWrapper<UserCoupon> expiredWrapper = new LambdaQueryWrapper<>();
        expiredWrapper.eq(UserCoupon::getStatus, "expired")
                .ge(UserCoupon::getGetTime, startDate)
                .le(UserCoupon::getGetTime, endDate);
        long expired = userCouponMapper.selectCount(expiredWrapper);

        LambdaQueryWrapper<UserCoupon> unusedWrapper = new LambdaQueryWrapper<>();
        unusedWrapper.eq(UserCoupon::getStatus, "unused")
                .ge(UserCoupon::getGetTime, startDate)
                .le(UserCoupon::getGetTime, endDate);
        long unused = userCouponMapper.selectCount(unusedWrapper);

        // Top coupons by used count in period
        LambdaQueryWrapper<UserCoupon> usedListWrapper = new LambdaQueryWrapper<>();
        usedListWrapper.eq(UserCoupon::getStatus, "used")
                .ge(UserCoupon::getUseTime, startDate)
                .le(UserCoupon::getUseTime, endDate);
        List<UserCoupon> usedRecords = userCouponMapper.selectList(usedListWrapper);

        Map<Integer, Integer> usedByCoupon = new HashMap<>();
        for (UserCoupon uc : usedRecords) {
            if (uc.getCouponId() == null) continue;
            usedByCoupon.merge(uc.getCouponId(), 1, Integer::sum);
        }
        List<Integer> usedCouponIds = new ArrayList<>(usedByCoupon.keySet());
        usedCouponIds.sort((a, b) -> Integer.compare(usedByCoupon.get(b), usedByCoupon.get(a)));
        usedCouponIds = usedCouponIds.subList(0, Math.min(n, usedCouponIds.size()));

        Map<Integer, Coupon> couponMap = new HashMap<>();
        if (!usedCouponIds.isEmpty()) {
            for (Coupon c : couponMapper.selectBatchIds(usedCouponIds)) {
                couponMap.put(c.getCouponId(), c);
            }
        }

        List<Map<String, Object>> topCoupons = new ArrayList<>();
        for (Integer cid : usedCouponIds) {
            Coupon c = couponMap.get(cid);
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("couponId", cid);
            row.put("couponName", c != null ? c.getCouponName() : null);
            row.put("usedCount", usedByCoupon.get(cid));
            topCoupons.add(row);
        }

        Map<String, Object> couponAnalysis = new LinkedHashMap<>();
        couponAnalysis.put("issuedCount", issued);
        couponAnalysis.put("usedCount", used);
        couponAnalysis.put("unusedCount", unused);
        couponAnalysis.put("expiredCount", expired);
        couponAnalysis.put("topCoupons", topCoupons);

        // Points economy analysis（基于 points_logs）
        LambdaQueryWrapper<PointsLog> plWrapper = new LambdaQueryWrapper<>();
        plWrapper.ge(PointsLog::getCreateTime, startDate)
                .le(PointsLog::getCreateTime, endDate);
        List<PointsLog> pointLogs = pointsLogMapper.selectList(plWrapper);
        long netChange = 0;
        Map<String, Long> changeByReason = new HashMap<>();
        for (PointsLog pl : pointLogs) {
            int change = pl.getChangeAmount() != null ? pl.getChangeAmount() : 0;
            netChange += change;
            String r = pl.getReason() != null ? pl.getReason() : "unknown";
            changeByReason.merge(r, (long) change, Long::sum);
        }

        List<String> reasons = new ArrayList<>(changeByReason.keySet());
        reasons.sort((a, b) -> Long.compare(changeByReason.get(b), changeByReason.get(a)));
        List<Map<String, Object>> reasonRows = new ArrayList<>();
        for (String r : reasons.subList(0, Math.min(n, reasons.size()))) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("reason", r);
            row.put("changeAmount", changeByReason.get(r));
            reasonRows.add(row);
        }

        Map<String, Object> pointsAnalysis = new LinkedHashMap<>();
        pointsAnalysis.put("netChange", netChange);
        pointsAnalysis.put("reasonBreakdown", reasonRows);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("salesTrend", salesTrend);
        data.put("topProducts", topProducts);
        data.put("topCategories", topCategories);
        data.put("userAnalysis", userAnalysis);
        data.put("couponAnalysis", couponAnalysis);
        data.put("pointsAnalysis", pointsAnalysis);

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
