package com.example.agentlearning.lab06;

import java.util.List;
import java.util.UUID;

/**
 * 持久化的 ReAct Agent 循环——lab06 的核心。
 *
 * <p>与 stage01 的 AgentRunner 相比，它把每次运行都<b>写进数据库</b>：
 * <ul>
 *   <li>用户输入、模型最终回答 → {@code message} 表（对话历史）；</li>
 *   <li>目标、当前状态机（RUNNING/WAITING_TOOL/COMPLETED/FAILED）、步数 → {@code agent_run} 表（Agent State）；</li>
 *   <li>开跑前先检索长期记忆，命中则注入 {@code [RETRIEVED MEMORY]} 区块（Long-term Memory）。</li>
 * </ul>
 * 工具执行中的 Observation 只存在本轮内存上下文里，属于运行细节，不入库。
 */
public final class StatefulAgentRunner {

    public static final int MAX_STEPS = 10;

    private final LlmClient llm;
    private final ToolRegistry tools;
    private final MessageRepository messages;
    private final AgentRunRepository runs;
    private final MemoryRetriever retriever;

    public StatefulAgentRunner(
            LlmClient llm,
            ToolRegistry tools,
            MessageRepository messages,
            AgentRunRepository runs,
            MemoryRetriever retriever) {
        this.llm = llm;
        this.tools = tools;
        this.messages = messages;
        this.runs = runs;
        this.retriever = retriever;
    }

    public RunResult run(String conversationId, String userInput) {
        // 1) 对话历史：用户消息先落库
        messages.append(conversationId, "user", userInput);

        // 2) Agent State：创建一次运行
        String runId = "r-" + UUID.randomUUID().toString().substring(0, 8);
        AgentRun run = runs.create(runId, conversationId, userInput);

        // 3) Long-term Memory：开跑前检索，命中则注入上下文
        List<Memory> retrieved = retriever.retrieve(userInput, 3);
        AgentContext context = buildContext(conversationId, retrieved);

        System.out.println("RUN " + runId + " (goal=" + userInput + ")");
        for (int step = 1; step <= MAX_STEPS; step++) {
            System.out.println("STEP " + step);

            LlmResponse reply = llm.chat(context.messages());
            AgentDecision decision = AgentDecisionParser.parse(reply.content());
            System.out.println("MODEL_ACTION: " + decision.type());

            if (decision.isFinal()) {
                messages.append(conversationId, "assistant", decision.answer());
                AgentRun finalRun = runs.updateStatus(runId, RunStatus.COMPLETED, step);
                System.out.println("FINAL: " + decision.answer());
                return new RunResult(runId, decision.answer(), finalRun);
            }

            ToolCall call = decision.toolCall();
            System.out.println("TOOL_CALL: " + call.name() + call.arguments());

            ToolResult result = tools.execute(call);
            runs.updateStatus(runId, RunStatus.WAITING_TOOL, step);
            context.addAssistant(reply.content());
            context.addObservation(call, result);

            System.out.println("TOOL_RESULT: " + result.message());
            System.out.println();
        }

        AgentRun failed = runs.updateStatus(runId, RunStatus.FAILED, MAX_STEPS);
        System.out.println("STOPPED: 超过最大步数 " + MAX_STEPS);
        return new RunResult(runId, "超过最大步数，任务未完成", failed);
    }

    private AgentContext buildContext(String conversationId, List<Memory> retrieved) {
        AgentContext context = new AgentContext();
        context.addSystem(systemPrompt());

        if (!retrieved.isEmpty()) {
            System.out.println("RETRIEVED MEMORY");
            StringBuilder block = new StringBuilder("[RETRIEVED MEMORY]\n");
            for (Memory memory : retrieved) {
                System.out.println("  - [" + memory.type() + "] " + memory.content());
                block.append("- [").append(memory.type()).append("] ").append(memory.content()).append('\n');
            }
            context.addSystem(block.toString().stripTrailing());
        }

        // 从库里重建已有对话历史，保证多轮对话连续（重启后可继续）
        for (StoredMessage stored : messages.findUserAndAssistantByConversation(conversationId)) {
            if ("user".equals(stored.role())) {
                context.addUser(stored.content());
            } else if ("assistant".equals(stored.role())) {
                context.addAssistant(stored.content());
            }
        }
        return context;
    }

    private String systemPrompt() {
        return """
                你是一个控制台 AI 任务助手。你可以调用工具完成用户的任务。
                当前可用的工具（JSON 数组描述）：
                %s

                决策协议（只输出一个 JSON，不要输出任何其他文字）：
                1. 需要调用工具时：
                   {"type":"tool_call","tool":"工具名","arguments":{参数名:值,...},"decisionSummary":"这一步为什么这么做，一句话"}
                2. 任务已完成、可以回答用户时：
                   {"type":"final","answer":"给用户的最终回答"}

                对话中带 [observation] 前缀的消息是工具执行后的观察结果，请基于它继续决策；
                不要重复执行一个已经执行过、且结果已知的工具调用。
                带 [RETRIEVED MEMORY] 前缀的是从长期记忆中检索到的用户信息，回答时应当尊重这些偏好。
                """.formatted(tools.toolsInstruction());
    }
}
