package com.example.agentlearning.lab01;

import java.util.ArrayList;
import java.util.List;

/**
 * 离线 LLM 客户端：不访问网络，返回固定回复。
 *
 * <p>用于确定性测试，以及未配置真实模型时的离线演示。它会记录最近一次收到的完整消息历史，
 * 便于测试断言"第二次请求是否携带了第一轮历史"。
 */
public final class FakeLlmClient implements LlmClient {

    private final String reply;
    private final List<Message> lastRequest = new ArrayList<>();

    public FakeLlmClient() {
        this("(fake) 我收到了你的消息。");
    }

    public FakeLlmClient(String reply) {
        this.reply = reply;
    }

    @Override
    public LlmResponse chat(List<Message> messages) {
        lastRequest.clear();
        lastRequest.addAll(messages);
        return new LlmResponse(reply);
    }

    /** 最近一次请求收到的完整消息历史（只读副本）。 */
    public List<Message> lastRequest() {
        return List.copyOf(lastRequest);
    }
}
