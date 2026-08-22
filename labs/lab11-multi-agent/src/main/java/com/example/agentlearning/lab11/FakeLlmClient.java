package com.example.agentlearning.lab11;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * 离线 LLM 客户端：不访问网络，按顺序返回脚本预设的结构化回复。
 *
 * <p>它同时记录调用次数与全部请求历史，便于：
 * <ul>
 *   <li>统计每个生成器的 model_calls；</li>
 *   <li>断言每个 Worker 是否收到了自己的上下文（每条请求独立）；</li>
 *   <li>累计 context_chars。</li>
 * </ul>
 *
 * <p>脚本耗尽后复用最后一条回复；从未预设过返回空字符串。
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

    public List<Message> lastRequest() {
        return history.isEmpty() ? List.of() : history.get(history.size() - 1);
    }

    public List<List<Message>> allRequests() {
        return List.copyOf(history);
    }

    public int chatCalls() {
        return chatCalls;
    }

    public void resetHistory() {
        history.clear();
        chatCalls = 0;
    }
}