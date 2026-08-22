package com.example.agentlearning.lab11;

/**
 * 对照实验：同一个 FakeLlmClient，按调用顺序注入 4 条回复
 * （1 条单 Agent + 3 条 Workers），然后分别运行 Version A / B 并打印对比。
 *
 * <pre>
 * 共享 FakeLlmClient 的脚本顺序：
 *   reply#1 单 Agent 综合输出
 *   reply#2 TaskStatsWorker 输出
 *   reply#3 FailureAnalysisWorker 输出
 *   reply#4 RecommendationWorker 输出
 * </pre>
 */
public final class Comparison {

    private Comparison() {
    }

    /** 构造一个预置 4 条回复的 FakeLlmClient（可观察调用次数）。 */
    public static FakeLlmClient scripted() {
        return new FakeLlmClient(
                // reply#1: 单 Agent
                """
                {"totalTasks":198,"completedTasks":180,"failedTasks":12,
                 "abnormalRatio":0.076,"avgDurationMinutes":8,"degradedTasks":3,
                 "mainFailures":["外部服务超时","数据校验失败"],"impact":"拖慢平均时长、影响交付",
                 "items":["为外部调用增加重试与熔断","提前校验数据质量"]}""",
                // reply#2: TaskStatsWorker
                "{\"totalTasks\":198,\"completedTasks\":180,\"failedTasks\":12,"
                        + "\"abnormalRatio\":0.076,\"avgDurationMinutes\":8,\"degradedTasks\":3}",
                // reply#3: FailureAnalysisWorker
                "{\"mainFailures\":[\"外部服务超时\",\"数据校验失败\"],"
                        + "\"impact\":\"失败导致重跑，拖慢平均时长\"}",
                // reply#4: RecommendationWorker
                "{\"items\":[\"为外部调用增加重试与熔断\",\"提前校验数据质量\"]}");
    }

    public static void run(TaskStats stats) {
        FakeLlmClient llm = scripted();
        SingleAgentReportGenerator single = new SingleAgentReportGenerator(llm);
        MultiAgentReportGenerator multi = new MultiAgentReportGenerator(llm);

        System.out.println("===== Version A: Single Agent =====");
        GenerationOutcome a = single.run(stats);
        print(a);

        System.out.println("\n===== Version B: Multi-Agent (Orchestrator + 3 Workers) =====");
        GenerationOutcome b = multi.run(stats,
                "生成任务系统运行分析");
        print(b);

        System.out.println("\n累计真实模型调用: " + llm.chatCalls()
                + "（应为 1 个 Single + 3 个 Workers = 4）");
        System.out.println("\n===== 对比表 =====");
        System.out.printf("%-10s %-12s %-14s %-8s %-8s%n",
                "方案", "model_calls", "context_chars", "steps", "success");
        System.out.printf("%-10s %-12d %-14d %-8d %-8s%n", "Single", a.modelCalls(), a.contextChars(), 1, a.success());
        System.out.printf("%-10s %-12d %-14d %-8d %-8s%n", "Multi", b.modelCalls(), b.contextChars(), 4, b.success());
        System.out.println("\n观察：Multi 的 model_calls 与 context 更『小』(失败分析/建议 Worker 只收到片段)，"
                + "但 steps 增加，编排复杂度上升。");
    }

    private static void print(GenerationOutcome g) {
        System.out.println("  model_calls  = " + g.modelCalls());
        System.out.println("  context_chars= " + g.contextChars());
        System.out.println("  success      = " + g.success());
        if (g.success()) {
            System.out.println("  stats.total  = " + g.report().stats().totalTasks()
                    + "  failures=" + g.report().failure().mainFailures()
                    + "  items=" + g.report().recommendations().items());
        }
    }
}