package com.example.agentlearning.lab02;

import java.util.ArrayList;
import java.util.List;

/**
 * 离线 LLM 客户端：不访问网络，返回预设的结构化回复。
 *
 * <p>用于确定性测试：通过 {@link #setReply(String)} 注入不同场景的 JSON 回复
 * （调用哪个工具、参数是什么、或纯文本），从而驱动并验证工具分派流程，不依赖真实模型。
 * 它也会记录最近一次收到的完整消息历史，便于断言请求构造是否正确。
 */
public final class FakeLlmClient implements LlmClient {

    private String reply;
    private final List<Message> lastRequest = new ArrayList<>();

    public FakeLlmClient() {
        this("{\"tool\":null,\"text\":\"(fake) 我收到了你的消息。\"}");
    }

    public FakeLlmClient(String reply) {
        this.reply = reply;
    }

    /** 更换预设回复（测试中驱动不同场景）。 */
    public void setReply(String reply) {
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
