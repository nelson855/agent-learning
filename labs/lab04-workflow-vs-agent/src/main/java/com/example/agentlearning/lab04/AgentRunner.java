package com.example.agentlearning.lab04;

import java.util.ArrayList;
import java.util.List;

/**
 * Version B 的执行器：Agent（ReAct 最小闭环）。
 *
 * <p>路径<b>不由程序预定</b>：每一步由模型根据当前上下文（含上一次工具观察）
 * 决定调用哪个工具、参数是什么，直到给出 final 或超过 maxSteps。
 *
 * <p>Stop Condition：final 回答 / maxSteps（默认 8）/ 工具错误不崩溃不静默
 * （失败也作为 Observation 交回给模型）。
 */
public final class AgentRunner {

    /** 超步数停止时的标记。 */
    public static final String MAX_STEPS_EXCEEDED = "AGENT_MAX_STEPS_EXCEEDED";

    private static final int DEFAULT_MAX_STEPS = 8;

    private final LlmClient llm;
    private final ToolRegistry registry;
    private final int maxSteps;

    public AgentRunner(LlmClient llm, ToolRegistry registry) {
        this(llm, registry, DEFAULT_MAX_STEPS);
    }

    public AgentRunner(LlmClient llm, ToolRegistry registry, int maxSteps) {
        if (maxSteps <= 0) {
            throw new IllegalArgumentException("maxSteps 必须大于 0，实际: " + maxSteps);
        }
        this.llm = llm;
        this.registry = registry;
        this.maxSteps = maxSteps;
    }

    public AgentRun run(String topic) {
        AgentContext context = new AgentContext();
        context.addSystem(systemPrompt());
        context.addUser(topic);
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

    private String systemPrompt() {
        return """
                你是一个任务助手。任务：根据用户给出的主题，生成一个任务标题和一段描述，
                校验后保存到任务库。
                你可以调用下面这些工具（JSON 数组描述）：
                %s

                决策协议（只输出一个 JSON，不要输出任何其他文字）：
                1. 需要调用工具时：
                   {"type":"tool_call","tool":"工具名","arguments":{参数名:值,...},"decisionSummary":"这一步为什么这么做，一句话"}
                2. 已完成任务、可以回答用户时：
                   {"type":"final","answer":"给用户的最终回答"}

                对话中带 [observation] 前缀的消息是工具执行后的观察结果，请基于它继续决策；
                不要重复执行一个已经执行过、且结果已知的工具调用。
                """.formatted(registry.toolsInstruction());
    }
}
