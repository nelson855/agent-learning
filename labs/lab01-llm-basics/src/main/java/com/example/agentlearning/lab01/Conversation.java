package com.example.agentlearning.lab01;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 一次会话：负责维护消息历史，并让模型基于完整历史回复。
 *
 * <p>多轮对话的连续性完全由这里的 {@code List<Message>} 维护——模型本身无状态。
 * history 只存在于进程内，程序退出即消失，因此这不是 Long-term Memory。
 */
public final class Conversation {

    private final List<Message> history = new ArrayList<>();
    private final LlmClient llmClient;

    public Conversation(LlmClient llmClient) {
        this.llmClient = llmClient;
    }

    /** 当前消息历史（只读副本）。 */
    public List<Message> history() {
        return List.copyOf(history);
    }

    /** 追加用户消息 → 携带完整历史调用模型 → 追加助手回复，返回模型回复。 */
    public LlmResponse sendUserMessage(String text) {
        history.add(new Message(Role.USER, text));
        LlmResponse response = llmClient.chat(history);
        history.add(new Message(Role.ASSISTANT, response.content()));
        return response;
    }

    /** 清空历史：模型立刻"失忆"。 */
    public void reset() {
        history.clear();
    }

    /** 只展示消息数量与角色列表，不打印内容，避免暴露敏感信息。 */
    public String historySummary() {
        String roles = history.stream()
                .map(message -> message.role().name())
                .collect(Collectors.joining(", "));
        return "history size=" + history.size() + " roles=[" + roles + "]";
    }
}
