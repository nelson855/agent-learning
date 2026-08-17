package com.example.agentlearning.lab08;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 原始对话历史的存取，对应 {@code message} 表。
 *
 * <p>压缩（compaction）时旧消息通过 {@link #deleteOldestMessages} 被删掉，
 * 只保留最近 {@code keepCount} 条——真正释放存储，而不是永远堆在库里。
 * 排序用 SQLite 隐式 rowid（插入顺序），避免时间戳相同导致顺序不稳。
 */
public final class MessageRepository {

    private final Database db;

    public MessageRepository(Database db) {
        this.db = db;
    }

    public StoredMessage append(String conversationId, String role, String content) {
        String id = "m-" + UUID.randomUUID().toString().substring(0, 8);
        StoredMessage message =
                new StoredMessage(id, conversationId, role, content, Instant.now().toString());
        try (PreparedStatement ps = db.connection().prepareStatement(
                "INSERT INTO message (id, conversation_id, role, content, created_at) VALUES (?, ?, ?, ?, ?)")) {
            ps.setString(1, message.id());
            ps.setString(2, message.conversationId());
            ps.setString(3, message.role());
            ps.setString(4, message.content());
            ps.setString(5, message.createdAt());
            ps.executeUpdate();
            return message;
        } catch (SQLException e) {
            throw new IllegalStateException("追加消息失败", e);
        }
    }

    public List<StoredMessage> findByConversation(String conversationId) {
        List<StoredMessage> result = new ArrayList<>();
        try (PreparedStatement ps = db.connection().prepareStatement(
                "SELECT id, conversation_id, role, content, created_at FROM message "
                        + "WHERE conversation_id = ? ORDER BY rowid")) {
            ps.setString(1, conversationId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new StoredMessage(
                            rs.getString("id"),
                            rs.getString("conversation_id"),
                            rs.getString("role"),
                            rs.getString("content"),
                            rs.getString("created_at")));
                }
            }
            return result;
        } catch (SQLException e) {
            throw new IllegalStateException("查询对话历史失败: " + conversationId, e);
        }
    }

    public int countByConversation(String conversationId) {
        try (PreparedStatement ps = db.connection().prepareStatement(
                "SELECT COUNT(*) FROM message WHERE conversation_id = ?")) {
            ps.setString(1, conversationId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("统计对话历史失败: " + conversationId, e);
        }
    }

    /**
     * 删除最早的消息，只保留最近 {@code keepCount} 条。
     *
     * <p>用标量子查询定位"第 keepCount 新的 rowid"，再删除所有更早的行——
     * 比 {@code NOT IN (SELECT ... ORDER BY ... LIMIT ...)} 更可靠，
     * 不依赖 SQLite 对子查询排序的保留行为。
     */
    public void deleteOldestMessages(String conversationId, int keepCount) {
        try (PreparedStatement ps = db.connection().prepareStatement(
                "DELETE FROM message WHERE conversation_id = ? AND rowid < ("
                        + "SELECT rowid FROM message WHERE conversation_id = ? "
                        + "ORDER BY rowid DESC LIMIT 1 OFFSET ?)")) {
            ps.setString(1, conversationId);
            ps.setString(2, conversationId);
            ps.setInt(3, keepCount - 1);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("删除旧消息失败: " + conversationId, e);
        }
    }
}
