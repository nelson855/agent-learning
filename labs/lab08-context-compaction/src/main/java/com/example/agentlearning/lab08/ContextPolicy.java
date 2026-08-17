package com.example.agentlearning.lab08;

/**
 * Context 策略：什么阈值触发压缩、上下文里保留多少最近消息。
 *
 * <p>默认（教材 9.8）：
 * <ul>
 *   <li>COMPACT_AFTER = 20 条消息：超过才压缩；</li>
 *   <li>RECENT_MESSAGES = 10 条：压缩后保留的最近消息数。</li>
 * </ul>
 * 阈值可配置，测试用更小的值方便验证。
 */
public final class ContextPolicy {

    public static final int DEFAULT_COMPACT_AFTER = 20;
    public static final int DEFAULT_RECENT_MESSAGES = 10;

    private final int compactAfter;
    private final int recentMessages;

    public ContextPolicy() {
        this(DEFAULT_COMPACT_AFTER, DEFAULT_RECENT_MESSAGES);
    }

    public ContextPolicy(int compactAfter, int recentMessages) {
        if (compactAfter <= recentMessages) {
            throw new IllegalArgumentException("COMPACT_AFTER 必须大于 RECENT_MESSAGES");
        }
        this.compactAfter = compactAfter;
        this.recentMessages = recentMessages;
    }

    /** 当前消息数超过阈值时应该压缩。 */
    public boolean shouldCompact(int messageCount) {
        return messageCount > compactAfter;
    }

    public int recentMessages() {
        return recentMessages;
    }

    public int compactAfter() {
        return compactAfter;
    }
}
