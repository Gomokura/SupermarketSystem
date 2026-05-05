package com.supermarket.service;

import com.supermarket.entity.Order;
import com.supermarket.entity.OrderItem;
import com.supermarket.entity.Product;
import com.supermarket.entity.ProductSku;
import com.supermarket.entity.Result;
import com.supermarket.util.DBUtil;
import com.supermarket.util.DateUtil;
import com.supermarket.util.StringUtil;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * OrderService - 订单服务（轻量级 JDBC 实现）
 */
public class OrderService {

    public Result<?> createOrder(Integer userId, Integer addressId, String paymentMethod,
                                  List<Map<String, Object>> cartItems,
                                  Integer couponId, Integer pointsUsed,
                                  String remark, String deliveryTimeSlot) {
        if (cartItems == null || cartItems.isEmpty()) {
            return Result.error("购物车不能为空");
        }
        try (Connection conn = DBUtil.getDataSource().getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 获取收货地址快照
                String receiverSnapshot = "";
                if (addressId != null) {
                    String addrSql = "SELECT * FROM ADDRESSES WHERE ADDRESS_ID = ? AND USER_ID = ?";
                    try (PreparedStatement ps = conn.prepareStatement(addrSql)) {
                        ps.setInt(1, addressId);
                        ps.setInt(2, userId);
                        ResultSet rs = ps.executeQuery();
                        if (rs.next()) {
                            String p = rs.getString("PROVINCE") != null ? rs.getString("PROVINCE") : "";
                            String c = rs.getString("CITY") != null ? rs.getString("CITY") : "";
                            String d = rs.getString("DISTRICT") != null ? rs.getString("DISTRICT") : "";
                            String detail = rs.getString("DETAIL_ADDRESS") != null ? rs.getString("DETAIL_ADDRESS") : "";
                            receiverSnapshot = String.format("%s %s %s %s|%s|%s",
                                    p, c, d, detail,
                                    rs.getString("CONTACT_NAME"),
                                    rs.getString("CONTACT_PHONE"));
                        }
                    }
                }

                // 计算总价
                double totalAmount = 0;
                double discountAmount = 0;
                List<OrderItem> items = new ArrayList<>();
                for (Map<String, Object> ci : cartItems) {
                    Integer productId = ci.get("productId") != null ? ((Number) ci.get("productId")).intValue() : null;
                    Integer skuId = ci.get("skuId") != null ? ((Number) ci.get("skuId")).intValue() : null;
                    Integer qty = ci.get("quantity") != null ? ((Number) ci.get("quantity")).intValue() : 1;

                    double price = 0;
                    if (skuId != null) {
                        String skuSql = "SELECT PRICE, STOCK, STATUS FROM PRODUCT_SKUS WHERE SKU_ID = ?";
                        try (PreparedStatement ps = conn.prepareStatement(skuSql)) {
                            ps.setInt(1, skuId);
                            ResultSet rs = ps.executeQuery();
                            if (!rs.next()) throw new SQLException("SKU不存在: " + skuId);
                            if (!"active".equals(rs.getString("STATUS"))) throw new SQLException("SKU已下架");
                            if (rs.getInt("STOCK") < qty) throw new SQLException("SKU库存不足");
                            price = rs.getDouble("PRICE");
                        }
                    } else {
                        String pSql = "SELECT PRICE, STOCK, STATUS FROM PRODUCTS WHERE PRODUCT_ID = ? AND IS_DELETED = 0";
                        try (PreparedStatement ps = conn.prepareStatement(pSql)) {
                            ps.setInt(1, productId);
                            ResultSet rs = ps.executeQuery();
                            if (!rs.next()) throw new SQLException("商品不存在: " + productId);
                            if (!"active".equals(rs.getString("STATUS"))) throw new SQLException("商品已下架");
                            if (rs.getInt("STOCK") < qty) throw new SQLException("商品库存不足");
                            price = rs.getDouble("PRICE");
                        }
                    }

                    double subtotal = StringUtil.round2(price * qty);
                    totalAmount += subtotal;

                    OrderItem item = new OrderItem();
                    item.setProductId(productId);
                    item.setSkuId(skuId);
                    item.setQuantity(qty);
                    item.setUnitPrice(price);
                    item.setSubtotal(subtotal);
                    // 查询商品名称
                    if (skuId != null) {
                        String nameSql = "SELECT p.PRODUCT_NAME, s.SKU_NAME FROM PRODUCTS p, PRODUCT_SKUS s WHERE s.SKU_ID = ? AND s.PRODUCT_ID = p.PRODUCT_ID";
                        try (PreparedStatement ps = conn.prepareStatement(nameSql)) {
                            ps.setInt(1, skuId);
                            ResultSet rs = ps.executeQuery();
                            if (rs.next()) {
                                item.setProductName(rs.getString("PRODUCT_NAME"));
                                item.setSkuName(rs.getString("SKU_NAME"));
                            }
                        }
                    } else {
                        String nameSql = "SELECT PRODUCT_NAME FROM PRODUCTS WHERE PRODUCT_ID = ?";
                        try (PreparedStatement ps = conn.prepareStatement(nameSql)) {
                            ps.setInt(1, productId);
                            ResultSet rs = ps.executeQuery();
                            if (rs.next()) {
                                item.setProductName(rs.getString("PRODUCT_NAME"));
                            }
                        }
                    }
                    items.add(item);
                }

                double freightAmount = totalAmount >= 29 ? 0 : 5;
                double payAmount = StringUtil.round2(totalAmount - discountAmount + freightAmount);
                String orderNo = String.format("%d%02d%02d%06d",
                        java.util.Calendar.getInstance().get(java.util.Calendar.YEAR),
                        java.util.Calendar.getInstance().get(java.util.Calendar.MONTH) + 1,
                        java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_MONTH),
                        new java.util.Random().nextInt(999999));

                Integer orderId = DBUtil.getNextId("SEQ_ORDERS");
                String status = "PAID".equals(paymentMethod) || "ONLINE".equals(paymentMethod) ? "PENDING_SHIP" : "PENDING_PAY";

                // 插入订单
                String orderSql = "INSERT INTO ORDERS (ORDER_ID, ORDER_NO, USER_ID, SOURCE, ADDRESS_ID, RECEIVER_SNAPSHOT, " +
                        "TOTAL_AMOUNT, DISCOUNT_AMOUNT, FREIGHT_AMOUNT, PAY_AMOUNT, PAY_METHOD, STATUS, " +
                        "COUPON_ID, UC_ID, POINTS_USED, POINTS_DEDUCT_AMOUNT, DELIVERY_TIME_SLOT, REMARK, " +
                        "CREATE_TIME, UPDATE_TIME) " +
                        "VALUES (?, ?, ?, 'ONLINE', ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, SYSDATE, SYSDATE)";
                try (PreparedStatement ps = conn.prepareStatement(orderSql)) {
                    ps.setInt(1, orderId);
                    ps.setString(2, orderNo);
                    ps.setInt(3, userId);
                    if (addressId != null) ps.setInt(4, addressId); else ps.setNull(4, Types.INTEGER);
                    ps.setString(5, receiverSnapshot);
                    ps.setDouble(6, totalAmount);
                    ps.setDouble(7, discountAmount);
                    ps.setDouble(8, freightAmount);
                    ps.setDouble(9, payAmount);
                    ps.setString(10, paymentMethod != null ? paymentMethod : "ONLINE");
                    ps.setString(11, status);
                    if (couponId != null) ps.setInt(12, couponId); else ps.setNull(12, Types.INTEGER);
                    ps.setNull(13, Types.INTEGER); // ucId
                    if (pointsUsed != null) ps.setInt(14, pointsUsed); else ps.setNull(14, Types.INTEGER);
                    ps.setDouble(15, 0); // pointsDeductAmount
                    ps.setString(16, deliveryTimeSlot);
                    ps.setString(17, remark);
                    ps.executeUpdate();
                }

                // 插入订单明细 & 扣减库存
                for (OrderItem item : items) {
                    Integer itemId = DBUtil.getNextId("SEQ_ORDER_ITEMS");
                    String itemSql = "INSERT INTO ORDER_ITEMS (ITEM_ID, ORDER_ID, PRODUCT_ID, SKU_ID, " +
                            "PRODUCT_NAME, SKU_NAME, QUANTITY, PRICE, SUBTOTAL) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement ps = conn.prepareStatement(itemSql)) {
                        ps.setInt(1, itemId);
                        ps.setInt(2, orderId);
                        ps.setInt(3, item.getProductId());
                        if (item.getSkuId() != null) ps.setInt(4, item.getSkuId()); else ps.setNull(4, Types.INTEGER);
                        ps.setString(5, item.getProductName());
                        ps.setString(6, item.getSkuName());
                        ps.setInt(7, item.getQuantity());
                        ps.setDouble(8, item.getUnitPrice());
                        ps.setDouble(9, item.getSubtotal());
                        ps.executeUpdate();
                    }

                    // 扣减库存
                    if (item.getSkuId() != null) {
                        String skuStockSql = "UPDATE PRODUCT_SKUS SET STOCK = STOCK - ? WHERE SKU_ID = ?";
                        try (PreparedStatement ps = conn.prepareStatement(skuStockSql)) {
                            ps.setInt(1, item.getQuantity());
                            ps.setInt(2, item.getSkuId());
                            ps.executeUpdate();
                        }
                        String prodStockSql = "UPDATE PRODUCTS SET STOCK = STOCK - ?, SALES_COUNT = SALES_COUNT + ? WHERE PRODUCT_ID = ?";
                        try (PreparedStatement ps = conn.prepareStatement(prodStockSql)) {
                            ps.setInt(1, item.getQuantity());
                            ps.setInt(2, item.getQuantity());
                            ps.setInt(3, item.getProductId());
                            ps.executeUpdate();
                        }
                    } else {
                        String stockSql = "UPDATE PRODUCTS SET STOCK = STOCK - ?, SALES_COUNT = SALES_COUNT + ? WHERE PRODUCT_ID = ?";
                        try (PreparedStatement ps = conn.prepareStatement(stockSql)) {
                            ps.setInt(1, item.getQuantity());
                            ps.setInt(2, item.getQuantity());
                            ps.setInt(3, item.getProductId());
                            ps.executeUpdate();
                        }
                    }
                }

                // 清空购物车
                String clearSql = "DELETE FROM CART WHERE USER_ID = ?";
                try (PreparedStatement ps = conn.prepareStatement(clearSql)) {
                    ps.setInt(1, userId);
                    ps.executeUpdate();
                }

                conn.commit();

                Map<String, Object> data = new HashMap<>();
                data.put("orderId", orderId);
                data.put("orderNo", orderNo);
                data.put("payAmount", payAmount);
                data.put("status", status);
                return Result.success(data);
            } catch (Exception e) {
                conn.rollback();
                e.printStackTrace();
                return Result.error(e.getMessage());
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return Result.error("数据库错误: " + e.getMessage());
        }
    }

    public Result<?> cashierCreateOrder(Integer adminId, List<Map<String, Object>> items,
                                         String payMethod, Double receivedAmount) {
        if (items == null || items.isEmpty()) return Result.error("商品不能为空");
        try (Connection conn = DBUtil.getDataSource().getConnection()) {
            conn.setAutoCommit(false);
            try {
                double totalAmount = 0;
                List<OrderItem> orderItems = new ArrayList<>();
                for (Map<String, Object> item : items) {
                    Integer productId = item.get("productId") != null ? ((Number) item.get("productId")).intValue() : null;
                    Integer skuId = item.get("skuId") != null ? ((Number) item.get("skuId")).intValue() : null;
                    Integer qty = item.get("quantity") != null ? ((Number) item.get("quantity")).intValue() : 1;
                    double price = item.get("price") != null ? ((Number) item.get("price")).doubleValue() : 0;

                    if (productId == null) continue;

                    double subtotal = StringUtil.round2(price * qty);
                    totalAmount += subtotal;

                    OrderItem oi = new OrderItem();
                    oi.setProductId(productId);
                    oi.setSkuId(skuId);
                    oi.setQuantity(qty);
                    oi.setUnitPrice(price);
                    oi.setSubtotal(subtotal);

                    if (skuId != null) {
                        String nameSql = "SELECT p.PRODUCT_NAME, s.SKU_NAME FROM PRODUCTS p, PRODUCT_SKUS s WHERE s.SKU_ID = ? AND s.PRODUCT_ID = p.PRODUCT_ID";
                        try (PreparedStatement ps = conn.prepareStatement(nameSql)) {
                            ps.setInt(1, skuId);
                            ResultSet rs = ps.executeQuery();
                            if (rs.next()) {
                                oi.setProductName(rs.getString("PRODUCT_NAME"));
                                oi.setSkuName(rs.getString("SKU_NAME"));
                            }
                        }
                    } else {
                        String nameSql = "SELECT PRODUCT_NAME FROM PRODUCTS WHERE PRODUCT_ID = ?";
                        try (PreparedStatement ps = conn.prepareStatement(nameSql)) {
                            ps.setInt(1, productId);
                            ResultSet rs = ps.executeQuery();
                            if (rs.next()) oi.setProductName(rs.getString("PRODUCT_NAME"));
                        }
                    }
                    orderItems.add(oi);
                }

                String orderNo = String.format("%d%02d%02d%06d",
                        java.util.Calendar.getInstance().get(java.util.Calendar.YEAR),
                        java.util.Calendar.getInstance().get(java.util.Calendar.MONTH) + 1,
                        java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_MONTH),
                        new java.util.Random().nextInt(999999));
                Integer orderId = DBUtil.getNextId("SEQ_ORDERS");
                double payAmount = StringUtil.round2(totalAmount);

                String orderSql = "INSERT INTO ORDERS (ORDER_ID, ORDER_NO, USER_ID, SOURCE, " +
                        "TOTAL_AMOUNT, DISCOUNT_AMOUNT, FREIGHT_AMOUNT, PAY_AMOUNT, PAY_METHOD, STATUS, " +
                        "CREATE_TIME, UPDATE_TIME) " +
                        "VALUES (?, ?, ?, 'CASHIER', ?, 0, 0, ?, ?, 'PAID', SYSDATE, SYSDATE)";
                try (PreparedStatement ps = conn.prepareStatement(orderSql)) {
                    ps.setInt(1, orderId);
                    ps.setString(2, orderNo);
                    ps.setInt(3, adminId); // userId = adminId for cashier orders
                    ps.setDouble(4, totalAmount);
                    ps.setDouble(5, payAmount);
                    ps.setString(6, payMethod != null ? payMethod : "CASH");
                    ps.executeUpdate();
                }

                for (OrderItem item : orderItems) {
                    Integer itemId = DBUtil.getNextId("SEQ_ORDER_ITEMS");
                    String itemSql = "INSERT INTO ORDER_ITEMS (ITEM_ID, ORDER_ID, PRODUCT_ID, SKU_ID, " +
                            "PRODUCT_NAME, SKU_NAME, QUANTITY, PRICE, SUBTOTAL) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement ps = conn.prepareStatement(itemSql)) {
                        ps.setInt(1, itemId);
                        ps.setInt(2, orderId);
                        ps.setInt(3, item.getProductId());
                        if (item.getSkuId() != null) ps.setInt(4, item.getSkuId()); else ps.setNull(4, Types.INTEGER);
                        ps.setString(5, item.getProductName());
                        ps.setString(6, item.getSkuName());
                        ps.setInt(7, item.getQuantity());
                        ps.setDouble(8, item.getUnitPrice());
                        ps.setDouble(9, item.getSubtotal());
                        ps.executeUpdate();
                    }

                    if (item.getSkuId() != null) {
                        try (PreparedStatement ps = conn.prepareStatement(
                                "UPDATE PRODUCT_SKUS SET STOCK = STOCK - ? WHERE SKU_ID = ?")) {
                            ps.setInt(1, item.getQuantity());
                            ps.setInt(2, item.getSkuId());
                            ps.executeUpdate();
                        }
                    }
                    try (PreparedStatement ps = conn.prepareStatement(
                            "UPDATE PRODUCTS SET STOCK = STOCK - ?, SALES_COUNT = SALES_COUNT + ? WHERE PRODUCT_ID = ?")) {
                        ps.setInt(1, item.getQuantity());
                        ps.setInt(2, item.getQuantity());
                        ps.setInt(3, item.getProductId());
                        ps.executeUpdate();
                    }
                }

                // 记录收银记录
                Integer shiftId = null;
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT SHIFT_ID FROM CASHIER_SHIFTS WHERE ADMIN_ID = ? AND END_TIME IS NULL ORDER BY START_TIME DESC")) {
                    ps.setInt(1, adminId);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) shiftId = rs.getInt("SHIFT_ID");
                }
                if (shiftId != null) {
                    Integer recordId = DBUtil.getNextId("SEQ_CASHIER_RECORDS");
                    try (PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO CASHIER_RECORDS (RECORD_ID, SHIFT_ID, ORDER_ID, ORDER_NO, TOTAL_AMOUNT, " +
                                    "PAY_METHOD, RECEIVED_AMOUNT, CHANGE_AMOUNT, OPERATOR_ID, CREATE_TIME) " +
                                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, SYSDATE)")) {
                        ps.setInt(1, recordId);
                        ps.setInt(2, shiftId);
                        ps.setInt(3, orderId);
                        ps.setString(4, orderNo);
                        ps.setDouble(5, payAmount);
                        ps.setString(6, payMethod);
                        ps.setDouble(7, receivedAmount != null ? receivedAmount : payAmount);
                        ps.setDouble(8, receivedAmount != null ? StringUtil.round2(receivedAmount - payAmount) : 0);
                        ps.setInt(9, adminId);
                        ps.executeUpdate();
                    }
                }

                conn.commit();

                Map<String, Object> data = new HashMap<>();
                data.put("orderId", orderId);
                data.put("orderNo", orderNo);
                data.put("payAmount", payAmount);
                return Result.success(data);
            } catch (Exception e) {
                conn.rollback();
                e.printStackTrace();
                return Result.error(e.getMessage());
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return Result.error("数据库错误: " + e.getMessage());
        }
    }

    public Result<?> payOrder(Integer orderId, Integer userId, String payMethod) {
        try (Connection conn = DBUtil.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE ORDERS SET STATUS = 'PENDING_SHIP', PAY_METHOD = ?, PAY_TIME = SYSDATE, UPDATE_TIME = SYSDATE " +
                             "WHERE ORDER_ID = ? AND USER_ID = ? AND STATUS = 'PENDING_PAY'")) {
            ps.setString(1, payMethod != null ? payMethod : "ONLINE");
            ps.setInt(2, orderId);
            ps.setInt(3, userId);
            int updated = ps.executeUpdate();
            if (updated == 0) return Result.error("订单不存在或状态已变更");
            return Result.success("支付成功");
        } catch (SQLException e) {
            e.printStackTrace();
            return Result.error("数据库错误: " + e.getMessage());
        }
    }

    public Result<?> confirmReceipt(Integer orderId, Integer userId) {
        try (Connection conn = DBUtil.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE ORDERS SET STATUS = 'COMPLETED', CONFIRM_TIME = SYSDATE, COMPLETE_TIME = SYSDATE, UPDATE_TIME = SYSDATE " +
                             "WHERE ORDER_ID = ? AND USER_ID = ? AND STATUS = 'PENDING_RECEIVED'")) {
            ps.setInt(1, orderId);
            ps.setInt(2, userId);
            int updated = ps.executeUpdate();
            if (updated == 0) return Result.error("订单不存在或状态已变更");
            return Result.success("确认收货成功");
        } catch (SQLException e) {
            e.printStackTrace();
            return Result.error("数据库错误: " + e.getMessage());
        }
    }

    public Result<?> getOrderDetail(Integer orderId, Integer userId) {
        String sql = "SELECT o.*, u.USERNAME FROM ORDERS o " +
                "LEFT JOIN USERS u ON o.USER_ID = u.USER_ID " +
                "WHERE o.ORDER_ID = ? AND o.USER_ID = ?";
        try (Connection conn = DBUtil.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ps.setInt(2, userId);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return Result.error("订单不存在");

            Order order = extractOrder(rs);

            // 加载订单明细
            List<OrderItem> items = new ArrayList<>();
            String itemSql = "SELECT * FROM ORDER_ITEMS WHERE ORDER_ID = ?";
            try (PreparedStatement ips = conn.prepareStatement(itemSql)) {
                ips.setInt(1, orderId);
                ResultSet irs = ips.executeQuery();
                while (irs.next()) {
                    OrderItem item = new OrderItem();
                    item.setItemId(irs.getInt("ITEM_ID"));
                    item.setOrderId(irs.getInt("ORDER_ID"));
                    item.setProductId(irs.getInt("PRODUCT_ID"));
                    item.setSkuId((Integer) irs.getObject("SKU_ID"));
                    item.setProductName(irs.getString("PRODUCT_NAME"));
                    item.setSkuName(irs.getString("SKU_NAME"));
                    item.setQuantity(irs.getInt("QUANTITY"));
                    item.setUnitPrice(irs.getDouble("PRICE"));
                    item.setSubtotal(irs.getDouble("SUBTOTAL"));
                    items.add(item);
                }
            }
            order.setItems(items);

            return Result.success(order);
        } catch (SQLException e) {
            e.printStackTrace();
            return Result.error("数据库错误: " + e.getMessage());
        }
    }

    public Result<?> getUserOrders(Integer userId, String status, Integer pageNum, Integer pageSize) {
        StringBuilder sql = new StringBuilder(
                "SELECT o.*, u.USERNAME FROM ORDERS o LEFT JOIN USERS u ON o.USER_ID = u.USER_ID WHERE o.USER_ID = ?");
        List<Object> params = new ArrayList<>();
        params.add(userId);
        if (status != null && !status.isEmpty()) {
            sql.append(" AND o.STATUS = ?");
            params.add(status);
        }
        sql.append(" ORDER BY o.CREATE_TIME DESC");

        int offset = (pageNum - 1) * pageSize;
        String fullSql = "SELECT * FROM (SELECT t.*, ROWNUM rn FROM (" + sql + ") t WHERE ROWNUM <= ?) WHERE rn > ?";
        params.add(offset + pageSize);
        params.add(offset);

        try (Connection conn = DBUtil.getDataSource().getConnection()) {
            List<Order> records = new ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement(fullSql)) {
                for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
                ResultSet rs = ps.executeQuery();
                while (rs.next()) records.add(extractOrder(rs));
            }
            Map<String, Object> data = new HashMap<>();
            data.put("records", records);
            data.put("current", pageNum);
            data.put("size", pageSize);
            return Result.success(data);
        } catch (SQLException e) {
            e.printStackTrace();
            return Result.error("数据库错误: " + e.getMessage());
        }
    }

    public Result<?> adminGetOrderList(String status, String orderNo, Integer userId, Integer pageNum, Integer pageSize) {
        StringBuilder sql = new StringBuilder(
                "SELECT o.*, u.USERNAME FROM ORDERS o LEFT JOIN USERS u ON o.USER_ID = u.USER_ID WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (status != null && !status.isEmpty()) {
            sql.append(" AND o.STATUS = ?");
            params.add(status);
        }
        if (orderNo != null && !orderNo.isEmpty()) {
            sql.append(" AND o.ORDER_NO LIKE ?");
            params.add("%" + orderNo + "%");
        }
        if (userId != null) {
            sql.append(" AND o.USER_ID = ?");
            params.add(userId);
        }
        sql.append(" ORDER BY o.CREATE_TIME DESC");

        int offset = (pageNum - 1) * pageSize;
        String fullSql = "SELECT * FROM (SELECT t.*, ROWNUM rn FROM (" + sql + ") t WHERE ROWNUM <= ?) WHERE rn > ?";
        params.add(offset + pageSize);
        params.add(offset);

        try (Connection conn = DBUtil.getDataSource().getConnection()) {
            List<Order> records = new ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement(fullSql)) {
                for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
                ResultSet rs = ps.executeQuery();
                while (rs.next()) records.add(extractOrder(rs));
            }
            Map<String, Object> data = new HashMap<>();
            data.put("records", records);
            data.put("current", pageNum);
            data.put("size", pageSize);
            return Result.success(data);
        } catch (SQLException e) {
            e.printStackTrace();
            return Result.error("数据库错误: " + e.getMessage());
        }
    }

    public Result<?> shipOrder(Integer orderId, Integer adminId, String expressCompany, String expressNo) {
        try (Connection conn = DBUtil.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE ORDERS SET STATUS = 'SHIPPING', EXPRESS_COMPANY = ?, EXPRESS_NO = ?, " +
                             "SHIP_TIME = SYSDATE, UPDATE_TIME = SYSDATE " +
                             "WHERE ORDER_ID = ? AND STATUS = 'PENDING_SHIP'")) {
            ps.setString(1, expressCompany);
            ps.setString(2, expressNo);
            ps.setInt(3, orderId);
            int updated = ps.executeUpdate();
            if (updated == 0) return Result.error("订单不存在或状态已变更");
            return Result.success("发货成功");
        } catch (SQLException e) {
            e.printStackTrace();
            return Result.error("数据库错误: " + e.getMessage());
        }
    }

    public Result<?> adminCancelOrder(Integer orderId, Integer adminId, String reason) {
        try (Connection conn = DBUtil.getDataSource().getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 查询订单
                Integer userId = null;
                String orderStatus = null;
                List<Integer> productIds = new ArrayList<>();
                List<Integer> skuIds = new ArrayList<>();
                List<Integer> quantities = new ArrayList<>();

                String selectSql = "SELECT USER_ID, STATUS FROM ORDERS WHERE ORDER_ID = ?";
                try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
                    ps.setInt(1, orderId);
                    ResultSet rs = ps.executeQuery();
                    if (!rs.next()) {
                        conn.rollback();
                        return Result.error("订单不存在");
                    }
                    userId = rs.getInt("USER_ID");
                    orderStatus = rs.getString("STATUS");
                }

                if (!"PENDING_PAY".equals(orderStatus) && !"PENDING_SHIP".equals(orderStatus)) {
                    conn.rollback();
                    return Result.error("当前状态不允许取消");
                }

                // 恢复库存
                String itemsSql = "SELECT PRODUCT_ID, SKU_ID, QUANTITY FROM ORDER_ITEMS WHERE ORDER_ID = ?";
                try (PreparedStatement ps = conn.prepareStatement(itemsSql)) {
                    ps.setInt(1, orderId);
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        productIds.add(rs.getInt("PRODUCT_ID"));
                        Integer sid = (Integer) rs.getObject("SKU_ID");
                        skuIds.add(sid);
                        quantities.add(rs.getInt("QUANTITY"));
                    }
                }

                for (int i = 0; i < productIds.size(); i++) {
                    Integer pid = productIds.get(i);
                    Integer sid = skuIds.get(i);
                    Integer qty = quantities.get(i);
                    if (sid != null) {
                        try (PreparedStatement ps = conn.prepareStatement(
                                "UPDATE PRODUCT_SKUS SET STOCK = STOCK + ? WHERE SKU_ID = ?")) {
                            ps.setInt(1, qty);
                            ps.setInt(2, sid);
                            ps.executeUpdate();
                        }
                    }
                    try (PreparedStatement ps = conn.prepareStatement(
                            "UPDATE PRODUCTS SET STOCK = STOCK + ? WHERE PRODUCT_ID = ?")) {
                        ps.setInt(1, qty);
                        ps.setInt(2, pid);
                        ps.executeUpdate();
                    }
                }

                // 取消订单
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE ORDERS SET STATUS = 'CANCELLED', CANCEL_REASON = ?, CANCEL_TIME = SYSDATE, UPDATE_TIME = SYSDATE WHERE ORDER_ID = ?")) {
                    ps.setString(1, reason != null ? reason : "管理员取消");
                    ps.setInt(2, orderId);
                    ps.executeUpdate();
                }

                conn.commit();
                return Result.success("订单已取消");
            } catch (Exception e) {
                conn.rollback();
                e.printStackTrace();
                return Result.error(e.getMessage());
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return Result.error("数据库错误: " + e.getMessage());
        }
    }

    private Order extractOrder(ResultSet rs) throws SQLException {
        Order o = new Order();
        o.setOrderId(rs.getInt("ORDER_ID"));
        o.setOrderNo(rs.getString("ORDER_NO"));
        o.setUserId(rs.getInt("USER_ID"));
        o.setSource(rs.getString("SOURCE"));
        o.setAddressId((Integer) rs.getObject("ADDRESS_ID"));
        o.setReceiverSnapshot(rs.getString("RECEIVER_SNAPSHOT"));
        o.setTotalAmount(rs.getDouble("TOTAL_AMOUNT"));
        o.setDiscountAmount(rs.getDouble("DISCOUNT_AMOUNT"));
        o.setCouponDiscount(rs.getDouble("COUPON_DISCOUNT"));
        o.setPointsDeductAmount(rs.getDouble("POINTS_DEDUCT_AMOUNT"));
        o.setFreightAmount(rs.getDouble("FREIGHT_AMOUNT"));
        o.setPayAmount(rs.getDouble("PAY_AMOUNT"));
        o.setPayMethod(rs.getString("PAY_METHOD"));
        o.setCouponId((Integer) rs.getObject("COUPON_ID"));
        o.setPointsUsed((Integer) rs.getObject("POINTS_USED"));
        o.setDeliveryTimeSlot(rs.getString("DELIVERY_TIME_SLOT"));
        o.setRemark(rs.getString("REMARK"));
        o.setCancelReason(rs.getString("CANCEL_REASON"));
        o.setRefundAmount(rs.getDouble("REFUND_AMOUNT"));
        o.setStatus(rs.getString("STATUS"));
        o.setPayTime(rs.getTimestamp("PAY_TIME"));
        o.setShipTime(rs.getTimestamp("SHIP_TIME"));
        o.setPickupTime(rs.getTimestamp("PICKUP_TIME"));
        o.setDeliverTime(rs.getTimestamp("DELIVER_TIME"));
        o.setConfirmTime(rs.getTimestamp("CONFIRM_TIME"));
        o.setCompleteTime(rs.getTimestamp("COMPLETE_TIME"));
        o.setCancelTime(rs.getTimestamp("CANCEL_TIME"));
        o.setRefundTime(rs.getTimestamp("REFUND_TIME"));
        o.setCreateTime(rs.getTimestamp("CREATE_TIME"));
        o.setUpdateTime(rs.getTimestamp("UPDATE_TIME"));
        o.setUsername(rs.getString("USERNAME"));
        return o;
    }
}
