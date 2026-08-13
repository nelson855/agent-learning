package com.example.agentlearning.lab05;

import java.util.List;

/**
 * 计数装饰器：包住任意 {@link LlmClient}，统计一共发起了多少次模型调用。
 *
 * <p>Planner 调 1 次、每次 Replan 调 1 次——通过它断言"正常步骤不触发 Replan"
 * 和"失败触发一次 / 达到上限"，让 Replan 成本可观测。
 */
public final class CountingLlmClient implements LlmClient {

    private final LlmClient delegate;
    private int count;

    public CountingLlmClient(LlmClient delegate) {
        this.delegate = delegate;
    }

    @Override
    public LlmResponse chat(List<Message> messages) {
        count++;
        return delegate.chat(messages);
    }

    /** 到目前为止发起的模型调用总次数。 */
    public int count() {
        return count;
    }
}
