package com.example.agentlearning.lab02;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * lab02 端到端验收：全部使用 {@link FakeLlmClient} 注入结构化 JSON，
 * 不依赖真实网络，覆盖 Prompt 要求的 5 个验收点。
 */
class ToolCallingTest {

    private TaskStore store;
    private ToolRegistry registry;
    private FakeLlmClient fake;

    @BeforeEach
    void setUp() {
        store = new TaskStore("jdbc:sqlite::memory:");
        registry = DemoTools.createDefault(store);
        fake = new FakeLlmClient();
    }

    @AfterEach
    void tearDown() {
        store.close();
    }

    private ToolResult run(String userInput, String modelJsonReply) {
        fake.setReply(modelJsonReply);
        return new ToolRunner(fake, registry).run(userInput);
    }

    /** 验收点：正确创建任务 —— 模型建议 createTask，程序把它真正写入 SQLite。 */
    @Test
    void createTaskStoresTaskInSqlite() {
        ToolResult result = run("帮我创建一个任务：写周报",
                "{\"tool\":\"createTask\",\"arguments\":{\"title\":\"写周报\"}}");

        assertTrue(result.success());
        assertTrue(result.message().contains("已创建任务"));

        List<Task> tasks = store.findAll();
        assertEquals(1, tasks.size());
        assertEquals("写周报", tasks.get(0).title());
        assertEquals("pending", tasks.get(0).status());
    }

    /** 验收点：查询不存在的任务 —— 不崩溃，返回"未找到"。 */
    @Test
    void getTaskMissingReturnsNotFoundMessage() {
        ToolResult result = run("查询任务 t-none",
                "{\"tool\":\"getTask\",\"arguments\":{\"taskId\":\"t-none\"}}");

        assertTrue(result.success());
        assertTrue(result.message().contains("未找到任务"));
    }

    /** 验收点：参数缺失 —— 被程序校验拒绝，而不是 NPE 崩溃。 */
    @Test
    void missingArgumentIsRejectedNotNpe() {
        ToolResult result = run("查询任务", "{\"tool\":\"getTask\",\"arguments\":{}}");

        assertFalse(result.success());
        assertTrue(result.message().contains("缺少参数"));
        assertTrue(result.message().contains("taskId"));
    }

    /** 验收点：参数类型错误 —— taskId 应为字符串，给数字被拒绝。 */
    @Test
    void wrongArgumentTypeIsRejected() {
        ToolResult result = run("查询任务", "{\"tool\":\"getTask\",\"arguments\":{\"taskId\":123}}");

        assertFalse(result.success());
        assertTrue(result.message().contains("应为字符串"));
    }

    /** 验收点：不存在的工具 —— 返回 UNKNOWN_TOOL，而不是静默忽略或崩溃。 */
    @Test
    void unknownToolReturnsErrorMessage() {
        ToolResult result = run("删除所有任务", "{\"tool\":\"deleteAllTasks\",\"arguments\":{}}");

        assertFalse(result.success());
        assertTrue(result.message().contains(ToolRegistry.UNKNOWN_TOOL));
        assertEquals(0, store.findAll().size());
    }

    /** 模型也可以选择不调用工具，直接给文本回复。 */
    @Test
    void plainTextReplyWithoutToolCall() {
        ToolResult result = run("你好", "{\"tool\":null,\"text\":\"好的，我明白了。\"}");

        assertTrue(result.success());
        assertTrue(result.message().contains("好的"));
        assertEquals(0, store.findAll().size());
    }

    /** calculator 工具由程序确定性求值，与模型无关。 */
    @Test
    void calculatorEvaluatesDeterministically() {
        ToolResult result = run("算一下", "{\"tool\":\"calculator\",\"arguments\":{\"expression\":\"(1+2)*3\"}}");

        assertTrue(result.success());
        assertTrue(result.message().contains("= 9"));
    }

    /** 请求里必须携带带工具说明的 system 消息 + 用户消息。 */
    @Test
    void requestContainsSystemPromptWithTools() {
        run("帮我创建任务", "{\"tool\":\"createTask\",\"arguments\":{\"title\":\"x\"}}");

        List<Message> messages = fake.lastRequest();
        assertEquals(2, messages.size());
        assertEquals(Role.SYSTEM, messages.get(0).role());
        assertTrue(messages.get(0).content().contains("getTask"));
        assertEquals(Role.USER, messages.get(1).role());
    }
}
