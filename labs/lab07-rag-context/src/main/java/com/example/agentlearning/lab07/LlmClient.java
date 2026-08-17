package com.example.agentlearning.lab07;

import java.util.List;

/**
 * 模型供应商无关的 LLM 接口。
 */
public interface LlmClient {

    LlmResponse chat(List<Message> messages);
}
