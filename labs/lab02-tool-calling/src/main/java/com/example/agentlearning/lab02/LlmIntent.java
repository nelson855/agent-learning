package com.example.agentlearning.lab02;

/**
 * 模型一次回复被解析出的意图：要么是"调用某个工具"，要么是"纯文本回复"。
 *
 * <p>两者互斥，用 {@code toolCall != null} 判断走哪条路。
 */
public record LlmIntent(ToolCall toolCall, String text) {

    public static LlmIntent toolCall(ToolCall call) {
        return new LlmIntent(call, null);
    }

    public static LlmIntent text(String text) {
        return new LlmIntent(null, text);
    }

    public boolean wantsToolCall() {
        return toolCall != null;
    }
}
