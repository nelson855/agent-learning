package com.example.agentlearning.lab04;

import java.util.List;

/**
 * 计数装饰器：包住任意 {@link LlmClient}，统计一共发起了多少次模型调用。
 *
 * <p>它让 Workflow 版与 Agent 版可以统一口径地比较 {@code model_call_count}：
 * Agent 版里"循环内的决策调用"和"工具内部再调模型生成"都会经过它被计数。
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
