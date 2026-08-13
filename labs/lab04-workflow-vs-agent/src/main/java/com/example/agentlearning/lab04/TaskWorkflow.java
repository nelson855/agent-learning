package com.example.agentlearning.lab04;

import java.util.ArrayList;
import java.util.List;

/**
 * Version A 的执行器：固定 Workflow。
 *
 * <p>路径<b>完全由代码预定</b>，模型只负责两个产出点（生成标题、生成描述），
 * 顺序固定：generateTitle → generateDescription → 程序校验 → 保存。
 * 全程不调用任何工具，模型调用次数固定为 2。
 */
public final class TaskWorkflow {

    private static final String TITLE_PROMPT = """
            你是任务标题生成器。根据用户的主题生成一个简洁的任务标题（不超过 40 字）。
            只输出一个 JSON，不要输出任何其他文字：{"title":"标题"}""";
    private static final String DESCRIPTION_PROMPT = """
            你是任务描述生成器。根据用户主题和任务标题，生成一段任务描述（不超过 200 字）。
            只输出一个 JSON，不要输出任何其他文字：{"description":"描述"}""";

    private final LlmClient llm;
    private final TaskStore store;

    public TaskWorkflow(LlmClient llm, TaskStore store) {
        this.llm = llm;
        this.store = store;
    }

    /** 按固定路径执行；任何一步失败都返回失败结果（记录执行到哪一步）。 */
    public WorkflowResult run(String topic) {
        List<String> steps = new ArrayList<>();
        try {
            steps.add("generateTitle");
            String title = generateTitle(topic);

            steps.add("generateDescription");
            String description = generateDescription(topic, title);

            steps.add("validate");
            String error = TaskRules.validate(title, description);
            if (error != null) {
                return WorkflowResult.failure(error, steps);
            }

            steps.add("save");
            Task task = TaskRules.save(store, title, description);
            return WorkflowResult.success(task, steps);
        } catch (RuntimeException e) {
            // 模型输出无法解析等 → 失败，不中断整个程序
            return WorkflowResult.failure("执行失败: " + e.getMessage(), steps);
        }
    }

    private String generateTitle(String topic) {
        LlmResponse reply = llm.chat(List.of(Message.system(TITLE_PROMPT), Message.user(topic)));
        return JsonExtract.field(reply.content(), "title");
    }

    private String generateDescription(String topic, String title) {
        LlmResponse reply = llm.chat(List.of(
                Message.system(DESCRIPTION_PROMPT),
                Message.user("主题: " + topic + "\n标题: " + title)));
        return JsonExtract.field(reply.content(), "description");
    }
}
