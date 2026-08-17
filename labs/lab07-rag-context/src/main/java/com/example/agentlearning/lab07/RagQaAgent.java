package com.example.agentlearning.lab07;

import java.util.List;

/**
 * 带 Context 的问答 Agent（本章无工具循环）：
 *
 * <pre>
 * 问题 → MemoryRetriever.retrieve（memory 表）
 *      → KnowledgeRetriever.retrieve（knowledge_doc 表）
 *      → ContextBuilder.build（System + Memory + Knowledge + Request）
 *      → LLM 回答
 * </pre>
 */
public final class RagQaAgent {

    private final LlmClient llm;
    private final MemoryRetriever memoryRetriever;
    private final KnowledgeRetriever knowledgeRetriever;
    private final ContextBuilder contextBuilder;
    private final String systemPrompt;

    public RagQaAgent(
            LlmClient llm,
            MemoryRetriever memoryRetriever,
            KnowledgeRetriever knowledgeRetriever,
            ContextBuilder contextBuilder,
            String systemPrompt) {
        this.llm = llm;
        this.memoryRetriever = memoryRetriever;
        this.knowledgeRetriever = knowledgeRetriever;
        this.contextBuilder = contextBuilder;
        this.systemPrompt = systemPrompt;
    }

    public static String defaultSystemPrompt() {
        return """
                你是一个项目助手。回答时注意消息里的来源标记：
                - 带 [RETRIEVED MEMORY] 前缀的是关于用户的长期偏好/约定，回答"我/我的"类问题时以它为准；
                - 带 [RETRIEVED KNOWLEDGE] 前缀的是项目知识文档，回答"本项目/项目规范"类问题时以它为准。
                如果两者都给出了信息，说明各自依据；如果没有任何检索信息，就如实说不知道。
                只输出回答正文，不要输出任何 JSON 或思考过程。
                """;
    }

    public QaResult answer(String question) {
        List<Memory> memories = memoryRetriever.retrieve(question, 3);
        List<KnowledgeDoc> knowledge = knowledgeRetriever.retrieve(question, 3);
        List<Message> context = contextBuilder.build(systemPrompt, List.of(), memories, knowledge, question);
        String answer = llm.chat(context).content();
        return new QaResult(answer, memories, knowledge);
    }
}
