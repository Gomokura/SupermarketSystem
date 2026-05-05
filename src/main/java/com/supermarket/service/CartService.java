package com.supermarket.service;

import com.supermarket.entity.Result;
import com.supermarket.entity.Cart;
import com.supermarket.entity.Product;
import com.supermarket.entity.ProductSku;
import com.supermarket.util.DBUtil;
import com.supermarket.util.StringUtil;

import java.sql.*;
import java.util.*;

/**
 * CartService - 购物车服务（轻量级 JDBC 实现）
 */
public class CartService {

    public Result<?> getCartList(Integer userId) {
        String sql = "SELECT * FROM CART WHERE USER_ID = ?";
        List<Map<String, Object>> result = new ArrayList<>();
        try (Connection conn = DBUtil.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> item = new HashMap<>();
                item.put("cartId", rs.getInt("CART_ID"));
                item.put("productId", rs.getInt("PRODUCT_ID"));
                item.put("skuId", rs.getObject("SKU_ID"));
                item.put("quantity", rs.getInt("QUANTITY"));
                item.put("isChecked", rs.getInt("IS_CHECKED"));
                item.put("addTime", rs.getTimestamp("ADD_TIME"));

                Integer productId = rs.getInt("PRODUCT_ID");
                Product p = getProductById(productId);
                if (p != null) {
                    item.put("productName", p.getProductName());
                    item.put("imageUrl", p.getCoverImage());
                    item.put("productStatus", p.getStatus());
                    item.put("price", p.getPrice());
                    item.put("stock", p.getStock());

                    Integer skuId = (Integer) item.get("skuId");
                    if (skuId != null) {
                        ProductSku sku = getSkuById(skuId);
                        if (sku != null) {
                            item.put("price", sku.getPrice());
                            item.put("stock", sku.getStock());
                            item.put("specName", sku.getSkuName());
                        }
                    }
                    Double price = (Double) item.get("price");
                    Integer qty = (Integer) item.get("quantity");
                    if (price != null && qty != null) {
                        item.put("subtotal", StringUtil.round2(price * qty));
                    }
                }
                result.add(item);
            }
            return Result.success(result);
        } catch (SQLException e) {
            e.printStackTrace();
            return Result.error("数据库错误: " + e.getMessage());
        }
    }

    public Result<?> addToCart(Integer userId, Integer productId, Integer quantity, Integer skuId) {
        Product product = getProductById(productId);
        if (product == null || product.getIsDeleted() == 1) {
            return Result.error("商品不存在");
        }
        if (!"active".equals(product.getStatus())) {
            return Result.error("商品已下架");
        }

        try (Connection conn = DBUtil.getDataSource().getConnection()) {
            // 检查是否已有相同商品
            String checkSql = skuId != null
                    ? "SELECT * FROM CART WHERE USER_ID = ? AND PRODUCT_ID = ? AND SKU_ID = ?"
                    : "SELECT * FROM CART WHERE USER_ID = ? AND PRODUCT_ID = ? AND SKU_ID IS NULL";
            try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                ps.setInt(1, userId);
                ps.setInt(2, productId);
                if (skuId != null) ps.setInt(3, skuId);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    // 合并数量
                    int existQty = rs.getInt("QUANTITY");
                    int newQty = existQty + quantity;
                    Integer stock = product.getStock();
                    if (skuId != null) {
                        ProductSku sku = getSkuById(skuId);
                        if (sku == null || !"active".equals(sku.getStatus()))
                            return Result.error("该规格不存在或已下架");
                        if (sku.getStock() < newQty)
                            return Result.error("该规格库存不足，剩余 " + sku.getStock());
                    } else {
                        if (stock != null && stock < newQty)
                            return Result.error("商品库存不足，剩余 " + stock);
                    }
                    try (PreparedStatement ups = conn.prepareStatement(
                            "UPDATE CART SET QUANTITY = ? WHERE CART_ID = ?")) {
                        ups.setInt(1, newQty);
                        ups.setInt(2, rs.getInt("CART_ID"));
                        ups.executeUpdate();
                    }
                } else {
                    // 新增
                    if (skuId != null) {
                        ProductSku sku = getSkuById(skuId);
                        if (sku == null || !"active".equals(sku.getStatus()))
                            return Result.error("该规格不存在或已下架");
                        if (sku.getStock() < quantity)
                            return Result.error("该规格库存不足，剩余 " + sku.getStock());
                    } else {
                        if (product.getStock() != null && product.getStock() < quantity)
                            return Result.error("商品库存不足，剩余 " + product.getStock());
                    }
                    Integer cartId = DBUtil.getNextId("SEQ_CART");
                    try (PreparedStatement ins = conn.prepareStatement(
                            "INSERT INTO CART (CART_ID, USER_ID, PRODUCT_ID, SKU_ID, QUANTITY, IS_CHECKED, ADD_TIME) " +
                                    "VALUES (?, ?, ?, ?, ?, 1, SYSDATE)")) {
                        ins.setInt(1, cartId);
                        ins.setInt(2, userId);
                        ins.setInt(3, productId);
                        if (skuId != null) ins.setInt(4, skuId);
                        else ins.setNull(4, Types.INTEGER);
                        ins.setInt(5, quantity);
                        ins.executeUpdate();
                    }
                }
            }
            return Result.success("已加入购物车");
        } catch (SQLException e) {
            e.printStackTrace();
            return Result.error("数据库错误: " + e.getMessage());
        }
    }

    public Result<?> updateCartQuantity(Integer userId, Integer cartId, Integer quantity) {
        if (quantity <= 0) return Result.error("数量必须大于 0");
        try (Connection conn = DBUtil.getDataSource().getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE CART SET QUANTITY = ? WHERE CART_ID = ? AND USER_ID = ?")) {
                ps.setInt(1, quantity);
                ps.setInt(2, cartId);
                ps.setInt(3, userId);
                int updated = ps.executeUpdate();
                if (updated == 0) return Result.error("购物车项不存在或无权操作");
            }
            return Result.success();
        } catch (SQLException e) {
            e.printStackTrace();
            return Result.error("数据库错误: " + e.getMessage());
        }
    }

    public Result<?> removeFromCart(Integer userId, Integer cartId) {
        try (Connection conn = DBUtil.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "DELETE FROM CART WHERE CART_ID = ? AND USER_ID = ?")) {
            ps.setInt(1, cartId);
            ps.setInt(2, userId);
            int deleted = ps.executeUpdate();
            if (deleted == 0) return Result.error("购物车项不存在或无权操作");
            return Result.success();
        } catch (SQLException e) {
            e.printStackTrace();
            return Result.error("数据库错误: " + e.getMessage());
        }
    }

    public Result<?> clearCart(Integer userId) {
        try (Connection conn = DBUtil.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "DELETE FROM CART WHERE USER_ID = ?")) {
            ps.setInt(1, userId);
            ps.executeUpdate();
            return Result.success();
        } catch (SQLException e) {
            e.printStackTrace();
            return Result.error("数据库错误: " + e.getMessage());
        }
    }

    public Result<?> checkItem(Integer userId, Integer cartId, Integer checked) {
        int v = (checked != null && checked == 1) ? 1 : 0;
        try (Connection conn = DBUtil.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE CART SET IS_CHECKED = ? WHERE CART_ID = ? AND USER_ID = ?")) {
            ps.setInt(1, v);
            ps.setInt(2, cartId);
            ps.setInt(3, userId);
            int updated = ps.executeUpdate();
            if (updated == 0) return Result.error("购物车项不存在或无权操作");
            return Result.success();
        } catch (SQLException e) {
            e.printStackTrace();
            return Result.error("数据库错误: " + e.getMessage());
        }
    }

    public Result<?> checkAll(Integer userId, Integer checked) {
        int v = (checked != null && checked == 1) ? 1 : 0;
        try (Connection conn = DBUtil.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE CART SET IS_CHECKED = ? WHERE USER_ID = ?")) {
            ps.setInt(1, v);
            ps.setInt(2, userId);
            ps.executeUpdate();
            return Result.success();
        } catch (SQLException e) {
            e.printStackTrace();
            return Result.error("数据库错误: " + e.getMessage());
        }
    }

    public Result<?> batchDelete(Integer userId, List<Integer> cartIds) {
        if (cartIds == null || cartIds.isEmpty()) return Result.success();
        StringBuilder sb = new StringBuilder("DELETE FROM CART WHERE USER_ID = ? AND CART_ID IN (");
        for (int i = 0; i < cartIds.size(); i++) sb.append("?,");
        sb.setCharAt(sb.length() - 1, ')');
        try (Connection conn = DBUtil.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sb.toString())) {
            ps.setInt(1, userId);
            for (int i = 0; i < cartIds.size(); i++) ps.setInt(i + 2, cartIds.get(i));
            ps.executeUpdate();
            return Result.success();
        } catch (SQLException e) {
            e.printStackTrace();
            return Result.error("数据库错误: " + e.getMessage());
        }
    }

    public Result<?> checkedSummary(Integer userId) {
        String sql = "SELECT c.*, p.PRICE, p.STATUS as P_STATUS, p.IS_DELETED, s.PRICE as SKU_PRICE, s.STATUS as SKU_STATUS " +
                "FROM CART c " +
                "LEFT JOIN PRODUCTS p ON c.PRODUCT_ID = p.PRODUCT_ID " +
                "LEFT JOIN PRODUCT_SKUS s ON c.SKU_ID = s.SKU_ID " +
                "WHERE c.USER_ID = ? AND c.IS_CHECKED = 1";
        int totalCount = 0;
        double totalAmount = 0;
        try (Connection conn = DBUtil.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int isDel = rs.getInt("IS_DELETED");
                String pStatus = rs.getString("P_STATUS");
                if (isDel == 1 || !"active".equals(pStatus)) continue;
                Integer skuId = (Integer) rs.getObject("SKU_ID");
                double price = 0;
                if (skuId != null) {
                    String skuStatus = rs.getString("SKU_STATUS");
                    if (!"active".equals(skuStatus)) continue;
                    price = rs.getDouble("SKU_PRICE");
                } else {
                    price = rs.getDouble("PRICE");
                }
                int qty = rs.getInt("QUANTITY");
                totalCount += qty;
                totalAmount += price * qty;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Map<String, Object> data = new HashMap<>();
        data.put("totalCount", totalCount);
        data.put("totalAmount", StringUtil.round2(totalAmount));
        return Result.success(data);
    }

    private Product getProductById(Integer productId) {
        if (productId == null) return null;
        String sql = "SELECT * FROM PRODUCTS WHERE PRODUCT_ID = ?";
        try (Connection conn = DBUtil.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Product p = new Product();
                p.setProductId(rs.getInt("PRODUCT_ID"));
                p.setProductName(rs.getString("PRODUCT_NAME"));
                p.setPrice(rs.getDouble("PRICE"));
                p.setStock(rs.getInt("STOCK"));
                p.setCoverImage(rs.getString("COVER_IMAGE"));
                p.setStatus(rs.getString("STATUS"));
                p.setIsDeleted(rs.getInt("IS_DELETED"));
                return p;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private ProductSku getSkuById(Integer skuId) {
        if (skuId == null) return null;
        String sql = "SELECT * FROM PRODUCT_SKUS WHERE SKU_ID = ?";
        try (Connection conn = DBUtil.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, skuId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                ProductSku s = new ProductSku();
                s.setSkuId(rs.getInt("SKU_ID"));
                s.setSkuName(rs.getString("SKU_NAME"));
                s.setPrice(rs.getDouble("PRICE"));
                s.setStock(rs.getInt("STOCK"));
                s.setStatus(rs.getString("STATUS"));
                return s;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
