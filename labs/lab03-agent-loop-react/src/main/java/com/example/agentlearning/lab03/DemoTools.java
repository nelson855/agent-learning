package com.example.agentlearning.lab03;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * 组装本章的教学工具并注册到 {@link ToolRegistry}。
 */
public final class DemoTools {

    private DemoTools() {
    }

    /** 注册 getTask / createTask / calculator，返回填好的注册表。 */
    public static ToolRegistry createDefault(TaskStore store) {
        return new ToolRegistry()
                .register(getTask(store))
                .register(createTask(store))
                .register(calculator());
    }

    /** 按任务编号查询任务；查不到返回"未找到"（查询本身成功）。 */
    private static Tool getTask(TaskStore store) {
        return new Tool(
                new ToolDefinition("getTask", "按任务编号 taskId 查询一个任务", Map.of("taskId", "string")),
                arguments -> {
                    String id = (String) arguments.get("taskId");
                    Optional<Task> found = store.findById(id);
                    if (found.isEmpty()) {
                        return ToolResult.ok("未找到任务: " + id);
                    }
                    Task task = found.get();
                    return ToolResult.ok("任务: id=" + task.id() + ", title=" + task.title()
                            + ", status=" + task.status());
                });
    }

    /** 新建一个任务（status 初始为 pending），id 由程序生成，不由模型决定。 */
    private static Tool createTask(TaskStore store) {
        return new Tool(
                new ToolDefinition("createTask", "创建一个新任务，标题为 title", Map.of("title", "string")),
                arguments -> {
                    String title = (String) arguments.get("title");
                    String id = "t-" + UUID.randomUUID().toString().substring(0, 8);
                    store.insert(new Task(id, title, "pending", Instant.now().toString()));
                    return ToolResult.ok("已创建任务: " + title + " (id=" + id + ")");
                });
    }

    /** 四则运算求值；表达式非法时返回失败结果，而不是抛异常。 */
    private static Tool calculator() {
        return new Tool(
                new ToolDefinition("calculator",
                        "计算四则运算表达式，支持 + - * / 和括号，例如 (1+2)*3",
                        Map.of("expression", "string")),
                arguments -> {
                    String expression = (String) arguments.get("expression");
                    try {
                        return ToolResult.ok("= " + formatNumber(Calculator.evaluate(expression)));
                    } catch (RuntimeException e) {
                        return ToolResult.fail("表达式不合法: " + e.getMessage());
                    }
                });
    }

    /** 整数结果显示为整数（1+2 → 3），小数保留原样。 */
    private static String formatNumber(double value) {
        if (value == Math.rint(value) && !Double.isInfinite(value)) {
            return Long.toString((long) value);
        }
        return Double.toString(value);
    }
}
