package com.example.agentlearning.lab02;

/**
 * 工具执行结果：是否成功 + 一段可展示的文本。
 *
 * <p>{@code success=false} 表示这个动作<b>没能执行</b>（参数不合法、工具不存在、
 * 业务规则拒绝等），消息里给出人类可读的原因。执行失败也是一种结果，
 * 不应当让程序抛异常崩溃。
 */
public record ToolResult(boolean success, String message) {

    public static ToolResult ok(String message) {
        return new ToolResult(true, message);
    }

    public static ToolResult fail(String message) {
        return new ToolResult(false, message);
    }
}
