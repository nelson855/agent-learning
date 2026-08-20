package com.example.agentlearning.lab09;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 把 {@link AgentState} 存取到 SQLite {@code agent_checkpoint} 表。
 *
 * <p><b>每次保存写新 version，不覆盖历史</b>：同一次运行可以观察到状态的演化过程，
 * 也能回滚到任意较早版本。{@code /resume} 只取最新 version。
 */
public final class CheckpointRepository {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final Database db;

    public CheckpointRepository(Database db) {
        this.db = db;
    }

    /** 取某次运行的最新 version；从未保存过返回 0。 */
    public int latestVersion(String runId) {
        String sql = "SELECT COALESCE(MAX(version), 0) FROM agent_checkpoint WHERE run_id = ?";
        try (PreparedStatement ps = db.connection().prepareStatement(sql)) {
            ps.setString(1, runId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("查询最新 version 失败: " + runId, e);
        }
    }

    /** 保存一个新版本；version 自动 +1，绝不覆盖已有行。 */
    public void save(AgentState state) {
        int nextVersion = latestVersion(state.runId()) + 1;
        String id = UUID.randomUUID().toString();
        String sql = """
                INSERT INTO agent_checkpoint (id, run_id, version, state_json, created_at)
                VALUES (?, ?, ?, ?, ?)""";
        try (PreparedStatement ps = db.connection().prepareStatement(sql)) {
            ps.setString(1, id);
            ps.setString(2, state.runId());
            ps.setInt(3, nextVersion);
            ps.setString(4, toJson(state));
            ps.setString(5, Instant.now().toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("保存 Checkpoint 失败: " + state.runId(), e);
        }
    }

    /** 读取某次运行的最新版本；没有则返回空。 */
    public Optional<AgentState> loadLatest(String runId) {
        String sql = """
                SELECT state_json FROM agent_checkpoint
                WHERE run_id = ?
                ORDER BY version DESC
                LIMIT 1""";
        try (PreparedStatement ps = db.connection().prepareStatement(sql)) {
            ps.setString(1, runId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(fromJson(rs.getString("state_json")));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("读取 Checkpoint 失败: " + runId, e);
        }
    }

    private static String toJson(AgentState state) {
        try {
            return MAPPER.writeValueAsString(state);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("序列化 AgentState 失败", e);
        }
    }

    private static AgentState fromJson(String json) {
        try {
            return MAPPER.readValue(json, new TypeReference<AgentState>() {});
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("反序列化 AgentState 失败: " + json, e);
        }
    }

    /** 便于测试断言：某次运行已保存的版本数。 */
    public int countVersions(String runId) {
        String sql = "SELECT COUNT(*) FROM agent_checkpoint WHERE run_id = ?";
        try (PreparedStatement ps = db.connection().prepareStatement(sql)) {
            ps.setString(1, runId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("统计版本数失败: " + runId, e);
        }
    }
}
