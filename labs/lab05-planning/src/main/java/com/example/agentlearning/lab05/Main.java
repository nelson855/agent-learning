package com.example.agentlearning.lab05;

/**
 * lab05-planning 入口：Plan-and-Replan 演示。
 *
 * <p>目标固定为「完成 Agent 学习第一阶段」。Fake 工具的第 2 步第一次执行固定失败
 * （{@code DEPENDENCY_MISSING}），触发一次 Replan，然后继续完成。
 *
 * <ul>
 *   <li>默认：离线剧本演示（Planner/Replanner 用 {@link ScriptedLlmClient}），结果确定；</li>
 *   <li>{@code --real}：Planner/Replanner 走真实模型（.env 配置），体验真实的规划能力。</li>
 * </ul>
 */
public final class Main {

    private static final String GOAL = "完成 Agent 学习第一阶段";

    private Main() {
    }

    public static void main(String[] args) {
        boolean real = args.length > 0 && "--real".equals(args[0]);
        LlmClient llm = real ? OpenAiCompatibleLlmClient.fromConfig() : offlineScripted();

        // 默认 Fake 工具：步骤 S2 第一次执行固定失败
        FakeLearningStepTool tool = new FakeLearningStepTool();

        PlanningRunner runner = new PlanningRunner(
                new Planner(llm),
                new Replanner(llm),
                new Executor(tool),
                3);

        System.out.println("=== lab05-planning: Planning + Plan-and-Replan ==="
                + (real ? "（真实模型）" : "（离线剧本）"));
        System.out.println();
        runner.run(GOAL);
    }

    /** 离线剧本：第 1 条给 Planner，第 2 条给 Replanner（S2 保留、补充前置条件）。 */
    private static ScriptedLlmClient offlineScripted() {
        return new ScriptedLlmClient(
                """
                {"goal":"完成 Agent 学习第一阶段","steps":[
                  {"id":"S1","description":"阅读 Agent 基础概念"},
                  {"id":"S2","description":"动手实现一个 ReAct 循环"},
                  {"id":"S3","description":"完成学习总结与验收"}]}""",
                """
                {"goal":"完成 Agent 学习第一阶段","steps":[
                  {"id":"S1","description":"阅读 Agent 基础概念"},
                  {"id":"S2","description":"先补齐前置依赖，再动手实现 ReAct 循环"},
                  {"id":"S3","description":"完成学习总结与验收"}]}""");
    }
}
