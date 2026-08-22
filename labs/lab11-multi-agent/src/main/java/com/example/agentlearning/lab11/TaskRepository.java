package com.example.agentlearning.lab11;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * 底层任务数据访问：建表、插入模拟数据、聚合统计。
 */
public final class TaskRepository {

    private final Database db;

    public TaskRepository(Database db) {
        this.db = db;
    }

    public void seedDemoData() {
        try (Statement st = db.connection().createStatement()) {
            st.executeUpdate("DELETE FROM task");
            for (int i = 0; i < 180; i++) {
                st.executeUpdate("INSERT INTO task (status, duration_seconds) VALUES ('SUCCESS', 60)");
            }
            for (int[] dur : new int[][]{{300}, {240}, {500}, {350}, {280}, {410}, {320}, {190}, {260}, {380}, {220}, {450}}) {
                st.executeUpdate("INSERT INTO task (status, duration_seconds) VALUES ('FAILURE', " + dur[0] + ")");
            }
            st.executeUpdate("INSERT INTO task (status, duration_seconds) VALUES ('DEGRADED', 600)");
            st.executeUpdate("INSERT INTO task (status, duration_seconds) VALUES ('DEGRADED', 750)");
            st.executeUpdate("INSERT INTO task (status, duration_seconds) VALUES ('DEGRADED', 900)");
        } catch (SQLException e) {
            throw new IllegalStateException("插入模拟数据失败", e);
        }
    }

    public TaskStats aggregateStats() {
        String sql = """
                SELECT
                    COUNT(*) AS total_tasks,
                    SUM(CASE WHEN status = 'SUCCESS' THEN 1 ELSE 0 END) AS completed_tasks,
                    SUM(CASE WHEN status = 'FAILURE' THEN 1 ELSE 0 END) AS failed_tasks,
                    ROUND(1.0 * SUM(CASE WHEN status IN ('FAILURE','DEGRADED') THEN 1 ELSE 0 END)
                        / MAX(COUNT(*), 1), 3) AS abnormal_ratio,
                    ROUND(AVG(duration_seconds) / 60.0, 0) AS avg_minutes,
                    SUM(CASE WHEN status = 'DEGRADED' THEN 1 ELSE 0 END) AS degraded_tasks
                FROM task""";
        try (Statement st = db.connection().createStatement();
                ResultSet rs = st.executeQuery(sql)) {
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