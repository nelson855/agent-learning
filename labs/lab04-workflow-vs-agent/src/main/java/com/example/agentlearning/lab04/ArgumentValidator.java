package com.example.agentlearning.lab04;

import java.util.Map;

/**
 * 工具参数校验：执行前检查"缺没缺、类型对不对"。
 */
public final class ArgumentValidator {

    private ArgumentValidator() {
    }

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
