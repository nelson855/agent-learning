package com.example.agentlearning.lab07;

import java.util.List;

/**
 * 知识检索器：用当前问题去 {@code knowledge_doc} 表里找"关于项目/外部世界"的相关知识。
 * keyword(title/content LIKE) + tags，不使用向量库。
 */
public final class KnowledgeRetriever {

    private final KnowledgeRepository repository;

    public KnowledgeRetriever(KnowledgeRepository repository) {
        this.repository = repository;
    }

    public List<KnowledgeDoc> retrieve(String query, int limit) {
        List<String> keywords = Tokens.tokenize(query);
        return repository.search(keywords, List.of(), limit);
    }
}
