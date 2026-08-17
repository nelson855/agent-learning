package com.example.agentlearning.lab07;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Context Builder：验证五块（System + Recent + Memory + Knowledge + Request）被组装成一次 LLM 调用。
 */
class ContextBuilderTest {

    @Test
    void buildAssemblesAllFiveSections() {
        ContextBuilder builder = new ContextBuilder();
        List<Memory> memories = List.of(
                new Memory("m1", "u", "PREFERENCE", "用户偏好 Maven 构建", 5, "now", null));
        List<KnowledgeDoc> knowledge = List.of(
                new KnowledgeDoc("k1", "编码规范", "本项目使用 Maven 作为构建工具。", "maven", "now"));
        List<Message> recent = List.of(Message.user("上一轮问题"), Message.assistant("上一轮回答"));

        List<Message> context = builder.build("系统提示", recent, memories, knowledge, "我的项目用什么构建？");

        // 1) 首条是主 System Prompt
        assertEquals("系统提示", context.get(0).content());
        // 2) 接着是带标记的 Memory 块与 Knowledge 块（放 System，让模型当背景知识）
        String memoryBlock = context.get(1).content();
        assertTrue(memoryBlock.startsWith("[RETRIEVED MEMORY]"));
        assertTrue(memoryBlock.contains("用户偏好 Maven 构建"));
        String knowledgeBlock = context.get(2).content();
        assertTrue(knowledgeBlock.startsWith("[RETRIEVED KNOWLEDGE]"));
        assertTrue(knowledgeBlock.contains("编码规范"));
        // 3) 中间是 Recent Messages
        assertEquals("上一轮问题", context.get(3).content());
        assertEquals("上一轮回答", context.get(4).content());
        // 4) 最后是 Current Request
        assertEquals("我的项目用什么构建？", context.get(context.size() - 1).content());
        assertEquals(Role.USER, context.get(context.size() - 1).role());
    }

    @Test
    void buildWithoutHitsHasNoBlocks() {
        ContextBuilder builder = new ContextBuilder();
        List<Message> context = builder.build("系统提示", List.of(), List.of(), List.of(), "你好");

        assertEquals(2, context.size()); // system + user
        assertEquals("你好", context.get(1).content());
    }

    @Test
    void snippetTruncatesLongContent() {
        assertEquals("短文本", ContextBuilder.snippet("短文本"));
        String longText = "a".repeat(500);
        String snippet = ContextBuilder.snippet(longText);
        assertTrue(snippet.endsWith("…"));
        assertTrue(snippet.length() < longText.length());
    }
}
