package com.example.agentlearning.lab07;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * 共享的 SQLite 连接 + 自动建表。
 *
 * <p>本模块只有两张表，二者<b>语义不同</b>：
 * <ul>
 *   <li>{@code memory}：关于"用户"的长期偏好/事实/约定（经过选择、跨会话有价值）；</li>
 *   <li>{@code knowledge_doc}：关于"项目/外部世界"的知识文档（导入的资料库）。</li>
 * </ul>
 * 它们都用 SQLite、都存文本、都会被检索放进 Prompt——但来源与语义完全不同。
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
                    CREATE TABLE IF NOT EXISTS memory (
                        id TEXT PRIMARY KEY,
                        user_id TEXT NOT NULL,
                        type TEXT NOT NULL,
                        content TEXT NOT NULL,
                        importance INTEGER NOT NULL,
                        created_at TEXT NOT NULL,
                        last_used_at TEXT
                    )"""); // 长期记忆：关于用户
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS knowledge_doc (
                        id TEXT PRIMARY KEY,
                        title TEXT NOT NULL,
                        content TEXT NOT NULL,
                        tags TEXT NOT NULL,
                        created_at TEXT NOT NULL
                    )"""); // 知识库：关于项目/外部世界
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
