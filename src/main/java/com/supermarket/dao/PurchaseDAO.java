package com.supermarket.dao;

import com.supermarket.util.DBUtil;
import java.sql.*;
import java.util.*;

public class PurchaseDAO {

    public List<Map<String, Object>> getAllPOs() {
        List<Map<String, Object>> list = new ArrayList<>();
        Connection conn = null; PreparedStatement stmt = null; ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(
                "SELECT po.*, s.supplier_name, u.username FROM purchase_orders po " +
                "JOIN suppliers s ON po.supplier_id=s.supplier_id " +
                "LEFT JOIN users u ON po.operator_id=u.user_id " +
                "ORDER BY po.create_time DESC");
            rs = stmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("poId",         rs.getInt("po_id"));
                row.put("supplierName", rs.getString("supplier_name"));
                row.put("totalCost",    rs.getDouble("total_cost"));
                row.put("status",       rs.getString("status"));
                row.put("operator",     rs.getString("username"));
                row.put("remark",       rs.getString("remark"));
                row.put("createTime",   rs.getTimestamp("create_time"));
                row.put("arriveTime",   rs.getTimestamp("arrive_time"));
                list.add(row);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        finally { DBUtil.close(conn, stmt, rs); }
        return list;
    }

    // 创建采购单 + 明细（事务）
    public boolean createPO(int supplierId, int operatorId, String remark,
                             int[] productIds, int[] quantities, double[] unitCosts) {
        Connection conn = null; PreparedStatement stmt = null;
        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);

            double total = 0;
            for (int i = 0; i < quantities.length; i++) total += quantities[i] * unitCosts[i];

            stmt = conn.prepareStatement(
                "INSERT INTO purchase_orders VALUES (seq_po.NEXTVAL,?,?,,'pending',?,?,SYSDATE,NULL)",
                new String[]{"po_id"});
            // Oracle 不支持上面写法，改用 RETURNING
            stmt.close();
            stmt = conn.prepareStatement(
                "INSERT INTO purchase_orders(po_id,supplier_id,total_cost,status,operator_id,remark,create_time) " +
                "VALUES (seq_po.NEXTVAL,?,?,'pending',?,?,SYSDATE)", new String[]{"po_id"});
            stmt.setInt(1, supplierId);
            stmt.setDouble(2, total);
            stmt.setInt(3, operatorId);
            stmt.setString(4, remark);
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            int poId = -1;
            if (rs.next()) poId = rs.getInt(1);
            rs.close(); stmt.close();

            for (int i = 0; i < productIds.length; i++) {
                stmt = conn.prepareStatement(
                    "INSERT INTO purchase_items VALUES (seq_po_item.NEXTVAL,?,?,?,?)");
                stmt.setInt(1, poId);
                stmt.setInt(2, productIds[i]);
                stmt.setInt(3, quantities[i]);
                stmt.setDouble(4, unitCosts[i]);
                stmt.executeUpdate();
                stmt.close();
            }
            conn.commit();
            return true;
        } catch (SQLException e) {
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) {}
            e.printStackTrace();
        } finally {
            try { if (conn != null) conn.setAutoCommit(true); } catch (SQLException e) {}
            DBUtil.close(conn, stmt, null);
        }
        return false;
    }

    // 审核到货：更新采购单状态 + 入库（事务）
    public boolean approvePO(int poId) {
        Connection conn = null; PreparedStatement stmt = null; ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);

            // 查明细
            stmt = conn.prepareStatement("SELECT product_id, quantity FROM purchase_items WHERE po_id=?");
            stmt.setInt(1, poId);
            rs = stmt.executeQuery();
            List<int[]> items = new ArrayList<>();
            while (rs.next()) items.add(new int[]{rs.getInt(1), rs.getInt(2)});
            rs.close(); stmt.close();

            // 更新库存
            for (int[] item : items) {
                stmt = conn.prepareStatement("UPDATE products SET stock=stock+? WHERE product_id=?");
                stmt.setInt(1, item[1]);
                stmt.setInt(2, item[0]);
                stmt.executeUpdate();
                stmt.close();

                stmt = conn.prepareStatement(
                    "INSERT INTO inventory_logs VALUES (seq_inventory_log.NEXTVAL,?,'in',?,NULL,'采购入库',SYSDATE)");
                stmt.setInt(1, item[0]);
                stmt.setInt(2, item[1]);
                stmt.executeUpdate();
                stmt.close();
            }

            // 更新采购单状态
            stmt = conn.prepareStatement(
                "UPDATE purchase_orders SET status='arrived', arrive_time=SYSDATE WHERE po_id=?");
            stmt.setInt(1, poId);
            stmt.executeUpdate();

            conn.commit();
            return true;
        } catch (SQLException e) {
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) {}
            e.printStackTrace();
        } finally {
            try { if (conn != null) conn.setAutoCommit(true); } catch (SQLException e) {}
            DBUtil.close(conn, stmt, rs);
        }
        return false;
    }

    public List<Map<String, Object>> getPOItems(int poId) {
        List<Map<String, Object>> list = new ArrayList<>();
        Connection conn = null; PreparedStatement stmt = null; ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(
                "SELECT pi.*, p.product_name FROM purchase_items pi " +
                "JOIN products p ON pi.product_id=p.product_id WHERE pi.po_id=?");
            stmt.setInt(1, poId);
            rs = stmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("productName", rs.getString("product_name"));
                row.put("quantity",    rs.getInt("quantity"));
                row.put("unitCost",    rs.getDouble("unit_cost"));
                row.put("subtotal",    rs.getInt("quantity") * rs.getDouble("unit_cost"));
                list.add(row);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        finally { DBUtil.close(conn, stmt, rs); }
        return list;
    }
}
