package com.supermarket.service;

import com.supermarket.entity.Result;
import com.supermarket.entity.Product;
import com.supermarket.entity.ProductSku;
import com.supermarket.entity.Category;
import com.supermarket.util.DBUtil;
import com.supermarket.util.StringUtil;

import java.sql.*;
import java.util.*;

/**
 * ProductService - 商品服务（轻量级 JDBC 实现）
 */
public class ProductService {

    public Result<?> getProductList(Integer categoryId, String keyword, Integer brandId,
                                    String sortBy, String sortOrder,
                                    Double minPrice, Double maxPrice,
                                    Integer pageNum, Integer pageSize) {
        StringBuilder sql = new StringBuilder("SELECT * FROM PRODUCTS WHERE IS_DELETED = 0 AND STATUS = 'active'");
        List<Object> params = new ArrayList<>();
        if (categoryId != null && categoryId > 0) { sql.append(" AND CATEGORY_ID = ?"); params.add(categoryId); }
        if (brandId != null && brandId > 0) { sql.append(" AND BRAND_ID = ?"); params.add(brandId); }
        if (keyword != null && !keyword.isEmpty()) {
            sql.append(" AND (PRODUCT_NAME LIKE ? OR BARCODE LIKE ?)");
            params.add("%" + keyword + "%");
            params.add("%" + keyword + "%");
        }
        if (minPrice != null) { sql.append(" AND PRICE >= ?"); params.add(minPrice); }
        if (maxPrice != null) { sql.append(" AND PRICE <= ?"); params.add(maxPrice); }

        if ("price".equals(sortBy)) {
            sql.append(" ORDER BY PRICE ").append("asc".equals(sortOrder) ? "ASC" : "DESC");
        } else if ("sales".equals(sortBy) || "salesCount".equals(sortBy)) {
            sql.append(" ORDER BY SALES_COUNT DESC");
        } else if ("rating".equals(sortBy)) {
            sql.append(" ORDER BY AVG_RATING DESC");
        } else {
            sql.append(" ORDER BY CREATE_TIME DESC");
        }

        // 分页
        int offset = (pageNum - 1) * pageSize;
        String pageSql = "SELECT * FROM (" + sql + ") WHERE ROWNUM <= ?";
        params.add(pageSize + offset);
        String fullSql = "SELECT * FROM (SELECT t.*, ROWNUM rn FROM (" + sql + ") t WHERE ROWNUM <= ?) WHERE rn > ?";

        try (Connection conn = DBUtil.getDataSource().getConnection()) {
            List<Product> records = executeQuery(conn, fullSql, params, offset, pageSize);
            Map<String, Object> data = new HashMap<>();
            data.put("records", records);
            data.put("total", records.size()); // 简化：实际生产应查 COUNT(*)
            data.put("current", pageNum);
            data.put("size", pageSize);
            return Result.success(data);
        } catch (SQLException e) {
            e.printStackTrace();
            return Result.error("数据库错误: " + e.getMessage());
        }
    }

    private List<Product> executeQuery(Connection conn, String sql, List<Object> params, int offset, int pageSize) throws SQLException {
        List<Product> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int idx = 1;
            for (Object p : params) {
                if (idx == params.size() - 1) {
                    ps.setObject(idx++, offset + pageSize); // end
                } else if (idx == params.size()) {
                    ps.setObject(idx++, offset); // start
                } else {
                    ps.setObject(idx++, p);
                }
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(extractProduct(rs));
            }
        }
        return list;
    }

    private List<Product> executeQuerySimple(Connection conn, String sql, List<Object> params) throws SQLException {
        List<Product> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(extractProduct(rs));
        }
        return list;
    }

    public Result<?> getProductById(Integer productId) {
        String sql = "SELECT * FROM PRODUCTS WHERE PRODUCT_ID = ? AND IS_DELETED = 0";
        try (Connection conn = DBUtil.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return Result.error("商品不存在");
            Product p = extractProduct(rs);
            // 加载 SKU 列表
            p.setSkus(getProductSkusList(productId));
            return Result.success(p);
        } catch (SQLException e) {
            e.printStackTrace();
            return Result.error("数据库错误: " + e.getMessage());
        }
    }

    public Result<?> getProductByBarcode(String barcode) {
        String sql = "SELECT * FROM PRODUCTS WHERE BARCODE = ? AND IS_DELETED = 0";
        try (Connection conn = DBUtil.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, barcode);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return Result.error("未找到条码对应商品");
            return Result.success(extractProduct(rs));
        } catch (SQLException e) {
            e.printStackTrace();
            return Result.error("数据库错误: " + e.getMessage());
        }
    }

    public Result<?> getRecommendedProducts(Integer limit) {
        int maxLimit = (limit != null && limit > 0) ? limit : 8;
        String sql = "SELECT * FROM PRODUCTS WHERE IS_DELETED = 0 AND STATUS = 'active' AND IS_RECOMMEND = 1 ORDER BY SALES_COUNT DESC";
        try (Connection conn = DBUtil.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setMaxRows(maxLimit);
            ResultSet rs = ps.executeQuery();
            List<Product> list = new ArrayList<>();
            while (rs.next()) list.add(extractProduct(rs));
            return Result.success(list);
        } catch (SQLException e) {
            e.printStackTrace();
            return Result.error("数据库错误: " + e.getMessage());
        }
    }

    public Result<?> getTopSalesProducts(Integer limit) {
        int maxLimit = (limit != null && limit > 0) ? limit : 10;
        String sql = "SELECT * FROM PRODUCTS WHERE IS_DELETED = 0 AND STATUS = 'active' AND SALES_COUNT > 0 ORDER BY SALES_COUNT DESC";
        try (Connection conn = DBUtil.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setMaxRows(maxLimit);
            ResultSet rs = ps.executeQuery();
            List<Product> list = new ArrayList<>();
            while (rs.next()) list.add(extractProduct(rs));
            return Result.success(list);
        } catch (SQLException e) {
            e.printStackTrace();
            return Result.error("数据库错误: " + e.getMessage());
        }
    }

    public Result<?> getNewProducts(Integer limit) {
        int maxLimit = (limit != null && limit > 0) ? limit : 10;
        String sql = "SELECT * FROM PRODUCTS WHERE IS_DELETED = 0 AND STATUS = 'active' ORDER BY CREATE_TIME DESC";
        try (Connection conn = DBUtil.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setMaxRows(maxLimit);
            ResultSet rs = ps.executeQuery();
            List<Product> list = new ArrayList<>();
            while (rs.next()) list.add(extractProduct(rs));
            return Result.success(list);
        } catch (SQLException e) {
            e.printStackTrace();
            return Result.error("数据库错误: " + e.getMessage());
        }
    }

    public Result<?> getSearchSuggestions(String keyword, Integer limit) {
        if (keyword == null || keyword.isEmpty()) return Result.success(new ArrayList<>());
        int maxLimit = (limit != null && limit > 0) ? limit : 10;
        String sql = "SELECT PRODUCT_NAME FROM PRODUCTS WHERE IS_DELETED = 0 AND STATUS = 'active' AND PRODUCT_NAME LIKE ? AND ROWNUM <= ?";
        try (Connection conn = DBUtil.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%");
            ps.setInt(2, maxLimit);
            ResultSet rs = ps.executeQuery();
            List<String> names = new ArrayList<>();
            while (rs.next()) names.add(rs.getString("PRODUCT_NAME"));
            return Result.success(names);
        } catch (SQLException e) {
            e.printStackTrace();
            return Result.error("数据库错误: " + e.getMessage());
        }
    }

    public Result<?> adminGetProductList(Integer categoryId, String keyword, String status, Integer pageNum, Integer pageSize) {
        StringBuilder sql = new StringBuilder("SELECT * FROM PRODUCTS WHERE IS_DELETED = 0");
        List<Object> params = new ArrayList<>();
        if (categoryId != null && categoryId > 0) { sql.append(" AND CATEGORY_ID = ?"); params.add(categoryId); }
        if (keyword != null && !keyword.isEmpty()) {
            sql.append(" AND (PRODUCT_NAME LIKE ? OR BARCODE LIKE ?)");
            params.add("%" + keyword + "%");
            params.add("%" + keyword + "%");
        }
        if (status != null && !status.isEmpty()) { sql.append(" AND STATUS = ?"); params.add(status); }
        sql.append(" ORDER BY CREATE_TIME DESC");

        int offset = (pageNum - 1) * pageSize;
        String fullSql = "SELECT * FROM (SELECT t.*, ROWNUM rn FROM (" + sql + ") t WHERE ROWNUM <= ?) WHERE rn > ?";
        params.add(offset + pageSize);
        params.add(offset);

        try (Connection conn = DBUtil.getDataSource().getConnection()) {
            List<Product> records = new ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement(fullSql)) {
                for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
                ResultSet rs = ps.executeQuery();
                while (rs.next()) records.add(extractProduct(rs));
            }
            Map<String, Object> data = new HashMap<>();
            data.put("records", records);
            data.put("total", records.size());
            data.put("current", pageNum);
            data.put("size", pageSize);
            return Result.success(data);
        } catch (SQLException e) {
            e.printStackTrace();
            return Result.error("数据库错误: " + e.getMessage());
        }
    }

    public Result<?> addProduct(Product product) {
        if (product.getBarcode() != null && !product.getBarcode().isEmpty()) {
            try (Connection conn = DBUtil.getDataSource().getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "SELECT PRODUCT_ID FROM PRODUCTS WHERE BARCODE = ? AND IS_DELETED = 0")) {
                ps.setString(1, product.getBarcode());
                if (ps.executeQuery().next()) return Result.error("条码已存在：" + product.getBarcode());
            } catch (SQLException e) { e.printStackTrace(); }
        }
        Integer productId = DBUtil.getNextId("SEQ_PRODUCTS");
        try (Connection conn = DBUtil.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO PRODUCTS (PRODUCT_ID, PRODUCT_NAME, CATEGORY_ID, BRAND_ID, SUPPLIER_ID, DESCRIPTION, COVER_IMAGE, UNIT, ORIGINAL_PRICE, PRICE, STOCK, STOCK_WARNING, BARCODE, COST_PRICE, STATUS, IS_DELETED, IS_RECOMMEND, SALES_COUNT, CREATE_TIME, UPDATE_TIME) " +
                             "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0, 0, 0, SYSDATE, SYSDATE)")) {
            int i = 1;
            ps.setInt(i++, productId);
            ps.setString(i++, product.getProductName());
            if (product.getCategoryId() != null) ps.setInt(i++, product.getCategoryId()); else ps.setNull(i++, Types.INTEGER);
            if (product.getBrandId() != null) ps.setInt(i++, product.getBrandId()); else ps.setNull(i++, Types.INTEGER);
            if (product.getSupplierId() != null) ps.setInt(i++, product.getSupplierId()); else ps.setNull(i++, Types.INTEGER);
            ps.setString(i++, product.getDescription());
            ps.setString(i++, product.getCoverImage());
            ps.setString(i++, product.getUnit());
            ps.setObject(i++, product.getOriginalPrice());
            ps.setObject(i++, product.getPrice());
            ps.setObject(i++, product.getStock() != null ? product.getStock() : 0);
            ps.setObject(i++, product.getStockWarning() != null ? product.getStockWarning() : 10);
            ps.setString(i++, product.getBarcode());
            ps.setObject(i++, product.getCostPrice());
            ps.setString(i++, product.getStatus() != null ? product.getStatus() : "active");
            ps.executeUpdate();
            return Result.success(productId);
        } catch (SQLException e) {
            e.printStackTrace();
            return Result.error("数据库错误: " + e.getMessage());
        }
    }

    public Result<?> updateProduct(Product product) {
        StringBuilder sql = new StringBuilder("UPDATE PRODUCTS SET UPDATE_TIME = SYSDATE");
        List<Object> params = new ArrayList<>();
        if (product.getProductName() != null) { sql.append(", PRODUCT_NAME = ?"); params.add(product.getProductName()); }
        if (product.getCategoryId() != null) { sql.append(", CATEGORY_ID = ?"); params.add(product.getCategoryId()); }
        if (product.getPrice() != null) { sql.append(", PRICE = ?"); params.add(product.getPrice()); }
        if (product.getStock() != null) { sql.append(", STOCK = ?"); params.add(product.getStock()); }
        if (product.getDescription() != null) { sql.append(", DESCRIPTION = ?"); params.add(product.getDescription()); }
        if (product.getCoverImage() != null) { sql.append(", COVER_IMAGE = ?"); params.add(product.getCoverImage()); }
        if (product.getBarcode() != null) { sql.append(", BARCODE = ?"); params.add(product.getBarcode()); }
        if (product.getStatus() != null) { sql.append(", STATUS = ?"); params.add(product.getStatus()); }
        sql.append(" WHERE PRODUCT_ID = ?");
        params.add(product.getProductId());
        try (Connection conn = DBUtil.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            ps.executeUpdate();
            return Result.success();
        } catch (SQLException e) {
            e.printStackTrace();
            return Result.error("数据库错误: " + e.getMessage());
        }
    }

    public Result<?> deleteProduct(Integer productId) {
        try (Connection conn = DBUtil.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE PRODUCTS SET IS_DELETED = 1, STATUS = 'off_shelf', UPDATE_TIME = SYSDATE WHERE PRODUCT_ID = ?")) {
            ps.setInt(1, productId);
            int updated = ps.executeUpdate();
            if (updated == 0) return Result.error("商品不存在");
            return Result.success("商品已删除");
        } catch (SQLException e) {
            e.printStackTrace();
            return Result.error("数据库错误: " + e.getMessage());
        }
    }

    public Result<?> updateProductStatus(Integer productId, String status) {
        try (Connection conn = DBUtil.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE PRODUCTS SET STATUS = ?, UPDATE_TIME = SYSDATE WHERE PRODUCT_ID = ? AND IS_DELETED = 0")) {
            ps.setString(1, status);
            ps.setInt(2, productId);
            int updated = ps.executeUpdate();
            if (updated == 0) return Result.error("商品不存在");
            return Result.success();
        } catch (SQLException e) {
            e.printStackTrace();
            return Result.error("数据库错误: " + e.getMessage());
        }
    }

    public Result<?> getLowStockProducts(Integer pageNum, Integer pageSize) {
        String sql = "SELECT * FROM PRODUCTS WHERE IS_DELETED = 0 AND STOCK <= STOCK_WARNING ORDER BY STOCK ASC";
        int offset = (pageNum - 1) * pageSize;
        String fullSql = "SELECT * FROM (SELECT t.*, ROWNUM rn FROM (" + sql + ") t WHERE ROWNUM <= ?) WHERE rn > ?";
        try (Connection conn = DBUtil.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(fullSql)) {
            ps.setObject(1, offset + pageSize);
            ps.setObject(2, offset);
            ResultSet rs = ps.executeQuery();
            List<Product> list = new ArrayList<>();
            while (rs.next()) list.add(extractProduct(rs));
            Map<String, Object> data = new HashMap<>();
            data.put("records", list);
            data.put("total", list.size());
            return Result.success(data);
        } catch (SQLException e) {
            e.printStackTrace();
            return Result.error("数据库错误: " + e.getMessage());
        }
    }

    public Result<?> updateStock(Integer productId, Integer newStock, Integer operatorId, String remark) {
        try (Connection conn = DBUtil.getDataSource().getConnection()) {
            // 获取当前库存
            int oldStock = 0;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT STOCK FROM PRODUCTS WHERE PRODUCT_ID = ?")) {
                ps.setInt(1, productId);
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) return Result.error("商品不存在");
                oldStock = rs.getInt("STOCK");
            }
            int changeAmount = newStock - oldStock;

            // 更新库存
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE PRODUCTS SET STOCK = ?, UPDATE_TIME = SYSDATE WHERE PRODUCT_ID = ?")) {
                ps.setInt(1, newStock);
                ps.setInt(2, productId);
                ps.executeUpdate();
            }

            // 写库存流水
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO INVENTORY_LOGS (LOG_ID, PRODUCT_ID, LOG_TYPE, CHANGE_AMOUNT, BALANCE_AFTER, OPERATOR_ID, REMARK, CREATE_TIME) " +
                            "VALUES (SEQ_INVENTORY_LOGS.NEXTVAL, ?, 'MANUAL', ?, ?, ?, ?, SYSDATE)")) {
                ps.setInt(1, productId);
                ps.setInt(2, changeAmount);
                ps.setInt(3, newStock);
                ps.setObject(4, operatorId);
                ps.setString(5, remark != null ? remark : "手动调整库存");
                ps.executeUpdate();
            }
            return Result.success();
        } catch (SQLException e) {
            e.printStackTrace();
            return Result.error("数据库错误: " + e.getMessage());
        }
    }

    public Result<?> getCategoryTree() {
        String sql = "SELECT * FROM CATEGORIES ORDER BY SORT_ORDER";
        try (Connection conn = DBUtil.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            List<Category> all = new ArrayList<>();
            while (rs.next()) {
                Category c = new Category();
                c.setCategoryId(rs.getInt("CATEGORY_ID"));
                c.setCategoryName(rs.getString("CATEGORY_NAME"));
                c.setParentId((Integer) rs.getObject("PARENT_ID"));
                c.setSortOrder((Integer) rs.getObject("SORT_ORDER"));
                c.setDescription(rs.getString("DESCRIPTION"));
                all.add(c);
            }
            List<Category> roots = new ArrayList<>();
            for (Category c : all) {
                if (c.getParentId() == null || c.getParentId() == 0) roots.add(c);
            }
            for (Category root : roots) {
                List<Category> children = new ArrayList<>();
                for (Category c : all) {
                    if (root.getCategoryId().equals(c.getParentId())) children.add(c);
                }
                root.setChildren(children);
            }
            return Result.success(roots);
        } catch (SQLException e) {
            e.printStackTrace();
            return Result.error("数据库错误: " + e.getMessage());
        }
    }

    public Result<?> getCategories() {
        String sql = "SELECT * FROM CATEGORIES ORDER BY SORT_ORDER";
        try (Connection conn = DBUtil.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            List<Category> list = new ArrayList<>();
            while (rs.next()) {
                Category c = new Category();
                c.setCategoryId(rs.getInt("CATEGORY_ID"));
                c.setCategoryName(rs.getString("CATEGORY_NAME"));
                c.setParentId((Integer) rs.getObject("PARENT_ID"));
                c.setSortOrder((Integer) rs.getObject("SORT_ORDER"));
                list.add(c);
            }
            return Result.success(list);
        } catch (SQLException e) {
            e.printStackTrace();
            return Result.error("数据库错误: " + e.getMessage());
        }
    }

    public Result<?> addCategory(Category category) {
        try (Connection conn = DBUtil.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO CATEGORIES (CATEGORY_ID, CATEGORY_NAME, PARENT_ID, SORT_ORDER, DESCRIPTION) VALUES (SEQ_CATEGORIES.NEXTVAL, ?, ?, ?, ?)")) {
            ps.setString(1, category.getCategoryName());
            ps.setObject(2, category.getParentId());
            ps.setObject(3, category.getSortOrder());
            ps.setString(4, category.getDescription());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            return Result.success();
        } catch (SQLException e) {
            e.printStackTrace();
            return Result.error("数据库错误: " + e.getMessage());
        }
    }

    public Result<?> updateCategory(Category category) {
        try (Connection conn = DBUtil.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE CATEGORIES SET CATEGORY_NAME = ?, PARENT_ID = ?, SORT_ORDER = ?, DESCRIPTION = ? WHERE CATEGORY_ID = ?")) {
            ps.setString(1, category.getCategoryName());
            ps.setObject(2, category.getParentId());
            ps.setObject(3, category.getSortOrder());
            ps.setString(4, category.getDescription());
            ps.setInt(5, category.getCategoryId());
            ps.executeUpdate();
            return Result.success();
        } catch (SQLException e) {
            e.printStackTrace();
            return Result.error("数据库错误: " + e.getMessage());
        }
    }

    public Result<?> deleteCategory(Integer categoryId) {
        try (Connection conn = DBUtil.getDataSource().getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT COUNT(*) FROM PRODUCTS WHERE CATEGORY_ID = ? AND IS_DELETED = 0")) {
                ps.setInt(1, categoryId);
                ResultSet rs = ps.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    return Result.error("该分类下还有商品，不能删除");
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM CATEGORIES WHERE CATEGORY_ID = ?")) {
                ps.setInt(1, categoryId);
                ps.executeUpdate();
            }
            return Result.success();
        } catch (SQLException e) {
            e.printStackTrace();
            return Result.error("数据库错误: " + e.getMessage());
        }
    }

    public Result<?> getProductSkus(Integer productId) {
        return Result.success(getProductSkusList(productId));
    }

    public List<ProductSku> getProductSkusList(Integer productId) {
        List<ProductSku> list = new ArrayList<>();
        String sql = "SELECT * FROM PRODUCT_SKUS WHERE PRODUCT_ID = ? AND STATUS = 'active'";
        try (Connection conn = DBUtil.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ProductSku sku = new ProductSku();
                sku.setSkuId(rs.getInt("SKU_ID"));
                sku.setProductId(rs.getInt("PRODUCT_ID"));
                sku.setSkuName(rs.getString("SKU_NAME"));
                sku.setPrice(rs.getDouble("PRICE"));
                sku.setStock(rs.getInt("STOCK"));
                sku.setStatus(rs.getString("STATUS"));
                list.add(sku);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Result<?> saveProductSkus(Integer productId, List<ProductSku> skus) {
        if (skus == null || skus.isEmpty()) return Result.success();
        try (Connection conn = DBUtil.getDataSource().getConnection()) {
            for (ProductSku sku : skus) {
                sku.setProductId(productId);
                if (sku.getSkuId() == null) {
                    Integer skuId = DBUtil.getNextId("SEQ_PRODUCT_SKUS");
                    try (PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO PRODUCT_SKUS (SKU_ID, PRODUCT_ID, SKU_NAME, PRICE, STOCK, STATUS) VALUES (?, ?, ?, ?, ?, 'active')")) {
                        ps.setInt(1, skuId);
                        ps.setInt(2, productId);
                        ps.setString(3, sku.getSkuName());
                        ps.setObject(4, sku.getPrice());
                        ps.setObject(5, sku.getStock());
                        ps.executeUpdate();
                    }
                } else {
                    try (PreparedStatement ps = conn.prepareStatement(
                            "UPDATE PRODUCT_SKUS SET SKU_NAME = ?, PRICE = ?, STOCK = ? WHERE SKU_ID = ?")) {
                        ps.setString(1, sku.getSkuName());
                        ps.setObject(2, sku.getPrice());
                        ps.setObject(3, sku.getStock());
                        ps.setInt(4, sku.getSkuId());
                        ps.executeUpdate();
                    }
                }
            }
            return Result.success();
        } catch (SQLException e) {
            e.printStackTrace();
            return Result.error("数据库错误: " + e.getMessage());
        }
    }

    public Result<?> deleteProductSku(Integer skuId) {
        try (Connection conn = DBUtil.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM PRODUCT_SKUS WHERE SKU_ID = ?")) {
            ps.setInt(1, skuId);
            ps.executeUpdate();
            return Result.success();
        } catch (SQLException e) {
            e.printStackTrace();
            return Result.error("数据库错误: " + e.getMessage());
        }
    }

    private Product extractProduct(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setProductId(rs.getInt("PRODUCT_ID"));
        p.setProductName(rs.getString("PRODUCT_NAME"));
        p.setCategoryId((Integer) rs.getObject("CATEGORY_ID"));
        p.setBrandId((Integer) rs.getObject("BRAND_ID"));
        p.setSupplierId((Integer) rs.getObject("SUPPLIER_ID"));
        p.setDescription(rs.getString("DESCRIPTION"));
        p.setCoverImage(rs.getString("COVER_IMAGE"));
        p.setUnit(rs.getString("UNIT"));
        p.setOriginalPrice(rs.getDouble("ORIGINAL_PRICE"));
        p.setPrice(rs.getDouble("PRICE"));
        p.setStock(rs.getInt("STOCK"));
        p.setBarcode(rs.getString("BARCODE"));
        p.setStatus(rs.getString("STATUS"));
        p.setIsDeleted(rs.getInt("IS_DELETED"));
        p.setCostPrice(rs.getDouble("COST_PRICE"));
        return p;
    }
}
