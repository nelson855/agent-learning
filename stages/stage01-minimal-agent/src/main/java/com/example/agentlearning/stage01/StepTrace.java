package com.example.agentlearning.stage01;

/**
 * Loop 中一步的轨迹记录（用于测试断言与统计 tool_call_count）。
 */
public record StepTrace(int step, AgentDecision decision, ToolResult toolResult) {
}
