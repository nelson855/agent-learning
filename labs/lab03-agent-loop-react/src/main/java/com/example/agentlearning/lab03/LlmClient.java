package com.example.agentlearning.lab03;

import java.util.List;

/**
 * 供应商无关的 LLM 客户端接口。
 *
 * <p>接真实模型观察智能行为，接 {@link ScriptedLlmClient} / {@link FunctionLlmClient}
 * 做确定性测试。
 */
public interface LlmClient {

    /**
     * 传入完整上下文（消息历史），返回模型回复。
     */
    LlmResponse chat(List<Message> messages);
}
