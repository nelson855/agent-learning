package com.example.agentlearning.stage01;

import java.util.ArrayList;
import java.util.List;

/**
 * Agent 循环主体（ReAct 最小闭环）：
 *
 * <pre>
 *   Context（消息历史）→ 模型决策 → 工具执行 → Observation → 回到 Context ...
 * </pre>
 *
 * <p>每一步把结构化运行事件打印到控制台：
 * {@code STEP / MODEL_ACTION / TOOL_CALL / TOOL_RESULT / FINAL}，方便观察"下一步由模型决定"。
 * 不输出模型的私有推理过程。
 *
 * <p>停止由注入的 {@link StopCondition} 负责；工具错误不崩溃、不静默，
 * 失败同样作为 Observation 交回给模型。
 */
public final class AgentRunner {

    private final LlmClient llm;
    private final ToolExecutor executor;
    private final StopCondition stopCondition;

    public AgentRunner(LlmClient llm, ToolExecutor executor, StopCondition stopCondition) {
        this.llm = llm;
        this.executor = executor;
        this.stopCondition = stopCondition;
    }

    public AgentRun run(String userInput) {
        AgentContext context = new AgentContext();
        context.addSystem(systemPrompt());
        context.addUser(userInput);
        List<StepTrace> steps = new ArrayList<>();

        for (int step = 1; ; step++) {
            System.out.println("STEP " + step);

            LlmResponse reply = llm.chat(context.messages());
            AgentDecision decision = AgentDecisionParser.parse(reply.content());
            System.out.println("MODEL_ACTION: " + decision.type());

            if (decision.isFinal()) {
                System.out.println("FINAL: " + decision.answer());
                return AgentRun.finalAnswer(decision.answer(), steps);
            }

            ToolCall call = decision.toolCall();
            System.out.println("TOOL_CALL: " + call.name() + call.arguments());

            ToolResult result = executor.execute(call);
            steps.add(new StepTrace(step, decision, result));
            context.addAssistant(reply.content());
            context.addObservation(call, result);

            System.out.println("TOOL_RESULT: " + result.message());
            System.out.println();

            String stopReason = stopCondition.evaluate(step);
            if (stopReason != null) {
                System.out.println("STOPPED: " + stopReason + " (" + stopCondition.description() + ")");
                return AgentRun.stopped(stopReason, steps);
            }
        }
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
                """.formatted(executor.toolsInstruction());
    }
}
