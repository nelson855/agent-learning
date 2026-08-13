package com.example.agentlearning.lab03;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * lab03 验收测试：全部使用离线 LLM 客户端（剧本/函数式），不依赖真实网络。
 *
 * <p>覆盖：剧本一步工具→final、死循环触发 maxSteps、maxSteps 可配置、
 * Observation 进入下一轮上下文、函数式 Fake 精确演示 createTask→getTask→final。
 */
class AgentLoopTest {

    private TaskStore store;
    private ToolRegistry registry;

    @BeforeEach
    void setUp() {
        store = new TaskStore("jdbc:sqlite::memory:");
        registry = DemoTools.createDefault(store);
    }

    @AfterEach
    void tearDown() {
        store.close();
    }

    /** 剧本：一步工具调用后给 final —— Loop 正常终止，任务真的落库。 */
    @Test
    void oneToolCallThenFinal() {
        ScriptedLlmClient llm = new ScriptedLlmClient(
                "{\"type\":\"tool_call\",\"tool\":\"createTask\",\"arguments\":{\"title\":\"学习 Agent Loop\"},\"decisionSummary\":\"先创建任务\"}",
                "{\"type\":\"final\",\"answer\":\"任务已创建，id=t-demo，状态=pending\"}");

        AgentRun run = new AgentLoop(llm, registry).run("创建一个任务");

        assertTrue(run.finished());
        assertEquals("任务已创建，id=t-demo，状态=pending", run.answer());
        assertEquals(1, run.steps().size());
        assertEquals("createTask", run.steps().get(0).decision().toolCall().name());
        assertEquals(1, store.findAll().size());
        assertEquals("学习 Agent Loop", store.findAll().get(0).title());
    }

    /** 死循环用例：永远调用 getTask(NOT_FOUND) —— maxSteps 必须拦停，不能无限跑。 */
    @Test
    void infiniteLoopStopsAtMaxSteps() {
        ScriptedLlmClient llm = new ScriptedLlmClient(
                "{\"type\":\"tool_call\",\"tool\":\"getTask\",\"arguments\":{\"taskId\":\"NOT_FOUND\"},\"decisionSummary\":\"继续查\"}");

        AgentRun run = new AgentLoop(llm, registry, 8).run("查一个不存在的任务");

        assertFalse(run.finished());
        assertEquals(AgentLoop.MAX_STEPS_EXCEEDED, run.answer());
        assertEquals(8, run.steps().size());
        assertEquals(8, llm.requestCount());
    }

    /** maxSteps 可配置：给 3 步，死循环第 3 步就被拦停。 */
    @Test
    void maxStepsIsConfigurable() {
        ScriptedLlmClient llm = new ScriptedLlmClient(
                "{\"type\":\"tool_call\",\"tool\":\"getTask\",\"arguments\":{\"taskId\":\"NOT_FOUND\"}}");

        AgentRun run = new AgentLoop(llm, registry, 3).run("x");

        assertFalse(run.finished());
        assertEquals(3, run.steps().size());
        assertEquals(3, llm.requestCount());
    }

    /** Observation 必须进入下一轮上下文：第二次请求里应能看到第一次工具的观察。 */
    @Test
    void observationFeedsNextRequest() {
        ScriptedLlmClient llm = new ScriptedLlmClient(
                "{\"type\":\"tool_call\",\"tool\":\"getTask\",\"arguments\":{\"taskId\":\"T-1\"},\"decisionSummary\":\"先查一下\"}",
                "{\"type\":\"final\",\"answer\":\"查完了\"}");

        new AgentLoop(llm, registry).run("查任务");

        String secondRequest = join(llm.requestAt(1));
        assertTrue(secondRequest.contains("[observation]"));
        assertTrue(secondRequest.contains("getTask"));
        assertTrue(secondRequest.contains("未找到任务"));
    }

    /**
     * 函数式 Fake：模拟"根据上一次 Observation 决定下一步"的 ReAct 行为，
     * 精确走 createTask → getTask → final 三步闭环，且 getTask 用的是观察里读到的真实 id。
     */
    @Test
    void reactLoopDrivesNextStepFromObservation() {
        FunctionLlmClient llm = new FunctionLlmClient(messages -> {
            String history = join(messages);
            if (history.contains("任务: id=")) {
                // 已有 getTask 的观察：信息足够，给 final
                String id = extractId(history);
                return "{\"type\":\"final\",\"answer\":\"任务 id=" + id + "，状态=pending\"}";
            }
            if (history.contains("已创建任务")) {
                // 已有 createTask 的观察：提取真实 id 去查询
                String id = extractId(history);
                return "{\"type\":\"tool_call\",\"tool\":\"getTask\",\"arguments\":{\"taskId\":\"" + id + "\"},\"decisionSummary\":\"查询刚创建的任务\"}";
            }
            return "{\"type\":\"tool_call\",\"tool\":\"createTask\",\"arguments\":{\"title\":\"学习 Agent Loop\"},\"decisionSummary\":\"先创建任务\"}";
        });

        AgentRun run = new AgentLoop(llm, registry)
                .run("创建一个“学习 Agent Loop”的任务，然后再次查询它，最后告诉我任务 ID 与状态");

        assertTrue(run.finished());
        assertTrue(run.answer().startsWith("任务 id=t-"));
        assertTrue(run.answer().contains("pending"));
        assertEquals(2, run.steps().size());
        assertEquals("createTask", run.steps().get(0).decision().toolCall().name());
        assertEquals("getTask", run.steps().get(1).decision().toolCall().name());
        assertEquals(1, store.findAll().size());
    }

    private static String join(List<Message> messages) {
        return messages.stream().map(Message::content).reduce("", (a, b) -> a + "\n" + b);
    }

    private static String extractId(String history) {
        Matcher matcher = Pattern.compile("id=(t-[0-9a-f]+)").matcher(history);
        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new IllegalStateException("history 里没有任务 id: " + history);
    }
}
