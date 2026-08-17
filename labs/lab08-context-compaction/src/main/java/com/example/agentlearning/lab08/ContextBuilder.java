package com.example.agentlearning.lab08;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Context Builder（lab08 版）：把压缩后的上下文组装成一次 LLM 调用。
 *
 * <pre>
 *   System Prompt
 * + Conversation Summary（压缩产物，可选）
 * + Recent Messages（最近保留的原始消息）
 * + Current Request
 * </pre>
 * 打印 SUMMARY_VERSION / RECENT_COUNT / FINAL_CONTEXT_COUNT，让压缩结果可观察。
 */
public final class ContextBuilder {

    public List<Message> build(
            String systemPrompt,
            ConversationSummary summary,
            List<StoredMessage> recent,
            String request) {

        List<Message> result = new ArrayList<>();
        result.add(Message.system(systemPrompt));

        if (summary != null) {
            result.add(Message.system(renderSummary(summary)));
        }
        for (StoredMessage message : recent) {
            if ("user".equals(message.role())) {
                result.add(Message.user(message.content()));
            } else {
                result.add(Message.assistant(message.content()));
            }
        }
        result.add(Message.user(request));

        System.out.println("SUMMARY_VERSION: " + (summary == null ? 0 : summary.version()));
        System.out.println("RECENT_COUNT: " + recent.size());
        System.out.println("FINAL_CONTEXT_COUNT: " + result.size());
        return result;
    }

    /** 把结构化摘要渲染成一段给模型看的文本。 */
    static String renderSummary(ConversationSummary summary) {
        return "[CONVERSATION SUMMARY v" + summary.version() + "]\n"
                + "- goal: " + summary.goal() + "\n"
                + "- completed: " + join(summary.completed()) + "\n"
                + "- importantFacts: " + join(summary.importantFacts()) + "\n"
                + "- decisions: " + join(summary.decisions()) + "\n"
                + "- openQuestions: " + join(summary.openQuestions()) + "\n"
                + "- pendingActions: " + join(summary.pendingActions());
    }

    private static String join(List<String> values) {
        if (values.isEmpty()) {
            return "[]";
        }
        return "[" + values.stream().map(v -> "\"" + v + "\"").collect(Collectors.joining(", ")) + "]";
    }
}
