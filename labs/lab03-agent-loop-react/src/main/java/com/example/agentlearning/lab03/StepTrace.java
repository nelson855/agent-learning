package com.example.agentlearning.lab03;

/**
 * Loop 中一步的轨迹记录：第几步、模型的决策、工具执行结果。
 *
 * <p>供调用方 / 测试断言"Agent 到底走了哪几步"。
 */
public record StepTrace(int step, AgentDecision decision, ToolResult toolResult) {

    /** 这一步的一行摘要。 */
    public String summary() {
        return "STEP " + step
                + " | action=" + decision.type()
                + " | tool=" + decision.toolCall().name()
                + " | result=" + toolResult.message();
    }
}
