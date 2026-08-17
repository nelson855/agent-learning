package com.example.agentlearning.lab07;

/**
 * 一条长期记忆，对应 {@code memory} 表的一行。
 *
 * <p>语义：关于用户——偏好、约定、事实。来自 Agent 对用户过去经历的提取，
 * 与 knowledge_doc（关于项目/外部世界的知识文档）来源不同、用途不同。
 */
public record Memory(
        String id,
        String userId,
        String type,
        String content,
        int importance,
        String createdAt,
        String lastUsedAt) {
}
