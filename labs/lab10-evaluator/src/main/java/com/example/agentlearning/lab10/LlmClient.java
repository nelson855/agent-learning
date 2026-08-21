package com.example.agentlearning.lab10;

import java.util.List;

/**
 * 供应商无关的 LLM 客户端接口。
 *
 * <p>生成器（Generator）与评估器（Evaluator）都通过此接口调用模型。
 * 接真实模型观察智能行为，接 {@link FakeLlmClient} 做确定性测试。
 */
public interface LlmClient {

    /** 传入完整消息历史，返回模型回复。模型本身无状态，连续性由调用方重新提交 history 保证。 */
    LlmResponse chat(List<Message> messages);
}