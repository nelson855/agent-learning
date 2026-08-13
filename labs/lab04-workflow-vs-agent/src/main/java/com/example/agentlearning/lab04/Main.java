package com.example.agentlearning.lab04;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * lab04 入口：同一个需求分别用固定 Workflow（Version A）和自主 Agent（Version B）执行一遍，
 * 打印两版的 model_call_count / tool_call_count / step_count / success 供对照。
 *
 * <p>未配置真实 LLM（仓库根目录 .env 缺少 LLM_BASE_URL 等）时自动退化为固定剧本的离线演示，
 * 保证任何环境都能跑通并观察到对比结构。
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) throws IOException {
        TaskStore store = createTaskStore();
        System.out.println("=== lab04-workflow-vs-agent: Workflow vs Agent 对照 ===");
        System.out.println("输入一个任务主题回车；输入 /exit 退出。");
        System.out.println("同一主题会用固定 Workflow 和自主 Agent 各执行一遍并对比。");
        System.out.println();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(System.in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String topic = line.trim();
                if (topic.isEmpty()) {
                    continue;
                }
                if (topic.equalsIgnoreCase("/exit")) {
                    break;
                }
                runComparison(topic, store);
            }
        }
        store.close();
    }

    private static TaskStore createTaskStore() throws IOException {
        Path dataDir = Path.of("data");
        Files.createDirectories(dataDir);
        Path dbFile = dataDir.resolve("lab04-workflow-vs-agent.db");
        return new TaskStore("jdbc:sqlite:" + dbFile);
    }

    static void runComparison(String topic, TaskStore store) {
        boolean real = isConfigured();
        System.out.println("===== 主题: " + topic + (real ? "（真实模型）" : "（离线剧本）") + " =====");

        System.out.println();
        System.out.println("--- Version A: Workflow（路径由程序固定） ---");
        CountingLlmClient wfLlm = new CountingLlmClient(real
                ? OpenAiCompatibleLlmClient.fromConfig()
                : offlineWorkflowLlm());
        WorkflowResult wf = new TaskWorkflow(wfLlm, store).run(topic);
        printWorkflow(wf, wfLlm);

        System.out.println();
        System.out.println("--- Version B: Agent（路径由模型决定） ---");
        CountingLlmClient agentLlm = new CountingLlmClient(real
                ? OpenAiCompatibleLlmClient.fromConfig()
                : offlineAgentLlm());
        ToolRegistry registry = AgentTools.createDefault(agentLlm, store);
        AgentRun run = new AgentRunner(agentLlm, registry).run(topic);
        printAgent(run, agentLlm);
    }

    private static void printWorkflow(WorkflowResult wf, CountingLlmClient llm) {
        System.out.println("执行步骤        = " + String.join(" → ", wf.steps()));
        System.out.println("model_call_count = " + llm.count());
        System.out.println("tool_call_count  = 0");
        System.out.println("step_count       = " + wf.steps().size());
        System.out.println("success          = " + wf.success());
        if (wf.success()) {
            System.out.println("task_id          = " + wf.task().id());
        } else {
            System.out.println("failureReason    = " + wf.failureReason());
        }
    }

    private static void printAgent(AgentRun run, CountingLlmClient llm) {
        System.out.println("FINAL            = " + run.answer());
        System.out.println("model_call_count = " + llm.count());
        System.out.println("tool_call_count  = " + run.steps().size());
        System.out.println("step_count       = " + run.steps().size());
        System.out.println("success          = " + run.finished());
    }

    private static boolean isConfigured() {
        return EnvFile.get("LLM_BASE_URL") != null && !EnvFile.get("LLM_BASE_URL").isBlank()
                && EnvFile.get("LLM_API_KEY") != null && !EnvFile.get("LLM_API_KEY").isBlank()
                && EnvFile.get("LLM_MODEL") != null && !EnvFile.get("LLM_MODEL").isBlank();
    }

    /** 离线演示：Workflow 版剧本（生成标题、生成描述）。 */
    private static ScriptedLlmClient offlineWorkflowLlm() {
        return new ScriptedLlmClient(
                "{\"title\":\"离线标题：Workflow 固定路径\"}",
                "{\"description\":\"离线描述：路径由程序预定，模型只负责两个产出点。\"}");
    }

    /** 离线演示：Agent 版剧本（决策 → 工具内生成 → 决策 → 工具内生成 → 决策保存 → final）。 */
    private static ScriptedLlmClient offlineAgentLlm() {
        return new ScriptedLlmClient(
                "{\"type\":\"tool_call\",\"tool\":\"generateTitle\",\"arguments\":{\"topic\":\"离线演示\"},"
                        + "\"decisionSummary\":\"先生成标题\"}",
                "{\"title\":\"离线标题：Agent 自主决策\"}",
                "{\"type\":\"tool_call\",\"tool\":\"generateDescription\","
                        + "\"arguments\":{\"topic\":\"离线演示\",\"title\":\"离线标题：Agent 自主决策\"},"
                        + "\"decisionSummary\":\"再生成描述\"}",
                "{\"description\":\"离线描述：路径由模型根据观察逐步决定。\"}",
                "{\"type\":\"tool_call\",\"tool\":\"saveTask\","
                        + "\"arguments\":{\"title\":\"离线标题：Agent 自主决策\","
                        + "\"description\":\"离线描述：路径由模型根据观察逐步决定。\"},"
                        + "\"decisionSummary\":\"校验并保存\"}",
                "{\"type\":\"final\",\"answer\":\"任务已保存（离线演示）。\"}");
    }
}
