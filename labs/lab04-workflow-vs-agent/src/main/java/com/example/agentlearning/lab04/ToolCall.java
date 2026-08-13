package com.example.agentlearning.lab04;

import java.util.Map;

/**
 * 一个工具调用：模型"建议"执行的动作（工具名 + 参数）。
 */
public record ToolCall(String name, Map<String, Object> arguments) {

    public ToolCall {
        arguments = arguments == null ? Map.of() : Map.copyOf(arguments);
    }
}
