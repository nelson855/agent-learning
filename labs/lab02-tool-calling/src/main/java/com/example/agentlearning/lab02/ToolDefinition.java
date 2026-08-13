package com.example.agentlearning.lab02;

import java.util.Map;

/**
 * 一个工具的定义：告诉模型"这个工具有什么、怎么调用"。
 *
 * <p>{@code parameters} 是参数名到类型描述（{@code "string"} / {@code "number"}）的映射，
 * 声明出来的每个参数都视为<b>必填</b>。定义越精确，模型越可能给出合法调用；
 * 但最终是否合法，仍由程序校验（见 {@link ArgumentValidator}）。
 *
 * <p>它是给模型看的行为契约，同时驱动两件事：生成给模型的工具说明 JSON，
 * 以及执行前的参数校验。
 */
public record ToolDefinition(String name, String description, Map<String, String> parameters) {

    public ToolDefinition {
        parameters = parameters == null ? Map.of() : Map.copyOf(parameters);
    }
}
