package com.example.agentlearning.lab09;

import java.util.List;

/**
 * 从 {@link AgentState} 组装"给 Agent 看的下文"。
 *
 * <p>Checkpoint 里不存放完整 Context 文本（章节 10.4：不一定要保存所有 Context），
 * 需要时由这里现组装：目标、已完成步骤及结果、下一步待办。
 * 这样恢复后的 Agent 能无缝衔接，而不会把已做完的事重做一遍。
 */
public final class ContextBuilder {

    public String build(AgentState state) {
        StringBuilder sb = new StringBuilder();
        sb.append("RUN: ").append(state.runId()).append('\n');
        sb.append("GOAL: ").append(state.goal()).append('\n');

        List<PlanStep> plan = state.plan();
        int next = state.nextPendingStepIndex();

        sb.append("PLAN:\n");
        for (int i = 0; i < plan.size(); i++) {
            PlanStep step = plan.get(i);
            String marker = switch (step.status()) {
                case DONE -> "[DONE]  ";
                case RUNNING -> "[RUNNING]";
                case PENDING -> "[PENDING]";
            };
            sb.append("  ").append(marker).append(' ').append(step.id()).append(": ")
                    .append(step.description()).append('\n');
        }

        sb.append("NEXT_STEP_INDEX: ").append(next).append('\n');
        sb.append("STEP_RESULTS:\n");
        if (state.stepResults().isEmpty()) {
            sb.append("  (none)\n");
        } else {
            for (String r : state.stepResults()) {
                sb.append("  - ").append(r).append('\n');
            }
        }
        return sb.toString();
    }
}
