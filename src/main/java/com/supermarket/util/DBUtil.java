package com.supermarket.util;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

/**
 * DBUtil - 数据库工具类
 * 提供获取连接、JdbcTemplate、事务管理器的能力
 *
 * 注意：实际项目中推荐通过 Spring 容器注入，这里提供静态方法以简化 Servlet 中的使用。
 */
public class DBUtil {

    private static Properties dbProps;
    private static DataSource dataSource;
    private static JdbcTemplate jdbcTemplate;
    private static PlatformTransactionManager transactionManager;

    static {
        loadDbProperties();
    }

    private static void loadDbProperties() {
        dbProps = new Properties();
        try {
            InputStream in = DBUtil.class.getClassLoader().getResourceAsStream("db.properties");
            if (in != null) {
                dbProps.load(in);
                in.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static DataSource getDataSource() {
        if (dataSource == null) {
            try {
                org.apache.commons.dbcp2.BasicDataSource ds =
                        new org.apache.commons.dbcp2.BasicDataSource();
                ds.setDriverClassName(dbProps.getProperty("db.driver", "oracle.jdbc.OracleDriver"));
                ds.setUrl(dbProps.getProperty("db.url", "jdbc:oracle:thin:@127.0.0.1:1521:XE"));
                ds.setUsername(dbProps.getProperty("db.username", "system"));
                ds.setPassword(dbProps.getProperty("db.password", "123456"));
                ds.setInitialSize(5);
                ds.setMaxTotal(20);
                ds.setMaxIdle(10);
                ds.setMinIdle(2);
                ds.setMaxWaitMillis(10000);
                ds.setValidationQuery("SELECT 1 FROM DUAL");
                ds.setTestOnBorrow(true);
                dataSource = ds;
            } catch (Exception e) {
                throw new RuntimeException("初始化数据源失败", e);
            }
        }
        return dataSource;
    }

    public static JdbcTemplate getJdbcTemplate() {
        if (jdbcTemplate == null) {
            jdbcTemplate = new JdbcTemplate(getDataSource());
        }
        return jdbcTemplate;
    }

    public static PlatformTransactionManager getTransactionManager() {
        if (transactionManager == null) {
            transactionManager = new DataSourceTransactionManager(getDataSource());
        }
        return transactionManager;
    }

    /**
     * 获取下一个序列值（Oracle）
     */
    public static Integer getNextId(String sequenceName) {
        String sql = "SELECT " + sequenceName + ".NEXTVAL FROM DUAL";
        return getJdbcTemplate().queryForObject(sql, Integer.class);
    }

    /**
     * 关闭连接
     */
    public static void close(Connection conn, Statement stmt, ResultSet rs) {
        try { if (rs != null) rs.close(); } catch (Exception ignored) {}
        try { if (stmt != null) stmt.close(); } catch (Exception ignored) {}
        try { if (conn != null) conn.close(); } catch (Exception ignored) {}
    }

    public static void close(Connection conn, Statement stmt) {
        close(conn, stmt, null);
    }
}
