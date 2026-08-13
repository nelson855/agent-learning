package com.example.agentlearning.lab05;

import java.util.List;

/**
 * LLM 客户端抽象：给一段消息历史，返回一段文本。
 *
 * <p>真实模型（{@link OpenAiCompatibleLlmClient}）与确定性测试
 * （{@link ScriptedLlmClient}）通过这个接口自由切换。
 */
public interface LlmClient {

    LlmResponse chat(List<Message> messages);
}
