package com.example.agentlearning.lab01;

import java.util.List;

/**
 * 供应商无关的 LLM 客户端接口。
 *
 * <p>接真实模型观察智能行为，接 {@link FakeLlmClient} 做确定性测试。
 */
public interface LlmClient {

    /**
     * 传入完整消息历史，返回模型回复。
     *
     * <p>模型本身无状态，多轮对话的连续性由调用方重新提交 history 来保证。
     */
    LlmResponse chat(List<Message> messages);
}
