package com.example.agentlearning.lab04;

import java.util.List;

/**
 * 供应商无关的 LLM 客户端接口。
 */
public interface LlmClient {

    LlmResponse chat(List<Message> messages);
}
