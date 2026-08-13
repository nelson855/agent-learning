package com.example.agentlearning.stage01;

/**
 * 工具执行器：把一次 {@link ToolCall} 变成 {@link ToolResult}。
 *
 * <p>这是 Agent 循环与具体工具之间的唯一接缝——{@link AgentRunner} 只依赖这个接口，
 * 具体实现（本阶段是 {@link ToolRegistry}）负责登记、校验、分派。
 */
public interface ToolExecutor {

    ToolResult execute(ToolCall call);

    /** 生成一段给模型看的工具说明文本，用于拼进系统提示。 */
    String toolsInstruction();
}
