package com.example.agentlearning.lab06;

/**
 * 一次 Agent 运行的持久化状态，对应 {@code agent_run} 表的一行。
 *
 * <p>这是"Agent State"：goal（本次要做什么）、status（当前状态机所处状态）、
 * currentStep（ReAct 循环走到第几步）。它和对话历史是两个不同的持久化维度。
 */
public record AgentRun(
        String runId,
        String conversationId,
        String goal,
        RunStatus status,
        int currentStep,
        String startedAt,
        String updatedAt) {
}
