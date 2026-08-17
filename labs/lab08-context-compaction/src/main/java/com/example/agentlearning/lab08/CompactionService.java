package com.example.agentlearning.lab08;

import java.util.List;

/**
 * 对话压缩服务：追加消息，并在历史超过阈值时压缩。
 *
 * <p>压缩 = 旧消息 → {@link ConversationSummarizer} 生成结构化摘要 → 存
 * {@code conversation_summary} 表 → 从 message 表<b>删除</b>旧消息，只留最近 N 条。
 * 这是"真正的压缩"：既保留可恢复的信息（摘要），又释放存储与上下文空间。
 *
 * <p>{@code rawHistoryCount} 累计本轮产生的原始消息总数，用于观察
 * "产出了多少"（RAW_HISTORY_COUNT）对比"上下文里还剩多少"。
 */
public final class CompactionService {

    private final MessageRepository messages;
    private final ConversationSummaryRepository summaries;
    private final ConversationSummarizer summarizer;
    private final ContextPolicy policy;
    private final ContextBuilder contextBuilder;
    private final String systemPrompt;

    private int rawHistoryCount;

    public CompactionService(
            MessageRepository messages,
            ConversationSummaryRepository summaries,
            ConversationSummarizer summarizer,
            ContextPolicy policy,
            ContextBuilder contextBuilder,
            String systemPrompt) {
        this.messages = messages;
        this.summaries = summaries;
        this.summarizer = summarizer;
        this.policy = policy;
        this.contextBuilder = contextBuilder;
        this.systemPrompt = systemPrompt;
    }

    public StoredMessage appendUser(String conversationId, String content) {
        rawHistoryCount++;
        StoredMessage message = messages.append(conversationId, "user", content);
        maybeCompact(conversationId);
        return message;
    }

    public StoredMessage appendAssistant(String conversationId, String content) {
        rawHistoryCount++;
        StoredMessage message = messages.append(conversationId, "assistant", content);
        maybeCompact(conversationId);
        return message;
    }

    /** 组装当前上下文：Summary + 最近保留消息 + 当前请求。 */
    public List<Message> buildContext(String conversationId, String request) {
        ConversationSummary summary = summaries.findLatest(conversationId).orElse(null);
        List<StoredMessage> recent = messages.findByConversation(conversationId);
        return contextBuilder.build(systemPrompt, summary, recent, request);
    }

    public int rawHistoryCount() {
        return rawHistoryCount;
    }

    public ConversationSummary latestSummary(String conversationId) {
        return summaries.findLatest(conversationId).orElse(null);
    }

    public ContextPolicy policy() {
        return policy;
    }

    private void maybeCompact(String conversationId) {
        int count = messages.countByConversation(conversationId);
        if (!policy.shouldCompact(count)) {
            return;
        }
        List<StoredMessage> all = messages.findByConversation(conversationId);
        int keep = policy.recentMessages();
        List<StoredMessage> old = all.subList(0, all.size() - keep);

        int version = summaries.nextVersion(conversationId);
        ConversationSummaryParser.ParsedSummary parsed = summarizer.summarize(old);
        summaries.save(
                conversationId,
                version,
                parsed.goal(),
                parsed.completed(),
                parsed.importantFacts(),
                parsed.decisions(),
                parsed.openQuestions(),
                parsed.pendingActions());
        messages.deleteOldestMessages(conversationId, keep);

        System.out.println("[COMPACT] " + old.size() + " 条旧消息 → Summary v" + version
                + "（goal=" + parsed.goal() + "），保留最近 " + keep + " 条");
    }
}
