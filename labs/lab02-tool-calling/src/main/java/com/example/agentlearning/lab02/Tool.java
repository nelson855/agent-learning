package com.example.agentlearning.lab02;

import java.util.Map;
import java.util.function.Function;

/**
 * 一个已注册的可执行工具 = 定义（给模型看）+ 执行器（程序自己写）。
 *
 * <p>执行器接收参数 Map，返回 {@link ToolResult}。业务实现全部是确定性 Java 代码，
 * 不依赖模型 —— 这正体现了"模型负责决定做什么，程序负责怎么做"。
 */
public final class Tool {

    private final ToolDefinition definition;
    private final Function<Map<String, Object>, ToolResult> executor;

    public Tool(ToolDefinition definition, Function<Map<String, Object>, ToolResult> executor) {
        this.definition = definition;
        this.executor = executor;
    }

    public ToolDefinition definition() {
        return definition;
    }

    /** 直接执行（调用方负责先做参数校验）。 */
    public ToolResult execute(Map<String, Object> arguments) {
        return executor.apply(arguments);
    }
}
