package com.example.agentlearning.lab08;

/**
 * 一条已持久化的对话消息，对应 {@code message} 表的一行。
 */
public record StoredMessage(String id, String conversationId, String role, String content, String createdAt) {
}
