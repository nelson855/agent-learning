package com.example.agentlearning.lab02;

import java.util.Map;

/**
 * 工具参数校验：执行前检查"缺没缺、类型对不对"。
 *
 * <p>模型给出的参数来自模型自身的猜测，完全可能缺失、类型错误。
 * 程序必须在执行前拒绝非法参数（返回校验失败），而不是把错误参数传给
 * 业务代码导致 NPE 或脏数据 —— 这是"结构化输出"让程序获得确定性的关键一环。
 */
public final class ArgumentValidator {

    private ArgumentValidator() {
    }

    /**
     * 校验参数是否满足工具定义的必填与类型要求。
     *
     * @return 校验通过返回 {@code null}，否则返回人类可读的错误消息
     */
    public static String validate(ToolDefinition definition, Map<String, Object> arguments) {
        for (Map.Entry<String, String> entry : definition.parameters().entrySet()) {
            String param = entry.getKey();
            String type = entry.getValue();
            Object value = arguments.get(param);

            if (value == null) {
                return "缺少参数: " + param;
            }
            if ("string".equals(type) && !(value instanceof String)) {
                return "参数 " + param + " 应为字符串，实际是 " + describe(value);
            }
            if ("number".equals(type) && !(value instanceof Number)) {
                return "参数 " + param + " 应为数字，实际是 " + describe(value);
            }
        }
        return null;
    }

    private static String describe(Object value) {
        if (value instanceof String s) {
            return "字符串 \"" + s + "\"";
        }
        return value.getClass().getSimpleName();
    }
}
