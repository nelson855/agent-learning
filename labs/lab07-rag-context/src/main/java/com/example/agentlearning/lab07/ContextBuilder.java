package com.example.agentlearning.lab07;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Context Builder——本章的核心。它回答 8.2 的问题：
 * "这一次模型调用，实际能看到什么？"
 *
 * <p>把五块东西组装成一次 LLM 调用的消息列表：
 * <pre>
 *   System Prompt
 *   + Retrieved Memory（用户长期偏好）
 *   + Retrieved Knowledge（RAG 项目知识）
 *   + Recent Messages（最近消息）
 *   + Current Request（当前请求）
 * </pre>
 * 运行时打印 MEMORY RETRIEVAL / RAG RETRIEVAL / CONTEXT SUMMARY 三段观察日志。
 */
public final class ContextBuilder {

    private static final int KNOWLEDGE_SNIPPET_LENGTH = 300;

    public List<Message> build(
            String systemPrompt,
            List<Message> recentMessages,
            List<Memory> memories,
            List<KnowledgeDoc> knowledge,
            String request) {

        printRetrievalLogs(memories, knowledge);

        List<Message> result = new ArrayList<>();
        result.add(Message.system(systemPrompt));

        if (!memories.isEmpty()) {
            String block = "[RETRIEVED MEMORY]\n" + memories.stream()
                    .map(m -> "- [" + m.type() + "] " + m.content())
                    .collect(Collectors.joining("\n"));
            result.add(Message.system(block));
        }
        if (!knowledge.isEmpty()) {
            String block = "[RETRIEVED KNOWLEDGE]\n" + knowledge.stream()
                    .map(d -> "- [" + d.title() + "] " + snippet(d.content()))
                    .collect(Collectors.joining("\n"));
            result.add(Message.system(block));
        }

        result.addAll(recentMessages);
        result.add(Message.user(request));

        printSummary(systemPrompt, recentMessages, memories, knowledge, request);
        return result;
    }

    private void printRetrievalLogs(List<Memory> memories, List<KnowledgeDoc> knowledge) {
        System.out.println("MEMORY RETRIEVAL");
        if (memories.isEmpty()) {
            System.out.println("  (无命中)");
        } else {
            for (Memory m : memories) {
                System.out.println("  - [" + m.type() + "] " + m.content());
            }
        }
        System.out.println("RAG RETRIEVAL");
        if (knowledge.isEmpty()) {
            System.out.println("  (无命中)");
        } else {
            for (KnowledgeDoc d : knowledge) {
                System.out.println("  - [" + d.title() + "] " + snippet(d.content()));
            }
        }
        System.out.println();
    }

    private void printSummary(
            String systemPrompt,
            List<Message> recentMessages,
            List<Memory> memories,
            List<KnowledgeDoc> knowledge,
            String request) {
        System.out.println("CONTEXT SUMMARY");
        System.out.println("  system prompt: 1 条");
        System.out.println("  recent messages: " + recentMessages.size() + " 条");
        System.out.println("  memory: " + memories.size() + " 条");
        System.out.println("  knowledge: " + knowledge.size() + " 条");
        System.out.println("  request: " + request);
        System.out.println();
    }

    /** 知识文档正文可能很长，注入 context 前截断成摘要片段。 */
    static String snippet(String content) {
        if (content == null) {
            return "";
        }
        String oneLine = content.replace('\n', ' ').strip();
        if (oneLine.length() <= KNOWLEDGE_SNIPPET_LENGTH) {
            return oneLine;
        }
        return oneLine.substring(0, KNOWLEDGE_SNIPPET_LENGTH) + "…";
    }
}
