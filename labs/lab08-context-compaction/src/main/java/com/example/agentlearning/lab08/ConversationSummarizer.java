package com.example.agentlearning.lab08;

import java.util.List;

/**
 * 对话摘要器：把"旧消息"压缩成结构化摘要。
 *
 * <p>摘要不是随意一段话，而是强制 JSON 结构（教材 9.6）：
 * goal / completed / importantFacts / decisions / openQuestions / pendingActions。
 * 这样比"把对话删掉"更可恢复——未完成事项、决定、事实都还留着。
 */
public final class ConversationSummarizer {

    private static final String PROMPT = """
            你是一个对话压缩器。把给定的对话历史压缩成结构化摘要，供后续继续该任务时使用。

            只输出一个 JSON，不要输出任何其他文字，格式如下：
            {
              "goal": "这个会话/任务的目标，一句话",
              "completed": ["已完成的事项"],
              "importantFacts": ["关键事实，如用户偏好、技术选型"],
              "decisions": ["做过的决定"],
              "openQuestions": ["还没回答的问题"],
              "pendingActions": ["还没做完、需要继续的动作"]
            }

            每条尽量精简。如果某项没有内容，用空数组 []。
            """;

    private final LlmClient llm;

    public ConversationSummarizer(LlmClient llm) {
        this.llm = llm;
    }

    public ConversationSummaryParser.ParsedSummary summarize(List<StoredMessage> oldMessages) {
        StringBuilder transcript = new StringBuilder("需要压缩的对话历史：\n");
        for (StoredMessage message : oldMessages) {
            transcript.append(message.role()).append(": ").append(message.content()).append('\n');
        }
        String reply = llm.chat(List.of(Message.system(PROMPT), Message.user(transcript.toString()))).content();
        return ConversationSummaryParser.parse(reply);
    }
}
