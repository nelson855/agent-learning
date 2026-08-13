package com.example.agentlearning.lab02;

import java.util.Map;

/**
 * 一个工具调用：模型"建议"执行的动作（工具名 + 参数）。
 *
 * <p>注意：这只是模型的<b>建议</b>。这个动作能不能执行、参数对不对、
 * 执行后返回什么，全部由程序决定 —— 这是本章的核心教学点之一。
 */
public record ToolCall(String name, Map<String, Object> arguments) {

    /** 参数可能缺失或为空对象，统一收敛为不可变 Map。 */
    public ToolCall {
        arguments = arguments == null ? Map.of() : Map.copyOf(arguments);
    }
}
