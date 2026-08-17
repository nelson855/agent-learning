package com.example.agentlearning.lab07;

import java.util.List;

/**
 * 记忆提取器：判断用户这条消息值不值得作为长期偏好保存。
 * 判断交给模型，落库由程序把关（只有 shouldRemember=true 且内容非空才写入）。
 */
public final class MemoryExtractor {

    private static final String PROMPT = """
            你是一个记忆提取器。判断用户这条消息是否值得保存为长期记忆（偏好/约定/事实）。

            值得保存（shouldRemember=true）：
            - 用户明确表达的偏好、习惯（例如"以后我的 Java Demo 都使用 Maven"）
            - 关于用户的长期事实、项目约定

            不值得保存（shouldRemember=false）：一次性请求、闲聊、需要立刻执行的命令。

            只输出一个 JSON，不要输出任何其他文字：
            {"shouldRemember": true, "memoryType": "PREFERENCE", "content": "归一化后的记忆内容，用第三人称描述用户"}
            memoryType 只能是 PREFERENCE（偏好）/ FACT（事实）/ PROJECT_CONVENTION（项目约定）。
            不值得保存时只输出：{"shouldRemember": false}
            """;

    private final LlmClient llm;

    public MemoryExtractor(LlmClient llm) {
        this.llm = llm;
    }

    public MemoryDecision extract(String userInput) {
        LlmResponse reply = llm.chat(List.of(Message.system(PROMPT), Message.user(userInput)));
        return MemoryDecisionParser.parse(reply.content());
    }
}
