package com.example.agentlearning.lab04;

/**
 * Loop 中一步的轨迹记录。
 */
public record StepTrace(int step, AgentDecision decision, ToolResult toolResult) {
}
