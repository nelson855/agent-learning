package com.example.agentlearning.lab07;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * 分词器：英文/数字整体成词，连续汉字按二元组切分，保证 LIKE 能命中长中文问句。
 */
class TokensTest {

    @Test
    void splitsEnglishWordsAndChineseBigrams() {
        List<String> tokens = Tokens.tokenize("我的 Java Demo");
        assertTrue(tokens.contains("Java"));
        assertTrue(tokens.contains("Demo"));
        assertTrue(tokens.contains("我的"), "连续汉字应切成二元组");
    }

    @Test
    void longChineseQuestionProducesBigramsForKeywordHits() {
        List<String> tokens = Tokens.tokenize("任务系统使用什么数据库");
        assertTrue(tokens.contains("任务"));
        assertTrue(tokens.contains("系统"));
        assertTrue(tokens.contains("使用"));
        assertTrue(tokens.contains("数据"), "长问句里的关键词应作为二元组出现，才能 LIKE 命中知识文档");
    }

    @Test
    void blankReturnsEmpty() {
        assertTrue(Tokens.tokenize("  ").isEmpty());
        assertTrue(Tokens.tokenize(null).isEmpty());
    }
}
