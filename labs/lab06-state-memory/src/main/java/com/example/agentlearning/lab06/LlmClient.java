package com.example.agentlearning.lab06;

import java.util.List;

/**
 * 模型供应商无关的 LLM 接口。Demo 既可以接真实模型，也可以接
 * {@link ScriptedLlmClient} 做确定性测试。
 */
public interface LlmClient {

    /** 传入上下文消息，返回模型回复。 */
    LlmResponse chat(List<Message> messages);
}
