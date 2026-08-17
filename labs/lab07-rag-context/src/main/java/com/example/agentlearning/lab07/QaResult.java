package com.example.agentlearning.lab07;

import java.util.List;

/**
 * 一次 RAG 问答的产出：回答 + 各自检索到的记忆/知识（供观察与测试）。
 */
public record QaResult(
        String answer,
        List<Memory> memories,
        List<KnowledgeDoc> knowledge) {
}
