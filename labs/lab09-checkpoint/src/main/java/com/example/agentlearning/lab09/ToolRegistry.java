package com.example.agentlearning.lab09;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 工具注册表：按名字注册、按名字查找工具。
 *
 * <p>Agent 步骤通过 {@code planStep.tool()} 指定工具名，由这里分发执行——
 * 这正是"模型输出（或预定义计划）驱动程序行为"的落点。
 */
public final class ToolRegistry {

    private final Map<String, Tool> tools = new LinkedHashMap<>();

    public ToolRegistry register(String name, Tool tool) {
        tools.put(name, tool);
        return this;
    }

    public Tool get(String name) {
        Tool tool = tools.get(name);
        if (tool == null) {
            throw new IllegalArgumentException("未注册的工具: " + name);
        }
        return tool;
    }

    public boolean contains(String name) {
        return tools.containsKey(name);
    }

    /** 演示用确定性工具：把参数原样回显。 */
    public static Tool echo() {
        return args -> "工具[echo]收到: " + args;
    }
}
