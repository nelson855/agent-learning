package com.example.agentlearning.lab02;

/**
 * 一条对话消息：角色 + 内容。
 *
 * <p>不可变 record，方便作为历史消息反复重新传给模型。
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
