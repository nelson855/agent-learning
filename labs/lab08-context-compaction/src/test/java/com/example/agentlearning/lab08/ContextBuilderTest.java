package com.example.agentlearning.lab08;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * ContextBuilder 组装顺序与数量验证：
 * System + [Summary 块] + Recent Messages + Request。
 */
class ContextBuilderTest {

    @Test
    void assemblesSystemSummaryRecentRequest() {
        ConversationSummary summary = new ConversationSummary(
                "s1", "c1", 3, "目标", List.of("A"), List.of(), List.of(), List.of(), List.of(), "t");
        List<StoredMessage> recent = List.of(
                new StoredMessage("m1", "c1", "user", "你好", "t1"),
                new StoredMessage("m2", "c1", "assistant", "你好呀", "t2"));

        List<Message> context = new ContextBuilder().build("系统提示", summary, recent, "最新请求");

        assertEquals(5, context.size()); // system + summary + 2 条最近 + request
        assertEquals("系统提示", context.get(0).content());
        assertEquals(Role.SYSTEM, context.get(0).role());

        assertEquals(Role.SYSTEM, context.get(1).role());
        assertTrue(context.get(1).content().contains("[CONVERSATION SUMMARY v3]"));

        assertEquals(Role.USER, context.get(2).role());
        assertEquals("你好", context.get(2).content());
        assertEquals(Role.ASSISTANT, context.get(3).role());
        assertEquals("你好呀", context.get(3).content());

        assertEquals(Role.USER, context.get(4).role());
        assertEquals("最新请求", context.get(4).content());
    }

    @Test
    void withoutSummaryContextIsSmaller() {
        List<Message> context = new ContextBuilder().build(
                "系统提示", null, List.of(), "最新请求");
        assertEquals(2, context.size()); // system + request
    }

    @Test
    void renderSummaryFormatsAllFields() {
        ConversationSummary summary = new ConversationSummary(
                "s1", "c1", 2, "目标", List.of("完成A"), List.of(),
                List.of("决定X"), List.of("问题Y"), List.of("动作Z"), "t");

        String text = ContextBuilder.renderSummary(summary);

        assertTrue(text.contains("[CONVERSATION SUMMARY v2]"));
        assertTrue(text.contains("- goal: 目标"));
        assertTrue(text.contains("\"完成A\""));
        assertTrue(text.contains("\"决定X\""));
        assertTrue(text.contains("\"问题Y\""));
        assertTrue(text.contains("\"动作Z\""));
    }
}
