package com.example.agentlearning.lab08;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * 压缩服务验收测试。
 *
 * <ul>
 *   <li>未达阈值：消息全保留、不生成摘要、不调用模型；</li>
 *   <li>达阈值：旧消息被删到最近 N 条、生成结构化摘要 v1；</li>
 *   <li>最近消息不会被误删：留下的是最新几条；</li>
 *   <li>多次压缩：摘要版本递增；</li>
 *   <li>重启后：摘要与保留消息仍可读取（压缩产物是持久化的，不是内存态）。</li>
 * </ul>
 */
class CompactionServiceTest {

    private Path dbFile;
    private Database db;
    private MessageRepository messages;
    private ConversationSummaryRepository summaries;
    private ScriptedLlmClient summarizerLlm;

    @BeforeEach
    void setUp() throws Exception {
        dbFile = Files.createTempFile("lab08-compaction-", ".db");
        db = new Database("jdbc:sqlite:" + dbFile);
        messages = new MessageRepository(db);
        summaries = new ConversationSummaryRepository(db);
        summarizerLlm = ScriptedLlmClient.of(SUMMARY_JSON);
    }

    @AfterEach
    void tearDown() {
        db.close();
    }

    private CompactionService service(ContextPolicy policy) {
        return new CompactionService(
                messages, summaries, new ConversationSummarizer(summarizerLlm),
                policy, new ContextBuilder(), "系统提示");
    }

    /** 交替追加一条 user + 一条 assistant，共 {@code rounds} 轮。 */
    private void appendRounds(CompactionService service, String conversationId, int rounds) {
        for (int i = 1; i <= rounds; i++) {
            service.appendUser(conversationId, "user-" + i);
            service.appendAssistant(conversationId, "assistant-" + i);
        }
    }

    @Test
    void belowThresholdNotCompacted() {
        // COMPACT_AFTER=20：20 条消息（20 > 20 为 false）不触发压缩
        CompactionService service = service(new ContextPolicy(20, 5));
        appendRounds(service, "c1", 10);

        assertEquals(20, messages.countByConversation("c1"));
        assertTrue(summaries.findLatest("c1").isEmpty());
        assertEquals(0, summarizerLlm.callCount());
    }

    @Test
    void exceedingThresholdCompactsIntoSummary() {
        CompactionService service = service(new ContextPolicy(7, 3));
        appendRounds(service, "c1", 4); // 8 条，第 8 条时触发压缩

        assertEquals(3, messages.countByConversation("c1"));
        Optional<ConversationSummary> latest = summaries.findLatest("c1");
        assertTrue(latest.isPresent());
        assertEquals(1, latest.get().version());
        assertEquals("压缩目标", latest.get().goal());
        assertEquals(List.of("事实A"), latest.get().importantFacts());
        assertEquals(List.of("动作A"), latest.get().pendingActions());
        assertEquals(1, summarizerLlm.callCount());
    }

    @Test
    void recentMessagesSurviveCompaction() {
        CompactionService service = service(new ContextPolicy(7, 3));
        // u1,a1,u2,a2,u3,a3,u4,a4 → 第 8 条 a4 触发，保留最新 3 条：a3,u4,a4
        appendRounds(service, "c1", 4);

        List<StoredMessage> remaining = messages.findByConversation("c1");
        assertEquals(3, remaining.size());
        assertEquals("assistant-3", remaining.get(0).content());
        assertEquals("user-4", remaining.get(1).content());
        assertEquals("assistant-4", remaining.get(2).content());
        assertFalse(remaining.stream().anyMatch(m -> m.content().equals("user-1")));
        assertFalse(remaining.stream().anyMatch(m -> m.content().equals("assistant-1")));
    }

    @Test
    void repeatedCompactionsIncreaseVersion() {
        CompactionService service = service(new ContextPolicy(7, 3));
        // 14 条：a4（第 8 条）→ v1；u7（再第 8 条）→ v2；a7 在 v2 之后追加
        appendRounds(service, "c1", 7);

        assertEquals(2, summaries.findLatest("c1").orElseThrow().version());
        assertEquals(4, messages.countByConversation("c1"));
        assertEquals(2, summarizerLlm.callCount());
    }

    @Test
    void summaryPersistsAcrossReopen() {
        CompactionService service = service(new ContextPolicy(7, 3));
        appendRounds(service, "c1", 4);
        db.close();

        Database reopened = new Database("jdbc:sqlite:" + dbFile);
        try {
            ConversationSummaryRepository repo2 = new ConversationSummaryRepository(reopened);
            Optional<ConversationSummary> latest = repo2.findLatest("c1");
            assertTrue(latest.isPresent());
            assertEquals(1, latest.get().version());
            assertEquals("压缩目标", latest.get().goal());
            assertEquals(3, new MessageRepository(reopened).countByConversation("c1"));
        } finally {
            reopened.close();
        }
    }

    private static final String SUMMARY_JSON = """
            {
              "goal": "压缩目标",
              "completed": ["完成A"],
              "importantFacts": ["事实A"],
              "decisions": ["决定A"],
              "openQuestions": ["问题A"],
              "pendingActions": ["动作A"]
            }""";
}
