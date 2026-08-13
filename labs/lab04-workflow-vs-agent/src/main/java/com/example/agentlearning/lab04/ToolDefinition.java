package com.example.agentlearning.lab04;

import java.util.Map;

/**
 * 一个工具的定义：告诉模型"这个工具有什么、怎么调用"。
 */
public record ToolDefinition(String name, String description, Map<String, String> parameters) {

    public ToolDefinition {
        parameters = parameters == null ? Map.of() : Map.copyOf(parameters);
    }
}
