package com.example.coursemanagementsystem;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.*;
import java.util.function.Consumer;

public class DatabaseUtil {
    private static final String URL = "jdbc:oracle:thin:@calvin.humber.ca:1521:grok";
    private static final String USER = "n01712678";
    private static final String PASSWORD = "oracle";
    private static HikariDataSource dataSource;

    static {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            System.out.println("Oracle JDBC Driver loaded successfully.");
        } catch (ClassNotFoundException e) {
            System.err.println("Oracle JDBC Driver not found.");
            throw new RuntimeException("Database driver missing", e);
        }
    }

    private static synchronized void initializeDataSource() {
        if (dataSource == null || dataSource.isClosed()) {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(URL);
            config.setUsername(USER);
            config.setPassword(PASSWORD);
            config.setMaximumPoolSize(20);
            config.setMinimumIdle(5);
            config.setPoolName("CourseManagementPool");
            config.setConnectionTimeout(10000); // 10 seconds
            config.setLeakDetectionThreshold(30000);
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            try {
                dataSource = new HikariDataSource(config);
                System.out.println("HikariCP pool initialized successfully.");
            } catch (Exception e) {
                System.err.println("Failed to initialize HikariCP pool: " + e.getMessage());
                throw new RuntimeException("Database pool initialization failed", e);
            }
        }
    }

    public static Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            initializeDataSource();
        }
        Connection conn = dataSource.getConnection();
        if (conn != null) {
            System.out.println("Connected to Oracle database via connection pool! Active: " +
                    dataSource.getHikariPoolMXBean().getActiveConnections() +
                    ", Idle: " + dataSource.getHikariPoolMXBean().getIdleConnections());
        }
        return conn;
    }

    public static void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            System.out.println("Database connection pool shut down.");
        }
    }

    public static void initializeDatabase() throws SQLException {
        String[] tableScripts = {
                "CREATE TABLE admin (admin_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY, username VARCHAR2(50) UNIQUE NOT NULL, password VARCHAR2(64) NOT NULL)",
                "CREATE TABLE courses (course_id VARCHAR2(10) PRIMARY KEY, course_name VARCHAR2(100) NOT NULL, credits NUMBER NOT NULL, max_students NUMBER NOT NULL, instructor_id VARCHAR2(10))",
                "CREATE TABLE instructors (instructor_id VARCHAR2(10) PRIMARY KEY, name VARCHAR2(100) NOT NULL, email VARCHAR2(100) NOT NULL)",
                "CREATE TABLE students (student_id VARCHAR2(10) PRIMARY KEY, name VARCHAR2(100) NOT NULL, email VARCHAR2(100) NOT NULL)",
                "CREATE TABLE course_students (course_id VARCHAR2(10), student_id VARCHAR2(10), PRIMARY KEY (course_id, student_id), " +
                        "FOREIGN KEY (course_id) REFERENCES courses(course_id), FOREIGN KEY (student_id) REFERENCES students(student_id))"
        };

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            DatabaseMetaData meta = conn.getMetaData();
            for (String script : tableScripts) {
                String tableName = script.split(" ")[2].toUpperCase();
                ResultSet rs = meta.getTables(null, null, tableName, new String[]{"TABLE"});
                if (!rs.next()) {
                    stmt.executeUpdate(script);
                    System.out.println(tableName + " table created.");
                }
                rs.close();
            }
        } catch (SQLException e) {
            throw new SQLException("Failed to initialize database: " + e.getMessage(), e);
        }
    }

    public static void executeQuery(String sql, Consumer<ResultSet> resultHandler, Object... params) throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = setParametersAndExecute(pstmt, params)) {
            resultHandler.accept(rs);
        }
    }

    private static ResultSet setParametersAndExecute(PreparedStatement pstmt, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            pstmt.setObject(i + 1, params[i]);
        }
        return pstmt.executeQuery();
    }

    public static void executeUpdate(String sql, Object... params) throws SQLException {
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
            pstmt.executeUpdate();
        }
    }
}