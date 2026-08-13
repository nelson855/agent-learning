package com.example.agentlearning.stage01;

import java.util.ArrayList;
import java.util.List;

/**
 * 对照实验 Version A：固定 Workflow（路径由程序决定）。
 *
 * <p>完成与验收任务"相似"的需求（创建任务并统计 OPEN 数量），但路径<b>完全由代码预定</b>：
 * <pre>
 *   generateTitle（模型只调用 1 次）→ 程序创建两个任务 → 程序统计 OPEN 数量
 * </pre>
 *
 * <p>与 {@link AgentRunner} 对比：这里模型不参与"下一步做什么"的决策，
 * 只负责一个固定的内容产出点；工具一个都不调。
 */
public final class TaskWorkflow {

    private static final String TITLE_PROMPT = """
            你是任务标题生成器。为"Agent 学习"这个主题生成一个简洁的任务标题（不超过 20 字）。
            只输出一个 JSON，不要输出任何其他文字：{"title":"标题"}""";

    private final LlmClient llm;
    private final TaskStore store;

    public TaskWorkflow(LlmClient llm, TaskStore store) {
        this.llm = llm;
        this.store = store;
    }

    /** 固定路径执行；任何一步失败都返回失败结果（记录执行到哪一步）。 */
    public WorkflowResult run() {
        List<String> steps = new ArrayList<>();
        try {
            steps.add("generateTitle");
            String title = generateTitle();

            steps.add("createTask x2");
            createTask(title);
            createTask(title);

            steps.add("countOpen");
            int open = (int) store.findAll().stream()
                    .filter(t -> TaskTools.STATUS_OPEN.equals(t.status()))
                    .count();

            return WorkflowResult.success(open, steps);
        } catch (RuntimeException e) {
            return WorkflowResult.failure("执行失败: " + e.getMessage(), steps);
        }
    }

    private String generateTitle() {
        LlmResponse reply = llm.chat(List.of(Message.system(TITLE_PROMPT)));
        return JsonExtract.field(reply.content(), "title");
    }

    private void createTask(String title) {
        store.insert(new Task(
                "t-" + java.util.UUID.randomUUID().toString().substring(0, 8),
                title,
                "由固定 Workflow 创建",
                TaskTools.STATUS_OPEN,
                java.time.Instant.now().toString()));
    }
}
