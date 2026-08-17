package com.example.agentlearning.lab08;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * 共享的 SQLite 连接 + 自动建表。
 *
 * <p>两张表：
 * <ul>
 *   <li>{@code message}：原始对话历史（压缩时旧消息从这里删除）；</li>
 *   <li>{@code conversation_summary}：压缩产生的结构化摘要（可跨重启保留）。</li>
 * </ul>
 */
public final class Database implements AutoCloseable {

    private final String jdbcUrl;
    private Connection connection;

    public Database(String jdbcUrl) {
        if (jdbcUrl == null || jdbcUrl.isBlank()) {
            throw new IllegalArgumentException("缺少 SQLite jdbcUrl");
        }
        this.jdbcUrl = jdbcUrl;
        initSchema();
    }

    public void initSchema() {
        try (Statement statement = connection().createStatement()) {
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS message (
                        id TEXT PRIMARY KEY,
                        conversation_id TEXT NOT NULL,
                        role TEXT NOT NULL,
                        content TEXT NOT NULL,
                        created_at TEXT NOT NULL
                    )""");
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS conversation_summary (
                        id TEXT PRIMARY KEY,
                        conversation_id TEXT NOT NULL,
                        version INTEGER NOT NULL,
                        goal TEXT NOT NULL,
                        completed TEXT NOT NULL,
                        important_facts TEXT NOT NULL,
                        decisions TEXT NOT NULL,
                        open_questions TEXT NOT NULL,
                        pending_actions TEXT NOT NULL,
                        created_at TEXT NOT NULL
                    )""");
        } catch (SQLException e) {
            throw new IllegalStateException("初始化数据库失败: " + jdbcUrl, e);
        }
    }

    public Connection connection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(jdbcUrl);
            }
            return connection;
        } catch (SQLException e) {
            throw new IllegalStateException("打开 SQLite 连接失败: " + jdbcUrl, e);
        }
    }

    @Override
    public void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ignored) {
                // 关闭失败无需处理
            }
        }
    }
}
