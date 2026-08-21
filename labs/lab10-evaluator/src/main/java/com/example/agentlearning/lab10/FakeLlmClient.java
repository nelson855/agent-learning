package com.example.agentlearning.lab10;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * 离线 LLM 客户端：不访问网络，返回脚本预设的结构化回复。
 *
 * <p>用于确定性测试与离线 Demo——通过 {@link #enqueue(String...)} 按调用顺序注入各轮回复
 * （Generator 的周报 JSON、Evaluator 的反馈 JSON），从而驱动并验证 Generator-Evaluator Loop，
 * 不依赖真实模型。它同时记录调用次数与最近一次收到的完整消息历史，便于断言请求构造是否正确。
 *
 * <p>队列为空时复用最后一条回复；从未预设过则返回空字符串。
 */
public final class FakeLlmClient implements LlmClient {

    private final Deque<String> script = new ArrayDeque<>();
    private final List<List<Message>> history = new ArrayList<>();
    private int chatCalls;

    public FakeLlmClient() {
    }

    public FakeLlmClient(String... replies) {
        enqueue(replies);
    }

    /** 追加若干条回复，每次 {@link #chat} 会按顺序弹出。 */
    public FakeLlmClient enqueue(String... replies) {
        for (String reply : replies) {
            script.addLast(reply);
        }
        return this;
    }

    @Override
    public LlmResponse chat(List<Message> messages) {
        chatCalls++;
        history.add(List.copyOf(messages));
        return new LlmResponse(script.isEmpty() ? "" : script.removeFirst());
    }

    /** 历史中某次请求收到的完整消息列表（含内容）。 */
    public List<Message> lastRequest() {
        return history.isEmpty() ? List.of() : history.get(history.size() - 1);
    }

    /** 到目前为止所有请求的只读副本（按调用先后）。 */
    public List<List<Message>> allRequests() {
        return List.copyOf(history);
    }

    /** 到目前为止总共发起的模型调用次数。用于断言「结构错误时不调用 Evaluator」。 */
    public int chatCalls() {
        return chatCalls;
    }
}