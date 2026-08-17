package com.example.agentlearning.lab07;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Knowledge Store：从本地 Markdown 导入 + keyword/tags 检索。
 */
class KnowledgeStoreTest {

    @Test
    void importerLoadsThreeMarkdownDocs() {
        Database db = new Database("jdbc:sqlite::memory:");
        KnowledgeRepository repository = new KnowledgeRepository(db);
        int imported = KnowledgeImporter.importFromResources(repository);

        assertEquals(3, imported);
        assertEquals(3, repository.count());
        assertTrue(repository.findAll().stream()
                .anyMatch(d -> d.title().equals("数据库规范")));
        db.close();
    }

    @Test
    void parseExtractsTitleTagsAndContent() {
        KnowledgeDoc doc = KnowledgeImporter.parse("database-rules.md",
                "# 数据库规范\nTags: sqlite, database\n\n任务系统使用 SQLite。\n");

        assertEquals("数据库规范", doc.title());
        assertEquals("sqlite, database", doc.tags());
        assertTrue(doc.content().contains("SQLite"));
    }

    @Test
    void searchFindsByKeywordAndTag() {
        Database db = new Database("jdbc:sqlite::memory:");
        KnowledgeRepository repository = new KnowledgeRepository(db);
        KnowledgeImporter.importFromResources(repository);

        // keyword 命中：数据库规范一文含"数据库"
        List<KnowledgeDoc> byKeyword = repository.search(List.of("数据库"), List.of(), 5);
        assertTrue(byKeyword.stream().anyMatch(d -> d.title().contains("数据库")));

        // tag 命中：按标签 maven 应找到编码规范
        List<KnowledgeDoc> byTag = repository.search(List.of(), List.of("maven"), 5);
        assertTrue(byTag.stream().anyMatch(d -> d.title().contains("编码规范")));

        // 无关键词无标签：返回全部（按创建时间）
        assertEquals(3, repository.search(List.of(), List.of(), 5).size());
        db.close();
    }

    @Test
    void deleteAllResetsTable() {
        Database db = new Database("jdbc:sqlite::memory:");
        KnowledgeRepository repository = new KnowledgeRepository(db);
        KnowledgeImporter.importFromResources(repository);
        assertEquals(3, repository.count());

        repository.deleteAll();
        assertEquals(0, repository.count());
        db.close();
    }
}
