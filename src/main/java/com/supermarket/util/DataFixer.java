package com.supermarket.util;

import java.sql.*;

public class DataFixer {
    public static void main(String[] args) {
        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false);
            String[] updates = {
                "UPDATE products SET product_name='可口可乐', supplier='可口可乐公司' WHERE product_id=1",
                "UPDATE products SET product_name='康师傅方便面', supplier='康师傅' WHERE product_id=2",
                "UPDATE products SET product_name='农夫山泉', supplier='农夫山泉' WHERE product_id=3",
                "UPDATE products SET product_name='奥利奥饼干', supplier='亿滋' WHERE product_id=4",
                "UPDATE products SET product_name='洗洁精', supplier='立白' WHERE product_id=5",
                "UPDATE products SET product_name='牙膏', supplier='高露洁' WHERE product_id=6",
                "UPDATE products SET product_name='酱油', supplier='海天' WHERE product_id=7",
                "UPDATE products SET product_name='笔记本', supplier='晨光' WHERE product_id=8",
                "UPDATE products SET product_name='充电线', supplier='品胜' WHERE product_id=9",
                "UPDATE products SET product_name='垃圾袋', supplier='妙洁' WHERE product_id=10"
            };

            Statement stmt = conn.createStatement();
            for (String sql : updates) {
                stmt.executeUpdate(sql);
            }
            stmt.close();
            conn.commit();
            System.out.println("数据更新成功！");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
