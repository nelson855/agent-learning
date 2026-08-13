package com.example.agentlearning.lab03;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * 函数式 LLM 客户端：把"模型"实现为一个接收消息历史、返回决策 JSON 的函数。
 *
 * <p>它让测试能精确模拟 ReAct 的关键行为 —— <b>根据上一次 Observation 决定下一步</b>：
 * 例如先 createTask，再从观察里提取真实任务 id 去 getTask，最后 final。
 * 用静态剧本表达不了的"循环依赖观察"场景，它能干净地表达。
 */
public final class FunctionLlmClient implements LlmClient {

    private final Function<List<Message>, String> responder;
    private final List<List<Message>> requests = new ArrayList<>();

    public FunctionLlmClient(Function<List<Message>, String> responder) {
        this.responder = responder;
    }

    @Override
    public LlmResponse chat(List<Message> messages) {
        requests.add(List.copyOf(messages));
        return new LlmResponse(responder.apply(messages));
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
