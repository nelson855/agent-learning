package com.example.agentlearning.lab10;

/** LLM 的一次响应结果。生成器 / 评估器约定的结构化 JSON 都落在 {@code content} 里。 */
public record LlmResponse(String content) {
}