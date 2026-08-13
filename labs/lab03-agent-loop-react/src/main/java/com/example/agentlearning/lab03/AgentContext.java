package com.example.agentlearning.lab03;

import java.util.ArrayList;
import java.util.List;

/**
 * Agent 的上下文（Context）：一段完整、不断增长的消息历史。
 *
 * <p>它是 Loop 的"记忆载体"：用户输入、模型的每次决策、每次工具执行后的
 * <b>Observation</b> 都会被追加进去，并在下一轮完整地重新提交给模型。
 * 模型没有后台记忆，它看到的"当前状况"就是这个上下文。
 */
public final class AgentContext {

    private final List<Message> messages = new ArrayList<>();

    public void addSystem(String content) {
        messages.add(Message.system(content));
    }

    public void addUser(String content) {
        messages.add(Message.user(content));
    }

    /** 追加模型上一次的原始决策输出（assistant 消息）。 */
    public void addAssistant(String modelReply) {
        messages.add(Message.assistant(modelReply));
    }

    /**
     * 追加一次工具执行后的观察（Observation）。
     *
     * <p>我们用带 {@code [observation]} 前缀的 user 消息承载工具结果，
     * system prompt 里告诉模型看到这个前缀就知道是工具执行的返回值，
     * 应当基于它继续决策。
     */
    public void addObservation(ToolCall call, ToolResult result) {
        String text = "[observation] " + call.name() + "(" + call.arguments() + ") => "
                + (result.success() ? "" : "[失败] ") + result.message();
        messages.add(Message.user(text));
    }

    /** 当前完整消息历史（只读副本）。 */
    public List<Message> messages() {
        return List.copyOf(messages);
    }
}
