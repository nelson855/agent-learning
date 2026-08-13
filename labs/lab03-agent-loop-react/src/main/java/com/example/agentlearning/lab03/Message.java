package com.example.agentlearning.lab03;

/**
 * 一条对话消息：角色 + 内容。
 *
 * <p>不可变 record。在 Agent Loop 里，它同时承载用户输入、模型决策、
 * 以及工具执行后的 Observation（带 {@code [observation]} 前缀）。
 */
public record Message(Role role, String content) {

    public static Message user(String content) {
        return new Message(Role.USER, content);
    }

    public static Message assistant(String content) {
        return new Message(Role.ASSISTANT, content);
    }

    public static Message system(String content) {
        return new Message(Role.SYSTEM, content);
    }
}
