package com.example.agentlearning.lab03;

import java.util.ArrayList;
import java.util.List;

/**
 * Agent Loop（ReAct 的最小工程实现）：上下文 → 模型决策 → 工具/观察 → 再决策。
 *
 * <pre>{@code
 * while (!finished) {
 *     response = llm(context);          // Model Decision
 *     if (response.isFinal()) return;    // Stop Condition 1: final
 *     toolResult = execute(response);    // Tool Call
 *     context.add(toolResult);           // Observation 回到上下文
 * }
 * }</pre>
 *
 * <p><b>Stop Condition</b>（本章重点）：
 * <ol>
 *   <li>模型给出 {@code final} 回答；</li>
 *   <li>工具不存在/参数不合法时程序拒绝执行 —— 失败也作为 Observation 返回给模型，
 *       由模型决定修正或终止（tool error policy：不崩溃、不静默）；</li>
 *   <li>超过 {@code maxSteps}（默认 8）强制停止，返回 {@code AGENT_MAX_STEPS_EXCEEDED}
 *       —— 这是对抗死循环的最后一道 Harness 边界。</li>
 * </ol>
 *
 * <p>每一步会打印 step / action type / tool name / tool result summary，便于观察 Loop 过程。
 */
public final class AgentLoop {

    /** 超步数停止时的标记。 */
    public static final String MAX_STEPS_EXCEEDED = "AGENT_MAX_STEPS_EXCEEDED";

    private static final int DEFAULT_MAX_STEPS = 8;

    private final LlmClient llm;
    private final ToolRegistry registry;
    private final int maxSteps;

    public AgentLoop(LlmClient llm, ToolRegistry registry) {
        this(llm, registry, DEFAULT_MAX_STEPS);
    }

    public AgentLoop(LlmClient llm, ToolRegistry registry, int maxSteps) {
        if (maxSteps <= 0) {
            throw new IllegalArgumentException("maxSteps 必须大于 0，实际: " + maxSteps);
        }
        this.llm = llm;
        this.registry = registry;
        this.maxSteps = maxSteps;
    }

    public int maxSteps() {
        return maxSteps;
    }

    /** 运行 Agent，直到 final 或超过 maxSteps。 */
    public AgentRun run(String userInput) {
        AgentContext context = new AgentContext();
        context.addSystem(systemPrompt());
        context.addUser(userInput);
        List<StepTrace> steps = new ArrayList<>();

        for (int step = 1; step <= maxSteps; step++) {
            System.out.println("STEP " + step);

            LlmResponse reply = llm.chat(context.messages());
            AgentDecision decision = AgentDecisionParser.parse(reply.content());

            if (decision.isFinal()) {
                System.out.println("ACTION TYPE: final");
                System.out.println("FINAL: " + decision.answer());
                return AgentRun.finalAnswer(decision.answer(), steps);
            }

            ToolResult result = registry.execute(decision.toolCall());
            steps.add(new StepTrace(step, decision, result));
            context.addAssistant(reply.content());
            context.addObservation(decision.toolCall(), result);

            System.out.println("ACTION TYPE: " + decision.type());
            System.out.println("TOOL NAME: " + decision.toolCall().name());
            System.out.println("TOOL RESULT: " + result.message());
            System.out.println();
        }

        System.out.println("AGENT_MAX_STEPS_EXCEEDED at maxSteps=" + maxSteps);
        return AgentRun.maxStepsExceeded(steps);
    }

    /** 把工具说明 + 决策协议 + 观察格式写进 system prompt。 */
    private String systemPrompt() {
        return """
                你是一个任务助手。你可以调用下面这些工具（JSON 数组描述）：
                %s

                决策协议（只输出一个 JSON，不要输出任何其他文字）：
                1. 需要调用工具时：
                   {"type":"tool_call","tool":"工具名","arguments":{参数名:值,...},"decisionSummary":"这一步为什么这么做，一句话"}
                2. 已经拿到足够信息、可以回答用户时：
                   {"type":"final","answer":"给用户的最终回答"}

                对话中带 [observation] 前缀的消息是工具执行后的观察结果，请基于它继续决策；
                不要重复执行一个已经执行过、且结果已知的工具调用。
                """.formatted(registry.toolsInstruction());
    }
}
