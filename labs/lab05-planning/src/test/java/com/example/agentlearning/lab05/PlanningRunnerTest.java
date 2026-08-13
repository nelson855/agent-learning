package com.example.agentlearning.lab05;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Plan-and-Replan 的确定性测试：Planner / Replanner 用 {@link ScriptedLlmClient} 剧本，
 * 工具用 {@link FakeLearningStepTool}，不依赖真实模型。
 *
 * <p>覆盖 Prompt 要求的三类断言：
 * 正常步骤不触发 Replan / 失败会触发一次 / max replans 有上限。
 */
class PlanningRunnerTest {

    private static final String INITIAL_PLAN = """
            {"goal":"完成 Agent 学习第一阶段","steps":[
              {"id":"S1","description":"阅读 Agent 基础概念"},
              {"id":"S2","description":"动手实现一个 ReAct 循环"},
              {"id":"S3","description":"完成学习总结与验收"}]}""";

    private static final String REPLAN_PLAN = """
            {"goal":"完成 Agent 学习第一阶段","steps":[
              {"id":"S1","description":"阅读 Agent 基础概念"},
              {"id":"S2","description":"先补齐前置依赖，再动手实现 ReAct 循环"},
              {"id":"S3","description":"完成学习总结与验收"}]}""";

    private PlanRunResult run(int maxReplans, FakeLearningStepTool tool, ScriptedLlmClient script) {
        CountingLlmClient llm = new CountingLlmClient(script);
        PlanningRunner runner = new PlanningRunner(
                new Planner(llm), new Replanner(llm), new Executor(tool), maxReplans);
        PlanRunResult result = runner.run("完成 Agent 学习第一阶段");
        return result;
    }

    @Test
    void normalStepsDoNotReplan() {
        // S2 配置为"永不失败" → 全部步骤一次成功，不触发 Replan
        FakeLearningStepTool tool = new FakeLearningStepTool().failOnFirst("S2", 0);
        ScriptedLlmClient script = new ScriptedLlmClient(INITIAL_PLAN);
        CountingLlmClient llm = new CountingLlmClient(script);
        PlanningRunner runner = new PlanningRunner(
                new Planner(llm), new Replanner(llm), new Executor(tool), 3);

        PlanRunResult result = runner.run("完成 Agent 学习第一阶段");

        assertTrue(result.allDone());
        assertEquals(0, result.replans());
        assertEquals(1, llm.count()); // 只有 Planner 调用 1 次，Replanner 0 次
        assertEquals("SUCCESS", result.summary().split("（")[0]);
    }

    @Test
    void failureTriggersOneReplan() {
        // 默认 Fake：S2 第一次失败 → 触发一次 Replan → 重试成功 → 全部 DONE
        FakeLearningStepTool tool = new FakeLearningStepTool();
        ScriptedLlmClient script = new ScriptedLlmClient(INITIAL_PLAN, REPLAN_PLAN);
        CountingLlmClient llm = new CountingLlmClient(script);
        PlanningRunner runner = new PlanningRunner(
                new Planner(llm), new Replanner(llm), new Executor(tool), 3);

        PlanRunResult result = runner.run("完成 Agent 学习第一阶段");

        assertTrue(result.allDone());
        assertEquals(1, result.replans());
        assertEquals(2, llm.count()); // Planner 1 次 + Replanner 1 次
    }

    @Test
    void maxReplansHasLimit() {
        // S2 前 100 次都失败 → 每次 Replan 后仍失败 → 到达上限后剩余步骤 SKIPPED
        FakeLearningStepTool tool = new FakeLearningStepTool().failOnFirst("S2", 100);
        ScriptedLlmClient script = new ScriptedLlmClient(INITIAL_PLAN, REPLAN_PLAN);
        CountingLlmClient llm = new CountingLlmClient(script);
        PlanningRunner runner = new PlanningRunner(
                new Planner(llm), new Replanner(llm), new Executor(tool), 3);

        PlanRunResult result = runner.run("完成 Agent 学习第一阶段");

        assertFalse(result.allDone());
        assertEquals(3, result.replans()); // 达到上限 3
        assertEquals(4, llm.count());      // Planner 1 次 + Replanner 3 次
        // 剩余 PENDING 步骤被标记 SKIPPED
        assertTrue(result.plan().steps().stream()
                .anyMatch(step -> step.status() == PlanStepStatus.SKIPPED));
        assertEquals("INCOMPLETE", result.summary().split("（")[0]);
    }

    @Test
    void planParserParsesStructuredJson() {
        Plan plan = PlanParser.parse(INITIAL_PLAN);

        assertEquals("完成 Agent 学习第一阶段", plan.goal());
        assertEquals(3, plan.steps().size());
        assertEquals("S1", plan.steps().get(0).id());
        assertEquals("动手实现一个 ReAct 循环", plan.steps().get(1).description());
        // 解析出来的步骤全部是 PENDING
        assertTrue(plan.steps().stream()
                .allMatch(step -> step.status() == PlanStepStatus.PENDING));
    }

    @Test
    void replannerPassesFailureContext() {
        // 验证 Replanner 把"原目标 + 计划状态 + 失败原因"传给模型（剧本能读到上下文）
        Plan current = PlanParser.parse(INITIAL_PLAN);
        PlanStep failed = current.steps().get(1);
        failed.setStatus(PlanStepStatus.FAILED);
        failed.setFailureReason(FakeLearningStepTool.DEPENDENCY_MISSING);

        // 用 FunctionLlmClient 检查传入的消息内容
        ScriptedLlmClient script = new ScriptedLlmClient(REPLAN_PLAN);
        Replanner replanner = new Replanner(script);
        Plan newPlan = replanner.replan(current, failed);

        assertEquals("完成 Agent 学习第一阶段", newPlan.goal());
        assertEquals(3, newPlan.steps().size());
    }
}
