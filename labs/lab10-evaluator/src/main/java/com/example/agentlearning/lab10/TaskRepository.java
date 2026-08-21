package com.example.agentlearning.lab10;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * 底层任务数据访问：建表、插入模拟数据、聚合统计。
 *
 * <p>本模块只有一张表 {@code task}，包含任务的执行记录：
 * <ul>
 *   <li>{@code status}：{@code SUCCESS} / {@code FAILURE} / {@code DEGRADED}</li>
 *   <li>{@code duration_seconds}：执行时长（秒）</li>
 *   <li>{@code created_at}：ISO 时间戳</li>
 * </ul>
 *
 * <p>SQL 保持简单可读，不引入 ORM。
 */
public final class TaskRepository {

    private static final String CREATE_TABLE = """
            CREATE TABLE IF NOT EXISTS task (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                status TEXT NOT NULL CHECK (status IN ('SUCCESS', 'FAILURE', 'DEGRADED')),
                duration_seconds INTEGER NOT NULL DEFAULT 0,
                created_at TEXT NOT NULL DEFAULT (datetime('now'))
            )""";

    private final Connection connection;

    public TaskRepository(Connection connection) {
        this.connection = connection;
        initSchema();
    }

    private void initSchema() {
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(CREATE_TABLE);
        } catch (SQLException e) {
            throw new IllegalStateException("初始化 task 表失败", e);
        }
    }

    /** 清空并插入一组模拟任务数据（演示用）。 */
    public void seedDemoData() {
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("DELETE FROM task");
            // 成功任务 180 条
            stmt.executeUpdate("INSERT INTO task (status, duration_seconds) VALUES ('SUCCESS', 60)");
            stmt.executeUpdate("INSERT INTO task (status, duration_seconds) VALUES ('SUCCESS', 45)");
            stmt.executeUpdate("INSERT INTO task (status, duration_seconds) VALUES ('SUCCESS', 120)");
            stmt.executeUpdate("INSERT INTO task (status, duration_seconds) VALUES ('SUCCESS', 30)");
            stmt.executeUpdate("INSERT INTO task (status, duration_seconds) VALUES ('SUCCESS', 90)");
            stmt.executeUpdate("INSERT INTO task (status, duration_seconds) VALUES ('SUCCESS', 55)");
            stmt.executeUpdate("INSERT INTO task (status, duration_seconds) VALUES ('SUCCESS', 80)");
            stmt.executeUpdate("INSERT INTO task (status, duration_seconds) VALUES ('SUCCESS', 70)");
            stmt.executeUpdate("INSERT INTO task (status, duration_seconds) VALUES ('SUCCESS', 110)");
            stmt.executeUpdate("INSERT INTO task (status, duration_seconds) VALUES ('SUCCESS', 40)");
            stmt.executeUpdate("INSERT INTO task (status, duration_seconds) VALUES ('SUCCESS', 65)");
            stmt.executeUpdate("INSERT INTO task (status, duration_seconds) VALUES ('SUCCESS', 95)");
            stmt.executeUpdate("INSERT INTO task (status, duration_seconds) VALUES ('SUCCESS', 50)");
            stmt.executeUpdate("INSERT INTO task (status, duration_seconds) VALUES ('SUCCESS', 75)");
            stmt.executeUpdate("INSERT INTO task (status, duration_seconds) VALUES ('SUCCESS', 85)");
            // 足够的数据量
            for (int i = 0; i < 165; i++) {
                stmt.executeUpdate("INSERT INTO task (status, duration_seconds) VALUES ('SUCCESS', 55)");
            }
            // 失败任务 12 条
            stmt.executeUpdate("INSERT INTO task (status, duration_seconds) VALUES ('FAILURE', 300)");
            stmt.executeUpdate("INSERT INTO task (status, duration_seconds) VALUES ('FAILURE', 240)");
            stmt.executeUpdate("INSERT INTO task (status, duration_seconds) VALUES ('FAILURE', 500)");
            stmt.executeUpdate("INSERT INTO task (status, duration_seconds) VALUES ('FAILURE', 350)");
            stmt.executeUpdate("INSERT INTO task (status, duration_seconds) VALUES ('FAILURE', 280)");
            stmt.executeUpdate("INSERT INTO task (status, duration_seconds) VALUES ('FAILURE', 410)");
            stmt.executeUpdate("INSERT INTO task (status, duration_seconds) VALUES ('FAILURE', 320)");
            stmt.executeUpdate("INSERT INTO task (status, duration_seconds) VALUES ('FAILURE', 190)");
            stmt.executeUpdate("INSERT INTO task (status, duration_seconds) VALUES ('FAILURE', 260)");
            stmt.executeUpdate("INSERT INTO task (status, duration_seconds) VALUES ('FAILURE', 380)");
            stmt.executeUpdate("INSERT INTO task (status, duration_seconds) VALUES ('FAILURE', 220)");
            stmt.executeUpdate("INSERT INTO task (status, duration_seconds) VALUES ('FAILURE', 450)");
            // 退化/超时 3 条
            stmt.executeUpdate("INSERT INTO task (status, duration_seconds) VALUES ('DEGRADED', 600)");
            stmt.executeUpdate("INSERT INTO task (status, duration_seconds) VALUES ('DEGRADED', 750)");
            stmt.executeUpdate("INSERT INTO task (status, duration_seconds) VALUES ('DEGRADED', 900)");
        } catch (SQLException e) {
            throw new IllegalStateException("插入模拟数据失败", e);
        }
    }

    /** 从 task 表聚合出本周统计摘要。 */
    public TaskStats aggregateStats() {
        String sql = """
                SELECT
                    COUNT(*)                                AS total_tasks,
                    SUM(CASE WHEN status = 'SUCCESS' THEN 1 ELSE 0 END) AS completed_tasks,
                    SUM(CASE WHEN status = 'FAILURE' THEN 1 ELSE 0 END) AS failed_tasks,
                    ROUND(1.0 * SUM(CASE WHEN status IN ('FAILURE','DEGRADED') THEN 1 ELSE 0 END)
                        / MAX(COUNT(*), 1), 3)             AS abnormal_ratio,
                    ROUND(AVG(duration_seconds) / 60.0, 0)  AS avg_minutes,
                    SUM(CASE WHEN status = 'DEGRADED' THEN 1 ELSE 0 END) AS degraded_tasks
                FROM task
                """;
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (!rs.next()) {
                return new TaskStats(0, 0, 0, 0.0, 0, 0);
            }
            return new TaskStats(
                    rs.getInt("total_tasks"),
                    rs.getInt("completed_tasks"),
                    rs.getInt("failed_tasks"),
                    rs.getDouble("abnormal_ratio"),
                    rs.getInt("avg_minutes"),
                    rs.getInt("degraded_tasks"));
        } catch (SQLException e) {
            throw new IllegalStateException("聚合任务统计失败", e);
        }
    }
}