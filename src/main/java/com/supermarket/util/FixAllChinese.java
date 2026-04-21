package com.supermarket.util;

import java.sql.*;

public class FixAllChinese {
    public static void main(String[] args) {
        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false);
            Statement stmt = conn.createStatement();

            // 更新分类
            stmt.executeUpdate("UPDATE categories SET category_name='食品', description='各类食品' WHERE category_id=1");
            stmt.executeUpdate("UPDATE categories SET category_name='饮料', description='各类饮品' WHERE category_id=2");
            stmt.executeUpdate("UPDATE categories SET category_name='日用品', description='生活用品' WHERE category_id=3");
            stmt.executeUpdate("UPDATE categories SET category_name='零食', description='休闲零食' WHERE category_id=4");
            stmt.executeUpdate("UPDATE categories SET category_name='调味品', description='厨房调料' WHERE category_id=5");
            stmt.executeUpdate("UPDATE categories SET category_name='清洁用品', description='家居清洁' WHERE category_id=6");
            stmt.executeUpdate("UPDATE categories SET category_name='个人护理', description='洗护用品' WHERE category_id=7");
            stmt.executeUpdate("UPDATE categories SET category_name='文具', description='办公文具' WHERE category_id=8");
            stmt.executeUpdate("UPDATE categories SET category_name='电子产品', description='数码配件' WHERE category_id=9");
            stmt.executeUpdate("UPDATE categories SET category_name='其他', description='其他商品' WHERE category_id=10");

            // 更新商品单位为中文
            stmt.executeUpdate("UPDATE products SET unit='瓶' WHERE product_id IN (1,3,5,7)");
            stmt.executeUpdate("UPDATE products SET unit='袋' WHERE product_id=2");
            stmt.executeUpdate("UPDATE products SET unit='盒' WHERE product_id=4");
            stmt.executeUpdate("UPDATE products SET unit='支' WHERE product_id=6");
            stmt.executeUpdate("UPDATE products SET unit='本' WHERE product_id=8");
            stmt.executeUpdate("UPDATE products SET unit='根' WHERE product_id=9");
            stmt.executeUpdate("UPDATE products SET unit='卷' WHERE product_id=10");

            stmt.close();
            conn.commit();
            System.out.println("所有中文数据更新成功！");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
