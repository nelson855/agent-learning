package com.example.agentlearning.lab03;

import java.util.ArrayList;
import java.util.List;

/**
 * 剧本式 LLM 客户端：按预设剧本依次返回回复，用于确定性测试。
 *
 * <p>剧本耗尽后<b>重复最后一条</b>——这正是制造"死循环"的方式：
 * 只给一条"永远调用 getTask(NOT_FOUND)"的剧本，Agent 就会一直循环，
 * 用来验证 {@code maxSteps} 会拦停。
 */
public final class ScriptedLlmClient implements LlmClient {

    private final List<String> script;
    private final List<List<Message>> requests = new ArrayList<>();
    private int index;

    public ScriptedLlmClient(String... script) {
        if (script.length == 0) {
            throw new IllegalArgumentException("剧本不能为空");
        }
        this.script = List.of(script);
    }

    @Override
    public LlmResponse chat(List<Message> messages) {
        requests.add(List.copyOf(messages));
        String reply = script.get(Math.min(index, script.size() - 1));
        index++;
        return new LlmResponse(reply);
    }

    /** 已发起的请求次数。 */
    public int requestCount() {
        return requests.size();
    }

    /** 第 i 次请求收到的完整消息历史（0-based）。 */
    public List<Message> requestAt(int i) {
        return requests.get(i);
    }
}
