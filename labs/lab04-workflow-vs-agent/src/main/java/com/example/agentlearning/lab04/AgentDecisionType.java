package com.example.agentlearning.lab04;

/**
 * 模型一次决策的类型：调用工具（Action），或给出最终回答（终止 Loop）。
 */
public enum AgentDecisionType {
    TOOL_CALL,
    FINAL
}
