package com.example.agentlearning.lab10;

/**
 * Lab10 入口：观察 "从 SQLite 任务统计生成 JSON 周报" 的 Generator-Evaluator Loop。
 *
 * <p>{@code --demo}：离线演示（FakeLlmClient，不访问网络），两个场景：
 * <ol>
 *   <li>Generator 漏掉 {@code recommendations} → ProgramValidator 结构拒绝，
 *       <b>不调用 Evaluator</b>；</li>
 *   <li>结构合法但语义差 → 进入 Evaluator → 反馈回灌 → Generator 重试 → 通过。</li>
 * </ol>
 *
 * <p>不带参数：真实 LLM 演示（需配置 {@code LLM_BASE_URL} / {@code LLM_API_KEY} /
 * {@code LLM_MODEL}，可用仓库根目录 {@code .env}）。
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length > 0 && "--demo".equals(args[0])) {
            runDemo();
        } else {
            runReal();
        }
    }

    // ------------------------------------------------------------------
    // 离线演示
    // ------------------------------------------------------------------

    private static void runDemo() {
        TaskStats stats = demoStats();
        System.out.println("==== Lab10 演示：Generator-Evaluator Loop（离线 Fake）====\n");
        System.out.println("任务统计: total=" + stats.totalTasks()
                + " completed=" + stats.completedTasks()
                + " failed=" + stats.failedTasks()
                + " abnormal=" + stats.abnormalRatio() + "\n");

        demoScenarioA(stats);
        System.out.println();
        demoScenarioB(stats);
    }

    /** 场景 A：Generator 第一次生成结构错误（漏 recommendations）→ 程序拒绝，不调 Evaluator。 */
    private static void demoScenarioA(TaskStats stats) {
        System.out.println("---- 场景 A：结构错误，应在 ProgramValidator 被拦下 ----");
        String missingRecommendations = """
                {"week":"2026-W33","totalTasks":210,"completedTasks":182,
                 "failedTasks":12,"abnormalRatio":0.071,"avgDurationMinutes":47,
                 "summary":"本周共 210 个任务，182 个成功，12 个失败。"}""";

        FakeLlmClient fake = new FakeLlmClient().enqueue(missingRecommendations);
        GeneratorEvaluatorLoop loop = new GeneratorEvaluatorLoop(fake);
        GeneratorEvaluatorLoop.LoopResult result = loop.run(stats);

        result.log().forEach(System.out::println);
        System.out.println("=> Evaluator 调用次数 = " + result.evaluatorCalls()
                + "（结构错误，0 次才正确）");
        System.out.println("=> 产出合格周报 = " + result.accepted());
    }

    /** 场景 B：结构过但语义差 → 进入 Evaluator → 反馈回灌 → 重试后通过。 */
    private static void demoScenarioB(TaskStats stats) {
        System.out.println("---- 场景 B：语义差，应在 Evaluator 被拦下并反馈重试 ----");
        String poorSemantics = """
                {"week":"2026-W33","totalTasks":210,"completedTasks":182,
                 "failedTasks":12,"abnormalRatio":0.071,"avgDurationMinutes":47,
                 "summary":"本周数据已经统计好了。",
                 "recommendations":["改进系统"]}""";
        String feedbackReject =
                "{\"pass\":false,\"score\":2,\"issues\":[\"summary 未解释失败/异常原因\",\"建议不够可执行\"]}";
        String goodReport = """
                {"week":"2026-W33","totalTasks":210,"completedTasks":182,
                 "failedTasks":12,"abnormalRatio":0.071,"avgDurationMinutes":47,
                 "summary":"本周219个任务完成182个，12个失败，异常率7.1%，集中在支付模块第三方接口超时。",
                 "recommendations":["为支付模块第三方接口增加超时重试并设置熔断",
                                    "将超过5分钟的大任务拆分为可重试的子任务"]}""";
        String feedbackPass =
                "{\"pass\":true,\"score\":4,\"issues\":[]}";

        FakeLlmClient fake = new FakeLlmClient()
                .enqueue(poorSemantics, feedbackReject, goodReport, feedbackPass);
        GeneratorEvaluatorLoop loop = new GeneratorEvaluatorLoop(fake);
        GeneratorEvaluatorLoop.LoopResult result = loop.run(stats);

        result.log().forEach(System.out::println);
        System.out.println("=> Evaluator 调用次数 = " + result.evaluatorCalls());
        System.out.println("=> 产出合格周报 = " + result.accepted());
    }

    // ------------------------------------------------------------------
    // 真实 LLM
    // ------------------------------------------------------------------

    private static void runReal() throws Exception {
        TaskStats stats = demoStats();
        LlmClient llm = OpenAiCompatibleLlmClient.fromConfig(); // 缺配置时快速失败
        GeneratorEvaluatorLoop loop = new GeneratorEvaluatorLoop(llm);
        GeneratorEvaluatorLoop.LoopResult result = loop.run(stats);
        result.log().forEach(System.out::println);
        if (result.accepted()) {
            System.out.println("\n最终周报:\n" + new com.fasterxml.jackson.databind.ObjectMapper()
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(result.report()));
        }
    }

    /**
     * 一组可复现的任务统计（相当于已由 {@link TaskRepository#aggregateStats()} 从 SQLite 聚合得出）。
     *
     * <p>线上数据源为 SQLite：建房→{@code TaskRepository.seedDemoData()}→{@code aggregateStats()}，
     * 产出与这里一致的结构。此处用固定值便于离线演示与测试稳定复现，不受数据库环境影响。
     */
    private static TaskStats demoStats() {
        return new TaskStats(210, 182, 12, 0.071, 47, 3);
    }
}