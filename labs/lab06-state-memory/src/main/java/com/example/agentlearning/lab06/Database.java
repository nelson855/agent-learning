package com.example.agentlearning.lab06;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * 共享的 SQLite 连接 + 全部表的自动建表。
 *
 * <p>本模块把所有"需要持久化"的东西都放进数据库，但它们是<b>五张不同的表</b>，
 * 语义互不相同（见 {@code initSchema} 里的注释）：
 * conversation（会话外壳）、message（对话历史）、agent_run（Agent 运行状态）、
 * memory（长期记忆）、task（工具操作的数据）。
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
                    CREATE TABLE IF NOT EXISTS conversation (
                        id TEXT PRIMARY KEY,
                        title TEXT NOT NULL,
                        created_at TEXT NOT NULL
                    )"""); // 会话外壳
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS message (
                        id TEXT PRIMARY KEY,
                        conversation_id TEXT NOT NULL,
                        role TEXT NOT NULL,
                        content TEXT NOT NULL,
                        created_at TEXT NOT NULL
                    )"""); // 对话历史（Conversation History）
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS agent_run (
                        run_id TEXT PRIMARY KEY,
                        conversation_id TEXT NOT NULL,
                        goal TEXT NOT NULL,
                        status TEXT NOT NULL,
                        current_step INTEGER NOT NULL,
                        started_at TEXT NOT NULL,
                        updated_at TEXT NOT NULL
                    )"""); // Agent 运行状态（Agent State）
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS memory (
                        id TEXT PRIMARY KEY,
                        user_id TEXT NOT NULL,
                        type TEXT NOT NULL,
                        content TEXT NOT NULL,
                        importance INTEGER NOT NULL,
                        created_at TEXT NOT NULL,
                        last_used_at TEXT
                    )"""); // 长期记忆（Long-term Memory）
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS task (
                        id TEXT PRIMARY KEY,
                        title TEXT NOT NULL,
                        description TEXT NOT NULL,
                        status TEXT NOT NULL,
                        created_at TEXT NOT NULL
                    )"""); // 任务数据（工具操作的对象）
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
