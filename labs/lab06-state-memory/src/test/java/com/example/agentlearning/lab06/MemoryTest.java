package com.example.agentlearning.lab06;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Memory 两条主线的行为测试：写入由模型判断、程序把关（Extractor），
 * 读取靠 keyword / type / importance / recency（Retriever）。
 */
class MemoryTest {

    private static Database inMemory() {
        return new Database("jdbc:sqlite::memory:");
    }

    // ---- 写入侧：MemoryExtractor ----

    @Test
    void extractorKeepsOnlyWhatModelSaysToRemember() {
        ScriptedLlmClient llm = ScriptedLlmClient.of(
                "{\"shouldRemember\":true,\"memoryType\":\"PREFERENCE\",\"content\":\"用户的 Java Demo 都使用 Maven\"}");
        MemoryExtractor extractor = new MemoryExtractor(llm);

        MemoryDecision decision = extractor.extract("以后我的 Java Demo 都使用 Maven。");
        assertTrue(decision.shouldRemember());
        assertEquals("PREFERENCE", decision.memoryType());
        assertEquals("用户的 Java Demo 都使用 Maven", decision.content());
    }

    @Test
    void extractorTreatsMalformedReplyAsNoRemember() {
        ScriptedLlmClient llm = ScriptedLlmClient.of("模型没有按 JSON 约定输出");
        MemoryExtractor extractor = new MemoryExtractor(llm);

        assertFalse(extractor.extract("随便聊聊").shouldRemember());
    }

    // ---- 读取侧：MemoryRetriever ----

    @Test
    void retrieverFindsByKeywordAndSortsByImportance() {
        Database db = inMemory();
        MemoryRepository repo = new MemoryRepository(db);
        repo.save("u", "PREFERENCE", "用户喜欢喝咖啡", 3);
        repo.save("u", "PREFERENCE", "用户的 Java Demo 都使用 Maven", 5);
        repo.save("u", "FACT", "用户是 Java 后端开发者", 4);

        MemoryRetriever retriever = new MemoryRetriever(repo);
        List<Memory> hits = retriever.retrieve("Java Demo 用什么来管理构建", 5);

        // "用户喜欢喝咖啡"不含任何关键词，不命中；两条含 Java 的记忆命中
        assertEquals(2, hits.size());
        assertTrue(hits.stream().noneMatch(m -> m.content().contains("喝咖啡")));
        // importance 排序：importance=5 的 Java Demo 记忆排在最前
        assertEquals("用户的 Java Demo 都使用 Maven", hits.get(0).content());
        assertEquals("用户是 Java 后端开发者", hits.get(1).content());
        // 命中后，库里对应行的最近使用时间被更新
        assertNotNull(repo.findById(hits.get(0).id()).orElseThrow().lastUsedAt(), "命中后应更新最近使用时间");

        // importance 排序：都是含"Java"的记忆时，importance 高者在前
        List<Memory> javaHits = retriever.retrieve("Java", 5);
        assertEquals(2, javaHits.size());
        assertEquals("用户的 Java Demo 都使用 Maven", javaHits.get(0).content()); // importance=5
        assertEquals("用户是 Java 后端开发者", javaHits.get(1).content());       // importance=4

        // type 过滤
        List<Memory> facts = repo.search(List.of("Java"), "FACT", 5);
        assertEquals(1, facts.size());
        assertEquals("FACT", facts.get(0).type());
        db.close();
    }

    @Test
    void tokenizeSplitsChineseAndEnglishWords() {
        List<String> tokens = MemoryRetriever.tokenize("帮我初始化一个 Java Demo");
        assertTrue(tokens.contains("Java"));
        assertTrue(tokens.contains("Demo"));
        assertTrue(tokens.stream().anyMatch(t -> t.contains("初始化") || t.contains("帮我")));
    }
}
