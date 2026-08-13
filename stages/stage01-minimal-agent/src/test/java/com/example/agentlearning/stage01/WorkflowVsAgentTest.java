package com.example.agentlearning.stage01;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * 对照实验测试：固定 Workflow（{@link TaskWorkflow}）vs 自主 Agent（{@link AgentRunner}）。
 *
 * <p>同一个"创建任务并统计 OPEN 数量"类需求：
 * Workflow 路径由程序固定、模型只调 1 次、不调工具；
 * Agent 由模型一步步选择工具完成。
 */
class WorkflowVsAgentTest {

    private TaskStore store;

    @BeforeEach
    void setUp() {
        store = new TaskStore("jdbc:sqlite::memory:");
    }

    @AfterEach
    void tearDown() {
        store.close();
    }

    @Test
    void workflowRunsFixedPathWithoutTools() {
        FakeLlmClient fake = new FakeLlmClient("{\"title\":\"Agent 学习任务\"}");

        WorkflowResult result = new TaskWorkflow(fake, store).run();

        assertTrue(result.success());
        // 路径固定：模型生成 1 次标题 → 程序创建 2 个任务 → 程序统计
        assertEquals(List.of("generateTitle", "createTask x2", "countOpen"), result.steps());
        assertEquals(1, fake.requestCount());    // 模型只调 1 次
        assertEquals(2, store.findAll().size()); // 程序创建 2 个任务
        assertEquals(2, result.openCount());     // 全部 OPEN
    }

    @Test
    void agentCompletesAcceptanceTaskByChoosingTools() {
        // 验收任务：创建两个 Agent 学习任务，然后告诉我现在一共有多少个 OPEN 任务
        FakeLlmClient fake = new FakeLlmClient(
                "{\"type\":\"tool_call\",\"tool\":\"createTask\",\"arguments\":{\"title\":\"任务一\"},\"decisionSummary\":\"创建第一个\"}",
                "{\"type\":\"tool_call\",\"tool\":\"createTask\",\"arguments\":{\"title\":\"任务二\"},\"decisionSummary\":\"创建第二个\"}",
                "{\"type\":\"tool_call\",\"tool\":\"listTasks\",\"arguments\":{},\"decisionSummary\":\"统计 OPEN\"}",
                "{\"type\":\"final\",\"answer\":\"目前一共有 2 个 OPEN 任务。\"}");

        AgentRunner runner = new AgentRunner(fake, TaskTools.createDefault(store), new MaxStepsStopCondition(8));
        AgentRun run = runner.run("创建两个 Agent 学习任务，然后告诉我现在一共有多少个 OPEN 任务");

        assertTrue(run.finished());
        assertEquals("目前一共有 2 个 OPEN 任务。", run.answer());
        assertEquals(3, run.steps().size()); // 3 次工具调用
        assertEquals(2, store.findAll().stream()
                .filter(t -> "OPEN".equals(t.status())).count());
    }
}
