package com.example.agentlearning.lab09;

/**
 * 计划中的一步。
 *
 * <p>用 {@code tool} 指向 {@link ToolRegistry} 里的工具名，{@code args} 是给工具的参数。
 * 例如 {@code tool="echo"}、{@code args="准备任务清单"}。
 */
public record PlanStep(String id, String description, String tool, String args, StepStatus status) {

    public PlanStep withStatus(StepStatus newStatus) {
        return new PlanStep(id, description, tool, args, newStatus);
    }
}
