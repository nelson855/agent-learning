package com.example.agentlearning.lab04;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * 同一需求的两个版本对照测试：
 * <ul>
 *   <li>Version A 固定 Workflow（{@link TaskWorkflow}）——路径由程序决定</li>
 *   <li>Version B Agent（{@link AgentRunner} + {@link AgentTools}）——路径由模型决定</li>
 * </ul>
 *
 * <p>用 {@link CountingLlmClient} 统一统计 model_call_count，两版可比。
 * 不依赖真实在线模型。
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
    void workflowRunsFixedPathAndSaves() {
        ScriptedLlmClient script = new ScriptedLlmClient(
                "{\"title\":\"学习 Workflow\"}",
                "{\"description\":\"用固定路径完成生成\"}");
        CountingLlmClient llm = new CountingLlmClient(script);

        WorkflowResult result = new TaskWorkflow(llm, store).run("学习 Workflow");

        assertTrue(result.success());
        assertNull(result.failureReason());
        // 路径固定：generateTitle → generateDescription → validate → save
        assertEquals(List.of("generateTitle", "generateDescription", "validate", "save"), result.steps());
        assertEquals("学习 Workflow", result.task().title());
        assertEquals("用固定路径完成生成", result.task().description());
        assertEquals("pending", result.task().status());
        // 模型调用次数固定 = 2，0 次工具调用
        assertEquals(2, llm.count());
        assertEquals(1, store.findAll().size());
    }

    @Test
    void workflowStopsAtValidationFailure() {
        ScriptedLlmClient script = new ScriptedLlmClient(
                "{\"title\":\"\"}",
                "{\"description\":\"描述\"}");
        CountingLlmClient llm = new CountingLlmClient(script);

        WorkflowResult result = new TaskWorkflow(llm, store).run("x");

        assertFalse(result.success());
        assertTrue(result.failureReason().contains("标题不能为空"));
        // 走到 validate 就停下，不会 save
        assertEquals(List.of("generateTitle", "generateDescription", "validate"), result.steps());
        assertEquals(0, store.findAll().size());
    }

    @Test
    void agentChoosesToolsAndSaves() {
        // 剧本：决策 → 工具内生成标题 → 决策 → 工具内生成描述 → 决策保存 → final
        ScriptedLlmClient script = new ScriptedLlmClient(
                "{\"type\":\"tool_call\",\"tool\":\"generateTitle\",\"arguments\":{\"topic\":\"学习 Agent\"},\"decisionSummary\":\"先生成标题\"}",
                "{\"title\":\"Agent 标题\"}",
                "{\"type\":\"tool_call\",\"tool\":\"generateDescription\",\"arguments\":{\"topic\":\"学习 Agent\",\"title\":\"Agent 标题\"},\"decisionSummary\":\"再生成描述\"}",
                "{\"description\":\"Agent 描述\"}",
                "{\"type\":\"tool_call\",\"tool\":\"saveTask\",\"arguments\":{\"title\":\"Agent 标题\",\"description\":\"Agent 描述\"},\"decisionSummary\":\"校验并保存\"}",
                "{\"type\":\"final\",\"answer\":\"任务已保存\"}");
        CountingLlmClient llm = new CountingLlmClient(script);
        ToolRegistry registry = AgentTools.createDefault(llm, store);

        AgentRun run = new AgentRunner(llm, registry).run("学习 Agent");

        assertTrue(run.finished());
        assertEquals("任务已保存", run.answer());
        // 3 次工具调用
        assertEquals(3, run.steps().size());
        assertEquals("generateTitle", run.steps().get(0).decision().toolCall().name());
        assertEquals("generateDescription", run.steps().get(1).decision().toolCall().name());
        assertEquals("saveTask", run.steps().get(2).decision().toolCall().name());
        // 4 次决策调用 + 2 次工具内部生成 = 6
        assertEquals(6, llm.count());
        assertEquals(1, store.findAll().size());
        assertEquals("Agent 标题", store.findAll().get(0).title());
    }

    @Test
    void agentSurvivesUnknownTool() {
        // 模型叫了不存在的工具 → 失败作为 Observation 交回，不崩溃、不静默
        ScriptedLlmClient script = new ScriptedLlmClient(
                "{\"type\":\"tool_call\",\"tool\":\"deleteAll\",\"arguments\":{}}",
                "{\"type\":\"final\",\"answer\":\"工具不存在\"}");
        CountingLlmClient llm = new CountingLlmClient(script);
        ToolRegistry registry = AgentTools.createDefault(llm, store);

        AgentRun run = new AgentRunner(llm, registry).run("x");

        assertTrue(run.finished());
        assertEquals(1, run.steps().size());
        assertFalse(run.steps().get(0).toolResult().success());
        assertTrue(run.steps().get(0).toolResult().message().startsWith(ToolRegistry.UNKNOWN_TOOL));
        assertEquals(0, store.findAll().size());
    }

    @Test
    void contrastWorkflowNeverCallsTools() {
        // 对照：同一剧本下 Workflow 版从不调用工具、模型调用次数固定
        ScriptedLlmClient script = new ScriptedLlmClient(
                "{\"title\":\"T\"}",
                "{\"description\":\"D\"}");
        CountingLlmClient llm = new CountingLlmClient(script);

        new TaskWorkflow(llm, store).run("x");

        assertEquals(2, llm.count());
        assertEquals(1, store.findAll().size());
    }
}
