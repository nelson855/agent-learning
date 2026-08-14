package com.example.agentlearning.lab06;

/**
 * Memory Extractor 的判定结果：这条消息值不值得长期保存。
 */
public record MemoryDecision(boolean shouldRemember, String memoryType, String content) {
}
