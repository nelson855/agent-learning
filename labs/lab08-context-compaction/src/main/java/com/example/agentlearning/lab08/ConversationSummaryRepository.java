package com.example.agentlearning.lab08;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 结构化摘要的存取，对应 {@code conversation_summary} 表。
 * 摘要不随 message 表的删除而丢失——重启后仍可读取。
 */
public final class ConversationSummaryRepository {

    private final Database db;

    public ConversationSummaryRepository(Database db) {
        this.db = db;
    }

    public ConversationSummary save(
            String conversationId,
            int version,
            String goal,
            List<String> completed,
            List<String> importantFacts,
            List<String> decisions,
            List<String> openQuestions,
            List<String> pendingActions) {
        String id = "s-" + UUID.randomUUID().toString().substring(0, 8);
        ConversationSummary summary = new ConversationSummary(
                id, conversationId, version, goal, completed, importantFacts, decisions,
                openQuestions, pendingActions, Instant.now().toString());
        try (PreparedStatement ps = db.connection().prepareStatement(
                "INSERT INTO conversation_summary (id, conversation_id, version, goal, completed, "
                        + "important_facts, decisions, open_questions, pending_actions, created_at) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
            ps.setString(1, summary.id());
            ps.setString(2, summary.conversationId());
            ps.setInt(3, summary.version());
            ps.setString(4, summary.goal());
            ps.setString(5, ConversationSummary.toJsonArray(summary.completed()));
            ps.setString(6, ConversationSummary.toJsonArray(summary.importantFacts()));
            ps.setString(7, ConversationSummary.toJsonArray(summary.decisions()));
            ps.setString(8, ConversationSummary.toJsonArray(summary.openQuestions()));
            ps.setString(9, ConversationSummary.toJsonArray(summary.pendingActions()));
            ps.setString(10, summary.createdAt());
            ps.executeUpdate();
            return summary;
        } catch (SQLException e) {
            throw new IllegalStateException("保存摘要失败", e);
        }
    }

    /** 最近一份摘要；从未压缩过则空。 */
    public Optional<ConversationSummary> findLatest(String conversationId) {
        try (PreparedStatement ps = db.connection().prepareStatement(
                "SELECT id, conversation_id, version, goal, completed, important_facts, decisions, "
                        + "open_questions, pending_actions, created_at FROM conversation_summary "
                        + "WHERE conversation_id = ? ORDER BY version DESC LIMIT 1")) {
            ps.setString(1, conversationId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(fromRow(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new IllegalStateException("查询摘要失败: " + conversationId, e);
        }
    }

    /** 下一版版本号：从未压缩过则从 1 开始，否则 +1。 */
    public int nextVersion(String conversationId) {
        return findLatest(conversationId).map(s -> s.version() + 1).orElse(1);
    }

    private ConversationSummary fromRow(ResultSet rs) throws SQLException {
        return new ConversationSummary(
                rs.getString("id"),
                rs.getString("conversation_id"),
                rs.getInt("version"),
                rs.getString("goal"),
                ConversationSummary.fromJsonArray(rs.getString("completed")),
                ConversationSummary.fromJsonArray(rs.getString("important_facts")),
                ConversationSummary.fromJsonArray(rs.getString("decisions")),
                ConversationSummary.fromJsonArray(rs.getString("open_questions")),
                ConversationSummary.fromJsonArray(rs.getString("pending_actions")),
                rs.getString("created_at"));
    }
}
