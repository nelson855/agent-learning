package com.example.agentlearning.stage01;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Agent 循环的确定性测试：全部用 {@link FakeLlmClient} 剧本驱动，不依赖真实模型。
 *
 * <p>覆盖 Prompt 要求的六类场景：
 * 单 Tool / 连续多 Tool / 未知 Tool / 参数错误 / 最大步数停止 / 正常 Final Answer。
 */
class AgentRunnerTest {

    private TaskStore store;
    private ToolRegistry registry;

    @BeforeEach
    void setUp() {
        store = new TaskStore("jdbc:sqlite::memory:");
        registry = TaskTools.createDefault(store);
    }

    @AfterEach
    void tearDown() {
        store.close();
    }

    private AgentRun run(String input, FakeLlmClient fake) {
        return new AgentRunner(fake, registry, new MaxStepsStopCondition(8)).run(input);
    }

    @Test
    void singleToolCall() {
        FakeLlmClient fake = new FakeLlmClient(
                "{\"type\":\"tool_call\",\"tool\":\"createTask\",\"arguments\":{\"title\":\"第一个任务\"},\"decisionSummary\":\"创建任务\"}",
                "{\"type\":\"final\",\"answer\":\"已创建\"}");

        AgentRun run = run("帮我创建一个任务", fake);

        assertTrue(run.finished());
        assertEquals("已创建", run.answer());
        assertEquals(1, run.steps().size());
        assertEquals("createTask", run.steps().get(0).decision().toolCall().name());
        assertEquals(1, store.findAll().size());
        assertEquals("第一个任务", store.findAll().get(0).title());
        assertEquals("OPEN", store.findAll().get(0).status());
    }

    @Test
    void multipleToolCalls() {
        FakeLlmClient fake = new FakeLlmClient(
                "{\"type\":\"tool_call\",\"tool\":\"createTask\",\"arguments\":{\"title\":\"任务A\"},\"decisionSummary\":\"第一个\"}",
                "{\"type\":\"tool_call\",\"tool\":\"createTask\",\"arguments\":{\"title\":\"任务B\"},\"decisionSummary\":\"第二个\"}",
                "{\"type\":\"tool_call\",\"tool\":\"listTasks\",\"arguments\":{},\"decisionSummary\":\"数一下\"}",
                "{\"type\":\"final\",\"answer\":\"一共 2 个任务\"}");

        AgentRun run = run("创建两个任务", fake);

        assertTrue(run.finished());
        assertEquals(3, run.steps().size());
        assertEquals("createTask", run.steps().get(0).decision().toolCall().name());
        assertEquals("createTask", run.steps().get(1).decision().toolCall().name());
        assertEquals("listTasks", run.steps().get(2).decision().toolCall().name());
        assertEquals(2, store.findAll().size());
        assertTrue(run.steps().get(2).toolResult().message().contains("共 2 个任务"));
    }

    @Test
    void unknownToolDoesNotCrash() {
        FakeLlmClient fake = new FakeLlmClient(
                "{\"type\":\"tool_call\",\"tool\":\"deleteAll\",\"arguments\":{}}",
                "{\"type\":\"final\",\"answer\":\"没有这个工具\"}");

        AgentRun run = run("删掉所有", fake);

        assertTrue(run.finished());
        assertEquals(1, run.steps().size());
        assertFalse(run.steps().get(0).toolResult().success());
        assertTrue(run.steps().get(0).toolResult().message().startsWith(ToolRegistry.UNKNOWN_TOOL));
        assertEquals(0, store.findAll().size());
    }

    @Test
    void parameterErrorBlockedByValidator() {
        // 模型漏传了 createTask 必需的 title 参数 → ArgumentValidator 拦截，不进入工具逻辑
        FakeLlmClient fake = new FakeLlmClient(
                "{\"type\":\"tool_call\",\"tool\":\"createTask\",\"arguments\":{},\"decisionSummary\":\"忘了传参数\"}",
                "{\"type\":\"final\",\"answer\":\"参数不对\"}");

        AgentRun run = run("创建一个任务", fake);

        assertTrue(run.finished());
        assertEquals(1, run.steps().size());
        assertFalse(run.steps().get(0).toolResult().success());
        assertTrue(run.steps().get(0).toolResult().message().contains("缺少参数: title"));
        assertEquals(0, store.findAll().size());
    }

    @Test
    void maxStepsStopsInfiniteLoop() {
        // 剧本永远返回同一个 tool_call → 耗尽后重复最后一条 → 死循环，靠 StopCondition 拦停
        FakeLlmClient fake = new FakeLlmClient(
                "{\"type\":\"tool_call\",\"tool\":\"listTasks\",\"arguments\":{}}");

        AgentRun run = new AgentRunner(fake, registry, new MaxStepsStopCondition(3)).run("一直查");

        assertFalse(run.finished());
        assertEquals(MaxStepsStopCondition.MAX_STEPS_EXCEEDED, run.answer());
        assertEquals(3, run.steps().size());
    }

    @Test
    void directFinalAnswer() {
        FakeLlmClient fake = new FakeLlmClient(
                "{\"type\":\"final\",\"answer\":\"你好，我是任务助手。\"}");

        AgentRun run = run("你好", fake);

        assertTrue(run.finished());
        assertEquals("你好，我是任务助手。", run.answer());
        assertEquals(0, run.steps().size());
    }
}
