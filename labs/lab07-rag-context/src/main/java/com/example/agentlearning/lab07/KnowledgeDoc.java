package com.example.agentlearning.lab07;

/**
 * 一篇知识文档，对应 {@code knowledge_doc} 表的一行。
 *
 * <p>语义：关于项目/外部世界——由本地 Markdown 导入的资料库，
 * 检索它回答"项目规范/外部知识"类问题。
 *
 * @param tags 逗号分隔的标签，如 {@code "sqlite,database"}
 */
public record KnowledgeDoc(String id, String title, String content, String tags, String createdAt) {
}
