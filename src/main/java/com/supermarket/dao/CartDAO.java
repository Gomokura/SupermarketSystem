package com.supermarket.dao;

import com.supermarket.bean.Cart;
import com.supermarket.util.DBUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CartDAO {

    public List<Cart> getCartByUser(int userId) {
        List<Cart> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT c.*, p.product_name, p.price FROM cart c " +
                        "JOIN products p ON c.product_id = p.product_id WHERE c.user_id=?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            rs = stmt.executeQuery();
            while (rs.next()) {
                Cart cart = new Cart();
                cart.setCartId(rs.getInt("cart_id"));
                cart.setUserId(rs.getInt("user_id"));
                cart.setProductId(rs.getInt("product_id"));
                cart.setQuantity(rs.getInt("quantity"));
                cart.setProductName(rs.getString("product_name"));
                cart.setPrice(rs.getDouble("price"));
                list.add(cart);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, stmt, rs);
        }
        return list;
    }

    public boolean addToCart(int userId, int productId, int quantity) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            String checkSql = "SELECT quantity FROM cart WHERE user_id=? AND product_id=?";
            stmt = conn.prepareStatement(checkSql);
            stmt.setInt(1, userId);
            stmt.setInt(2, productId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                int newQty = rs.getInt("quantity") + quantity;
                stmt.close();
                stmt = conn.prepareStatement("UPDATE cart SET quantity=? WHERE user_id=? AND product_id=?");
                stmt.setInt(1, newQty);
                stmt.setInt(2, userId);
                stmt.setInt(3, productId);
                return stmt.executeUpdate() > 0;
            } else {
                stmt.close();
                stmt = conn.prepareStatement("INSERT INTO cart VALUES (seq_cart.NEXTVAL,?,?,?,SYSDATE)");
                stmt.setInt(1, userId);
                stmt.setInt(2, productId);
                stmt.setInt(3, quantity);
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, stmt, rs);
        }
        return false;
    }

    public boolean deleteCart(int cartId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement("DELETE FROM cart WHERE cart_id=?");
            stmt.setInt(1, cartId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, stmt, null);
        }
        return false;
    }

    public boolean updateQuantity(int cartId, int quantity) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement("UPDATE cart SET quantity=? WHERE cart_id=?");
            stmt.setInt(1, quantity);
            stmt.setInt(2, cartId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, stmt, null);
        }
        return false;
    }
}
