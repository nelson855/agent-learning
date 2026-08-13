package com.example.agentlearning.stage01;

import java.util.Map;

/**
 * 工具定义：给模型看的"说明书"——名字、用途、参数名与类型。
 *
 * <p>类型约定为字符串：{@code "string"} / {@code "number"}。
 */
public record ToolDefinition(String name, String description, Map<String, String> parameters) {

    public static ToolDefinition of(String name, String description, Map<String, String> parameters) {
        return new ToolDefinition(name, description, parameters);
    }
}
