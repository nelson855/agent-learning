package com.example.agentlearning.lab02;

/**
 * LLM 的一次响应结果。
 *
 * <p>本章模型按约定输出一段结构化 JSON（工具调用或纯文本回复），
 * 该 JSON 就落在 {@code content} 字段里，由 {@link ToolCallParser} 负责解析。
 */
public record LlmResponse(String content) {
}
