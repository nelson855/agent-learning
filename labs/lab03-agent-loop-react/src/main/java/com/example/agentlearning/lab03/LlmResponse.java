package com.example.agentlearning.lab03;

/**
 * LLM 的一次响应结果。
 *
 * <p>本章模型按约定输出一段结构化 JSON（tool_call 决策或 final 回答），
 * 该 JSON 落在 {@code content} 字段里，由 {@link AgentDecisionParser} 解析。
 */
public record LlmResponse(String content) {
}
