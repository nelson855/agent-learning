package com.example.agentlearning.lab06;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 消息角色。注意 {@link #wireName()} 上的 {@code @JsonValue}：
 * Jackson 序列化时输出小写 wire 名（system / user / assistant），
 * 否则真实模型会收到大写的枚举名并报错。
 */
public enum Role {
    SYSTEM("system"),
    USER("user"),
    ASSISTANT("assistant");

    private final String wireName;

    Role(String wireName) {
        this.wireName = wireName;
    }

    @JsonValue
    public String wireName() {
        return wireName;
    }

    @Override
    public String toString() {
        return wireName;
    }
}
