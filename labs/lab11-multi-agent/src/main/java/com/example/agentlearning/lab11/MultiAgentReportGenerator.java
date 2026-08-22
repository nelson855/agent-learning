package com.example.agentlearning.lab11;

/**
 * Version B：Multi-Agent —— Orchestrator + 3 Workers。
 *
 * <p>Orchestrator 决定调用哪些 Worker，每个 Worker 用独立 Prompt + 独立 Context 片段，
 * 最后确定性合并。代价：调用次数与编排复杂度上升。
 */
public final class MultiAgentReportGenerator {

    private final Orchestrator orchestrator;

    public MultiAgentReportGenerator(LlmClient llm) {
        this.orchestrator = new Orchestrator(llm);
    }

    public GenerationOutcome run(TaskStats stats, String goal) {
        OrchestratorResult or = orchestrator.run(stats, goal);
        return new GenerationOutcome(or.modelCalls(), or.contextChars(), or.success(), or.report());
    }
}