package com.example.agentlearning.lab04;

import java.util.List;
import java.util.Map;

/**
 * Agent 版的任务工具：让模型自己决定"先做什么、再做什么、最后怎么收尾"。
 *
 * <p>注意两个生成工具内部<b>还会再调一次模型</b>来产出内容——工具是"能力封装"：
 * 模型决定调用 generateTitle，工具内部用模型把它变成标题文本。
 * 这样 Agent 版的模型调用次数天然比固定 Workflow 更多，也更不可预测。
 */
public final class AgentTools {

    private static final String TITLE_PROMPT = """
            你是任务标题生成器。根据用户的主题生成一个简洁的任务标题（不超过 40 字）。
            只输出一个 JSON，不要输出任何其他文字：{"title":"标题"}""";
    private static final String DESCRIPTION_PROMPT = """
            你是任务描述生成器。根据用户主题和任务标题，生成一段任务描述（不超过 200 字）。
            只输出一个 JSON，不要输出任何其他文字：{"description":"描述"}""";

    private AgentTools() {
    }

    public static ToolRegistry createDefault(LlmClient llm, TaskStore store) {
        return new ToolRegistry()
                .register(generateTitle(llm))
                .register(generateDescription(llm))
                .register(saveTask(store));
    }

    private static Tool generateTitle(LlmClient llm) {
        return new Tool(
                new ToolDefinition("generateTitle", "根据任务主题生成一个简洁的任务标题", Map.of("topic", "string")),
                args -> {
                    String topic = (String) args.get("topic");
                    try {
                        LlmResponse reply = llm.chat(List.of(Message.system(TITLE_PROMPT), Message.user(topic)));
                        String title = JsonExtract.field(reply.content(), "title");
                        return ToolResult.ok("生成的标题: " + title);
                    } catch (RuntimeException e) {
                        return ToolResult.fail("生成标题失败: " + e.getMessage());
                    }
                });
    }

    private static Tool generateDescription(LlmClient llm) {
        return new Tool(
                new ToolDefinition("generateDescription",
                        "根据任务主题和标题生成任务描述",
                        Map.of("topic", "string", "title", "string")),
                args -> {
                    String topic = (String) args.get("topic");
                    String title = (String) args.get("title");
                    try {
                        LlmResponse reply = llm.chat(List.of(
                                Message.system(DESCRIPTION_PROMPT),
                                Message.user("主题: " + topic + "\n标题: " + title)));
                        String description = JsonExtract.field(reply.content(), "description");
                        return ToolResult.ok("生成的描述: " + description);
                    } catch (RuntimeException e) {
                        return ToolResult.fail("生成描述失败: " + e.getMessage());
                    }
                });
    }

    /** 保存前仍走确定性的 {@link TaskRules} 校验——确定性留给程序，Agent 只是发起调用。 */
    private static Tool saveTask(TaskStore store) {
        return new Tool(
                new ToolDefinition("saveTask",
                        "校验并保存一个任务（title 标题 + description 描述）",
                        Map.of("title", "string", "description", "string")),
                args -> {
                    String title = (String) args.get("title");
                    String description = (String) args.get("description");
                    String error = TaskRules.validate(title, description);
                    if (error != null) {
                        return ToolResult.fail("校验失败: " + error);
                    }
                    Task task = TaskRules.save(store, title, description);
                    return ToolResult.ok("已保存任务: " + title
                            + " (id=" + task.id() + ", status=" + task.status() + ")");
                });
    }
}
