package com.example.agentlearning.stage01;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Stage 01 入口：控制台 AI 任务助手（CLI）。
 *
 * <ul>
 *   <li>默认：交互式 Agent —— 每输入一句话，{@link AgentRunner} 多步调用工具直到给出最终回答；</li>
 *   <li>{@code --workflow}：跑固定 {@link TaskWorkflow} 对照实验。</li>
 * </ul>
 *
 * <p>未配置真实 LLM（仓库根目录 .env 缺少 LLM_BASE_URL 等）时自动退化为
 * {@link FakeLlmClient} 剧本演示，任何环境都能观察 Agent Loop 结构。
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) throws IOException {
        TaskStore store = createTaskStore();
        if (args.length > 0 && "--workflow".equals(args[0])) {
            runWorkflowDemo(store);
        } else {
            runAgentCli(store);
        }
        store.close();
    }

    /** 交互式 Agent CLI：每个输入开一个全新的 Agent 会话（无跨轮记忆，是刻意简化）。 */
    private static void runAgentCli(TaskStore store) throws IOException {
        System.out.println("=== stage01-minimal-agent: 控制台 AI 任务助手 ===");
        System.out.println("输入一句话回车，Agent 会多步调用工具完成它；输入 /exit 退出。");
        System.out.println("示例：创建两个 Agent 学习任务，然后告诉我现在一共有多少个 OPEN 任务。");
        System.out.println();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(System.in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String input = line.trim();
                if (input.isEmpty()) {
                    continue;
                }
                if (input.equalsIgnoreCase("/exit")) {
                    break;
                }
                System.out.println(">>> 用户: " + input);
                AgentRunner runner = new AgentRunner(
                        isConfigured() ? OpenAiCompatibleLlmClient.fromConfig() : offlineAgentLlm(),
                        TaskTools.createDefault(store),
                        new MaxStepsStopCondition(8));
                runner.run(input);
                System.out.println();
            }
        }
    }

    /** 对照实验：固定 Workflow 完成"相似"任务，打印对比指标。 */
    private static void runWorkflowDemo(TaskStore store) {
        System.out.println("=== stage01-minimal-agent: 对照实验（固定 Workflow） ===");
        System.out.println();

        FakeLlmClient llm = offlineWorkflowLlm();
        TaskWorkflow workflow = new TaskWorkflow(llm, store);
        WorkflowResult result = workflow.run();

        System.out.println("执行步骤       = " + String.join(" → ", result.steps()));
        System.out.println("model_call_count = " + llm.requestCount());
        System.out.println("tool_call_count  = 0");
        System.out.println("step_count       = " + result.steps().size());
        System.out.println("open_count       = " + result.openCount());
        System.out.println("success          = " + result.success());
        if (!result.success()) {
            System.out.println("failureReason    = " + result.failureReason());
        }
        System.out.println();
        System.out.println("对照要点：Workflow 的路径由程序决定（模型只生成 1 次标题）；");
        System.out.println("          Agent 的下一步由模型基于状态决定（见交互模式）。");
    }

    private static TaskStore createTaskStore() throws IOException {
        Path dataDir = Path.of("data");
        Files.createDirectories(dataDir);
        Path dbFile = dataDir.resolve("stage01.db");
        return new TaskStore("jdbc:sqlite:" + dbFile);
    }

    private static boolean isConfigured() {
        return EnvFile.get("LLM_BASE_URL") != null && !EnvFile.get("LLM_BASE_URL").isBlank()
                && EnvFile.get("LLM_API_KEY") != null && !EnvFile.get("LLM_API_KEY").isBlank()
                && EnvFile.get("LLM_MODEL") != null && !EnvFile.get("LLM_MODEL").isBlank();
    }

    /** 离线演示：Workflow 版剧本（模型只生成一次标题）。 */
    private static FakeLlmClient offlineWorkflowLlm() {
        return new FakeLlmClient("{\"title\":\"Agent 学习任务\"}");
    }

    /** 离线演示：Agent 版剧本——验收任务"创建两个任务并统计 OPEN 数量"。 */
    private static FakeLlmClient offlineAgentLlm() {
        return new FakeLlmClient(
                "{\"type\":\"tool_call\",\"tool\":\"createTask\",\"arguments\":{\"title\":\"Agent 学习任务一\",\"description\":\"了解 Agent 原理\"},\"decisionSummary\":\"创建第一个任务\"}",
                "{\"type\":\"tool_call\",\"tool\":\"createTask\",\"arguments\":{\"title\":\"Agent 学习任务二\",\"description\":\"动手实现工具\"},\"decisionSummary\":\"创建第二个任务\"}",
                "{\"type\":\"tool_call\",\"tool\":\"listTasks\",\"arguments\":{},\"decisionSummary\":\"查看当前任务，统计 OPEN 数量\"}",
                "{\"type\":\"final\",\"answer\":\"目前一共有 2 个 OPEN 任务。\"}");
    }
}
