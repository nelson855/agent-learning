package com.example.agentlearning.lab06;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 发给 LLM 的一条消息。与数据库里的 {@link StoredMessage} 是两个概念：
 * 这个是"上下文里的消息"，不落库。
 */
public record Message(Role role, String content) {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static Message system(String content) {
        return new Message(Role.SYSTEM, content);
    }

    public static Message user(String content) {
        return new Message(Role.USER, content);
    }

    public static Message assistant(String content) {
        return new Message(Role.ASSISTANT, content);
    }

    /** 序列化为 OpenAI 兼容的 JSON。 */
    public String toJson() {
        try {
            return MAPPER.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("消息序列化失败", e);
        }
    }
}
