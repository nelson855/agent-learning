package com.example.agentlearning.lab02;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Map;

/**
 * 把模型输出的结构化 JSON 解析成程序可执行的动作。
 *
 * <p>与模型约定的输出格式（写在 system prompt 里）：
 * <ul>
 *   <li>要调用工具：{@code {"tool":"<工具名>","arguments":{...}}}</li>
 *   <li>纯文本回复：{@code {"tool":null,"text":"回复内容"}}</li>
 * </ul>
 *
 * <p>这里就是"结构化输出"落地的入口：自然语言经模型转成了字段，程序才能可靠地读取、
 * 校验、分派。解析失败时按纯文本处理（原样返回），不让解析异常打断流程。
 */
public final class ToolCallParser {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private ToolCallParser() {
    }

    public static LlmIntent parse(String modelReply) {
        try {
            JsonNode root = MAPPER.readTree(modelReply);
            // hasNonNull 区分"字段缺失 / tool 为 null"（→ 文本回复）与"工具名存在"（→ 工具调用）
            if (root.hasNonNull("tool")) {
                String toolName = root.path("tool").asText();
                Map<String, Object> arguments = toArguments(root.get("arguments"));
                return LlmIntent.toolCall(new ToolCall(toolName, arguments));
            }
            return LlmIntent.text(root.path("text").asText(modelReply));
        } catch (IOException | RuntimeException e) {
            // 模型没按约定输出 → 当作普通文本回复，不中断
            return LlmIntent.text(modelReply);
        }
    }

    private static Map<String, Object> toArguments(JsonNode node) {
        if (node == null || !node.isObject()) {
            return Map.of();
        }
        return MAPPER.convertValue(node, new TypeReference<Map<String, Object>>() {
        });
    }
}
