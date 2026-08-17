package com.example.agentlearning.lab07;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 解析 Memory Extractor 输出的结构化 JSON：
 * {@code {"shouldRemember":true,"memoryType":"PREFERENCE","content":"..."}}
 *
 * <p>程序做确定性校验：字段缺失/类型不对时按"不保存"处理。
 */
public final class MemoryDecisionParser {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private MemoryDecisionParser() {
    }

    public static MemoryDecision parse(String modelReply) {
        try {
            JsonNode root = MAPPER.readTree(modelReply);
            boolean shouldRemember = root.path("shouldRemember").asBoolean(false);
            String type = root.path("memoryType").asText("PREFERENCE");
            String content = root.path("content").asText("");
            return new MemoryDecision(shouldRemember, type, content);
        } catch (Exception e) {
            // 模型没按约定输出：不保存
            return new MemoryDecision(false, "PREFERENCE", "");
        }
    }
}
