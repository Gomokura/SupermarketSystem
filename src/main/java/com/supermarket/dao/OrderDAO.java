package com.supermarket.dao;

import com.supermarket.bean.Cart;
import com.supermarket.bean.Order;
import com.supermarket.bean.OrderItem;
import com.supermarket.util.DBUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {

    public int createOrder(int userId, List<Cart> items, String paymentMethod, double discount) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int orderId = -1;
        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);

            double total = items.stream().mapToDouble(Cart::getSubtotal).sum();
            total = total * discount;

            stmt = conn.prepareStatement("INSERT INTO orders VALUES (seq_order.NEXTVAL,?,?,SYSDATE,'pending',?) RETURNING order_id INTO ?",
                new String[]{"order_id"});
            stmt.setInt(1, userId);
            stmt.setDouble(2, total);
            stmt.setString(3, paymentMethod);
            stmt.executeUpdate();
            rs = stmt.getGeneratedKeys();
            if (rs.next()) orderId = rs.getInt(1);

            stmt.close();
            for (Cart item : items) {
                stmt = conn.prepareStatement("INSERT INTO order_items VALUES (seq_order_item.NEXTVAL,?,?,?,?)");
                stmt.setInt(1, orderId);
                stmt.setInt(2, item.getProductId());
                stmt.setInt(3, item.getQuantity());
                stmt.setDouble(4, item.getPrice());
                stmt.executeUpdate();
                stmt.close();

                // 写出库记录（动态库存，禁止直接改 stock 字段）
                stmt = conn.prepareStatement(
                    "INSERT INTO outbound VALUES (seq_outbound.NEXTVAL,?,?,?,SYSDATE)");
                stmt.setInt(1, orderId);
                stmt.setInt(2, item.getProductId());
                stmt.setInt(3, item.getQuantity());
                stmt.executeUpdate();
                stmt.close();

                stmt = conn.prepareStatement("DELETE FROM cart WHERE cart_id=?");
                stmt.setInt(1, item.getCartId());
                stmt.executeUpdate();
                stmt.close();
            }

            // 写支付记录
            stmt = conn.prepareStatement(
                "INSERT INTO payment VALUES (seq_payment.NEXTVAL,?,?,'success',SYSDATE)");
            stmt.setInt(1, orderId);
            stmt.setDouble(2, total);
            stmt.executeUpdate();
            stmt.close();

            conn.commit();
        } catch (SQLException e) {
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) {}
            e.printStackTrace();
        } finally {
            try { if (conn != null) conn.setAutoCommit(true); } catch (SQLException e) {}
            DBUtil.close(conn, stmt, rs);
        }
        return orderId;
    }

    public List<Order> getOrdersByUser(int userId) {
        List<Order> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement("SELECT * FROM orders WHERE user_id=? ORDER BY order_time DESC");
            stmt.setInt(1, userId);
            rs = stmt.executeQuery();
            while (rs.next()) {
                Order order = new Order();
                order.setOrderId(rs.getInt("order_id"));
                order.setUserId(rs.getInt("user_id"));
                order.setTotalAmount(rs.getDouble("total_amount"));
                order.setOrderTime(rs.getTimestamp("order_time"));
                order.setOrderStatus(rs.getString("order_status"));
                order.setPaymentMethod(rs.getString("payment_method"));
                list.add(order);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, stmt, rs);
        }
        return list;
    }

    public List<OrderItem> getOrderItems(int orderId) {
        List<OrderItem> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement("SELECT oi.*, p.product_name FROM order_items oi JOIN products p ON oi.product_id=p.product_id WHERE oi.order_id=?");
            stmt.setInt(1, orderId);
            rs = stmt.executeQuery();
            while (rs.next()) {
                OrderItem item = new OrderItem();
                item.setOrderItemId(rs.getInt("order_item_id"));
                item.setOrderId(rs.getInt("order_id"));
                item.setProductId(rs.getInt("product_id"));
                item.setQuantity(rs.getInt("quantity"));
                item.setPrice(rs.getDouble("price"));
                item.setProductName(rs.getString("product_name"));
                list.add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, stmt, rs);
        }
        return list;
    }

    public boolean updateOrderStatus(int orderId, String status) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement("UPDATE orders SET order_status=? WHERE order_id=?");
            stmt.setString(1, status);
            stmt.setInt(2, orderId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, stmt, null);
        }
        return false;
    }

    // 管理端：查询所有订单，支持状态过滤和关键词（订单号/用户名）
    public List<Order> searchAllOrders(String keyword, String status) {
        List<Order> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            StringBuilder sql = new StringBuilder(
                "SELECT o.*, u.username FROM orders o JOIN users u ON o.user_id=u.user_id WHERE 1=1");
            if (status != null && !status.isEmpty()) sql.append(" AND o.order_status=?");
            if (keyword != null && !keyword.isEmpty()) sql.append(" AND (CAST(o.order_id AS VARCHAR(20)) LIKE ? OR u.username LIKE ?)");
            sql.append(" ORDER BY o.order_time DESC");
            stmt = conn.prepareStatement(sql.toString());
            int idx = 1;
            if (status != null && !status.isEmpty()) stmt.setString(idx++, status);
            if (keyword != null && !keyword.isEmpty()) {
                stmt.setString(idx++, "%" + keyword + "%");
                stmt.setString(idx++, "%" + keyword + "%");
            }
            rs = stmt.executeQuery();
            while (rs.next()) {
                Order o = new Order();
                o.setOrderId(rs.getInt("order_id"));
                o.setUserId(rs.getInt("user_id"));
                o.setTotalAmount(rs.getDouble("total_amount"));
                o.setOrderTime(rs.getTimestamp("order_time"));
                o.setOrderStatus(rs.getString("order_status"));
                o.setPaymentMethod(rs.getString("payment_method"));
                o.setUsername(rs.getString("username"));
                list.add(o);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, stmt, rs);
        }
        return list;
    }

    // 管理端统计：总订单数
    public int countAllOrders() {
        Connection conn = null; PreparedStatement stmt = null; ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement("SELECT COUNT(*) FROM orders");
            rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        finally { DBUtil.close(conn, stmt, rs); }
        return 0;
    }

    // 管理端统计：总销售额
    public double sumTotalAmount() {
        Connection conn = null; PreparedStatement stmt = null; ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement("SELECT NVL(SUM(total_amount),0) FROM orders WHERE order_status != 'cancelled'");
            rs = stmt.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) { e.printStackTrace(); }
        finally { DBUtil.close(conn, stmt, rs); }
        return 0;
    }

    // 管理端统计：热销商品 TOP N
    public List<java.util.Map<String, Object>> getTopProducts(int n) {
        List<java.util.Map<String, Object>> list = new ArrayList<>();
        Connection conn = null; PreparedStatement stmt = null; ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(
                "SELECT p.product_name, SUM(oi.quantity) AS total_qty, SUM(oi.quantity*oi.price) AS total_revenue " +
                "FROM order_items oi JOIN products p ON oi.product_id=p.product_id " +
                "GROUP BY p.product_name ORDER BY total_qty DESC FETCH FIRST ? ROWS ONLY");
            stmt.setInt(1, n);
            rs = stmt.executeQuery();
            while (rs.next()) {
                java.util.Map<String, Object> row = new java.util.LinkedHashMap<>();
                row.put("productName", rs.getString("product_name"));
                row.put("totalQty", rs.getInt("total_qty"));
                row.put("totalRevenue", rs.getDouble("total_revenue"));
                list.add(row);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        finally { DBUtil.close(conn, stmt, rs); }
        return list;
    }
}
