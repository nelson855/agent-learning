package com.example.agentlearning.lab02;

import java.util.List;

/**
 * 单步编排：用户输入 → 模型决策 → 0 或 1 次工具调用 → 展示结果。
 *
 * <p>这是"Agent Loop"的一个最小切片。本章<b>刻意不做循环</b>：
 * 工具执行结果不会再次喂回模型。多次往返、循环终止条件等属于
 * {@code lab03-agent-loop-react} 的内容。
 */
public final class ToolRunner {

    private final LlmClient llm;
    private final ToolRegistry registry;

    public ToolRunner(LlmClient llm, ToolRegistry registry) {
        this.llm = llm;
        this.registry = registry;
    }

    public ToolRegistry registry() {
        return registry;
    }

    /** 跑一次"用户输入 → 模型 → （可选）工具调用"，返回可展示的结果。 */
    public ToolResult run(String userInput) {
        List<Message> messages = List.of(
                Message.system(systemPrompt()),
                Message.user(userInput));
        LlmResponse reply = llm.chat(messages);
        LlmIntent intent = ToolCallParser.parse(reply.content());
        if (intent.wantsToolCall()) {
            return registry.execute(intent.toolCall());
        }
        return ToolResult.ok(intent.text());
    }

    /** 把工具说明塞进 system prompt，告诉模型"能干什么、怎么输出"。 */
    private String systemPrompt() {
        return """
                你是一个任务助手。你可以调用下面这些工具（JSON 数组描述）：
                %s

                决策规则：
                1. 需要调用工具时，只输出一个 JSON 对象，不要输出任何其他文字，格式为：
                   {"tool":"工具名","arguments":{参数名:值,...}}
                2. 不需要调用工具时，输出：
                   {"tool":null,"text":"给用户的回复"}
                """.formatted(registry.toolsInstruction());
    }
}
