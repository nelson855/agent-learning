package com.example.agentlearning.lab06;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * StatefulAgentRunner 的行为测试：一次运行下来，
 * 对话历史、Agent 状态、长期记忆三件事都被正确持久化。
 */
class StatefulAgentRunnerTest {

    private Database db;
    private Conversation conversation;
    private MessageRepository messages;
    private AgentRunRepository runs;
    private MemoryRepository memories;
    private StatefulAgentRunner runner;

    private void setUp(ScriptedLlmClient llm) {
        db = new Database("jdbc:sqlite::memory:");
        messages = new MessageRepository(db);
        runs = new AgentRunRepository(db);
        memories = new MemoryRepository(db);
        TaskStore taskStore = new TaskStore(db);
        ToolRegistry tools = TaskTools.createDefault(taskStore);
        runner = new StatefulAgentRunner(llm, tools, messages, runs, new MemoryRetriever(memories));
        conversation = new ConversationRepository(db).create("测试会话");
    }

    @Test
    void runPersistsHistoryAndStateAndToolData() {
        setUp(ScriptedLlmClient.of(
                "{\"type\":\"tool_call\",\"tool\":\"createTask\",\"arguments\":{\"title\":\"学习 SQLite\"},\"decisionSummary\":\"用户要求创建任务\"}",
                "{\"type\":\"final\",\"answer\":\"任务已创建，请查收。\"}"));

        RunResult result = runner.run(conversation.id(), "帮我创建一个任务：学习 SQLite");

        // Agent State：COMPLETED，走了 2 步
        assertEquals(RunStatus.COMPLETED, result.run().status());
        assertEquals(2, result.run().currentStep());

        // Conversation History：user 输入 + assistant 最终回答都落库
        List<StoredMessage> history = messages.findUserAndAssistantByConversation(conversation.id());
        assertEquals(2, history.size());
        assertEquals("帮我创建一个任务：学习 SQLite", history.get(0).content());
        assertEquals("任务已创建，请查收。", history.get(1).content());
        // Observation 属于运行细节，不混入对话历史
        assertTrue(history.stream().noneMatch(m -> m.content().contains("[observation]")));

        // 工具操作的数据也落库了
        assertEquals(1, new TaskStore(db).findAll().size());
        db.close();
    }

    @Test
    void retrievedMemoryTouchesAndInformsRun() {
        setUp(ScriptedLlmClient.of(
                "{\"type\":\"final\",\"answer\":\"好的，我会用 Maven 来初始化你的 Java Demo。\"}"));
        Memory saved = memories.save("u", "PREFERENCE", "用户的 Java Demo 都使用 Maven", 5);

        // 记忆命中后 last_used_at 被更新，说明检索真实发生并注入上下文
        RunResult result = runner.run(conversation.id(), "帮我初始化一个 Java Demo。");

        assertEquals(RunStatus.COMPLETED, result.run().status());
        assertNotNull(memories.findById(saved.id()).orElseThrow().lastUsedAt());
        db.close();
    }

    @Test
    void maxStepsMarksRunFailed() {
        // 剧本永远输出 tool_call，永远不 final → 走到最大步数
        setUp(ScriptedLlmClient.of(
                "{\"type\":\"tool_call\",\"tool\":\"listTasks\",\"arguments\":{},\"decisionSummary\":\"看一下当前任务\"}"));

        RunResult result = runner.run(conversation.id(), "一直列出任务");
        assertEquals(RunStatus.FAILED, result.run().status());
        assertEquals(StatefulAgentRunner.MAX_STEPS, result.run().currentStep());
        db.close();
    }
}
