package com.example.agentlearning.lab08;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;

/**
 * 把 Summarizer 输出的结构化 JSON 解析成 {@link ConversationSummary} 的内容字段。
 *
 * <p>程序做确定性校验：字段缺失/数组解析失败时用空数组兜底，不抛异常。
 * 这样模型输出偶尔不合规也不会让压缩流程崩掉。
 */
public final class ConversationSummaryParser {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private ConversationSummaryParser() {
    }

    public static ParsedSummary parse(String modelReply) {
        try {
            JsonNode root = MAPPER.readTree(modelReply);
            return new ParsedSummary(
                    root.path("goal").asText(""),
                    stringList(root.path("completed")),
                    stringList(root.path("importantFacts")),
                    stringList(root.path("decisions")),
                    stringList(root.path("openQuestions")),
                    stringList(root.path("pendingActions")));
        } catch (Exception e) {
            // 模型没按 JSON 输出：退化为空摘要，不中断流程
            return new ParsedSummary("", List.of(), List.of(), List.of(), List.of(), List.of());
        }
    }

    private static List<String> stringList(JsonNode node) {
        if (node == null || !node.isArray()) {
            return List.of();
        }
        try {
            return MAPPER.convertValue(node, new TypeReference<List<String>>() {
            });
        } catch (RuntimeException e) {
            return List.of();
        }
    }

    /** 解析出的摘要内容（不含 id/version 等存储字段）。 */
    public record ParsedSummary(
            String goal,
            List<String> completed,
            List<String> importantFacts,
            List<String> decisions,
            List<String> openQuestions,
            List<String> pendingActions) {
    }
}
