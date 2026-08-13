package com.example.agentlearning.lab01;

/**
 * LLM 的一次响应结果。
 *
 * <p>本章只关心最终文本；后续章节会在此基础上扩展结构化输出、Tool Call 等能力。
 */
public record LlmResponse(String content) {
}
