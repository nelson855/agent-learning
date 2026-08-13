package com.example.agentlearning.lab03;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 对话消息角色，对应 OpenAI-compatible Chat API 的 {@code role} 字段。
 */
public enum Role {

    SYSTEM,
    USER,
    ASSISTANT;

    /**
     * API 线格式使用小写；Jackson 序列化 {@link Message} 时会自动调用该方法。
     */
    @JsonValue
    public String wireValue() {
        return name().toLowerCase();
    }
}
