package com.example.agentlearning.lab06;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * 验收核心：证明 Conversation History / Long-term Memory / Agent State 是三个
 * <b>不同</b>的持久化维度——不是同一张表换名字。各自写入、各自读出、互不混淆。
 */
class PersistenceTest {

    private Path tempDbFile() throws Exception {
        Path file = Files.createTempFile("lab06-persist-", ".db");
        Files.deleteIfExists(file);
        return file;
    }

    // ---- 维度一：Conversation History（message 表）----

    @Test
    void conversationHistoryPersistsAcrossDatabaseReopen() throws Exception {
        Path file = tempDbFile();

        Database db1 = new Database("jdbc:sqlite:" + file);
        ConversationRepository conversations = new ConversationRepository(db1);
        MessageRepository messages = new MessageRepository(db1);
        Conversation conversation = conversations.create("测试会话");
        messages.append(conversation.id(), "user", "你好");
        messages.append(conversation.id(), "assistant", "你好，有什么可以帮你？");
        db1.close(); // 模拟进程退出

        // "重启"后重新打开同一个文件，历史必须还在
        Database db2 = new Database("jdbc:sqlite:" + file);
        List<StoredMessage> history = new MessageRepository(db2).findUserAndAssistantByConversation(conversation.id());
        db2.close();
        Files.deleteIfExists(file);

        assertEquals(2, history.size());
        assertEquals("user", history.get(0).role());
        assertEquals("你好", history.get(0).content());
        assertEquals("assistant", history.get(1).role());
    }

    // ---- 维度二：Agent State（agent_run 表）----

    @Test
    void agentRunStatePersistsAndTransitions() throws Exception {
        Path file = tempDbFile();

        Database db1 = new Database("jdbc:sqlite:" + file);
        AgentRunRepository runs = new AgentRunRepository(db1);
        AgentRun run = runs.create("r-test-1", "c-test-1", "创建一个任务");
        assertEquals(RunStatus.RUNNING, run.status());
        assertEquals(0, run.currentStep());

        runs.updateStatus("r-test-1", RunStatus.WAITING_TOOL, 1);
        runs.updateStatus("r-test-1", RunStatus.COMPLETED, 2);
        db1.close();

        Database db2 = new Database("jdbc:sqlite:" + file);
        AgentRunRepository restoredRepo = new AgentRunRepository(db2);
        AgentRun restored = restoredRepo.findById("r-test-1").orElseThrow();
        db2.close();
        Files.deleteIfExists(file);

        assertEquals(RunStatus.COMPLETED, restored.status());
        assertEquals(2, restored.currentStep());
        assertEquals("创建一个任务", restored.goal());
        assertNotEquals(restored.startedAt(), restored.updatedAt());
    }

    // ---- 维度三：三张表彼此独立 ----

    @Test
    void threeConceptsLiveInSeparateTables() {
        Database db = new Database("jdbc:sqlite::memory:");

        // 同一时刻，往三个维度各写一份数据
        ConversationRepository conversations = new ConversationRepository(db);
        MessageRepository messages = new MessageRepository(db);
        AgentRunRepository runs = new AgentRunRepository(db);
        MemoryRepository memories = new MemoryRepository(db);

        Conversation conversation = conversations.create("会话X");
        messages.append(conversation.id(), "user", "帮我建一个任务");
        messages.append(conversation.id(), "assistant", "已创建任务 t-1");

        runs.create("r-x-1", conversation.id(), "帮我建一个任务");

        memories.save("demo-user", "PREFERENCE", "用户的 Java Demo 都使用 Maven", 5);

        // 1) 对话历史只包含用户与助手的对白，不包含 run 状态
        List<StoredMessage> history = messages.findUserAndAssistantByConversation(conversation.id());
        assertEquals(2, history.size());
        assertTrue(history.stream().noneMatch(m -> m.content().contains("COMPLETED")));

        // 2) run 状态独立存在，goal 与会话历史内容不同语义，但不混进 message 表
        AgentRun run = runs.findById("r-x-1").orElseThrow();
        assertEquals(RunStatus.RUNNING, run.status());

        // 3) memory 表只含记忆，检索"Java Demo"只能命中记忆而不是对话
        MemoryRetriever retriever = new MemoryRetriever(memories);
        List<Memory> hits = retriever.retrieve("Java Demo", 5);
        assertEquals(1, hits.size());
        assertEquals("用户的 Java Demo 都使用 Maven", hits.get(0).content());

        // 反证：如果三者共用一张表，删除任一语义都会相互影响；这里各表独立成行
        assertEquals(2, history.size()); // 历史仍是 2 条
        db.close();
    }
}
