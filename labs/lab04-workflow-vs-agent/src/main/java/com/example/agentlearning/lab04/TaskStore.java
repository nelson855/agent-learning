package com.example.agentlearning.lab04;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 任务的 SQLite 存储（JDBC 直连）。启动时自动建表。
 */
public final class TaskStore implements AutoCloseable {

    private final String jdbcUrl;
    private Connection connection;

    public TaskStore(String jdbcUrl) {
        if (jdbcUrl == null || jdbcUrl.isBlank()) {
            throw new IllegalArgumentException("缺少 SQLite jdbcUrl");
        }
        this.jdbcUrl = jdbcUrl;
        initSchema();
    }

    public void initSchema() {
        try (Statement statement = connection().createStatement()) {
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS task (
                        id TEXT PRIMARY KEY,
                        title TEXT NOT NULL,
                        description TEXT NOT NULL,
                        status TEXT NOT NULL,
                        created_at TEXT NOT NULL
                    )""");
        } catch (SQLException e) {
            throw new IllegalStateException("初始化 task 表失败: " + jdbcUrl, e);
        }
    }

    public void insert(Task task) {
        try (PreparedStatement ps = connection().prepareStatement(
                "INSERT INTO task (id, title, description, status, created_at) VALUES (?, ?, ?, ?, ?)")) {
            ps.setString(1, task.id());
            ps.setString(2, task.title());
            ps.setString(3, task.description());
            ps.setString(4, task.status());
            ps.setString(5, task.createdAt());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("插入任务失败: " + task.id(), e);
        }
    }

    public Optional<Task> findById(String id) {
        try (PreparedStatement ps = connection().prepareStatement(
                "SELECT id, title, description, status, created_at FROM task WHERE id = ?")) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(fromRow(rs));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("查询任务失败: " + id, e);
        }
    }

    public List<Task> findAll() {
        try (Statement st = connection().createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT id, title, description, status, created_at FROM task ORDER BY created_at")) {
            List<Task> tasks = new ArrayList<>();
            while (rs.next()) {
                tasks.add(fromRow(rs));
            }
            return tasks;
        } catch (SQLException e) {
            throw new IllegalStateException("查询全部任务失败", e);
        }
    }

    private Task fromRow(ResultSet rs) throws SQLException {
        return new Task(
                rs.getString("id"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getString("status"),
                rs.getString("created_at"));
    }

    private Connection connection() {
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
