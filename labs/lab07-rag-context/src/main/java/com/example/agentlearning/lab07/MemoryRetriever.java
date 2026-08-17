package com.example.agentlearning.lab07;

import java.util.List;

/**
 * 记忆检索器：用当前问题去 {@code memory} 表里找"关于用户"的相关记忆。
 * keyword/LIKE + importance/recency 排序，不使用向量库。
 */
public final class MemoryRetriever {

    private final MemoryRepository repository;

    public MemoryRetriever(MemoryRepository repository) {
        this.repository = repository;
    }

    public List<Memory> retrieve(String query, int limit) {
        List<String> keywords = Tokens.tokenize(query);
        List<Memory> hits = repository.search(keywords, null, limit);
        for (Memory memory : hits) {
            repository.touch(memory.id());
        }
        return hits;
    }
}
