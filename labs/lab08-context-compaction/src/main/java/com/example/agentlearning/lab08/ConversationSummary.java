package com.example.agentlearning.lab08;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;

/**
 * 一份结构化对话摘要，对应 {@code conversation_summary} 表的一行。
 *
 * <p>比"一段随意摘要"更容易恢复（教材 9.6）：压缩时旧消息被归纳成
 * goal / completed / importantFacts / decisions / openQuestions / pendingActions。
 * 列表字段在数据库里存 JSON 数组字符串。
 */
public record ConversationSummary(
        String id,
        String conversationId,
        int version,
        String goal,
        List<String> completed,
        List<String> importantFacts,
        List<String> decisions,
        List<String> openQuestions,
        List<String> pendingActions,
        String createdAt) {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static String toJsonArray(List<String> values) {
        try {
            return MAPPER.writeValueAsString(values);
        } catch (Exception e) {
            throw new IllegalStateException("序列化摘要数组失败", e);
        }
    }

    public static List<String> fromJsonArray(String json) {
        try {
            return MAPPER.readValue(json, new TypeReference<List<String>>() {
            });
        } catch (Exception e) {
            return List.of();
        }
    }
}
