package com.supermarket.dao;

import com.supermarket.bean.Product;
import com.supermarket.util.DBUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    public List<Product> searchProducts(String keyword, Integer categoryId, String orderBy) {
        List<Product> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            StringBuilder sql = new StringBuilder(
                "SELECT p.*, c.category_name FROM products p LEFT JOIN categories c ON p.category_id=c.category_id WHERE 1=1"
            );
            if (keyword != null && !keyword.trim().isEmpty()) {
                sql.append(" AND (p.product_name LIKE ? OR p.supplier LIKE ?)");
            }
            if (categoryId != null && categoryId > 0) {
                sql.append(" AND p.category_id=?");
            }
            if (orderBy != null && !orderBy.isEmpty()) {
                sql.append(" ORDER BY ").append(orderBy);
            }
            stmt = conn.prepareStatement(sql.toString());
            int index = 1;
            if (keyword != null && !keyword.trim().isEmpty()) {
                stmt.setString(index++, "%" + keyword + "%");
                stmt.setString(index++, "%" + keyword + "%");
            }
            if (categoryId != null && categoryId > 0) {
                stmt.setInt(index++, categoryId);
            }
            rs = stmt.executeQuery();
            while (rs.next()) {
                Product product = new Product();
                product.setProductId(rs.getInt("product_id"));
                product.setProductName(rs.getString("product_name"));
                product.setCategoryId(rs.getInt("category_id"));
                product.setPrice(rs.getDouble("price"));
                product.setStock(rs.getInt("stock"));
                product.setUnit(rs.getString("unit"));
                product.setSupplier(rs.getString("supplier"));
                product.setStatus(rs.getString("status"));
                product.setCategoryName(rs.getString("category_name"));
                list.add(product);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, stmt, rs);
        }
        return list;
    }

    public boolean addProduct(Product product) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "INSERT INTO products VALUES (seq_product.NEXTVAL,?,?,?,?,?,?,?,SYSDATE)";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, product.getProductName());
            stmt.setInt(2, product.getCategoryId());
            stmt.setDouble(3, product.getPrice());
            stmt.setInt(4, product.getStock());
            stmt.setString(5, product.getUnit());
            stmt.setString(6, product.getSupplier());
            stmt.setString(7, product.getStatus());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, stmt, null);
        }
        return false;
    }

    public boolean updateProduct(Product product) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "UPDATE products SET product_name=?,category_id=?,price=?,stock=?,unit=?,supplier=?,status=? WHERE product_id=?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, product.getProductName());
            stmt.setInt(2, product.getCategoryId());
            stmt.setDouble(3, product.getPrice());
            stmt.setInt(4, product.getStock());
            stmt.setString(5, product.getUnit());
            stmt.setString(6, product.getSupplier());
            stmt.setString(7, product.getStatus());
            stmt.setInt(8, product.getProductId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, stmt, null);
        }
        return false;
    }

    public boolean deleteProduct(int productId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "DELETE FROM products WHERE product_id=?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, productId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, stmt, null);
        }
        return false;
    }

    public int countAllProducts() {
        Connection conn = null; PreparedStatement stmt = null; ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement("SELECT COUNT(*) FROM products");
            rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        finally { DBUtil.close(conn, stmt, rs); }
        return 0;
    }
}
