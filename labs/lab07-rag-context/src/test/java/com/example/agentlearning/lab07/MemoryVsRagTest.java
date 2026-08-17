package com.example.agentlearning.lab07;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * 验收核心：证明 Memory 与 RAG 是<b>两条独立检索路径</b>——
 * 同样都存 SQLite、都做 keyword/LIKE，但查的是不同表、回答不同语义的问题。
 */
class MemoryVsRagTest {

    private Database db;
    private MemoryRepository memories;
    private RagQaAgent agent;

    @BeforeEach
    void setUp() {
        db = new Database("jdbc:sqlite::memory:");
        memories = new MemoryRepository(db);
        KnowledgeRepository knowledge = new KnowledgeRepository(db);
        KnowledgeImporter.importFromResources(knowledge);
        memories.save("u", "PREFERENCE", "用户偏好 Maven 构建", 5);
        ScriptedLlmClient llm = ScriptedLlmClient.of("ok");
        agent = new RagQaAgent(
                llm,
                new MemoryRetriever(memories),
                new KnowledgeRetriever(knowledge),
                new ContextBuilder(),
                "你是测试助手");
    }

    /** 项目/数据库规范类问题 → 命中 knowledge_doc（RAG），不命中用户偏好（Memory）。 */
    @Test
    void projectQuestionAnswersFromKnowledgeOnly() {
        QaResult result = agent.answer("任务系统使用什么数据库？");

        assertFalse(result.knowledge().isEmpty(), "RAG 应命中知识库");
        assertTrue(result.knowledge().stream()
                        .anyMatch(d -> d.title().contains("数据库")),
                "应命中 database-rules 文档");

        // 用户偏好里没有"数据库/任务系统"相关内容，Memory 不命中
        assertTrue(result.memories().isEmpty(), "该问题不应命中 Memory");
    }

    /** 用户偏好类问题 → 命中 memory 表（用户长期偏好）。 */
    @Test
    void preferenceQuestionAnswersFromMemory() {
        QaResult result = agent.answer("我的项目用什么构建？");

        assertFalse(result.memories().isEmpty(), "应命中用户长期偏好");
        assertTrue(result.memories().get(0).content().contains("Maven"));
    }

    /** 同一条事实可以有两种身份（教材 8.4）：既能是 Memory，也能是 Knowledge，来源表各自分开。 */
    @Test
    void sameFactCanExistAsMemoryAndAsKnowledge() {
        // memory 表存"用户偏好 Maven"；knowledge_doc 里 coding-rules.md 也写了"Maven 构建"
        QaResult result = agent.answer("项目使用什么构建工具？");

        // 两条路径独立命中，结果对象里来源字段类型与内容各自对应不同表
        assertTrue(result.memories().stream()
                .anyMatch(m -> m.content().contains("Maven")), "Memory 命中用户偏好");
        assertTrue(result.knowledge().stream()
                .anyMatch(k -> k.title().contains("编码规范")), "RAG 命中项目规范");
        // memories 是 Memory 类型（来自 memory 表），knowledge 是 KnowledgeDoc 类型（来自 knowledge_doc 表）
        assertTrue(result.memories().get(0) instanceof Memory);
        assertTrue(result.knowledge().get(0) instanceof KnowledgeDoc);
        db.close();
    }
}
